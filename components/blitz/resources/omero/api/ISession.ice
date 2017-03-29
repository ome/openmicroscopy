/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

#ifndef OMERO_API_ISESSION_ICE
#define OMERO_API_ISESSION_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/System.ice>
#include <omero/Collections.ice>

module omero {

    module api {

        /**
         * {@link omero.model.Session} creation service for OMERO. Access to
         * all other services is dependent upon a properly created and still
         * active {@link omero.model.Session}.
         *
         * The session uuid ({@link omero.model.Session#getUuid}) can be
         * considered a capability token, or temporary single use password.
         * Simply by possessing it the client has access to all information
         * available to the {@link omero.model.Session}.
         *
         * Note: Both the RMI <code>ome.system.ServiceFactory</code> as well
         * as the Ice {@link omero.api.ServiceFactory} use
         * {@link omero.api.ISession} to acquire a
         * {@link omero.model.Session}. In the Ice case, Glacier2
         * contacts {@link omero.api.ISession} itself and returns a
         * ServiceFactory remote proxy. From both ServiceFactory
         * instances, it is possible but not necessary to access
         * {@link omero.api.ISession}.
         */
        ["ami", "amd"] interface ISession extends ServiceInterface
            {
                /**
                 * Creates a new session and returns it to the user.
                 *
                 * @throws ApiUsageException if principal is null
                 * @throws SecurityViolation if the password check fails
                 */
                omero::model::Session createSession(omero::sys::Principal p, string credentials)
                throws ServerError, Glacier2::CannotCreateSessionException;

                /**
                 * Allows a user to open up another session for him/herself with the given
                 * defaults without needing to re-enter password.
                 */
                omero::model::Session createUserSession(long timeToLiveMilliseconds, long timeToIdleMilliseconds, string defaultGroup)
                throws ServerError, Glacier2::CannotCreateSessionException;

                //
                // System users
                //

                /**
                 * Allows an admin to create a {@link Session} for the give
                 * {@link omero.sys.Principal}.
                 *
                 * @param principal
                 *            Non-null {@link omero.sys.Principal} with the
                 *            target user's name
                 * @param timeToLiveMilliseconds
                 *            The time that this {@link omero.model.Session}
                 *            has until destruction. This is useful to
                 *            override the server default so that an initial
                 *            delay before the user is given the token will
                 *            not be construed as idle time. A value less than
                 *            1 will cause the default max timeToLive to be
                 *            used; but timeToIdle will be disabled.
                 */
                omero::model::Session createSessionWithTimeout(omero::sys::Principal p, long timeToLiveMilliseconds)
                throws ServerError, Glacier2::CannotCreateSessionException;

                /**
                 * Allows an admin to create a {@link omero.model.Session} for
                 * the given {@link omero.sys.Principal}.
                 *
                 * @param principal
                 *            Non-null {@link omero.sys.Principal} with the
                 *            target user's name
                 * @param timeToLiveMilliseconds
                 *            The time that this {@link omero.model.Session}
                 *            has until destruction. Setting the value to 0
                 *            will prevent destruction unless the session
                 *            remains idle.
                 * @param timeToIdleMilliseconds
                 *            The time that this {@link omero.model.Session}
                 *            can remain idle before being destroyed. Setting
                 *            the value to 0 will prevent idleness based
                 *            destruction.
                 */
                omero::model::Session createSessionWithTimeouts(omero::sys::Principal p, long timeToLiveMilliseconds, long timeToIdleMilliseconds)
                throws ServerError, Glacier2::CannotCreateSessionException;

                /**
                 * Retrieves the session associated with this uuid, updating
                 * the last access time as well. Throws a
                 * {@link RemovedSessionException} if not present, or
                 * a {@link SessionTimeoutException} if expired.
                 *
                 * This method can be used as a {@link Session} ping.
                 */
                idempotent omero::model::Session getSession(string sessionUuid) throws ServerError;

                /**
                 * Retrieves the current reference count for the given uuid.
                 * Has the same semantics as {@link #getSession}.
                 */
                idempotent int getReferenceCount(string sessionUuid) throws ServerError;

                /**
                 * Closes session and releases all resources. It is preferred
                 * that all clients call this method as soon as possible to
                 * free memory, but it is possible to not call close, and
                 * rejoin a session later.
                 *
                 * The current reference count for the session is returned. If
                 * the session does not exist, -1. If this call caused the
                 * death of the session, then -2.
                 */
                int closeSession(omero::model::Session sess) throws ServerError;

                // Listing
                /**
                 * Returns a list of open sessions for the current user. The
                 * list is ordered by session creation time, so that the last
                 * item was created last.
                 */
                idempotent SessionList getMyOpenSessions() throws ServerError;

                /**
                 * Like {@link #getMyOpenSessions} but returns only those
                 * sessions with the given agent string.
                 */
                idempotent SessionList getMyOpenAgentSessions(string agent) throws ServerError;

                /**
                 * Like {@link #getMyOpenSessions} but returns only those
                 * sessions started by official OMERO clients.
                 */
                idempotent SessionList getMyOpenClientSessions() throws ServerError;

                // Environment
                /**
                 * Retrieves an entry from the given
                 * {@link omero.model.Session} input environment.
                 */
                idempotent omero::RType getInput(string sess, string key) throws ServerError;

                /**
                 * Retrieves an entry from the {@link omero.model.Session}
                 * output environment.
                 */
                idempotent omero::RType getOutput(string sess, string key) throws ServerError;

                /**
                 * Places an entry in the given {@link omero.model.Session}
                 * input environment.
                 * If the value is null, the key will be removed.
                 */
                idempotent void setInput(string sess, string key, omero::RType value) throws ServerError;

                /**
                 * Places an entry in the given {@link omero.model.Session}
                 * output environment. If the value is null, the key will be
                 * removed.
                 */
                idempotent void setOutput(string sess, string key, omero::RType value) throws ServerError;

                /**
                 * Retrieves all keys in the {@link omero.model.Session} input
                 * environment.
                 *
                 * @return a {@link StringSet} of keys
                 */
                idempotent StringSet getInputKeys(string sess) throws ServerError;

                /**
                 * Retrieves all keys in the {@link omero.model.Session}
                 * output environment.
                 */
                idempotent StringSet getOutputKeys(string sess) throws ServerError;

                /**
                 * Retrieves all inputs from the given
                 * {@link omero.model.Session} input environment.
                 */
                idempotent omero::RTypeDict getInputs(string sess) throws ServerError;

                /**
                 * Retrieves all outputs from the given
                 * {@link omero.model.Session} input environment.
                 */
                idempotent omero::RTypeDict getOutputs(string sess) throws ServerError;
            };

    };
};

#endif
