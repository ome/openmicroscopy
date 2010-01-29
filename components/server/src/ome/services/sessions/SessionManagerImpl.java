/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
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
import ome.conditions.InternalException;
import ome.conditions.RemovedSessionException;
import ome.conditions.SecurityViolation;
import ome.conditions.SessionException;
import ome.conditions.SessionTimeoutException;
import ome.model.enums.EventType;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Node;
import ome.model.meta.Session;
import ome.model.meta.Share;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.security.basic.PrincipalHolder;
import ome.services.messages.CreateSessionMessage;
import ome.services.messages.DestroySessionMessage;
import ome.services.sessions.events.UserGroupUpdateEvent;
import ome.services.sessions.state.SessionCache;
import ome.services.sessions.state.SessionCache.StaleCacheListener;
import ome.services.sessions.stats.CounterFactory;
import ome.services.sessions.stats.SessionStats;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.transaction.annotation.Transactional;

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
     * The id of this session manager, used to identify its own actions. This
     * value may be overwritten by an injector with a value which is used
     * throughout this server instance.
     */
    private String internal_uuid = UUID.randomUUID().toString();

    // Injected
    protected OmeroContext context;
    protected Roles roles;
    protected SessionCache cache;
    protected Executor executor;
    protected long defaultTimeToIdle;
    protected long maxUserTimeToIdle;
    protected long defaultTimeToLive;
    protected long maxUserTimeToLive;
    protected PrincipalHolder principalHolder;
    protected CounterFactory factory;
    protected boolean readOnly = false;

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

    public void setUuid(String uuid) {
        this.internal_uuid = uuid;
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
        this.maxUserTimeToIdle = Math.min(Long.MAX_VALUE / 10,
                defaultTimeToIdle);
        this.maxUserTimeToIdle *= 10;
    }

    public void setDefaultTimeToLive(long defaultTimeToLive) {
        this.defaultTimeToLive = defaultTimeToLive;
        this.maxUserTimeToLive = Math.min(Long.MAX_VALUE / 10,
                defaultTimeToLive);
        this.maxUserTimeToLive *= 10;

    }

    public void setPrincipalHolder(PrincipalHolder principal) {
        this.principalHolder = principal;
    }

    public void setCounterFactory(CounterFactory factory) {
        this.factory = factory;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Initialization method called by the Spring run-time to acquire an initial
     * {@link Session}.
     */
    public void init() {
        try {
            asroot = new Principal(internal_uuid, "system", "Sessions");
            final Session session = executeInternalSession();
            internalSession = new InternalSessionContext(session, roles);
            cache.putSession(internal_uuid, internalSession);
        } catch (DataAccessException dataAccess) {
            throw new RuntimeException(
                    "          "
                            + "=====================================================\n"
                            + "Data access exception: Did you create your database? \n"
                            + "=====================================================\n",
                    dataAccess);
        }
    }

    // ~ Session definition
    // =========================================================================

    protected void define(Session s, String uuid, String message, long started,
            long idle, long live, String eventType, Permissions umask) {

        s.setUuid(uuid);
        s.setMessage(message);
        s.setStarted(new Timestamp(started));
        s.setTimeToIdle(idle);
        s.setTimeToLive(live);
        s.setDefaultEventType(eventType);
        // TODO: be careful of these two values.
        // FIXME: REVIEW! ticket:1731
        s.setDefaultPermissions(umask.toString());
        s.getDetails().setPermissions(umask);
    }

    // ~ Session management
    // =========================================================================

    /*
     * Is given trustable values by the {@link SessionBean}
     */
    public Session create(final Principal _principal, final String credentials) {

        // If credentials exist as session, then return that
        try {
            SessionContext context = cache
                    .getSessionContext(credentials, false);
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

    @SuppressWarnings("unchecked")
    private Session createSession(final Principal principal,
            final Session oldsession) {
        // If username exists as session, then return that
        try {
            SessionContext context = cache.getSessionContext(principal
                    .getName(), false);
            if (context != null) {
                context.increment();
                return context.getSession(); // EARLY EXIT!
            }
        } catch (SessionException se) {
            // oh well
        }

        List rv;
        if (readOnly) {
            rv = (List) executor.execute(this.asroot, new Executor.SimpleWork(
                    this, "read-only createSession") {
                @Transactional(readOnly = true)
                public Object doWork(org.hibernate.Session __s,
                        ServiceFactory sf) {
                    Principal p = checkPrincipalNameAndDefaultGroup(sf,
                            principal);
                    long userId = executeLookupUser(sf, p);
                    // Not performed! Session s = executeUpdate(sf, oldsession,
                    // userId);
                    Session s = oldsession;
                    return executeSessionContextLookup(sf, p, s);
                }
            });
        } else {
            rv = (List) executor.execute(this.asroot, new Executor.SimpleWork(
                    this, "createSession") {
                @Transactional(readOnly = false)
                public Object doWork(org.hibernate.Session __s,
                        ServiceFactory sf) {
                    Principal p = checkPrincipalNameAndDefaultGroup(sf,
                            principal);
                    long userId = executeLookupUser(sf, p);
                    Session s = executeUpdate(sf, oldsession, userId);
                    return executeSessionContextLookup(sf, p, s);
                }

            });
        }

        if (rv == null) {
            throw new RemovedSessionException("No info in database for "
                    + principal);
        }
        SessionContext newctx = createSessionContext(rv);

        // This the publishEvent returns successfully, then we will have to
        // handle rolling back this addition our selves
        String uuid = newctx.getCurrentSessionUuid();
        cache.putSession(uuid, newctx);
        try {
            context.publishEvent(new CreateSessionMessage(this, uuid));
        } catch (RuntimeException re) {
            log.warn("Session creation cancelled by event listener", re);
            cache.removeSession(uuid);
            throw re;
        }

        // All successful, increment and return.
        newctx.increment();
        return newctx.getSession();
    }

    public Session update(Session session) {
        return update(session, false);
    }

    public Session update(final Session session, final boolean trusted) {

        if (session == null || !session.isLoaded() || session.getUuid() == null) {
            throw new RemovedSessionException("Cannot update; No uuid.");
        }

        final String uuid = session.getUuid();
        final Details details = session.getDetails();
        final SessionContext ctx = cache.getSessionContext(uuid, true);
        if (ctx == null) {
            throw new RemovedSessionException(
                    "Can't update; No session with uuid:" + uuid);
        }

        final Session orig = ctx.getSession();
        
        // TODO // FIXME
        // =====================================================
        // This needs to get smarter

        List list = (List) executor.execute(asroot, new Executor.SimpleWork(
                this, "load_for_update") {
            @Transactional(readOnly = false)
            public Object doWork(org.hibernate.Session __s, ServiceFactory sf) {

                // Allow user to change default group
                String defaultGroup = null;
                if (details != null) {
                    ExperimenterGroup group = details.getGroup();
                    if (group != null) {
                        try {
                            Long groupId = group.getId();
                            if (groupId != null) {
                                group = ((LocalAdmin) sf.getAdminService())
                                        .groupProxy(groupId);
                                if (group != null) {
                                    defaultGroup = group.getName();
                                }
                            }
                        } catch (Exception e) {
                            throw new ApiUsageException(
                                    "Cannot change default group to " + group
                                            + "\n" + e.getMessage());
                        }
                    }
                }

                // If still null, take the current
                if (defaultGroup == null) {
                    defaultGroup = ctx.getCurrentGroupName();
                }

                Principal principal = new Principal(ctx.getCurrentUserName(),
                        defaultGroup, ctx.getCurrentEventType());
                principal = checkPrincipalNameAndDefaultGroup(sf, principal);

                // Unconditionally settable; these are open to the user for
                // change
                parseAndSetDefaultType(session.getDefaultEventType(), orig);
                parseAndSetDefaultPermissions(session.getDefaultPermissions(),
                        orig);
                parseAndSetUserAgent(session.getUserAgent(), orig);

                // Conditionally settable
                parseAndSetTimeouts(session.getTimeToLive(), session
                        .getTimeToIdle(), orig, trusted);

                // TODO Need to handle notifications
                return executeSessionContextLookup(sf, principal, orig);

            }
        });

        if (list == null) {
            cache.removeSession(uuid);
            throw new RemovedSessionException("Database contains no info for "
                    + uuid);
        }
        ;

        final SessionContext newctx = createSessionContext(list);
        final Session copy = copy(orig);
        executor.execute(asroot, new Executor.SimpleWork(this, "update") {
            @Transactional(readOnly = false)
            public Object doWork(org.hibernate.Session __s, ServiceFactory sf) {
                return executeUpdate(sf, copy, newctx.getCurrentUserId());
            }
        });
        cache.putSession(uuid, newctx);
        return copy(orig);

    }

    /**
     * Takes a snapshot as from
     * {@link #executeSessionContextLookup(ServiceFactory, Principal)} and turns
     * it into a {@link SessionContext} instance. List argument should never be
     * null. Abort if
     * {@link #executeSessionContextLookup(ServiceFactory, Principal)} returns
     * null.
     */
    @SuppressWarnings("unchecked")
    protected SessionContext createSessionContext(List<?> list) {

        final Experimenter exp = (Experimenter) list.get(0);
        final ExperimenterGroup grp = (ExperimenterGroup) list.get(1);
        final List<Long> memberOfGroupsIds = (List<Long>) list.get(2);
        final List<Long> leaderOfGroupsIds = (List<Long>) list.get(3);
        final List<String> userRoles = (List<String>) list.get(4);
        final Principal principal = (Principal) list.get(5);
        final Session session = (Session) list.get(6);

        parseAndSetDefaultType(principal.getEventType(), session);
        parseAndSetDefaultPermissions(principal.getUmask(), session);

        session.getDetails().setOwner(exp);
        session.getDetails().setGroup(grp);

        SessionContext sessionContext = new SessionContextImpl(session,
                leaderOfGroupsIds, memberOfGroupsIds, userRoles, factory
                        .createStats());
        return sessionContext;
    }

    public Session find(String uuid) {
        SessionContext sessionContext = cache.getSessionContext(uuid, true);
        return (sessionContext == null) ? null : sessionContext.getSession();
    }

    public int getReferenceCount(String uuid) {
        SessionContext ctx = cache.getSessionContext(uuid, true);
        return ctx.refCount();
    }

    public int detach(String uuid) {
        SessionContext ctx = cache.getSessionContext(uuid, false);
        return ctx.decrement();
    }

    public SessionStats getSessionStats(String uuid) {
        SessionContext ctx = cache.getSessionContext(uuid, true);
        return ctx.stats();
    }

    /*
     */
    public int close(String uuid) {

        SessionContext ctx;
        try {
            ctx = cache.getSessionContext(uuid, false);
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
        SessionContext ctx = cache.getSessionContext(uuid, true);
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

        // Bump the last accessed time.
        // May throw a session exception
        getReferenceCount(session);

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
        final SessionContext ctx = cache.getSessionContext(principal.getName(),
                true);
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
    private Principal checkPrincipalNameAndDefaultGroup(ServiceFactory sf,
            Principal p) {

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
            ExperimenterGroup g = _getDefaultGroup(sf, p.getName());
            if (g == null) {
                throw new ApiUsageException("Can't find default group for "
                        + p.getName());
            }
            group = g.getName();
        }

        // Also checking event type. Throws if missing (and at least a NPE)
        type = sf.getTypesService().getEnumeration(EventType.class, type)
                .getValue();

        Principal copy = new Principal(p.getName(), group, type);
        Permissions umask = p.getUmask();
        copy.setUmask(umask);
        return copy;
    }

    private void parseAndSetDefaultPermissions(Permissions perms,
            Session session) {
        String _perm = (perms == null) ? null : perms.toString();
        parseAndSetDefaultPermissions(_perm, session);
    }

    private void parseAndSetDefaultPermissions(String perms, Session session) {
        session.setDefaultPermissions(perms);
    }

    private void parseAndSetDefaultType(String type, Session session) {
        String _type = (type == null) ? "User" : type;
        session.setDefaultEventType(_type);
    }

    /**
     * For the moment, user agent is nullable meaning that the only way to unset
     * a set value is by passing in null, so this is allowed here. This implies
     * that the best way to keep userAgent from being set to null is to always
     * return to ISession.updateSession() a session value which was originally
     * retrieved.
     */
    private void parseAndSetUserAgent(String userAgent, Session session) {
        session.setUserAgent(userAgent);
    }

    private void parseAndSetTimeouts(Long timeToLive, Long timeToIdle,
            Session session, boolean trusted) {

        if (timeToLive != null) {

            if (trusted) {
                session.setTimeToLive(timeToLive);
            } else {

                // Let users set a value within reasons
                long activeTTL = Math.min(maxUserTimeToLive, timeToLive);

                // But if the value is 0, then the default must also be 0
                if (activeTTL == 0 && defaultTimeToLive != 0) {
                    throw new SecurityViolation("Cannot disable timeToLive. "
                            + "Value must be between 1 and "
                            + maxUserTimeToLive);
                }
                session.setTimeToLive(activeTTL);
            }
        }

        // As above
        if (timeToIdle != null) {
            if (trusted) {
                session.setTimeToIdle(timeToIdle);
            } else {
                long activeTTI = Math.min(maxUserTimeToIdle, timeToIdle);
                if (activeTTI == 0 && defaultTimeToIdle != 0) {
                    throw new SecurityViolation("Cannot disable timeToIdle. "
                            + "Value must be between 1 and "
                            + maxUserTimeToIdle);
                }
                session.setTimeToIdle(activeTTI);
            }
        }
    }

    public Session copy(Session source) {
        if (source == null) {
            throw new ApiUsageException("Source may not be null.");
        }

        Session target;
        if (source instanceof Share) {
            target = new Share();
        } else {
            target = new Session();
        }

        target.setId(source.getId());
        target.setClosed(source.getClosed());
        target.setDefaultEventType(source.getDefaultEventType());
        target.setDefaultPermissions(source.getDefaultPermissions());
        target.getDetails().shallowCopy(source.getDetails());
        target.setMessage(source.getMessage());
        target.setNode(source.getNode());
        target.setStarted(source.getStarted());
        target.setTimeToIdle(source.getTimeToIdle());
        target.setTimeToLive(source.getTimeToLive());
        target.setUserAgent(source.getUserAgent());
        target.setUuid(source.getUuid());

        if (target instanceof Share) {
            Share to = (Share) target;
            Share from = (Share) source;
            to.setItemCount(from.getItemCount());
            to.setActive(from.getActive());
            to.setData(from.getData());
        }

        return target;
    }

    // StaleCacheListener
    // =========================================================================

    /**
     */
    public void prepareReload() {
        // Noop
    }

    /**
     * Will be called in a synchronized block by {@link SessionCache} in order
     * to allow for an update.
     */
    @SuppressWarnings("unchecked")
    public SessionContext reload(final SessionContext ctx) {
        final Principal p = new Principal(ctx.getCurrentUserName(), ctx
                .getCurrentGroupName(), ctx.getCurrentEventType());
        List list = (List) executor.execute(asroot, new Executor.SimpleWork(
                this, "reload") {
            @Transactional(readOnly = true)
            public Object doWork(org.hibernate.Session session,
                    ServiceFactory sf) {
                return executeSessionContextLookup(sf, p, ctx.getSession());
            }
        });
        if (list == null) {
            return null;
        }
        return createSessionContext(list);
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
    private Session executeUpdate(ServiceFactory sf, Session session,
            long userId) {
        Node node = sf.getQueryService().findByQuery(
                "select n from Node n where uuid = :uuid",
                new Parameters().addString("uuid", internal_uuid).setFilter(
                        new Filter().page(0, 1)));
        if (node == null) {
            node = new Node(0L, false); // Using default node.
        }
        session.setNode(node);
        session.setOwner(new Experimenter(userId, false));
        return sf.getUpdateService().saveAndReturnObject(session);
    }

    private boolean executeCheckPassword(final Principal _principal,
            final String credentials) {
        boolean ok = false;
        try {
            ok = executeCheckPasswordRO(_principal, credentials);
            // WORKAROUND: If this throws a Spring exception then most likely
            // it's because the previous block was read-only. So try again with
            // read-write
        } catch (InternalException ie) {
            ok = executeCheckPasswordRW(_principal, credentials);
        }
        return ok;
    }

    private boolean executeCheckPasswordRO(final Principal _principal,
            final String credentials) {
        return (Boolean) executor.execute(asroot, new Executor.SimpleWork(this,
                "executeCheckPassword") {
            @Transactional(readOnly = true)
            public Object doWork(org.hibernate.Session session,
                    ServiceFactory sf) {
                return ((LocalAdmin) sf.getAdminService()).checkPassword(
                        _principal.getName(), credentials);
            }
        });
    }

    private boolean executeCheckPasswordRW(final Principal _principal,
            final String credentials) {
        return (Boolean) executor.execute(asroot, new Executor.SimpleWork(this,
                "executeCheckPassword") {
            @Transactional(readOnly = false)
            public Object doWork(org.hibernate.Session session,
                    ServiceFactory sf) {
                return ((LocalAdmin) sf.getAdminService()).checkPassword(
                        _principal.getName(), credentials);
            }
        });
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
                .executeStateless(new Executor.SimpleStatelessWork(this,
                        "executeCloseSession") {
                    @Transactional(readOnly = false)
                    public Object doWork(SimpleJdbcOperations jdbcOps) {
                        try {
                            int count = jdbcOps.update(
                                    "UPDATE session SET closed = now() "
                                            + "WHERE uuid = ?", uuid);
                            if (count == 0) {
                                log.warn("No session updated on closeSession:"
                                        + uuid);
                            } else {
                                log.debug("Session.closed set to now() for "
                                        + uuid);
                            }
                        } catch (Exception e) {
                            log.error("FAILED TO CLOSE SESSION IN DATABASE: "
                                    + uuid, e);
                        }
                        return null;
                    }
                });
    }

    private Session executeInternalSession() {
        final Long sessionId = executeNextSessionId();
        return (Session) executor
                .executeStateless(new Executor.SimpleStatelessWork(this,
                        "executeInternalSession") {
                    @Transactional(readOnly = false)
                    public Object doWork(SimpleJdbcOperations jdbcOps) {

                        // Create a basic session
                        final Permissions p = Permissions.USER_PRIVATE;
                        final Session s = new Session();
                        define(s, internal_uuid, "Session Manager internal",
                                System.currentTimeMillis(), Long.MAX_VALUE, 0L,
                                "Sessions", p);

                        // Set the owner and node specially for an internal sess
                        long nodeId = 0L;
                        try {
                            jdbcOps.queryForLong(
                                    "SELECT id FROM node where uuid = ?",
                                    internal_uuid);
                        } catch (EmptyResultDataAccessException erdae) {
                            // Using default node
                        }

                        // Have to copy values over due to unloaded
                        final Session s2 = copy(s);

                        // SQL defined in data.vm for creating original session
                        // (id,permissions,timetoidle,timetolive,started,closed,defaultpermissions,defaulteventtype,uuid,owner,node)
                        // select nextval('seq_session'),-35,
                        // 0,0,now(),now(),'rw----','PREVIOUSITEMS','1111',0,0;
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("sid", sessionId);
                        params.put("ttl", s.getTimeToLive());
                        params.put("tti", s.getTimeToIdle());
                        params.put("start", s.getStarted());
                        params.put("perms", s.getDefaultPermissions());
                        params.put("type", s.getDefaultEventType());
                        params.put("uuid", s.getUuid());
                        params.put("node", nodeId);
                        params.put("owner", roles.getRootId());
                        int count = jdbcOps
                                .update(
                                        "insert into session "
                                                + "(id,permissions,timetoidle,timetolive,started,closed,"
                                                + "defaultpermissions,defaulteventtype,uuid,owner,node)"
                                                + "values (:sid,-35,:ttl,:tti,:start,null,"
                                                + ":perms,:type,:uuid,:owner,:node)",
                                        params);
                        if (count == 0) {
                            throw new InternalException(
                                    "Failed to insert new session: "
                                            + s.getUuid());
                        }
                        Long id = jdbcOps.queryForLong(
                                "SELECT id FROM session WHERE uuid = ?", s
                                        .getUuid());
                        s.setId(id);
                        return s;
                    }
                });
    }

    /**
     * Added as an attempt to cure ticket:1176
     * 
     * @return
     */
    private Long executeNextSessionId() {
        return (Long) executor
                .executeStateless(new Executor.SimpleStatelessWork(this,
                        "executeNextSessionId") {
                    @Transactional(readOnly = false)
                    public Object doWork(SimpleJdbcOperations jdbcOps) {
                        return jdbcOps
                                .queryForLong("select ome_nextval('seq_session')");
                    }
                });
    }

    // ~ Non-executor helpers
    // =========================================================================

    /**
     * To prevent having the transaction rolled back, this method returns null
     * rather than throw an exception.
     */
    @SuppressWarnings("unchecked")
    private ExperimenterGroup _getDefaultGroup(ServiceFactory sf, String name) {
        LocalAdmin admin = (LocalAdmin) sf.getAdminService();
        try {
            Experimenter exp = admin.userProxy(name);
            ExperimenterGroup grp = admin.getDefaultGroup(exp.getId());
            return grp;
        } catch (Exception e) {
            log.warn("Exception while running " + "executeDefaultGroup", e);
            return null;
        }
    }

    /**
     * Looks up a user id by principal. If the name of the principal is actually
     * a removed user session, then a {@link RemovedSessionException} is thrown.
     */
    private long executeLookupUser(ServiceFactory sf, Principal p) {
        try {
            return sf.getAdminService().lookupExperimenter(p.getName()).getId();
        } catch (ApiUsageException aue) {
            throw new RemovedSessionException("Cannot find a user with name "
                    + p.getName());
        }
    }

    /**
     * Returns a List of state for creating a new {@link SessionContext}. If an
     * exception is thrown, return nulls since throwing an exception within the
     * Work will set our transaction to rollback only.
     */
    @SuppressWarnings("unchecked")
    private List<Object> executeSessionContextLookup(ServiceFactory sf,
            Principal principal, Session session) {
        try {
            List<Object> list = new ArrayList<Object>();
            LocalAdmin admin = (LocalAdmin) sf.getAdminService();
            final Experimenter exp = admin.userProxy(principal.getName());
            final ExperimenterGroup grp = admin
                    .groupProxy(principal.getGroup());
            final List<Long> memberOfGroupsIds = admin.getMemberOfGroupIds(exp);
            final List<Long> leaderOfGroupsIds = admin.getLeaderOfGroupIds(exp);
            final List<String> userRoles = admin.getUserRoles(exp);
            list.add(exp);
            list.add(grp);
            list.add(memberOfGroupsIds);
            list.add(leaderOfGroupsIds);
            list.add(userRoles);
            list.add(principal);
            list.add(session);
            return list;
        } catch (Exception e) {
            return null;
        }
    }
}
