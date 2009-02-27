#!/usr/bin/env python
# 
# 
# 
# Copyright (c) 2008 University of Dundee. 
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
# 
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
# 
# Version: 1.0
#

import traceback
import os.path
import threading
import smtplib
import logging
import time

from email.MIMEMultipart import MIMEMultipart
from email.MIMEText import MIMEText
from email.MIMEImage import MIMEImage
from django.conf import settings

logger = logging.getLogger('sendemail')
SLEEPTIME = 30

class SendEmail(threading.Thread):

    smtp_server = None

    def __init__(self):
        super(SendEmail, self).__init__()
        self.setDaemon(True)
        self.smtp_server = settings.EMAIL_SMTP_SERVER
        self.thread_timeout = False
        self.to_send = list()
        self.start()
    
    def run (self):
        """ this thread lives forever, pinging whatever connection exists to keep it's services alive """
        logger.info("Starting sendemail thread...")
        while not (self.thread_timeout):
            try:
                logger.info("%i emails in the queue." % (len(self.to_send)))
                if len(self.to_send) > 0:
                    try:
                        email = self.to_send[0]
                        logger.info("Sending...")
                        smtp = smtplib.SMTP()
                        smtp.connect(self.smtp_server)
                        smtp.sendmail(email['sender'], email['recipients'], email['message'])
                        smtp.quit()
                        self.to_send.remove(email)
                        logger.info("Email was sent.")
                    except:
                        logger.error("Email could not be sent.")
                        logger.error(traceback.format_exc())
                        try:
                            logger.error(email['message'])
                        except:
                            logger.error("Couldn't save the message")
                logger.info("sleep...")
                time.sleep(SLEEPTIME)
            except:
                logger.error("!! something bad on the SENDER keepalive thread !!")
                logger.error(traceback.format_exc())
        self.seppuku()
        logger.info("Thread death")

    def seppuku (self):
        logger.info("Thread will be closed")
        self._timeout = 0
        logger.info("Thread Deleted")

    def __del__ (self):
        logger.info("Garbage Collector KICK IN")

    def create_error_message(self, app, user, error):
        # Create the root message and fill in the from, to, and subject headers
        msgRoot = MIMEMultipart('related')
        msgRoot['Subject'] = '%s - error message by %s' % (app, user)
        msgRoot['From'] = settings.EMAIL_SENDER_ADDRESS
        msgRoot['To'] = settings.ADMINS[0][1]
        msgRoot.preamble = 'This is a multi-part message in MIME format.'
        msgAlternative = MIMEMultipart('alternative')
        msgRoot.attach(msgAlternative)
        
        email_txt = "%s/email_error.txt" % os.path.join(os.path.dirname(__file__), 'templatemail/').replace('\\','/')
        email_html = "%s/email_error.html" % os.path.join(os.path.dirname(__file__), 'templatemail/').replace('\\','/')

        contentAlternative = open(email_txt, 'r').read() % (error)
        msgText = MIMEText(contentAlternative)
        msgAlternative.attach(msgText)
        content = open(email_html, 'r').read() % (error)
        msgText = MIMEText(content, 'html')
        msgAlternative.attach(msgText)
        self.to_send.append({"message": msgRoot.as_string(), "sender": settings.EMAIL_SENDER_ADDRESS, "recipients": [settings.ADMINS[0][1]]})
    
    def create_share_message(self, host, blitz_id, user, share_id, recipients):
        app = settings.WEBCLIENT_ROOT_BASE
        # Create the root message and fill in the from, to, and subject headers
        msgRoot = MIMEMultipart('related')
        try:
            msgRoot['Subject'] = 'OMERO.%s - %s %s shares with you some of the data' % (app, user.firstName.val, user.lastName.val)
        except:
            msgRoot['Subject'] = 'OMERO.%s - unknown person shares with you some of the data' % (app)
        try:
            msgRoot['From'] = '%s %s <%s>' % (user.firstName.val, user.lastName.val, user.email.val)
        except:
            msgRoot['From'] = 'Unknown'
        #msgRoot['To'] = self.recipients
        msgRoot.preamble = 'This is a multi-part message in MIME format.'
        msgAlternative = MIMEMultipart('alternative')
        msgRoot.attach(msgAlternative)
        
        email_txt = "%s/email_share.txt" % os.path.join(os.path.dirname(__file__), 'templatemail/').replace('\\','/')
        email_html = "%s/email_share.html" % os.path.join(os.path.dirname(__file__), 'templatemail/').replace('\\','/')

        contentAlternative = open(email_txt, 'r').read() % (host, share_id, blitz_id, user.firstName.val, user.lastName.val)
        msgText = MIMEText(contentAlternative)
        msgAlternative.attach(msgText)
        content = open(email_html, 'r').read() % (host, share_id, blitz_id, host, share_id, blitz_id, user.firstName.val, user.lastName.val)
        msgText = MIMEText(content, 'html')
        msgAlternative.attach(msgText)

        fp = open(settings.STATIC_LOGO, 'rb')
        msgImage = MIMEImage(fp.read())
        fp.close()

        msgImage.add_header('Content-ID', '<image1>')
        msgRoot.attach(msgImage)
        self.to_send.append({"message": msgRoot.as_string(), "sender": settings.EMAIL_SENDER_ADDRESS, "recipients": recipients})

    def create_sharecomment_message(self, host, blitz_id, share_id, recipients):
        app = settings.WEBCLIENT_ROOT_BASE
        # Create the root message and fill in the from, to, and subject headers
        msgRoot = MIMEMultipart('related')
        msgRoot['Subject'] = 'OMERO.%s - new comment on the share available' % (app)
        msgRoot['From'] = settings.EMAIL_SENDER_ADDRESS

        msgRoot.preamble = 'This is a multi-part message in MIME format.'
        msgAlternative = MIMEMultipart('alternative')
        msgRoot.attach(msgAlternative)
        
        email_txt = "%s/email_comment.txt" % os.path.join(os.path.dirname(__file__), 'templatemail/').replace('\\','/')
        email_html = "%s/email_comment.html" % os.path.join(os.path.dirname(__file__), 'templatemail/').replace('\\','/')

        contentAlternative = open(email_txt, 'r').read() % (host, share_id, blitz_id)
        msgText = MIMEText(contentAlternative)
        msgAlternative.attach(msgText)
        content = open(email_html, 'r').read() % (host, share_id, blitz_id, host, share_id, blitz_id)
        msgText = MIMEText(content, 'html')
        msgAlternative.attach(msgText)

        fp = open(settings.STATIC_LOGO, 'rb')
        msgImage = MIMEImage(fp.read())
        fp.close()

        msgImage.add_header('Content-ID', '<image1>')
        msgRoot.attach(msgImage)
        self.to_send.append({"message": msgRoot.as_string(), "sender": settings.EMAIL_SENDER_ADDRESS, "recipients": recipients})
        