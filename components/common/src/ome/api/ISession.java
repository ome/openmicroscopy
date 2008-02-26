/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

import java.util.Set;

import ome.annotations.Hidden;
import ome.annotations.NotNull;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.model.meta.Session;
import ome.system.Principal;

/**
 * <em>Start here</em>: {@link Session} creation service for OMERO. Access to
 * all other services is dependent upon a properly created and still active
 * {@link Session}.
 * 
 * The {@link Session session's} {@link Session#getUuid() uuid} can be
 * considered a capability token, or temporary single use password Simply by
 * possessing it the client has access to all information available to the
 * {@link Session}.
 * 
 * Is not stateful because of the timeouts. Easier to control Responsible for
 * validating inputs (including authentication) and creating sessions by
 * properly using the PermissionsVerifer and SessionManager
 * 
 * 
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public interface ISession extends ServiceInterface {

    /*
     * Not called ServiceFactory because it doesn't return proxies. perhaps
     * ServiceFactoryI should extends SessionhandleI
     */
    // where principal has a session field for use in joining a disconnect
    // session
    /**
     * Allows an admin to create a {@link Session} for the give
     * {@link Principal}
     * 
     * @param principal
     *            Non-null {@link Principal} with the target user's name
     * @param seconds
     *            The time that this {@link Session} has until destruction. This
     *            is useful to override the server default so that an initial
     *            delay before the user is given the token will not be construed
     *            as idle time. A value less than 1 will cause the default max
     *            timeToLive to be used; but timeToIdle will be disabled.
     */
    Session createSessionWithTimeout(@NotNull
    Principal principal, long milliseconds);

    /**
     * Creates a new session and returns it to the user.
     * 
     * @throws ApiUsageException
     *             if principal is null
     * @throws SecurityViolation
     *             if the password check fails
     */
    Session createSession(@NotNull
    Principal principal, @Hidden
    String credentials);

    /**
     * Updates subset of the fields from the {@link Session} object.
     * 
     * Updated: group, {@link Session#userAgent}, {@link Session#message},
     * {@link Session#defaultUmask},
     * {@link Session#setDefaultEventType(String)}
     * 
     * Ignored: All others, but especially user, {@link Session#events}
     * {@link Session#uuid}, and the timestamps
     * 
     * @param session
     *            The {@link Session} instance to be updated.
     * @return The {@link Session} updated instance. Should replace the current
     *         value: <code> session = iSession.updateSession(session); </code>
     */
    Session updateSession(@NotNull
    Session session);

    Session getSession(@NotNull
    String sessionUuid);

    void closeSession(@NotNull
    Session session);

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
     * Retrieves an entry from the given {@link Session session's} cache.
     */
    Object getInput(String session, String key);

    /**
     * Retrieves
     * 
     * @param session
     * @return
     */
    Set<String> getInputKeys(String session);

    /**
     * Places an entry in the given {@link Session session's} cache. If the
     * value is null, the key will be removed.
     * 
     * @param session
     * @param key
     * @param objection
     */
    void setInput(String session, String key, Object objection);

    Set<String> getOutputKeys(String session);

    Object getOutput(String session, String key);

    void setOutput(String session, String key, Object objection);
}
