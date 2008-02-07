/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions.state;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import ome.conditions.SessionTimeoutException;
import ome.model.meta.Session;
import ome.services.sessions.SessionCallback;
import ome.services.sessions.SessionContext;
import ome.services.sessions.SessionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.mathcs.backport.java.util.Collections;

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

    private final ThreadLocal<Boolean> throwsOnExpiration = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    /**
     * Observer pattern used to clear the blocked
     * {@link SessionCache#needsUpdate} state, which prevents all further calls
     * from happening.
     */
    public interface StaleCacheListener {
        boolean attemptCacheUpdate();
    }

    private boolean needsUpdate = false;
    private long lastUpdate = System.currentTimeMillis();
    private CacheFactory factory;
    private Ehcache sessions;
    private final ConcurrentHashMap<String, Ehcache> diskCacheMap = new ConcurrentHashMap<String, Ehcache>(
            64);
    private final ConcurrentHashMap<String, Set<SessionCallback>> sessionCallbackMap = new ConcurrentHashMap<String, Set<SessionCallback>>(
            64);
    private final Set<StaleCacheListener> staleCacheListeners = Collections
            .synchronizedSet(new HashSet<StaleCacheListener>());

    public void setCacheFactory(CacheFactory factory) {
        this.factory = factory;
        // Setup for in memory in spring
        this.sessions = this.factory.createCache();
        this.sessions.getCacheEventNotificationService().registerListener(this);

        // Setup for disk storage
        this.factory.setOverflowToDisk(true);
        //
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
        if (throwsOnExpiration.get().booleanValue()) {
            throw new SessionTimeoutException(String.format(
                    "Session %s started at %s, hit %s times, "
                            + "last used at %s, has expired.", elt.getKey(),
                    elt.getCreationTime(), elt.getExpirationTime(), elt
                            .getHitCount()));
        }
    }

    // Accessors
    // ========================================================================

    public boolean addStaleCacheListener(StaleCacheListener listener) {
        return staleCacheListeners.add(listener);
    }

    public boolean removeStaleCacheListener(StaleCacheListener listener) {
        return staleCacheListeners.remove(listener);
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

    public void setNeedsUpdate(boolean needsUpdate) {
        boolean wasLocked = this.needsUpdate;
        this.needsUpdate = needsUpdate;
        if (this.needsUpdate) {
            updateLock.writeLock().lock();
        } else {
            if (wasLocked) {
                updateLock.writeLock().unlock();
            }
        }
    }

    public boolean getNeedsUpdate() {
        return this.needsUpdate;
    }

    public long getTimeToIdle() {
        return sessions.getTimeToIdleSeconds();
    }

    public long getTimeToLive() {
        return sessions.getTimeToLiveSeconds();
    }

    // State management
    // ========================================================================
    // These methods are currently the only access to the sessions cache, and
    // are responsible for synchronization and the update mechanism.

    public void putSession(String uuid, SessionContext sessionContext) {
        updateLock.readLock().lock();
        try {
            sessions.put(new Element(uuid, sessionContext));
        } finally {
            updateLock.readLock().unlock();
        }
    }

    public SessionContext getSessionContext(String uuid) {
        updateLock.readLock().lock();
        try {
            return getSessionContextThrows(uuid, false);
        } finally {
            updateLock.readLock().unlock();
        }
    }

    /**
     * 
     * @param uuid
     * @param throwOnExpiration
     * @return
     */
    public SessionContext getSessionContextThrows(String uuid,
            boolean throwOnExpiration) {
        updateLock.readLock().lock();
        throwsOnExpiration.set(Boolean.TRUE);
        try {
            Element elt = sessions.get(uuid);
            if (elt == null) {
                return null;
            }
            return (SessionContext) elt.getObjectValue();
        } finally {
            throwsOnExpiration.set(Boolean.FALSE);
            updateLock.readLock().unlock();
        }
    }

    public void removeSession(String uuid) {
        updateLock.readLock().lock();
        try {
            sessions.remove(uuid);
        } finally {
            updateLock.readLock().unlock();
        }
    }

    public List<String> getIds() {
        updateLock.readLock().lock();
        try {
            return sessions.getKeysWithExpiryCheck();
        } finally {
            updateLock.readLock().unlock();
        }
    }

    /**
     * Called in a separate thread
     */
    protected void doUpdate() {
        updateLock.writeLock().lock();
        boolean success = false;
        int tries = 0;
        for (StaleCacheListener listener : staleCacheListeners) {
            tries++;
            try {
                success |= listener.attemptCacheUpdate();
            } catch (Exception e) {
                log.error("Error while attempting to update cache with "
                        + listener, e);
            }
            if (success) {
                needsUpdate = false;
                lastUpdate = System.currentTimeMillis();
            }
        }
        if (!success) {
            needsUpdate = true;
            throw new RuntimeException("Could not update stale cache. "
                    + "Number of failed listeners:" + tries);
        } else {
            updateLock.writeLock().unlock();
        }
    }

}
