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

module omero {

    module cmd {
		
		class ResetPasswordRequest extends Request {
			string omename;
			string email;
		};

		class ResetPasswordResponse extends Response {
        };
        
    };
};

#endif
