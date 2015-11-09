/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
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
         * See <a href="http://downloads.openmicroscopy.org/latest/omero5.2/api/ome/api/ISession.html">ISession.html</a>
         **/
        ["ami", "amd"] interface ISession extends ServiceInterface
            {

                omero::model::Session createSession(omero::sys::Principal p, string credentials)
                throws ServerError, Glacier2::CannotCreateSessionException;

                /**
                 * HasPassword: Requires the session to have been created with a password
                 * as opposed to with a session uuid (via joinSession). If that's not the
                 * case, a SecurityViolation will be thrown, in which case
                 * ServiceFactory.setSecurityPassword can be used.
                 **/
                omero::model::Session createUserSession(long timeToLiveMilliseconds, long timeToIdleMilliseconds, string defaultGroup)
                throws ServerError, Glacier2::CannotCreateSessionException;

                //
                // System users
                //

                omero::model::Session createSessionWithTimeout(omero::sys::Principal p, long timeToLiveMilliseconds)
                throws ServerError, Glacier2::CannotCreateSessionException;

                omero::model::Session createSessionWithTimeouts(omero::sys::Principal p, long timeToLiveMilliseconds, long timeToIdleMilliseconds)
                throws ServerError, Glacier2::CannotCreateSessionException;

                idempotent omero::model::Session getSession(string sessionUuid) throws ServerError;
                idempotent int getReferenceCount(string sessionUuid) throws ServerError;
                int closeSession(omero::model::Session sess) throws ServerError;

                // Listing
                idempotent SessionList getMyOpenSessions() throws ServerError;
                idempotent SessionList getMyOpenAgentSessions(string agent) throws ServerError;
                idempotent SessionList getMyOpenClientSessions() throws ServerError;

                // Environment
                idempotent omero::RType getInput(string sess, string key) throws ServerError;
                idempotent omero::RType getOutput(string sess, string key) throws ServerError;
                idempotent void setInput(string sess, string key, omero::RType value) throws ServerError;
                idempotent void setOutput(string sess, string key, omero::RType value) throws ServerError;
                idempotent StringSet getInputKeys(string sess) throws ServerError;
                idempotent StringSet getOutputKeys(string sess) throws ServerError;
                idempotent omero::RTypeDict getInputs(string sess) throws ServerError;
                idempotent omero::RTypeDict getOutputs(string sess) throws ServerError;
            };

    };
};

#endif
