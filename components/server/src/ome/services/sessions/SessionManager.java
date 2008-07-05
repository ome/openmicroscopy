/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import java.util.Map;

import net.sf.ehcache.Ehcache;
import ome.conditions.RemovedSessionException;
import ome.model.meta.Session;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Principal;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Responsible for holding onto {@link Session} instances for optimized login.
 * 
 * Receives notifications as an {@link ApplicationListener}, which should be
 * used to keep the {@link Session} instances up-to-date.
 * 
 * {@link SessionManager} implementations should strive to be only in-memory
 * representations of the database used as a performance optimization. When
 * possible, all changes should be made to the database as quickly and as
 * synchronously as possible.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public interface SessionManager extends ApplicationListener {

    /**
     * 
     * @param principal
     * @param credentials
     * @return Not null. Instead an exception will be thrown.
     */
    Session create(Principal principal, String credentials);

    /**
     * 
     * @param principal
     * @return Not null. Instead an exception will be thrown.
     */
    Session create(Principal principal);

    /**
     * 
     * @param session
     * @return
     */
    Session update(Session session);

    /**
     * Allows decrementing the reference count for a session without calling the
     * actual {@link #close(String)} logic. This is useful when it is assumed
     * that another user will re-attach to the same session. A timeout can still
     * cause the session to be removed.
     * 
     * @param uuid
     * @return
     */
    int detach(String uuid);

    /**
     * Return the number of client which are presumed to be attached to this
     * session.
     */
    int getReferenceCount(String uuid);

    /**
     * @param sessionId
     * @return A current session. Null if the session id is not found.
     */
    Session find(String uuid);

    /**
     * If reference count for the session is less than 1, close the session.
     * Otherwise decrement the reference count. The current reference count is
     * returned. If -1, then no such session existed. If -2, then the session
     * was removed.
     */
    int close(String uuid);

    // Security methods
    // =========================================================================

    /**
     * Provides a partial {@link EventContext} for the current {@link Session}.
     * 
     * @param uuid
     *            Non null.
     * @return Never null.
     * @throws RemovedSessionException
     *             if no session with the given {@link Principal#getName()}
     */
    EventContext getEventContext(Principal principal)
            throws RemovedSessionException;

    java.util.List<String> getUserRoles(String uuid);

    void onApplicationEvent(ApplicationEvent event);

    /**
     * Executes a password check using the {@link Executor} framework. Also
     * checks the credentials against current session uuids.
     * 
     * @param name
     * @param credentials
     */
    boolean executePasswordCheck(String name, String credentials);

    // State
    // =========================================================================

    /**
     * Returns after possibly creating an in-memory {@link Ehcache cache} which
     * can be used throughout the session. On close, the cache will be disposed.
     */
    Ehcache inMemoryCache(String uuid);

    /**
     * Returns after possibly creating an on-disk {@link Ehcache cache} which
     * can be used throughout the session. On close, the cache will be disposed.
     */
    Ehcache onDiskCache(String uuid);

    /**
     * Returns the input environment {@link Object} stored under the given key
     * or null if none present. Throws an exception if there is no
     * {@link Session} with the given identifier.
     * 
     * @param session
     *            Not null.
     * @param key
     *            Not null.
     * @return Possibly null.
     */
    public Object getInput(String session, String key)
            throws RemovedSessionException;

    /**
     * Returns the output environment {@link Object} stored under the given key
     * or null if none present. Throws an exception if there is no
     * {@link Session} with the given identifier.
     * 
     * @param session
     *            Not null.
     * @param key
     *            Not null.
     * @return Possibly null.
     */
    public Object getOutput(String session, String key)
            throws RemovedSessionException;

    /**
     * Places the {@link Object argument} in the input environment under the
     * given key, possibly initializing the {@link Map} Throws an exception if
     * there is no {@link Session} with the given identifier.
     * 
     * @param session
     *            Not null.
     * @param key
     *            Not null.
     * @param object
     *            If null, key will be removed.
     */
    public void setInput(String session, String key, Object object)
            throws RemovedSessionException;

    /**
     * Places the {@link Object argument} in the output environment under the
     * given key, possibly initializing the {@link Map} Throws an exception if
     * there is no {@link Session} with the given identifier.
     * 
     * @param session
     *            Not null.
     * @param key
     *            Not null.
     * @param object
     *            If null, key will be removed.
     */
    public void setOutput(String session, String key, Object object)
            throws RemovedSessionException;

    /**
     * Returns a copy of the input environment.
     * 
     * @return Not null
     */
    public Map<String, Object> inputEnvironment(String session);

    /**
     * Returns a copy of the output environment.
     * 
     * @return Not null.
     */
    public Map<String, Object> outputEnvironment(String session);

}