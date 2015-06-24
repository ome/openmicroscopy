/*
 *   $Id$
 *
 *   Copyright 2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CMD_ADMIN_ICE
#define OMERO_CMD_ADMIN_ICE

#include <omero/Collections.ice>
#include <omero/System.ice>
#include <omero/cmd/API.ice>

/*
 * Callback interface allowing to reset password for the given user.
 */

module omero {

    module cmd {

         /**
          * Requests a reset password for the given user.
          *
          * examples:
          *  - omero.cmd.ResetPasswordRequest(omename, email)
          *      sends new password to the given user
          **/
         class ResetPasswordRequest extends Request {
             string omename;
             string email;
         };

         /**
          * Successful response for [ResetPasswordRequest].
          * If no valid user with matching email is found,
          * an [ERR] will be returned.
          **/
         class ResetPasswordResponse extends Response {
         };

        /**
         * Proposes a change to one or both of the timeToLive
         * and timeToIdle properties of a live session. The session
         * uuid cannot be null. If either other argument is null,
         * it will be ignored. Otherwise, the long value will be
         * interpreted as the the millisecond value which should
         * be set. Non-administrators will not be able to reduce
         * current values. No special response is returned, but
         * an [omero::cmd::OK] counts as success.
         **/
        class UpdateSessionTimeoutRequest extends Request {
            string session;
            omero::RLong timeToLive;
            omero::RLong timeToIdle;
        };

        /**
         * Argument-less request that will produce a
         * [CurrentSessionsResponse] if no [omero::cmd::ERR] occurs.
         **/
        class CurrentSessionsRequest extends Request {
        };

        /**
         * Return value from [omero::cmd::CurrentSessionsRequest] consisting of
         * two ordered lists of matching length. The sessions field
         * contains a list of the OMERO [omero::model::Session] objects
         * that are currently active *after* all timeouts have been applied.
         * This is the value that would be returned by
         * [omero::api::ISession::getSession] when joined to that session.
         * Similarly, the contexts field contains the value that would be
         * returned by a call to [omero::api::IAdmin::getEventContext].
         * For non-administrators, most values for all sessions other than
         * those belonging to that user will be nulled.
         **/
        class CurrentSessionsResponse extends Response {

            /**
             * [omero::model::Session] objects loaded from
             * the database.
             **/
            omero::api::SessionList sessions;

            /**
             * [omero::sys::EventContext] objects stored in
             * memory by the server.
             **/
            omero::api::EventContextList contexts;

            /**
             * Other session state which may vary based on
             * usage. This may include "hitCount", "lastAccess",
             * and similar metrics.
             **/
            omero::api::RTypeDictArray data;
        };

    };
};

#endif
