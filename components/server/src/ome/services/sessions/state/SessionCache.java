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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import ome.conditions.ApiUsageException;
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
     * Read/write lock used to protect access to the {@link #doUpdate()} method
     * and any other use of {@link #internalRemove(String)}
     */
    private final ReadWriteLock updateLock = new ReentrantReadWriteLock();

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
    private long updateInterval = 1800000;

    /**
     * Time of the last update.
     */
    private long lastUpdate = System.currentTimeMillis();

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
        this.updateInterval = milliseconds;
    }

    // Accessors
    // ========================================================================

    public void setStaleCacheListener(StaleCacheListener staleCacheListener) {
        updateLock.writeLock().lock();
        try {
            this.staleCacheListener = staleCacheListener;
        } finally {
            updateLock.writeLock().unlock();
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

    }

    public SessionContext getSessionContext(String uuid) {
        return getSessionContext(uuid, true);
    }

    public SessionContext getSessionContext(String uuid, boolean blocking) {

        if (uuid == null) {
            throw new ApiUsageException("Uuid cannot be null.");
        }

        // Here it is necessary to possibly allow actions, like creation
        // to pass through without blocking, but if an internal remove is
        // later necessary these will be blockage anyway.
        if (blocking) {
            blockingUpdate();
        }

        //
        // All times are in milliseconds
        //

        // Getting quiet so that we have the previous access and hit info
        Element elt = sessions.getQuiet(uuid);

        if (elt == null) {
            // Previously we called internalRemove here, under the
            // assumption that some other thread/event could cause the
            // element to be set to null. That's no longer allowed
            // and will only occur by a call to internalRemove,
            // making that call unneeded.
            throw new RemovedSessionException("No context for " + uuid);
        }

        long lastAccess = elt.getLastAccessTime();
        long hits = elt.getHitCount();
        // Up'ing access time
        elt = sessions.get(uuid);

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
            internalRemove(uuid, reason);
            throw new SessionTimeoutException(reason);
        } else if (0 < timeToIdle && timeToIdle < idle) {
            String reason = reason("timeToIdle", lastAccess, hits, start,
                    timeToIdle, (idle - timeToIdle));
            internalRemove(uuid, reason);
            throw new SessionTimeoutException(reason);
        }
        return ctx;
    }

    private String reason(String why, long lastAccess, long hits, long start,
            long setting, long exceeded) {
        return String.format("Session (started=%s, hits=%s, last access=%s) "
                + "exceeded %s (%s) by %s ms", new Timestamp(start), hits,
                new Timestamp(lastAccess), why, setting, exceeded);
    }

    public void removeSession(String uuid) {
        blockingUpdate();
        internalRemove(uuid, "Remove session called");
    }

    private void internalRemove(String uuid, String reason) {
        updateLock.writeLock().lock();
        try {

            if (!sessions.isKeyInCache(uuid)) {
                log.info("Session already destroyed: " + uuid);
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
            updateLock.writeLock().unlock();
        }
    }

    public List<String> getIds() {
        blockingUpdate();
        return sessions.getKeys();
    }

    // State
    // =========================================================================

    public Ehcache inMemoryCache(String uuid) {
        // Check to make sure exists
        getSessionContext(uuid);
        String key = "memory:" + uuid;
        return createCache(key, true, Integer.MAX_VALUE);
    }

    public Ehcache onDiskCache(String uuid) {
        // Check to make sure exists
        getSessionContext(uuid);
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
        updateLock.readLock().lock();
        try {
            return lastUpdate;
        } finally {
            updateLock.readLock().unlock();
        }
    }

    public void markForUpdate() {
        updateLock.writeLock().lock();
        try {
            lastUpdate = 1;
        } finally {
            updateLock.writeLock().unlock();
        }
    }
    
    /**
     * May be called simultaneously by several threads. Similar to
     * {@link #blockingUpdate()} but uses the timestamp of the
     * {@link UserGroupUpdateEvent} rather than the current time.
     */
    public void updateEvent(UserGroupUpdateEvent ugue) {
        if (ugue == null || ugue.getTimestamp() > System.currentTimeMillis()) {
            // If the event is not valid, perform a regular blockingUpdate
            blockingUpdate();
        } else {
            updateLock.readLock().lock();
            if (lastUpdate <= ugue.getTimestamp()) {
                updateLock.readLock().unlock();
                doUpdate(ugue.getTimestamp());
            } else {
                updateLock.readLock().unlock();
            }
        }
    }

    /**
     * May be called simultaneously by several threads. Similar to
     * {@link #updateEvent(UserGroupUpdateEvent)} but uses the current time
     * rather than the event timestamp.
     */
    protected void blockingUpdate() {
        updateLock.readLock().lock();
        long targetUpdate = System.currentTimeMillis() - updateInterval;
        if (lastUpdate <= targetUpdate) {
            updateLock.readLock().unlock();
            doUpdate(targetUpdate);
        } else {
            updateLock.readLock().unlock();
        }
    }

    /**
     * Will only ever be accessed by a single thread. Rechecks the target update
     * time again in case a second write thread was blocking the current one.
     */
    @SuppressWarnings("unchecked")
    protected void doUpdate(long targetUpdate) {
        updateLock.writeLock().lock();
        try {
            // Could have been unset while we were waiting.
            if (0 <= lastUpdate && lastUpdate <= targetUpdate) {

                // Prevent recursion!
                // ------------------
                // To prevent another call from entering this block it's
                // necessary to update the lastUpdate time here.
                lastUpdate = -1;
                boolean success = false;

                try {
                    log.info("Synchronizing session cache");
                    if (staleCacheListener != null) {
                        staleCacheListener.prepareReload();
                        List<String> ids = sessions.getKeys();
                        for (String id : ids) {
                            Element elt = sessions.getQuiet(id);
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
                                long nextToLast = elt.getNextToLastAccessTime();
                                long update = System.currentTimeMillis();
                                long hits = elt.getHitCount();
                                Element fresh = new Element(id, replacement,
                                        version, creation, access, nextToLast,
                                        update, hits);
                                sessions.putQuiet(fresh);
                            }
                        }
                        success = true;
                    }
                } catch (Exception e) {
                    log.error("Error synchronizing cache", e);
                } finally {
                    if (success) {
                        lastUpdate = System.currentTimeMillis();
                    } else {
                        lastUpdate = 0L;
                        throw new InternalException(
                                "Could not update session cache."
                                        + "\nAll further attempts to access the server may fail."
                                        + "\nPlease contact your server administrator.");
                    }
                }
            }
        } finally {
            updateLock.writeLock().unlock();
        }
    }
}
