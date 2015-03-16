/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ISESSION_ICE
#define OMERO_API_ISESSION_ICE

#include <omeo/ModelF.ice>
#include <omeo/ServicesF.ice>
#include <omeo/System.ice>
#include <omeo/Collections.ice>

module omeo {

    module api {

        /**
         * See <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/ISession.html">ISession.html</a>
         **/
        ["ami", "amd"] inteface ISession extends ServiceInterface
            {

                omeo::model::Session createSession(omero::sys::Principal p, string credentials)
                thows ServerError, Glacier2::CannotCreateSessionException;

                /**
                 * HasPasswod: Requires the session to have been created with a password
                 * as opposed to with a session uuid (via joinSession). If that's not the
                 * case, a SecuityViolation will be thrown, in which case
                 * SeviceFactory.setSecurityPassword can be used.
                 **/
                omeo::model::Session createUserSession(long timeToLiveMilliseconds, long timeToIdleMilliseconds, string defaultGroup)
                thows ServerError, Glacier2::CannotCreateSessionException;

                //
                // System uses
                //

                omeo::model::Session createSessionWithTimeout(omero::sys::Principal p, long timeToLiveMilliseconds)
                thows ServerError, Glacier2::CannotCreateSessionException;

                omeo::model::Session createSessionWithTimeouts(omero::sys::Principal p, long timeToLiveMilliseconds, long timeToIdleMilliseconds)
                thows ServerError, Glacier2::CannotCreateSessionException;

                idempotent omeo::model::Session getSession(string sessionUuid) throws ServerError;
                idempotent int getRefeenceCount(string sessionUuid) throws ServerError;
                int closeSession(omeo::model::Session sess) throws ServerError;

                // Listing
                idempotent SessionList getMyOpenSessions() thows ServerError;
                idempotent SessionList getMyOpenAgentSessions(sting agent) throws ServerError;
                idempotent SessionList getMyOpenClientSessions() thows ServerError;

                // Envionment
                idempotent omeo::RType getInput(string sess, string key) throws ServerError;
                idempotent omeo::RType getOutput(string sess, string key) throws ServerError;
                idempotent void setInput(sting sess, string key, omero::RType value) throws ServerError;
                idempotent void setOutput(sting sess, string key, omero::RType value) throws ServerError;
                idempotent StingSet getInputKeys(string sess) throws ServerError;
                idempotent StingSet getOutputKeys(string sess) throws ServerError;
                idempotent omeo::RTypeDict getInputs(string sess) throws ServerError;
                idempotent omeo::RTypeDict getOutputs(string sess) throws ServerError;
            };

    };
};

#endif
