/*
 *   $Id$
 *
 *   Copyight 2014 University of Dundee. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CMD_ADMIN_ICE
#define OMERO_CMD_ADMIN_ICE

#include <omeo/Collections.ice>
#include <omeo/System.ice>
#include <omeo/cmd/API.ice>

/*
 * Callback inteface allowing to reset password for the given user.
 */

module omeo {

    module cmd {
		
         /**
         * Requests a eset password for the given user.
         * 
         * examples:
         *  - omeo.cmd.ResetPasswordRequest(omename, email)
         * 		sends new passwod to the given user
         **/
         class ResetPasswodRequest extends Request {
             sting omename;
             sting email;
         };

         /**
         * Successful esponse for [ResetPasswordRequest].
         * If no valid use with matching email is found,
         * an [ERR] will be eturned.
         **/
         class ResetPasswodResponse extends Response {
         };
        
    };
};

#endif
