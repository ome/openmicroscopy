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

/*
 * Callback interface allowing to send email using JavaMailSender, supporting
 * MIME messages through preparation callbacks.
 */

module omero {

    module cmd {

         /**
         * Requests an email to be send to all users of the omero
         * determines inactive users, an active members of given groups
         * and/or specific users.
         *
         * examples:
         *  - omero.cmd.SendEmailRequest(subject, body, everyone=True)
         *		sends message to everyone who has email set
         *		and is an active user
         *  - omero.cmd.SendEmailRequest(subject, body, everyone=True, inactive=True)
         *		sends message to everyone who has email set,
         *		even inactive users
         *  - omero.cmd.SendEmailRequest(subject, body, groupIds=[...],
         *		userIds=[...] )
         *		sends email to active members of given groups and selected users
         * 	- extra=[...] allows to set extra email address if not in DB
         **/
         class SendEmailRequest extends Request {
             string subject;
             string body;
             bool html;
             omero::sys::LongList userIds;
             omero::sys::LongList groupIds;
             omero::api::StringSet extra;
             bool inactive;
             bool everyone;
         };

         /**
         * Successful response for [SendEmailRequest]. Contains
         * a list of invalid users that has no email address set.
         * If no recipients or invalid users found, an [ERR] will be returned.
         *
         * - invalidusers is a list of userIds that email didn't pass criteria
         *					such as was empty or less then 5 characters
         * - invalidemails is a list of email addresses that send email failed
         * - total is a total number of email in the pull to be sent.
         * - success is a number of emails that were sent successfully.
         **/
         class SendEmailResponse extends Response {
             long total;
             long success;
             omero::api::LongList invalidusers;
             omero::api::StringSet invalidemails;
         };

    };
};

#endif
