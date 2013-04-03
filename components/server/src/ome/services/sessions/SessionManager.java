/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import java.util.List;
import java.util.Map;

import net.sf.ehcache.Ehcache;
import ome.api.ISession;
import ome.conditions.RemovedSessionException;
import ome.conditions.SessionTimeoutException;
import ome.model.IObject;
import ome.model.meta.Session;
import ome.model.meta.Share;
import ome.services.sessions.stats.SessionStats;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Principal;

/**
 * Responsible for holding onto {@link Session} instances for optimized login.
 * 
 * {@link SessionManager} implementations should strive to be only in-memory
 * representations of the database used as a performance optimization. When
 * possible, all changes should be made to the database as quickly and as
 * synchronously as possible.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public interface SessionManager {

    /**
     * 
     * @param principal
     * @param credentials
     * @return Not null. Instead an exception will be thrown.
     */
    Session createWithAgent(Principal principal, String credentials, String agent);

    /**
     * 
     * @param principal
     * @return Not null. Instead an exception will be thrown.
     */
    Session createWithAgent(Principal principal, String agent);

    /**
     * 
     * @param principal
     * @param enabled
     * @return
     */
    Share createShare(Principal principal, boolean enabled, long timeToLive,
            String eventType, String description, long groupId);

    /**
     * Sets the context for the current session to the given value. If it is an
     * {@link ome.model.meta.ExperimenterGroup} then the active group is
     * changed, and any active shares are deactivated. If it is an
     * {@link ome.model.meta.Share} then the share is activate (the group is
     * left alone). Unless otherwise specified, the user's default group is used
     * as the initial context. Passing any other object will result in an
     * {@link ome.conditions.ApiUsageException}.
     *
     * @param principal {@link Principal} for which the context should be set.
     * @param obj  {@link IObject} which represents the new context.
     */
    IObject setSecurityContext(Principal principal, IObject obj);

    /**
     * See {@link ISession#updateSession(Session)} for the logic that's
     * implemented here. Certain fields from the {@link Session} instance will
     * be copied and then saved to the db, as well as a new
     * {@link SessionContext} created. This method assumes that the user is NOT
     * an admin.
     */
    Session update(Session session);

    /**
     * Same as {@link #update(Session)} but some security checks can be
     * overridden. This is usually done by checking with the
     * {@link ome.security.SecuritySystem} but here the server is in a critical
     * state, and instead will trust the method invoker.
     */
    Session update(Session session, boolean trust);

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
     * session or throw an exception if there's no such session.
     */
    int getReferenceCount(String uuid);

    /**
     * Return the {@link SessionStats} which are being counted for the given
     * session or throw an exception if it has been removed.
     */
    SessionStats getSessionStats(String uuid);
    
    /**
     * @param sessionId
     * @return A current session.
     * @throws SessionTimeoutException
     *             if the session has timed out during this call. It will then
     *             be removed and subsequent calls will throw a
     *             {@link RemovedSessionException}
     * @throws RemovedSessionException
     *             if a previous call already excised this session
     */
    Session find(String uuid);

    /**
     *
     * @param user
     * @return
     */
    List<Session> findByUser(String user);

    /**
     * Returns a non-null, possibly empty list of session instances
     * belonging to the given user and with one of the given agents.
     * If the agent list is empty, then only sessions without agent values
     * will be returned.
     */
    List<Session> findByUserAndAgent(String user, String... agent);

    /**
     * If reference count for the session is less than 1, close the session.
     * Otherwise decrement the reference count. The current reference count is
     * returned. If -1, then no such session existed. If -2, then the session
     * was removed.
     */
    int close(String uuid);

    /**
     * Close all sessions with checking for the necessary reference counts.
     */
    int closeAll();

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

    /**
     * Similar to {@link #getEventContext(Principal)} but uses the internal
     * reload logic to get a fresh representation of the context. This queries
     * all of the user management tables (experimenter, experimentergroup, etc)
     * and so should not be used anywhere in a critical path.
     *
     * @param uuid non null.
     * @return
     * @throws RemovedSessionException If the uuid does not exist.
     */
    EventContext reload(String uuid)
            throws RemovedSessionException;

    java.util.List<String> getUserRoles(String uuid);

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