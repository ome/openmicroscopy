/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions.state;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import ome.conditions.ApiUsageException;
import ome.conditions.RemovedSessionException;
import ome.conditions.SessionTimeoutException;
import ome.model.meta.Session;
import ome.services.messages.DestroySessionMessage;
import ome.services.sessions.SessionCallback;
import ome.services.sessions.SessionContext;
import ome.services.sessions.SessionManager;
import ome.services.sessions.SessionManagerImpl;
import ome.services.sessions.events.UserGroupUpdateEvent;
import ome.system.OmeroContext;

import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;

/**
 * Synchronized and lockable state for the {@link SessionManager}. Maps from
 * {@link Session} uuid to {@link SessionContext} in memory, with each mapping
 * also having an additional cache which may spill over to disk,
 * {@link StaleCacheListener listeners}.
 *
 * Uses {@link MapMaker} and various implementations from
 * java.util.concurrent.atomic to provide a lock-free implementation.
 *
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.2.1
 * @see ticket:3173
 */
public class SessionCache implements ApplicationContextAware {

    private final static Logger log = LoggerFactory.getLogger(SessionCache.class);

    /**
     * Observer pattern used to refresh sessions in doUpdate.
     */
    public interface StaleCacheListener {

        /**
         * Method called for every active session in the cache. The returned
         * {@link SessionContext} will be used to replace the current one.
         * 
         * Any runtime exception can be thrown to show that an update is not
         * possible.
         */
        SessionContext reload(SessionContext context);
    }

    /**
     * Container which can be put in a single {@link AtomicReference} instance.
     * Contains all the data for a single session immutably. Therefore any
     * thread that manages to get access to this instance can work with this
     * data even if another thread is currently in the process of removing this
     * from the map.
     */
    private static class Data {

        public final static Integer MAX_ERROR = 3;

        /**
         * Count of errors which occur during {@link SessionCache#doUpdate()}.
         * These are not copied from previous instances since if a new Data
         * is created, then it is valid for the error count to be reset. The
         * intended effect of counting errors is that a sporadic DB exception
         * on {@link StaleCacheListener#reload(SessionContext)} does not remove
         * a perfectly valid session. Instead, the error must occur multiple
         * times.
         */
        final AtomicInteger error = new AtomicInteger(0);

        final SessionContext sessionContext;
        final long lastAccessTime;
        final long hitCount;

        /**
         * Initial creation of a Data instance when a new session is
         * added to the cache.
         */
        Data(SessionContext sc) {
            this(sc, System.currentTimeMillis(), 1);
        }

        /**
         * Copy constructor which uses the current time for {@link #lastAccessTime} and
         * increments {@link #hitCount} by one. Used when updating the access
         * time for a session.
         */
        Data(Data old) {
            this(old, true);
        }

        /**
         * Copy constructor which uses either the current time for {@link #lastAccessTime}
         * (if reset is true) or the previous lastAccessTime (if reset is false); and
         * increments {@link #hitCount} by one. Used when reloading the session.
         */
        Data(Data old, boolean reset) {
            this(old, old.sessionContext, reset);
        }

        /**
         * Like {@link Data#Data(Data, boolean)} but allows setting the
         * {@link SessionContext} which should be stored in the new instance.
         * This is used on reload. See {@link SessionCache#doUpdate()}.
         * @param old
         * @param ctx
         * @param reset
         */
        Data(Data old, SessionContext ctx, boolean reset) {
            this(ctx, reset ? System.currentTimeMillis() : old.lastAccessTime, old.hitCount+1);
        }

        Data(SessionContext sc, long last, long count) {
            this.sessionContext = sc;
            this.lastAccessTime = last;
            this.hitCount = count;
            // clear context
            sc.getSession().getDetails().setContexts(null);
        }

    }

    /**
     * Container which can be put in a single {@link AtomicReference} instance
     * to hold all the state of the session cache instance immutably.
     *
     * Similar to {@link Data}, any thread which manages to get access to a
     * {@link State} instance may act on it, even if a new instance is activated
     * in the background.
     */
    private static class State {

        /**
         * Time of the last update. This will be updated by a background thread.
         */
        final long lastUpdateRun;

        /**
         * Time of the last update request. Most likely occurs via
         * SessionManagerImpl.onApplicationEvent(). Initialized to <em>before</em>
         * {@link #lastUpdateRun} to prevent initial blocking.
         */
        final long lastUpdateRequest;

        /**
         * Initial creation of State, used on cache creation.
         */
        State() {
            this.lastUpdateRun = System.currentTimeMillis();
            this.lastUpdateRequest = this.lastUpdateRun - 1;
        }

        /**
         * Update constructor for State, which is used when a new update request
         * is received by the cache.
         *
         * Specifies that a new request has occurred, but the old run
         * is kept.
         */
        State(State old, long request) {
            this.lastUpdateRun = old.lastUpdateRun;
            this.lastUpdateRequest = request;
        }


        /**
         * Whether or not {@link #doUpdate()} should run. Returns immediately
         * if {@link #active} contains true.
         */
        boolean checkNeedsUpdate(long forceUpdateInterval) {

            // Check whether entry is required.
            if (lastUpdateRun < 0) {
                // Then we are currently running
                return false;
            }

            if (lastUpdateRun <= lastUpdateRequest) {
                return true;
            }

            long timed = System.currentTimeMillis() - forceUpdateInterval;
            if (lastUpdateRun <= timed) {
                return true;
            }

            return false;
        }

    }

    /**
     *
     */
    private final Map<String, Data> sessions;

    /**
     *
     */
    private final AtomicReference<State> state = new AtomicReference<State>(new State());

    /**
     * Time in milliseconds between updates. Can be set via
     * {@link #setUpdateInterval(long)} but has a non-null value just in case
     * (30 minutes)
     */
    private long forceUpdateInterval = 1800000;

    /**
     * Injected {@link CacheManager} used to create various caches.
     */
    private CacheManager ehmanager;

    /**
     * 
     */
    private final Map<String, Set<SessionCallback>> sessionCallbackMap;

    private final AtomicReference<StaleCacheListener> staleCacheListener = new AtomicReference<StaleCacheListener>();

    /**
     * Whether or not {@link #doUpdate()} is currently running.
     */
    private final AtomicBoolean active = new AtomicBoolean();

    /**
     * {@link OmeroContext} instance used to publish
     * {@link DestroySessionMessage} on {@link #removeSession(String)}
     */
    private OmeroContext context;

    public SessionCache() {
        final MapMaker mapMaker = new MapMaker();
        sessions = mapMaker.makeMap();
        sessionCallbackMap = mapMaker.makeMap();
    }

    /**
     * Injection method, also performs the creation of {@link #sessions}
     */
    public void setCacheManager(CacheManager manager) {
        this.ehmanager = manager;
    }

    /**
     * Context injector.
     */
    public void setApplicationContext(ApplicationContext ctx)
            throws BeansException {
        context = (OmeroContext) ctx;
    }

    /**
     * Inject time in milliseconds between updates.
     */
    public void setUpdateInterval(long milliseconds) {
        this.forceUpdateInterval = milliseconds;
    }

    // Accessors
    // ========================================================================

    public void setStaleCacheListener(StaleCacheListener staleCacheListener) {
        this.staleCacheListener.set(staleCacheListener);
    }

    public boolean addSessionCallback(String session, SessionCallback cb) {
        synchronized (sessionCallbackMap) {
            Set<SessionCallback> set = sessionCallbackMap.get(session);
            if (set == null) {
                set = new HashSet<SessionCallback>();
                sessionCallbackMap.put(session, set);
            }
            return set.add(cb);
        }
    }

    public boolean removeSessionCallback(String session, SessionCallback cb) {
        return null != sessionCallbackMap.remove(session);
    }

    // State management
    // ========================================================================
    // These methods are currently the only access to the sessions cache, and
    // are responsible for synchronization and the update mechanism.

    /**
     * Puts a session blindly into the context. This does nothing to a context
     * which was previously present (e.g. call internalRemove, etc.) and
     * therefore usage should be proceeded by a check.
     */
    public void putSession(String uuid, SessionContext sessionContext) {
        Data data = new Data(sessionContext);
        this.sessions.put(uuid, data);
        final StopWatch sw = new Slf4JStopWatch("omero.session");
        addSessionCallback(uuid, new SessionCallback.SimpleCloseCallback(){
            public void close() {
                sw.stop();
            }});
    }

    /**
     * Used externally to refresh the {@link SessionContext} instance
     * associated with the session uuid
     * @param id
     * @param replacement
     */
    public void refresh(String uuid, SessionContext replacement) {
        Data data = getDataNullOrThrowOnTimeout(uuid, true);
        refresh(uuid, data, replacement);
    }

    /**
     *
     * @param uuid
     * @param data
     * @param replacement
     */
    private void refresh(String uuid, Data data, SessionContext replacement) {
        // Adding and upping hit information.
        Data fresh = new Data(data, replacement, false);
        this.sessions.put(uuid, fresh);
    }

    /**
     * Retrieve a session possibly raising either
     * {@link RemovedSessionException} or {@link SessionTimeoutException}.
     */
    public SessionContext getSessionContext(String uuid) {
        return getSessionContext(uuid, false);
    }

    /**
     * Retrieve a session possibly raising either
     * {@link RemovedSessionException} or {@link SessionTimeoutException}.
     *
     * @param quietly If true, then the access time for the given UUID
     *                  will not be updated.
     */
    public SessionContext getSessionContext(String uuid, boolean quietly) {
        if (uuid == null) {
            throw new ApiUsageException("Uuid cannot be null.");
        }

        Data data = getDataNullOrThrowOnTimeout(uuid, true);

        if (!quietly) {
            // Up'ing access time
            this.sessions.put(uuid, new Data(data));
        }
        return data.sessionContext;
    }

    /**
     * Returns all the data contained in the internal implementation of
     * this manger.
     *
     * @param quietly If true, then the access time for the given UUID
     *                  will not be updated.
     */
    public Map<String, Object> getSessionData(String uuid, boolean quietly) {

        if (uuid == null) {
            throw new ApiUsageException("Uuid cannot be null.");
        }

        Data data = getDataNullOrThrowOnTimeout(uuid, true);

        if (!quietly) {
            // Up'ing access time
            this.sessions.put(uuid, new Data(data));
        }

        return new ImmutableMap.Builder<String, Object>()
            .put("class", getClass().getName())
            .put("sessionContext", data.sessionContext)
            .put("hitCount", data.hitCount)
            .put("lastAccessTime", data.lastAccessTime)
            // .put("error", data.error.get())
            .build();
    }

    /**
     * Gets the {@link SessionContext} without upping its access information and
     * instead checks the current values for timeouts. Can optionally return
     * null or throw an exception.
     * 
     * @param uuid
     * @param strict
     *            If true, an exception will be raised on timeout; otherwise
     *            null returned.
     * @return
     */
    private Data getDataNullOrThrowOnTimeout(String uuid, boolean strict) {
        //
        // All times are in milliseconds
        //

        // Getting quiet so that we have the previous access and hit info
        final Data data = this.sessions.get(uuid);

        if (data == null) {
            // Previously we called internalRemove here, under the
            // assumption that some other thread/event could cause the
            // element to be set to null. That's no longer allowed
            // and will only occur by a call to internalRemove,
            // making that call unneeded.
            if (strict) {
                throw new RemovedSessionException("No context for " + uuid);
            } else {
                return null;
            }
        }

        long lastAccess = data.lastAccessTime;
        long hits = data.hitCount;

        // Get session info
        SessionContext ctx = data.sessionContext;
        long now = System.currentTimeMillis();
        long start = ctx.getSession().getStarted().getTime();
        long timeToIdle = ctx.getSession().getTimeToIdle();
        long timeToLive = ctx.getSession().getTimeToLive();

        // If never accessed, used creation time
        if (lastAccess == 0) {
            lastAccess = start;
        }

        // Calculated
        long alive = now - start;
        long idle = now - lastAccess;

        // Do comparisons if timeTo{} is non-0
        if (0 < timeToLive && timeToLive < alive) {
            String reason = reason("timeToLive", lastAccess, hits, start,
                    timeToLive, (alive - timeToLive));
            if (strict) {
                throw new SessionTimeoutException(reason, ctx);
            } else {
                return null;
            }
        } else if (0 < timeToIdle && timeToIdle < idle) {
            String reason = reason("timeToIdle", lastAccess, hits, start,
                    timeToIdle, (idle - timeToIdle));
            if (strict) {
                throw new SessionTimeoutException(reason, ctx);
            } else {
                return null;
            }
        }
        return data;
    }

    private String reason(String why, long lastAccess, long hits, long start,
            long setting, long exceeded) {
        return String.format("Session (started=%s, hits=%s, last access=%s) "
                + "exceeded %s (%s) by %s ms", new Timestamp(start), hits,
                new Timestamp(lastAccess), why, setting, exceeded);
    }

    public void removeSession(String uuid) {
        internalRemove(uuid, "Remove session called");
    }

    private void internalRemove(String uuid, String reason) {

        if (!sessions.containsKey(uuid)) {
            log.warn("Session not in cache: " + uuid);
            return; // EARLY EXIT!
        }

        log.info("Destroying session " + uuid + " due to : " + reason);

        // Announce to all callbacks.
        Set<SessionCallback> cbs = sessionCallbackMap.get(uuid);
        if (cbs != null) {
            for (SessionCallback cb : cbs) {
                try {
                    cb.close();
                } catch (Exception e) {
                    final String msg = "SessionCallback %s throw exception for session %s";
                    log.warn(String.format(msg, cb, uuid), e);
                }
            }
        }

        // Announce to all listeners
        try {
            context.publishEvent(new DestroySessionMessage(this, uuid));
        } catch (RuntimeException re) {
            final String msg = "Session listener threw an exception for session %s";
            log.warn(String.format(msg, uuid), re);
        }

        ehmanager.removeCache("memory:" + uuid);
        ehmanager.removeCache("ondisk:" + uuid);
        sessions.remove(uuid);

    }

    /**
     * Since all methods which use {@link #getIds()} will subsequently check for
     * the existing session, we do not block here. Blocking is primarily useful
     * for post-admintype changes which can add or remove a user from a group.
     * The existence of a session (which is what getIds specifies) is not
     * significantly effected.
     */
    public Set<String> getIds() {
        return sessions.keySet();
    }

    // State
    // =========================================================================

    public Ehcache inMemoryCache(String uuid) {
        // Check to make sure exists
        getDataNullOrThrowOnTimeout(uuid, true);
        String key = "memory:" + uuid;
        return createCache(key, true, Integer.MAX_VALUE);
    }

    public Ehcache onDiskCache(String uuid) {
        // Check to make sure exists
        getDataNullOrThrowOnTimeout(uuid, true);
        String key = "ondisk:" + uuid;
        return createCache(key, false, 100);
    }

    protected Ehcache createCache(String key, boolean inMemory, int maxInMemory) {
        Ehcache cache = null;
        try {
            cache = ehmanager.getEhcache(key);
        } catch (Exception e) {
            // ok
        }

        if (cache == null) {
            CacheFactory factory = new CacheFactory();
            factory.setBeanName(key);
            factory.setCacheManager(ehmanager);
            factory.setOverflowToDisk(!inMemory);
            factory.setMaxElementsInMemory(maxInMemory);
            factory.setMaxElementsOnDisk(0);
            factory.setDiskPersistent(false);
            factory.setTimeToIdle(0);
            factory.setTimeToLive(0);
            cache = factory.createCache();
        }
        return cache;
    }

    // Update
    // =========================================================================

    // Primarily used for testing.
    public long getLastUpdated() {
        return state.get().lastUpdateRun;
    }

    /**
     * Marks a new update request in {@link State#lastUpdateRequest}. If the
     * timestamp on the event is invalid, then
     * {@link System#currentTimeMillis()} will be used.
     */
    public void updateEvent(UserGroupUpdateEvent ugue) {
        long time = 0;
        if (ugue == null || ugue.getTimestamp() > System.currentTimeMillis()) {
            time = System.currentTimeMillis();
        } else {
            time = ugue.getTimestamp();
        }

        State old = state.get();
        state.set(new State(old, time));
    }

    /**
     * Will only ever be accessed by a single thread. Rechecks the target update
     * time again in case a second write thread was blocking the current one.
     * {@link #lastUpdateRun} gets set to a negative value to specify that this
     * method is currently running.
     */
    public void doUpdate() {

        // Check whether entry is required.
        if (!state.get().checkNeedsUpdate(forceUpdateInterval)) {
            return;
        }

        // Prevent recursion!
        // ------------------
        // To prevent another call from entering this block it's
        // necessary to set active
        if (!active.compareAndSet(false, true)) {
            return;
        }

        try {
            final Set<String> ids = sessions.keySet();
            log.info("Synchronizing session cache. Count = " + ids.size());
            final StopWatch sw = new Slf4JStopWatch();
            for (String id : ids) {
                reload(id);
            }

            sw.stop("omero.sessions.synchronization");
            log.info(String.format("Synchronization took %s ms.",
                    sw.getElapsedTime()));

        } catch (Exception e) {
            log.error("Error synchronizing cache", e);
        } finally {
            active.set(false);
        }

    }

    /**
     * Provides the reloading logic of the {@link SessionCache} for the
     * {@link SessionManagerImpl} to use.
     *
     * @see ticket:4011
     * @see ticket:5849
     */
    public void reload(String id) {

        final StaleCacheListener listener = staleCacheListener.get();
        if (listener == null) {
            log.error("Null stale cache listener!");
            return;
        }

        Data data = null;
        try {
            data = getDataNullOrThrowOnTimeout(id, false);
            if (data == null) {
                internalRemove(id, "Timeout");
                return;
            }
        } catch (Exception e) {
            // If an exception occurs here, then something is wrong
            // with the Data instance itself since no DB calls are
            // made. Therefore the instance will be removed.
            log.warn("Removing session on get error of " + id, e);
            internalRemove(id, "Get error");
        }

        try {
            SessionContext ctx = data.sessionContext;
            // May throw an exception
            SessionContext replacement = listener.reload(ctx);
            if (replacement == null) {
                internalRemove(id, "Replacement null");
            } else {
                refresh(id, data, replacement);
            }
        } catch (Exception e) {
            // If an exception occurs it MAY be transient, therefore
            // we count the number of errors that have happened for
            // this specific instance as described under Data#errors
            // just to be safe.
            int count = data.error.incrementAndGet();
            if (count > Data.MAX_ERROR) {
                log.warn("Removing session on reload error of " + id, e);
                internalRemove(id, "Reload error");
            } else {
                log.warn(count + "error(s) on reload of " + id, e);
            }
        }
    }

}
