/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import ome.api.local.LocalAdmin;
import ome.conditions.ApiUsageException;
import ome.conditions.AuthenticationException;
import ome.conditions.RemovedSessionException;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.services.sessions.events.UserGroupUpdateEvent;
import ome.services.sessions.state.SessionCache;
import ome.services.sessions.state.SessionCache.StaleCacheListener;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;

import org.hibernate.StatelessSession;
import org.springframework.context.ApplicationEvent;
import org.springframework.transaction.TransactionStatus;

/**
 * Is for ISession a cache and will be kept there in sync? OR Factors out the
 * logic from ISession and SessionManagerI
 * 
 * Therefore either called directly, or via synchronous messages.
 * 
 * Uses the name of a Principal as the key to the session. We may need to limit
 * user names to prevent this. (Strictly alphanumeric)
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class SessionManagerImpl implements SessionManager, StaleCacheListener {

    /**
     * The id of this session manager, used to identify its own actions.
     */
    private final String internal_uuid = UUID.randomUUID().toString();

    // Injected
    Roles roles;
    SessionCache cache;
    Executor executor;

    /**
     * A private session for use only by this instance for running methods via
     * {@link Executor}
     */
    Principal asroot;
    SessionContext sc;

    // ~ Injectors
    // =========================================================================

    public void setSessionCache(SessionCache sessionCache) {
        cache = sessionCache;
        this.cache.addStaleCacheListener(this);
    }

    public void setRoles(Roles securityRoles) {
        roles = securityRoles;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void init() {
        asroot = new Principal(internal_uuid, "system", "Sessions");
        sc = new InternalSessionContext(executeInternalSession(), roles);
        cache.putSession(internal_uuid, sc);
    }

    // ~ Session definition
    // =========================================================================

    protected Session define(String uuid, String message, long started,
            long idle, long live, String eventType, String defaultPermissions) {
        final Session s = new Session();
        s.setUuid(uuid);
        s.setMessage(message);
        s.setStarted(new Timestamp(started));
        s.setTimeToIdle(idle);
        s.setTimeToLive(live);
        s.setDefaultEventType(eventType);
        s.setDefaultPermissions(defaultPermissions);
        s.getDetails().setPermissions(Permissions.USER_PRIVATE);
        return s;
    }

    // ~ Session management
    // =========================================================================

    /*
     * Is given trustable values by the {@link SessionBean}
     */
    public Session create(final Principal _principal, final String credentials) {

        boolean ok = executeCheckPassword(_principal, credentials);

        if (!ok) {
            throw new AuthenticationException("Authentication exception.");
        }

        // authentication checked. Now delegating to the admin method (no pass)
        return create(_principal);
    }

    public Session create(Principal principal) {

        principal = checkPrincipalNameAndDefaultGroup(principal);

        Session session = define(UUID.randomUUID().toString(),
                "Initial message.", System.currentTimeMillis(), cache
                        .getTimeToIdle(), cache.getTimeToLive(), principal
                        .getEventType(), principal.getUmask().toString());
        session = executeUpdate(session);
        SessionContext ctx = currentDatabaseShapshot(principal, session);
        cache.putSession(session.getUuid(), ctx);
        return session;
    }

    public Session update(Session session) {

        if (session == null || session.getUuid() == null) {
            throw new RemovedSessionException("Cannot update; No uuid.");
        }

        SessionContext ctx = cache.getSessionContext(session.getUuid());
        if (ctx == null) {
            throw new RemovedSessionException(
                    "Can't update; No session with uuid:" + session.getUuid());
        }
        Session orig = ctx.getSession();

        // TODO // FIXME
        // =====================================================
        // This needs to get smarter
        //
        Principal principal = new Principal(ctx.getCurrentUserName(), ctx
                .getCurrentGroupName(), ctx.getCurrentEventType());
        principal = checkPrincipalNameAndDefaultGroup(principal);

        // Unconditionally settable; these are open to the user for change
        parseAndSetDefaultType(session.getDefaultEventType(), orig);
        parseAndSetDefaultPermissions(session.getDefaultPermissions(), orig);
        orig.setUserAgent(session.getUserAgent());

        // Need to handle notifications

        ctx = currentDatabaseShapshot(principal, orig);
        Session copy = copy(orig);
        executeUpdate(copy);
        cache.putSession(orig.getUuid(), ctx);

        return session;

    }

    @SuppressWarnings("unchecked")
    protected SessionContext currentDatabaseShapshot(Principal principal,
            Session session) {
        // Do lookups
        List<?> list = executeSessionContextLookup(principal);
        final Experimenter exp = (Experimenter) list.get(0);
        final ExperimenterGroup grp = (ExperimenterGroup) list.get(1);
        final List<Long> memberOfGroupsIds = (List<Long>) list.get(2);
        final List<Long> leaderOfGroupsIds = (List<Long>) list.get(3);
        final List<String> userRoles = (List<String>) list.get(4);

        parseAndSetDefaultType(principal.getEventType(), session);
        parseAndSetDefaultPermissions(principal.getUmask(), session);

        session.getDetails().setOwner(exp);
        session.getDetails().setGroup(grp);

        SessionContext sessionContext = new SessionContextImpl(session,
                leaderOfGroupsIds, memberOfGroupsIds, userRoles);
        return sessionContext;

    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.server.utests.sessions.SessionManager#getSession(java.lang.String)
     */
    public Session find(String uuid) {
        SessionContext sessionContext = cache.getSessionContext(uuid);
        return (sessionContext == null) ? null : sessionContext.getSession();
    }

    /*
     */
    public void close(String uuid) {

        SessionContext ctx = cache.getSessionContext(uuid);
        if (ctx == null) {
            return;
        }

        // TODO this is not safe
        Session s = ctx.getSession();
        s.setClosed(new Timestamp(System.currentTimeMillis()));
        update(s);
        cache.removeSession(uuid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.services.sessions.SessionManager#getUserRoles(String)
     */
    public List<String> getUserRoles(String uuid) {
        SessionContext ctx = cache.getSessionContext(uuid);
        if (ctx == null) {
            return Collections.emptyList();
        }
        return ctx.getUserRoles();
    }

    // ~ Security methods
    // =========================================================================

    public EventContext getEventContext(Principal principal) {
        final SessionContext ctx = cache.getSessionContextThrows(principal
                .getName(), true);
        if (ctx == null) {
            throw new RemovedSessionException("No session with uuid:"
                    + principal.getName());
        }
        return ctx;
    }

    // ~ Notifications
    // =========================================================================

    public String[] notifications(String sessionId) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.server.utests.sessions.SessionManager#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event) {

        if (event instanceof UserGroupUpdateEvent) {
            executor.trigger("update-cache-manual");
        }

        // TODO
        // if is a session event or admin event (user change, etc.) act.
        // send to all notifications.
    }

    // ~ Callbacks (Registering session-based components)
    // =========================================================================

    public void addCallback(String sessionId, SessionCallback cb) {

    }

    public Object getCallbackObject(String sessionId, String name) {
        return null;
    }

    // ~ Misc
    // =========================================================================

    /**
     * Checks the validity of the given {@link Principal}, and in the case of
     * an error attempts to correct the problem by returning a new Principal.
     */
    private Principal checkPrincipalNameAndDefaultGroup(Principal p) {

        if (p == null || p.getName() == null) {
            throw new ApiUsageException("Null principal name.");
        }

        // Null or bad event type values as well as umasks are handled
        // within the SessionManager and EventHandler. It is necessary
        String group = p.getGroup();
        if (group == null) {
            group = "user";
        }

        // ticket:404 -- preventing users from logging into "user" group
        else if (roles.getUserGroupName().equals(p.getGroup())) {
            // Throws an exception if no properly defined default group
            group = executeDefaultGroup(p.getName()).getName();
        }
        Principal copy = new Principal(p.getName(), group, p.getEventType());
        Permissions umask = p.getUmask();
        if (umask == null) {
            umask = Permissions.DEFAULT;
        }
        copy.setUmask(umask);
        return copy;
    }

    private void parseAndSetDefaultPermissions(Permissions perms,
            Session session) {
        Permissions _perm = (perms == null) ? Permissions.DEFAULT : perms;
        parseAndSetDefaultPermissions(_perm.toString(), session);
    }

    private void parseAndSetDefaultPermissions(String perms, Session session) {
        String _perm = (perms == null) ? Permissions.DEFAULT.toString() : perms
                .toString();
        session.setDefaultPermissions(_perm);
    }

    private void parseAndSetDefaultType(String type, Session session) {
        String _type = (type == null) ? "User" : type;
        session.setDefaultEventType(_type);
    }

    public Session copy(Session source) {
        if (source == null) {
            throw new ApiUsageException("Source may not be null.");
        }

        final Session target = new Session();
        target.setId(source.getId());
        target.setClosed(source.getClosed());
        target.setDefaultEventType(source.getDefaultEventType());
        target.setDefaultPermissions(source.getDefaultPermissions());
        target.getDetails().shallowCopy(source.getDetails());
        target.setMessage(source.getMessage());
        target.setStarted(source.getStarted());
        target.setTimeToIdle(source.getTimeToIdle());
        target.setTimeToLive(source.getTimeToLive());
        target.setUserAgent(source.getUserAgent());
        target.setUuid(source.getUuid());
        return target;
    }

    // StaleCacheListener
    // =========================================================================

    /**
     * Will be called in a synchronized block by {@link SessionCache} in order
     * to allow for an update.
     */
    public boolean attemptCacheUpdate() {
        for (String key : cache.getIds()) {
            SessionContext ctx = cache.getSessionContext(key);
            if (ctx == null) {
                cache.removeSession(key);
            }
            this.update(copy(ctx.getSession()));
        }
        return true;
    }

    // Executor methods
    // =========================================================================
    @SuppressWarnings("unchecked")
    private <T extends IObject> T executeUpdate(final T obj) {
        return (T) executor.execute(asroot, new Executor.Work() {
            public Object doWork(TransactionStatus status,
                    org.hibernate.Session s, ServiceFactory sf) {
                return sf.getUpdateService().saveAndReturnObject(obj);
            }
        });
    }

    private boolean executeCheckPassword(final Principal _principal,
            final String credentials) {
        boolean ok = (Boolean) executor.execute(asroot, new Executor.Work() {
            public Object doWork(TransactionStatus status,
                    org.hibernate.Session session, ServiceFactory sf) {
                return ((LocalAdmin) sf.getAdminService()).checkPassword(
                        _principal.getName(), credentials);
            }
        });
        return ok;
    }

    private Experimenter executeUserProxy(final long uid) {
        return (Experimenter) executor.execute(asroot, new Executor.Work() {
            public Object doWork(TransactionStatus status,
                    org.hibernate.Session session, ServiceFactory sf) {
                return ((LocalAdmin) sf.getAdminService()).userProxy(uid);
            }
        });
    }

    private ExperimenterGroup executeGroupProxy(final long gid) {
        return (ExperimenterGroup) executor.execute(asroot,
                new Executor.Work() {
                    public Object doWork(TransactionStatus status,
                            org.hibernate.Session session, ServiceFactory sf) {
                        return ((LocalAdmin) sf.getAdminService())
                                .groupProxy(gid);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private ExperimenterGroup executeDefaultGroup(final String name) {
        return (ExperimenterGroup) executor.execute(asroot,
                new Executor.Work() {
                    public Object doWork(TransactionStatus status,
                            org.hibernate.Session session, ServiceFactory sf) {
                        LocalAdmin admin = (LocalAdmin) sf.getAdminService();
                        long id;
                        try {
                            id = admin.userProxy(name).getId();
                        } catch (ApiUsageException api) {
                            throw new SecurityViolation("No known user:" + name);
                        }
                        try {
                            return sf.getAdminService().getDefaultGroup(id);
                        } catch (ValidationException ve) {
                            throw new SecurityViolation(
                                    "User has no default group.");
                        }
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private List<Object> executeSessionContextLookup(final Principal principal) {
        return (List<Object>) executor.execute(asroot, new Executor.Work() {

            public Object doWork(TransactionStatus status,
                    org.hibernate.Session session, ServiceFactory sf) {
                List<Object> list = new ArrayList<Object>();
                LocalAdmin admin = (LocalAdmin) sf.getAdminService();
                final Experimenter exp = admin.userProxy(principal.getName());
                final ExperimenterGroup grp = admin.groupProxy(principal
                        .getGroup());
                final List<Long> memberOfGroupsIds = admin
                        .getMemberOfGroupIds(exp);
                final List<Long> leaderOfGroupsIds = admin
                        .getLeaderOfGroupIds(exp);
                final List<String> userRoles = admin.getUserRoles(exp);
                list.add(exp);
                list.add(grp);
                list.add(memberOfGroupsIds);
                list.add(leaderOfGroupsIds);
                list.add(userRoles);
                return list;
            }
        });

    }

    private Session executeInternalSession() {
        return (Session) executor
                .executeStateless(new Executor.StatelessWork() {
                    public Object doWork(StatelessSession sSession) {
                        final Permissions p = Permissions.USER_PRIVATE;
                        final Session s = define(internal_uuid,
                                "Session Manager internal", System
                                        .currentTimeMillis(), Long.MAX_VALUE,
                                0L, "Sessions", p.toString());

                        // Have to copy values over due to unloaded
                        final Session s2 = copy(s);
                        Long id = (Long) sSession.insert(s2);

                        s.setId(id);
                        return s;
                    }
                });
    }
}
