/*
 *   $Id$
 *
 *   Copyright 2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CMD_MAIL_ICE
#define OMERO_CMD_MAIL_ICE

#include <omero/Collections.ice>
#include <omero/System.ice>
#include <omero/cmd/API.ice>

module omero {

    module cmd {
		
		class SendEmailRequest extends Request {
			omero::sys::LongList userIds;
			omero::sys::LongList groupIds;
			omero::api::StringSet cc;
			string subject;
			string body;
			string mimetype;
		};

		class SendEmailResponse extends Response {
        };
        
    };
};

#endif
