/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions.state;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import ome.conditions.ApiUsageException;
import ome.conditions.DatabaseBusyException;
import ome.conditions.InternalException;
import ome.conditions.RemovedSessionException;
import ome.conditions.SessionTimeoutException;
import ome.model.meta.Session;
import ome.services.messages.DestroySessionMessage;
import ome.services.sessions.SessionCallback;
import ome.services.sessions.SessionContext;
import ome.services.sessions.SessionManager;
import ome.services.sessions.events.UserGroupUpdateEvent;
import ome.system.OmeroContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Synchronized and lockable state for the {@link SessionManager}. Maps from
 * {@link Session} to {@link SessionContext} in memory, with each mapping also
 * having an additional cache which may spill over to disk.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class SessionCache implements ApplicationContextAware {

    private final static Log log = LogFactory.getLog(SessionCache.class);

    /**
     * Observer pattern used to clear the blocked
     * {@link SessionCache#needsUpdate} state, which prevents all further calls
     * from happening.
     */
    public interface StaleCacheListener {

        /**
         * Called once before all the reload methods are called to push out the
         * current state to database and trigger any exceptions as the current
         * user. Reload must be executed as root and so can't be run with
         * readOnly set to false.
         */
        void prepareReload();

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
     * Time in milliseconds between updates. Can be set via
     * {@link #setUpdateInterval(long)} but has a non-null value just in case
     * (30 minutes)
     */
    private long forceUpdateInterval = 1800000;

    /**
     * The amount of time in milliseconds that a thread is allowed to block for
     * during {@link #waitForUpdate()}.
     */
    private long allowedBlockTime = 10000L;

    /**
     * Time of the last update. This will be updated by a background thread.
     */
    private long lastUpdateRun = System.currentTimeMillis();

    /**
     * Time of the last update request. Most likely occurs via
     * SessionManagerImpl.onApplicationEvent(). Initialized to <em>before</em>
     * {@link #lastUpdateRun} to prevent initial blocking.
     */
    private AtomicLong lastUpdateRequest = new AtomicLong(lastUpdateRun - 1);

    /**
     * Read/write lock used to protect access to the {@link #doUpdate()} method
     * and any other use of {@link #internalRemove(String)}
     */
    private final ReadWriteLock runUpdate = new ReentrantReadWriteLock();

    /**
     * Injected {@link CacheManager} used to create various caches.
     */
    private CacheManager ehmanager;

    /**
     * Primary in-memory cache which maps from session uuids as strings to
     * {@link SessionContext} instances.
     */
    private Ehcache sessions;

    /**
     * 
     */
    private final ConcurrentHashMap<String, Set<SessionCallback>> sessionCallbackMap = new ConcurrentHashMap<String, Set<SessionCallback>>(
            64);

    private StaleCacheListener staleCacheListener = null;

    /**
     * {@link OmeroContext} instance used to publish
     * {@link DestroySessionMessage} on {@link #removeSession(String)}
     */
    private OmeroContext context;

    /**
     * Injection method, also performs the creation of {@link #sessions}
     */
    public void setCacheManager(CacheManager manager) {
        this.ehmanager = manager;
        sessions = createCache("SessionCache", true, Integer.MAX_VALUE);
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

    /**
     * Inject time in milliseconds to allow blocking
     */
    public void setAllowedBlockTime(long allowedBlockTime) {
        this.allowedBlockTime = allowedBlockTime;
    }

    // Accessors
    // ========================================================================

    public void setStaleCacheListener(StaleCacheListener staleCacheListener) {
        runUpdate.writeLock().lock();
        try {
            this.staleCacheListener = staleCacheListener;
        } finally {
            runUpdate.writeLock().unlock();
        }
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
        synchronized (sessionCallbackMap) {
            Set<SessionCallback> set = sessionCallbackMap.get(session);
            if (set == null) {
                return false;
            }
            return set.remove(cb);
        }
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
        sessions.put(new Element(uuid, sessionContext));
        final StopWatch sw = new CommonsLogStopWatch("omero.session");
        addSessionCallback(uuid, new SessionCallback.SimpleCloseCallback(){
            public void close() {
                sw.stop();
            }});
    }

    /**
     * Retrieve a session possibly raising either
     * {@link RemovedSessionException} or {@link SessionTimeoutException}.
     */
    public SessionContext getSessionContext(String uuid, boolean blocking) {

        if (uuid == null) {
            throw new ApiUsageException("Uuid cannot be null.");
        }

        // Here it is necessary to possibly allow actions, like creation
        // to pass through without blocking, but if an internal remove is
        // later necessary these will be blockage anyway.
        if (blocking) {
            waitForUpdate();
        }

        SessionContext ctx = (SessionContext) getElementNullOrThrowOnTimeout(
                uuid, true).getObjectValue();

        // Up'ing access time
        sessions.get(uuid);
        return ctx;
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
    private Element getElementNullOrThrowOnTimeout(String uuid, boolean strict) {
        //
        // All times are in milliseconds
        //

        // Getting quiet so that we have the previous access and hit info
        final Element elt = sessions.getQuiet(uuid);

        if (elt == null) {
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

        long lastAccess = elt.getLastAccessTime();
        long hits = elt.getHitCount();

        // Get session info
        SessionContext ctx = (SessionContext) elt.getObjectValue();
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
        return elt;
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
        try {

            if (!sessions.isKeyInCache(uuid)) {
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
        } finally {
            // pass ticket:3181
        }
    }

    /**
     * Since all methods which use {@link #getIds()} will subsequently check for
     * the existing session, we do not block here. Blocking is primarily useful
     * for post-admintype changes which can add or remove a user from a group.
     * The existence of a session (which is what getIds specifies) is not
     * significantly effected.
     */
    public List<String> getIds() {
        // waitForUpdate();
        return sessions.getKeys();
    }

    // State
    // =========================================================================

    public Ehcache inMemoryCache(String uuid) {
        // Check to make sure exists
        getSessionContext(uuid, true);
        String key = "memory:" + uuid;
        return createCache(key, true, Integer.MAX_VALUE);
    }

    public Ehcache onDiskCache(String uuid) {
        // Check to make sure exists
        getSessionContext(uuid, true);
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

    public long getLastUpdated() {
        runUpdate.readLock().lock();
        try {
            return lastUpdateRun;
        } finally {
            runUpdate.readLock().unlock();
        }
    }

    /**
     * Marks a new update request in {@link #lastUpdateRequest}. If the
     * timestamp on the event is invalid, then
     * {@link System#currentTimeMillis()} will be used. This method updates
     * {@link #lastUpdateRequest} in the {@link ReadWriteLock#readLock()} of
     * {@link #runUpdate}, since only blocking changed during the background
     * thread are of importance (i.e. we don't want to miss a change). However,
     * because getting/setting a long value is not always an atomic operation,
     * we use a {@link AtomicLong}.
     */
    public void updateEvent(UserGroupUpdateEvent ugue) {
        long time = 0;
        if (ugue == null || ugue.getTimestamp() > System.currentTimeMillis()) {
            time = System.currentTimeMillis();
        } else {
            time = ugue.getTimestamp();
        }

        runUpdate.readLock().lock();
        try {
            lastUpdateRequest.set(time);
        } finally {
            runUpdate.readLock().unlock();
        }
    }

    /**
     * If {@link #lastUpdateRun} is older than {@link #lastUpdateRequest}, then
     * wait until the next background thread updates lastUpdateRequest. Note:
     * this method does not use {@link #forceUpdateInterval} since that is
     * primarily to guarantee that old sessions are removed. If synchronization
     * takes too long, an {@link DatabaseBusyException} is thrown.
     */
    protected void waitForUpdate() {
        long start = System.currentTimeMillis();
        long finish = start + allowedBlockTime;
        boolean first = true;
        while (finish > System.currentTimeMillis()) {
            boolean needsUpdate = false;

            // Gets the current status, possibly waiting on any running updates
            try {
                boolean locked = runUpdate.readLock().tryLock(500L,
                        TimeUnit.MILLISECONDS);
                if (!locked) {
                    log.debug("Failed to acquire read lock in 500 ms");
                    continue;
                }
            } catch (InterruptedException e1) {
                log.debug("Interrupted while waiting on read lock");
                continue;
            }

            try {
                needsUpdate = checkNeedsUpdateWithoutLock();
            } finally {
                runUpdate.readLock().unlock();
            }

            if (needsUpdate) {
                try {
                    if (first) {
                        log.info("Waiting for synchronization");
                        first = false;
                    }
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    log.warn("Interrupted. Retrying wait...");
                    continue; // Not sure about this. Shouldn't happen.
                }
            } else {
                return;
            }
        }

        throw new DatabaseBusyException(
                "Timed out while waiting on synchronization", 2000L);

    }

    /**
     * Whether or not {@link #doUpdate()} should run. This does not perform any
     * synchronization so that methods can choose to use a read or write lock.
     */
    protected boolean checkNeedsUpdateWithoutLock() {

        // Check whether entry is required.
        if (lastUpdateRun < 0) {
            // Then we are currently running
            return false;
        }

        long manual = lastUpdateRequest.get();
        if (lastUpdateRun <= manual) {
            return true;
        }

        long timed = System.currentTimeMillis() - forceUpdateInterval;
        if (lastUpdateRun <= timed) {
            return true;
        }

        return false;
    }

    /**
     * Will only ever be accessed by a single thread. Rechecks the target update
     * time again in case a second write thread was blocking the current one.
     * {@link #lastUpdateRun} gets set to a negative value to specify that this
     * method is currently running.
     */
    @SuppressWarnings("unchecked")
    public void doUpdate() {

        boolean locked = false;
        try {
            locked = runUpdate.writeLock().tryLock(3 * 60, TimeUnit.SECONDS);
        } catch (InterruptedException e1) {
            log.debug("Interrupted while waiting on update lock");
        }

        if (!locked) {
            throw new InternalException("Cannot get access to update lock");
        }

        try {
            // Check whether entry is required.
            if (checkNeedsUpdateWithoutLock()) {

                // Prevent recursion!
                // ------------------
                // To prevent another call from entering this block it's
                // necessary to update the lastUpdate time here.
                lastUpdateRun = -1;
                boolean success = false;

                try {
                    if (staleCacheListener != null) {
                        staleCacheListener.prepareReload();
                        List<String> ids = sessions.getKeys();
                        log.info("Synchronizing session cache. Count = "
                                + ids.size());
                        long start = System.currentTimeMillis();
                        for (String id : ids) {
                            Element elt = getElementNullOrThrowOnTimeout(id,
                                    false);
                            if (elt == null) {
                                internalRemove(id, "Timeout");
                            } else {
                                SessionContext ctx = (SessionContext) elt
                                        .getObjectValue();
                                // May throw an exception
                                SessionContext replacement = staleCacheListener
                                        .reload(ctx);
                                if (replacement == null) {
                                    internalRemove(id, "Replacement null");
                                } else {
                                    // Adding and upping access information.
                                    long version = elt.getVersion() + 1;
                                    long creation = elt.getCreationTime();
                                    long access = elt.getLastAccessTime();
                                    long nextToLast = elt
                                            .getNextToLastAccessTime();
                                    long update = System.currentTimeMillis();
                                    long hits = elt.getHitCount();
                                    Element fresh = new Element(id,
                                            replacement, version, creation,
                                            access, nextToLast, update, hits);
                                    sessions.putQuiet(fresh);
                                }
                            }
                        }
                        success = true;
                        log.info(String.format("Synchronization took %s ms.",
                                System.currentTimeMillis() - start));
                    }
                } catch (Exception e) {
                    log.error("Error synchronizing cache", e);
                } finally {
                    if (success) {
                        lastUpdateRun = System.currentTimeMillis();
                    } else {
                        lastUpdateRun = 0L;
                        throw new InternalException(
                                "Could not update session cache."
                                        + "\nAll further attempts to access the server may fail."
                                        + "\nPlease contact your server administrator.");
                    }
                }
            }
        } finally {
            runUpdate.writeLock().unlock();
        }
    }
}
