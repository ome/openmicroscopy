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
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.AuthenticationException;
import ome.conditions.RemovedSessionException;
import ome.conditions.SessionException;
import ome.conditions.SessionTimeoutException;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.model.meta.Share;
import ome.security.basic.PrincipalHolder;
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
import org.hibernate.Query;
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
    protected OmeroContext context;
    protected Roles roles;
    protected SessionCache cache;
    protected Executor executor;
    protected long defaultTimeToIdle;
    protected long defaultTimeToLive;
    protected PrincipalHolder principalHolder;

    // Local state

    /**
     * A private session for use only by this instance for running methods via
     * {@link Executor}. The name of this {@link Principal} will not be removed
     * by calls to {@link #closeAll()}.
     */
    protected Principal asroot;

    /**
     * Internal {@link SessionContext} created during {@link #init()} which is
     * used for all method calls internal to the session manager (see execute*
     * methods)
     */
    protected SessionContext internalSession;

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

    public void setPrincipalHolder(PrincipalHolder principal) {
        this.principalHolder = principal;
    }

    /**
     * Initialization method called by the Spring run-time to acquire an initial
     * {@link Session}.
     */
    public void init() {
        asroot = new Principal(internal_uuid, "system", "Sessions");
        internalSession = new InternalSessionContext(executeInternalSession(),
                roles);
        cache.putSession(internal_uuid, internalSession);
    }

    // ~ Session definition
    // =========================================================================

    protected void define(Session s, String uuid, String message, long started,
            long idle, long live, String eventType, Permissions umask) {

        if (umask == null) {
            umask = Permissions.DEFAULT;
        }

        s.setUuid(uuid);
        s.setMessage(message);
        s.setStarted(new Timestamp(started));
        s.setTimeToIdle(idle);
        s.setTimeToLive(live);
        s.setDefaultEventType(eventType);
        s.setDefaultPermissions(umask.toString());
        s.getDetails().setPermissions(Permissions.USER_PRIVATE);
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
                context.increment();
                return context.getSession(); // EARLY EXIT!
            }
        } catch (SessionException se) {
            // oh well.
        }

        // Though trusted values, if we receive a null principal, not ok;
        boolean ok = _principal == null ? false : executeCheckPassword(
                _principal, credentials);

        if (!ok) {
            log.warn("Failed to authenticate: " + _principal);
            throw new AuthenticationException("Authentication exception.");
        }

        // authentication checked. Now delegating to the admin method (no pass)
        return create(_principal);
    }

    public Session create(Principal principal) {
        Session session = new Session();
        define(session, UUID.randomUUID().toString(), "Initial message.",
                System.currentTimeMillis(), defaultTimeToIdle,
                defaultTimeToLive, principal.getEventType(), principal
                        .getUmask());
        return createSession(principal, session);
    }

    public Share createShare(Principal principal, boolean enabled,
            long timeToLive, String eventType, String description) {
        Share share = new Share();
        define(share, UUID.randomUUID().toString(), description, System
                .currentTimeMillis(), defaultTimeToIdle, timeToLive, eventType,
                principal.getUmask());
        share.setActive(enabled);
        share.setData(new byte[] {});
        share.setItemCount(0L);
        return (Share) createSession(principal, share);
    }

    private Session createSession(Principal principal, Session session) {
        // If username exists as session, then return that
        try {
            SessionContext context = cache.getSessionContext(principal
                    .getName());
            if (context != null) {
                context.increment();
                return context.getSession(); // EARLY EXIT!
            }
        } catch (SessionException se) {
            // oh well
        }

        principal = checkPrincipalNameAndDefaultGroup(principal);
        session = executeUpdate(session);
        SessionContext ctx = currentDatabaseShapshot(principal, session);
        if (ctx == null) {
            throw new RemovedSessionException("No info in database for "
                    + principal);
        }

        // This the publishEvent returns successfully, then we will have to
        // handle rolling back this addition our selves
        cache.putSession(session.getUuid(), ctx);
        try {
            context.publishEvent(new CreateSessionMessage(this, session
                    .getUuid()));
        } catch (RuntimeException re) {
            log.warn("Session creation cancelled by event listener", re);
            cache.removeSession(session.getUuid());
            throw re;
        }

        // All successful, increment and return.
        ctx.increment();
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

        // Allow user to change default group
        String defaultGroup = null;
        final ome.model.internal.Details d = session.getDetails();
        if (d != null) {
            final ExperimenterGroup group = d.getGroup();
            if (group != null) {
                try {
                    defaultGroup = this.executeGroupProxy(group.getId())
                            .getName();
                } catch (Exception e) {
                    throw new ApiUsageException(
                            "Cannot change default group to " + group + "\n"
                                    + e.getMessage());
                }
            }
        }

        if (defaultGroup == null) {
            defaultGroup = ctx.getCurrentGroupName();
        }

        Principal principal = new Principal(ctx.getCurrentUserName(),
                defaultGroup, ctx.getCurrentEventType());
        principal = checkPrincipalNameAndDefaultGroup(principal);

        // Unconditionally settable; these are open to the user for change
        parseAndSetDefaultType(session.getDefaultEventType(), orig);
        parseAndSetDefaultPermissions(session.getDefaultPermissions(), orig);
        orig.setUserAgent(session.getUserAgent());

        // Need to handle notifications

        ctx = currentDatabaseShapshot(principal, orig);
        if (ctx == null) {
            cache.removeSession(principal.getName());
            throw new RemovedSessionException("Database contains no info for "
                    + principal);
        } else {
            Session copy = copy(orig);
            executeUpdate(copy);
            cache.putSession(orig.getUuid(), ctx);
            return session;
        }

    }

    @SuppressWarnings("unchecked")
    protected SessionContext currentDatabaseShapshot(Principal principal,
            Session session) {

        // Do lookups
        final List<?> list = executeSessionContextLookup(principal);
        if (list == null) {
            return null; // EARLY EXIT when user no longer exists (on delete)
        }

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

    public int getReferenceCount(String uuid) {
        SessionContext ctx = cache.getSessionContext(uuid);
        return ctx.refCount();
    }

    public int detach(String uuid) {
        SessionContext ctx = cache.getSessionContext(uuid);
        return ctx.decrement();
    }

    /*
     */
    public int close(String uuid) {

        SessionContext ctx;
        try {
            ctx = cache.getSessionContext(uuid);
        } catch (SessionException se) {
            return -1; // EARLY EXIT!
        }

        int refCount = ctx.decrement();
        if (refCount < 1) {
            cache.removeSession(uuid);
            return -2;
        } else {
            return refCount;
        }
    }

    public int closeAll() {
        List<String> ids = cache.getIds();
        for (String id : ids) {
            if (asroot.getName().equals(id)) {
                continue; // DON'T KILL OUR ROOT SESSION
            }
            try {
                cache.removeSession(id);
            } catch (RemovedSessionException rse) {
                // Ok. Done for us
            } catch (SessionTimeoutException ste) {
                // Also ok
            } catch (Exception e) {
                log.warn(String.format("Exception thrown on closeAll: %s:%s", e
                        .getClass().getName(), e.getMessage()));
            }
        }
        return ids.size();
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

    public Map<String, Object> inputEnvironment(String session) {
        return environment(session, INPUT_ENVIRONMENT);
    }

    public Map<String, Object> outputEnvironment(String session) {
        return environment(session, OUTPUT_ENVIRONMENT);
    }

    protected Map<String, Object> environment(String session, String env) {
        Map<String, Object> rv = new HashMap<String, Object>();
        Element elt = inMemoryCache(session).get(env);
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

    /**
     */
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof UserGroupUpdateEvent) {
            cache.updateEvent((UserGroupUpdateEvent) event);
        } else if (event instanceof DestroySessionMessage) {
            executeCloseSession(((DestroySessionMessage) event).getSessionId());
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
     * Checks the validity of the given {@link Principal}, and in the case of an
     * error attempts to correct the problem by returning a new Principal.
     */
    private Principal checkPrincipalNameAndDefaultGroup(Principal p) {

        if (p == null || p.getName() == null) {
            throw new ApiUsageException("Null principal name.");
        }

        String type = p.getEventType();
        if (type == null) {
            type = "User";
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
            ExperimenterGroup g = executeDefaultGroup(p.getName());
            if (g == null) {
                throw new ApiUsageException("Can't find default group for "
                        + p.getName());
            }
            group = g.getName();
        }
        Principal copy = new Principal(p.getName(), group, type);
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

        if (source instanceof Share) {
            throw new UnsupportedOperationException("NYI");
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
     * Calls {@link #flushAsCurrentUser()} to prevent strange Hibernate
     * exceptions during {@link #reload(SessionContext)}. This is not necessary
     * the best solution to the problem, but will work for now. Most likely the
     * session manager should have its own session, but how it takes parts in
     * transactions would have to be clarified.
     */
    public void prepareReload() {
        flushAsCurrentUser();
    }

    /**
     * Will be called in a synchronized block by {@link SessionCache} in order
     * to allow for an update.
     */
    public SessionContext reload(SessionContext ctx) {
        Principal p = new Principal(ctx.getCurrentUserName(), ctx
                .getCurrentGroupName(), ctx.getCurrentEventType());
        return currentDatabaseShapshot(p, ctx.getSession());
    }

    // Executor methods
    // =========================================================================

    /**
     * Calls flush without passing a principal to
     * {@link Executor#execute(Principal, ome.services.util.Executor.Work)} and
     * read-only set to false to make way for proper read-only reloading via
     * {@link StaleCacheListener#reload(SessionContext)}. This should be safe
     * since currently no stateful services make changes to the users/groups
     * which would cause a {@link UserGroupUpdateEvent} to be raised.
     * 
     * Note: if there is not currently a principal (i.e. no one is logged in),
     * then this is skipped, since no action can be active.
     */
    private void flushAsCurrentUser() {
        if (principalHolder.size() > 0) {
            executor.execute(null, new Executor.Work() {
                public Object doWork(TransactionStatus status,
                        org.hibernate.Session s, ServiceFactory sf) {
                    ((LocalUpdate) sf.getUpdateService()).flush();
                    return null;
                }
            }, false);
        }
    }

    public boolean executePasswordCheck(final String name,
            final String credentials) {

        if (cache.getIds().contains(credentials)) {
            return true;
        }
        return executeCheckPassword(new Principal(name), credentials);
    }

    @SuppressWarnings("unchecked")
    private Session executeUpdate(final Session session) {
        return (Session) executor.execute(asroot, new Executor.Work() {
            public Object doWork(TransactionStatus status,
                    org.hibernate.Session s, ServiceFactory sf) {
                return sf.getUpdateService().saveAndReturnObject(session);
            }
        }, false);
    }

    private boolean executeCheckPassword(final Principal _principal,
            final String credentials) {
        boolean ok = (Boolean) executor.execute(asroot, new Executor.Work() {
            public Object doWork(TransactionStatus status,
                    org.hibernate.Session session, ServiceFactory sf) {
                return ((LocalAdmin) sf.getAdminService()).checkPassword(
                        _principal.getName(), credentials);
            }
        }, false);
        return ok;
    }

    private Experimenter executeUserProxy(final long uid) {
        return (Experimenter) executor.execute(asroot, new Executor.Work() {
            public Object doWork(TransactionStatus status,
                    org.hibernate.Session session, ServiceFactory sf) {
                return ((LocalAdmin) sf.getAdminService()).userProxy(uid);
            }
        }, true);
    }

    private ExperimenterGroup executeGroupProxy(final long gid) {
        return (ExperimenterGroup) executor.execute(asroot,
                new Executor.Work() {
                    public Object doWork(TransactionStatus status,
                            org.hibernate.Session session, ServiceFactory sf) {
                        return ((LocalAdmin) sf.getAdminService())
                                .groupProxy(gid);
                    }
                }, true);
    }

    /**
     * To prevent having the transaction rolled back, this method returns null
     * rather than throw an exception.
     */
    @SuppressWarnings("unchecked")
    private ExperimenterGroup executeDefaultGroup(final String name) {
        return (ExperimenterGroup) executor.execute(asroot,
                new Executor.Work() {
                    public Object doWork(TransactionStatus status,
                            org.hibernate.Session session, ServiceFactory sf) {
                        LocalAdmin admin = (LocalAdmin) sf.getAdminService();

                        try {
                            Experimenter exp = admin.userProxy(name);
                            ExperimenterGroup grp = admin.getDefaultGroup(exp
                                    .getId());
                            return grp;
                        } catch (Exception e) {
                            return null;
                        }
                    }
                }, true);
    }

    /**
     * Returns a List of state for creating a new {@link SessionContext}. If an
     * exception is thrown, return nulls since throwing an exception within the
     * Work will set our transaction to rollback only.
     */
    @SuppressWarnings("unchecked")
    private List<Object> executeSessionContextLookup(final Principal principal) {
        return (List<Object>) executor.execute(asroot, new Executor.Work() {

            public Object doWork(TransactionStatus status,
                    org.hibernate.Session session, ServiceFactory sf) {
                try {
                    List<Object> list = new ArrayList<Object>();
                    LocalAdmin admin = (LocalAdmin) sf.getAdminService();
                    final Experimenter exp = admin.userProxy(principal
                            .getName());
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
                } catch (Exception e) {
                    return null;
                }
            }
        }, false);

    }

    /**
     * Loads a session directly from the database, sets its "closed" value and
     * immediately saves it back to the database. This method is not called
     * directly from the {@link #close(String)} and {@link #closeAll()} methods
     * since there are other non-explicit ways for a session to be destroy, such
     * as a timeout within {@link SessionCache} and so this is called from
     * {@link #onApplicationEvent(ApplicationEvent)} when a
     * {@link DestroySessionMessage} is received.
     */
    private Session executeCloseSession(final String uuid) {
        return (Session) executor
                .executeStateless(new Executor.StatelessWork() {
                    public Object doWork(TransactionStatus status,
                            StatelessSession sSession) {
                        try {
                            Query q = sSession
                                    .createQuery("select s from Session s where s.uuid = :uuid");
                            q.setString("uuid", uuid);
                            Session s = (Session) q.uniqueResult();
                            s.setClosed(new Timestamp(System
                                    .currentTimeMillis()));
                            sSession.update(s);
                        } catch (Exception e) {
                            log.error("FAILED TO CLOSE SESSION IN DATABASE: "
                                    + uuid, e);
                        }
                        return null;
                    }
                });
    }

    private Session executeInternalSession() {
        return (Session) executor
                .executeStateless(new Executor.StatelessWork() {
                    public Object doWork(TransactionStatus status,
                            StatelessSession sSession) {
                        final Permissions p = Permissions.USER_PRIVATE;
                        final Session s = new Session();
                        define(s, internal_uuid, "Session Manager internal",
                                System.currentTimeMillis(), Long.MAX_VALUE, 0L,
                                "Sessions", p);

                        // Have to copy values over due to unloaded
                        final Session s2 = copy(s);
                        Long id = (Long) sSession.insert(s2);

                        s.setId(id);
                        return s;
                    }
                });
    }
}
