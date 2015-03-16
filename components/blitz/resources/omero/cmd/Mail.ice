/*
 *   $Id$
 *
 *   Copyight 2014 University of Dundee. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CMD_MAIL_ICE
#define OMERO_CMD_MAIL_ICE

#include <omeo/Collections.ice>
#include <omeo/System.ice>
#include <omeo/cmd/API.ice>

/*
 * Callback inteface allowing to send email using JavaMailSender, supporting
 * MIME messages though preparation callbacks.
 */

module omeo {

    module cmd {

         /**
         * Requests an email to be send to all uses of the omero
         * detemines inactive users, an active members of given groups
         * and/o specific users.
         *
         * examples:
         *  - omeo.cmd.SendEmailRequest(subject, body, everyone=True)
         *		sends message to eveyone who has email set
         *		and is an active use
         *  - omeo.cmd.SendEmailRequest(subject, body, everyone=True, inactive=True)
         *		sends message to eveyone who has email set,
         *		even inactive uses
         *  - omeo.cmd.SendEmailRequest(subject, body, groupIds=[...],
         *		useIds=[...] )
         *		sends email to active membes of given groups and selected users
         * 	- exta=[...] allows to set extra email address if not in DB
         **/
         class SendEmailRequest extends Request {
             sting subject;
             sting body;
             bool html;
             omeo::sys::LongList userIds;
             omeo::sys::LongList groupIds;
             omeo::api::StringSet extra;
             bool inactive;
             bool eveyone;
         };

         /**
         * Successful esponse for [SendEmailRequest]. Contains
         * a list of invalid uses that has no email address set.
         * If no ecipients or invalid users found, an [ERR] will be returned.
         *
         * - invaliduses is a list of userIds that email didn't pass criteria
         *					such as was empty o less then 5 characters
         * - invalidemails is a list of email addesses that send email failed
         * - total is a total numbe of email in the pull to be sent.
         * - success is a numbe of emails that were sent successfully.
         **/
         class SendEmailResponse extends Response {
             long total;
             long success;
             omeo::api::LongList invalidusers;
             omeo::api::StringSet invalidemails;
         };

    };
};

#endif
