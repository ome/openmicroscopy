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
			string subject;
			string body;
			bool html;
			omero::sys::LongList userIds;
			omero::sys::LongList groupIds;
			omero::api::StringSet cc;
			omero::api::StringSet bcc;
			bool inactive;
		};

		class SendEmailResponse extends Response {
        };
        
    };
};

#endif
