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
         *
         **/
        class UpdateSessionTimeoutRequest extends Request {
            string session;
            omero::RLong timeToLive;
            omero::RLong timeToIdle;
        };

        class CurrentSessionsRequest extends Request {
        };

        class CurrentSessionsResponse extends Response {
            omero::api::SessionList sessions;
            omero::api::EventContextList contexts;
        };

    };
};

#endif
