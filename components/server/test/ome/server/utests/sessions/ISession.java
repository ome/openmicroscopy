/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.utests.sessions;

import ome.annotations.Hidden;
import ome.annotations.NotNull;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.model.meta.Session;
import ome.system.Principal;

/**
 * Is not stateful because of the timeouts. Easier to control Responsible for
 * validating inputs (including authentication) and creating sessions by
 * properly using the PermissionsVerifer and SessionManager
 * 
 * The Session which is returned can be considered a capability token. Simply by
 * possessing it the client has access to all information available to the
 * {@link Session}
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
interface ISession extends ServiceInterface {
    /*
     * Not called ServiceFactory because it doesn't return proxies. perhaps
     * ServiceFactoryI should extends SessionhandleI
     */
    // where principal has a session field for use in joining a disconnect
    // session
    /**
     * Creates a new session and returns it to the user.
     * 
     * @throws ApiUsageException
     *                 if principal is null
     * @throws SecurityViolation
     *                 if the password check fails
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
     *                The {@link Session} instance to be updated.
     * @return The {@link Session} updated instance. Should replace the current
     *         value: <code> session = iSession.updateSession(session); </code>
     */
    Session updateSession(@NotNull
    Session session);

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

}