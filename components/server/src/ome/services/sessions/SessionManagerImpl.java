/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import ome.api.local.LocalAdmin;
import ome.conditions.ApiUsageException;
import ome.conditions.AuthenticationException;
import ome.conditions.RemovedSessionException;
import ome.conditions.SecurityViolation;
import ome.conditions.SessionException;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.services.messages.CreateSessionMessage;
import ome.services.messages.DestroySessionMessage;
import ome.services.sessions.events.UserGroupUpdateEvent;
import ome.services.sessions.state.SessionCache;
import ome.services.sessions.state.SessionCache.StaleCacheListener;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.StatelessSession;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
public class SessionManagerImpl implements SessionManager, StaleCacheListener,
        ApplicationContextAware {

    private final static Log log = LogFactory.getLog(SessionManagerImpl.class);

    /**
     * The id of this session manager, used to identify its own actions.
     */
    private final String internal_uuid = UUID.randomUUID().toString();

    // Injected
    OmeroContext context;
    Roles roles;
    SessionCache cache;
    Executor executor;
    long defaultTimeToIdle;
    long defaultTimeToLive;

    /**
     * A private session for use only by this instance for running methods via
     * {@link Executor}
     */
    Principal asroot;
    SessionContext sc;

    // ~ Injectors
    // =========================================================================

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.context = (OmeroContext) applicationContext;
    }

    public void setSessionCache(SessionCache sessionCache) {
        cache = sessionCache;
        this.cache.setStaleCacheListener(this);
    }

    public void setRoles(Roles securityRoles) {
        roles = securityRoles;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void setDefaultTimeToIdle(long defaultTimeToIdle) {
        this.defaultTimeToIdle = defaultTimeToIdle;
    }

    public void setDefaultTimeToLive(long defaultTimeToLive) {
        this.defaultTimeToLive = defaultTimeToLive;
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

        // If credentials exist as session, then return that
        try {
            SessionContext context = cache.getSessionContext(credentials);
            if (context != null) {
                return context.getSession(); // EARLY EXIT!
            }
        } catch (SessionException se) {
            // oh well.
        }

        boolean ok = executeCheckPassword(_principal, credentials);

        if (!ok) {
            log.warn("Failed to authenticate: " + _principal);
            throw new AuthenticationException("Authentication exception.");
        }

        // authentication checked. Now delegating to the admin method (no pass)
        return create(_principal);
    }

    public Session create(Principal principal) {

        // If username exists as session, then return that
        try {
            SessionContext context = cache.getSessionContext(principal
                    .getName());
            if (context != null) {
                return context.getSession(); // EARLY EXIT!
            }
        } catch (SessionException se) {
            // oh well
        }

        principal = checkPrincipalNameAndDefaultGroup(principal);

        Session session = define(UUID.randomUUID().toString(),
                "Initial message.", System.currentTimeMillis(),
                defaultTimeToIdle, defaultTimeToLive, principal.getEventType(),
                principal.getUmask().toString());
        session = executeUpdate(session);
        SessionContext ctx = currentDatabaseShapshot(principal, session);

        // This the publishEvent returns successfully, then we will have to
        // handle rolling back this addition our selvces
        cache.putSession(session.getUuid(), ctx);
        try {
            context.publishEvent(new CreateSessionMessage(this, session
                    .getUuid()));
        } catch (RuntimeException re) {
            log.warn("Session creation cancelled by event listener", re);
            cache.removeSession(session.getUuid());
            throw re;
        }
        return session;
    }

    public Session update(Session session) {

        if (session == null || !session.isLoaded() || session.getUuid() == null) {
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

    public Session find(String uuid) {
        SessionContext sessionContext = cache.getSessionContext(uuid);
        return (sessionContext == null) ? null : sessionContext.getSession();
    }

    /*
     */
    public void close(String uuid) {

        SessionContext ctx;
        try {
            ctx = cache.getSessionContext(uuid);
        } catch (SessionException se) {
            ctx = null;
        }

        if (ctx == null) {
            return;
        }

        Session s = ctx.getSession();
        s.setClosed(new Timestamp(System.currentTimeMillis()));
        update(s);

        try {
            context.publishEvent(new DestroySessionMessage(this, s.getUuid()));
        } catch (RuntimeException re) {
            log.warn("Session destruction cancelled by event listener", re);
            throw re;
        }
        // This the publishEvent returns successfully, then we update our cache
        // since ehcache is not tx-friendly.
        cache.removeSession(uuid);
    }

    public List<String> getUserRoles(String uuid) {
        SessionContext ctx = cache.getSessionContext(uuid);
        if (ctx == null) {
            throw new RemovedSessionException("No session with uuid: " + uuid);
        }
        return ctx.getUserRoles();
    }

    // ~ State attached to session
    // =========================================================================

    public Ehcache inMemoryCache(String uuid) {
        return cache.inMemoryCache(uuid);
    }

    public Ehcache onDiskCache(String uuid) {
        return cache.onDiskCache(uuid);
    }

    static String INPUT_ENVIRONMENT = "InputEnvironment";
    static String OUTPUT_ENVIRONMENT = "OutputEnvironment";

    public Object getInput(String session, String key)
            throws RemovedSessionException {
        return getEnvironmentVariable(session, key, INPUT_ENVIRONMENT);
    }

    public Object getOutput(String session, String key)
            throws RemovedSessionException {
        return getEnvironmentVariable(session, key, OUTPUT_ENVIRONMENT);
    }

    public Map<String, Object> outputEnvironment(String session) {
        Map<String, Object> rv = new HashMap<String, Object>();
        Element elt = inMemoryCache(session).get(OUTPUT_ENVIRONMENT);
        if (elt == null) {
            return rv;
        }
        Map<String, Object> cv = (Map<String, Object>) elt.getObjectValue();
        if (cv == null) {
            return rv;
        }

        rv.putAll(cv);
        return rv;
    }

    public void setInput(String session, String key, Object object)
            throws RemovedSessionException {
        setEnvironmentVariable(session, key, object, INPUT_ENVIRONMENT);
    }

    public void setOutput(String session, String key, Object object)
            throws RemovedSessionException {
        setEnvironmentVariable(session, key, object, OUTPUT_ENVIRONMENT);
    }

    private Object getEnvironmentVariable(String session, String key, String env) {
        Ehcache cache = inMemoryCache(session);
        Element elt = cache.get(env);
        if (elt == null) {
            return null;
        }

        Map<String, Object> map = (Map<String, Object>) elt.getObjectValue();
        if (map == null) {
            return null;
        } else {
            return map.get(key);
        }
    }

    private void setEnvironmentVariable(String session, String key,
            Object object, String env) {
        Ehcache cache = inMemoryCache(session);
        Element elt = cache.get(env);
        Map<String, Object> map;
        if (elt == null) {
            map = new ConcurrentHashMap<String, Object>();
            elt = new Element(env, map);
            cache.put(elt);
        } else {
            map = (Map<String, Object>) elt.getObjectValue();
        }
        if (object == null) {
            map.remove(key);
        } else {
            map.put(key, object);
        }
    }

    // ~ Security methods
    // =========================================================================

    public EventContext getEventContext(Principal principal) {
        final SessionContext ctx = cache.getSessionContext(principal.getName());
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
            cache.updateEvent((UserGroupUpdateEvent) event);
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
    public SessionContext reload(SessionContext ctx) {
        Principal p = new Principal(ctx.getCurrentUserName(), ctx
                .getCurrentGroupName(), ctx.getCurrentEventType());
        SessionContext replacement = currentDatabaseShapshot(p, ctx
                .getSession());
        return replacement;
    }

    // Executor methods
    // =========================================================================

    public boolean executePasswordCheck(final String name,
            final String credentials) {

        if (cache.getIds().contains(credentials)) {
            return true;
        }
        return executeCheckPassword(new Principal(name), credentials);
    }

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
