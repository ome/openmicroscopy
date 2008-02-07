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
import ome.conditions.InternalException;
import ome.conditions.RemovedSessionException;
import ome.conditions.SessionTimeoutException;
import ome.model.meta.Session;
import ome.services.sessions.SessionCallback;
import ome.services.sessions.SessionContext;
import ome.services.sessions.SessionManager;
import ome.services.sessions.events.UserGroupUpdateEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Synchronized and lockable state for the {@link SessionManager}. Maps from
 * {@link Session} to {@link SessionContext} in memory, with each mapping also
 * having an additional cache which may spill over to disk.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class SessionCache extends CacheListener {

    private final static Log log = LogFactory.getLog(SessionCache.class);

    private final ReadWriteLock updateLock = new ReentrantReadWriteLock();

    /**
     * Observer pattern used to clear the blocked
     * {@link SessionCache#needsUpdate} state, which prevents all further calls
     * from happening.
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

    private boolean needsUpdate = false;
    private long lastUpdate = System.currentTimeMillis();
    private CacheManager ehmanager;
    private Ehcache sessions;
    private final ConcurrentHashMap<String, Ehcache> diskCacheMap = new ConcurrentHashMap<String, Ehcache>(
            64);
    private final ConcurrentHashMap<String, Set<SessionCallback>> sessionCallbackMap = new ConcurrentHashMap<String, Set<SessionCallback>>(
            64);
    private StaleCacheListener staleCacheListener = null;

    public void setCacheManager(CacheManager manager) {
        this.ehmanager = manager;
        CacheFactory inmemory = new CacheFactory();
        inmemory.setBeanName("SessionCache");
        inmemory.setCacheManager(ehmanager);
        inmemory.setOverflowToDisk(false);
        inmemory.setMaxElementsInMemory(Integer.MAX_VALUE);
        inmemory.setTimeToIdle(0);
        inmemory.setTimeToLive(0);
        sessions = inmemory.createCache();
    }

    // Callbacks from main sessions
    // ========================================================================

    @Override
    public void notifyElementExpired(Ehcache c, Element elt) {
        String key = (String) elt.getKey();
        Ehcache c2 = diskCacheMap.get(key);
        if (c2 != null) {
            c2.getCacheManager().removeCache(c2.getName());
            diskCacheMap.remove(key);
        }
        Set<SessionCallback> set = sessionCallbackMap.get(key);
        if (set != null) {
            for (SessionCallback cb : set) {
                try {
                    // TODO possibly pass in the state that we're about to kill
                    // here.
                    cb.close();
                } catch (Exception e) {
                    log.error("Session callback threw exception:" + cb, e);
                }
            }
        }
    }

    // Accessors
    // ========================================================================

    public void setStaleCacheListener(StaleCacheListener staleCacheListener) {
        this.staleCacheListener = staleCacheListener;
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

    public long getLastUpdated() {
        return lastUpdate;
    }

    public void updateEvent(UserGroupUpdateEvent ugue) {
        updateLock.writeLock().lock();
        try {
            if (lastUpdate < ugue.getTimestamp()) {
                doUpdate();
            }
        } finally {
            updateLock.writeLock().unlock();
        }
    }

    public void setNeedsUpdate(boolean needsUpdate) {
        this.needsUpdate = needsUpdate;
        doUpdate();
    }

    public boolean getNeedsUpdate() {
        return this.needsUpdate;
    }

    // State management
    // ========================================================================
    // These methods are currently the only access to the sessions cache, and
    // are responsible for synchronization and the update mechanism.

    public void putSession(String uuid, SessionContext sessionContext) {
        blockingUpdate();
        sessions.put(new Element(uuid, sessionContext));

    }

    public SessionContext getSessionContext(String uuid) {
        blockingUpdate();

        //
        // All times are in milliseconds
        //

        // Getting quiet so that we have the previous access and hit info
        Element elt = sessions.getQuiet(uuid);
        if (elt == null) {
            internalRemove(uuid);
            throw new RemovedSessionException("No context for " + uuid);
        }
        long lastAccess = elt.getLastAccessTime();
        long hits = elt.getHitCount();
        // Up'ing access time
        sessions.get(uuid);

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
            internalRemove(uuid);
            throwExpiredException("timeToLive", lastAccess, hits, start,
                    timeToLive, (alive - timeToLive));
        } else if (0 < timeToIdle && timeToIdle < idle) {
            internalRemove(uuid);
            throwExpiredException("timeToLive", lastAccess, hits, start,
                    timeToIdle, (idle - timeToIdle));
        }
        return ctx;
    }

    private void throwExpiredException(String why, long lastAccess, long hits,
            long start, long setting, long exceeded) {
        throw new SessionTimeoutException(String.format(
                "Session (started=%s, hits=%s, last access=%s) "
                        + "exceeded %s (%s) by %s ms", new Timestamp(start),
                hits, new Timestamp(lastAccess), why, setting, exceeded));
    }

    public void removeSession(String uuid) {
        blockingUpdate();
        internalRemove(uuid);
    }

    private void internalRemove(String uuid) {
        sessions.remove(uuid);
        Set<SessionCallback> cbs = sessionCallbackMap.get(uuid);
        if (cbs != null) {
            for (SessionCallback cb : cbs) {
                try {
                    cb.close();
                } catch (Exception e) {
                    log.warn(String.format(
                            "SessionCallback %s throw exception.", cb), e);
                }
            }
        }
    }

    public List<String> getIds() {
        blockingUpdate();
        return sessions.getKeys();
    }

    /**
     * 
     */
    protected void blockingUpdate() {
        updateLock.readLock().lock();
        if (needsUpdate) {
            updateLock.readLock().unlock();
            doUpdate();
        } else {
            updateLock.readLock().unlock();
        }
    }

    /**
     * 
     */
    protected void doUpdate() {
        updateLock.writeLock().lock();
        try {
            // Could have been unset while we were waiting.
            if (needsUpdate) {
                // Prevent recursion!
                needsUpdate = false;
                boolean success = false;

                try {
                    if (staleCacheListener != null) {
                        List<String> ids = sessions.getKeysWithExpiryCheck();
                        for (String id : ids) {
                            Element elt = sessions.getQuiet(id);
                            SessionContext ctx = (SessionContext) elt
                                    .getObjectValue();
                            // May throw an exception
                            SessionContext replacement = staleCacheListener
                                    .reload(ctx);
                            if (replacement == null) {
                                internalRemove(id);
                            } else {
                                sessions.putQuiet(new Element(id, replacement));
                            }
                        }
                        success = true;
                    }
                } finally {
                    if (success) {
                        needsUpdate = false;
                        lastUpdate = System.currentTimeMillis();
                    } else {
                        needsUpdate = true;
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
