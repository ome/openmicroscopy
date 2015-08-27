/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.annotations.Hidden;
import ome.annotations.NotNull;
import ome.conditions.ApiUsageException;
import ome.conditions.RemovedSessionException;
import ome.conditions.SecurityViolation;
import ome.conditions.SessionTimeoutException;
import ome.model.internal.Permissions;
import ome.model.meta.Session;
import ome.system.Principal;

/**
 * <em>Start here</em>: {@link Session} creation service for OMERO. Access to
 * all other services is dependent upon a properly created and still active
 * {@link Session}.
 * 
 * The {@link Session session's} {@link Session#getUuid() uuid} can be
 * considered a capability token, or temporary single use password. Simply by
 * possessing it the client has access to all information available to the
 * {@link Session}.
 * 
 * Note: Both the RMI {@link ome.system.ServiceFactory} as well as the Ice
 * {@code omero.api.ServiceFactoryPrx} use {@link ISession} to acquire a
 * {@link Session}. In the RMI case, the {@link ISession} instance is the first
 * remote proxy accessed. In the Ice case, Glacier2 contacts {@link ISession}
 * itself and returns a ServiceFactory remote proxy. From both ServiceFactory
 * instances, it is possible but not necessary to access {@link ISession}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public interface ISession extends ServiceInterface {

    /**
     * Allows a user to open up another session for him/herself with the given
     * defaults without needing to re-enter password.
     * 
     * TODO Review the security of this method.
     */
    Session createUserSession(long timeToLiveMilliseconds,
            long timeToIdleMillisecond, String defaultGroup);

    /**
     * Allows an admin to create a {@link Session} for the give
     * {@link Principal}
     * 
     * @param principal
     *            Non-null {@link Principal} with the target user's name
     * @param timeToLiveMilliseconds
     *            The time that this {@link Session} has until destruction. This
     *            is useful to override the server default so that an initial
     *            delay before the user is given the token will not be construed
     *            as idle time. A value less than 1 will cause the default max
     *            timeToLive to be used; but timeToIdle will be disabled.
     */
    Session createSessionWithTimeout(@NotNull Principal principal,
            long timeToLiveMilliseconds);

    /**
     * Allows an admin to create a {@link Session} for the give
     * {@link Principal}
     * 
     * @param principal
     *            Non-null {@link Principal} with the target user's name
     * @param timeToLiveMilliseconds
     *            The time that this {@link Session} has until destruction.
     *            Setting the value to 0 will prevent destruction unless the
     *            session remains idle.
     * @param timeToIdleMilliseconds
     *            The time that this {@link Session} can remain idle before
     *            being destroyed. Setting the value to 0 will prevent idleness
     *            based destruction.
     */
    Session createSessionWithTimeouts(@NotNull Principal principal,
            long timeToLiveMilliseconds, long timeToIdleMilliseconds);

    /**
     * Creates a new session and returns it to the user.
     * 
     * @throws ApiUsageException
     *             if principal is null
     * @throws SecurityViolation
     *             if the password check fails
     */
    Session createSession(@NotNull Principal principal,
            @Hidden String credentials);

    /**
     * Retrieves the session associated with this uuid, updating the last access
     * time as well. Throws a {@link RemovedSessionException} if not present, or
     * a {@link SessionTimeoutException} if expired.
     * 
     * This method can be used as a {@link Session} ping.
     */
    Session getSession(@NotNull String sessionUuid);

    /**
     * Retrieves the current reference count for the given uuid. Has the same
     * semantics as {@link #getSession(String)}.
     */
    int getReferenceCount(@NotNull String sessionUuid);

    /**
     * Closes session and releases all resources. It is preferred that all
     * clients call this method as soon as possible to free memory, but it is
     * possible to not call close, and rejoin a session later.
     * 
     * The current reference count for the session is returned. If the session
     * does not exist, -1. If this call caused the death of the session, then
     * -2.
     */
    int closeSession(@NotNull Session session);

    // Session listings (ticket:1975)

    /**
     * Returns a list of open sessions for the current user. The list is ordered
     * by session creation time, so that the last item was created last.
     */
    List<Session> getMyOpenSessions();

    /**
     * Like {@link #getMyOpenSessions()} but returns only those sessions
     * with the given agent string.
     */
    List<Session> getMyOpenAgentSessions(String agent);

    /**
     * Like {@link #getMyOpenSessions()} but returns only those sessions
     * started by official OMERO clients.
     */
    List<Session> getMyOpenClientSessions();

    // void addNotification(String notification);
    // void removeNotification(String notification);
    // List<String> listNotifications();
    // void defaultNotifications();
    // void clearNotifications();

    // Session joinSessionByName(@NotNull String sessionName); // Here you don't
    // have a
    // void disconnectSession(@NotNull Session session);
    // void pingSession(@NotNull Session session); // Add to ServiceFactoryI

    // Environment contents
    // =========================================================================

    /**
     * Retrieves an entry from the given {@link Session session's} input
     * environment.
     */
    Object getInput(String session, String key);

    /**
     * Retrieves all keys in the {@link Session session's} input environment.
     * 
     * @param session
     * @return a {@link Set} of keys
     */
    Set<String> getInputKeys(String session);

    /**
     * Retrieves all inputs from the given {@link Session session's} input
     * environment.
     */
    Map<String, Object> getInputs(String session);

    /**
     * Places an entry in the given {@link Session session's} input environment.
     * If the value is null, the key will be removed.
     */
    void setInput(String session, String key, Object objection);

    /**
     * Retrieves all keys in the {@link Session sesson's} output environment.
     */
    Set<String> getOutputKeys(String session);

    /**
     * Retrieves an entry from the {@link Session session's} output environment.
     */
    Object getOutput(String session, String key);

    /**
     * Retrieves all outputs from the given {@link Session session's} input
     * environment.
     */
    Map<String, Object> getOutputs(String session);

    /**
     * Places an entry in the given {@link Session session's} output
     * environment. If the value is null, the key will be removed.
     */
    void setOutput(String session, String key, Object objection);
}
