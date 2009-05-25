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

def prepareRecipientsAsString(recipients):
    recps = list()
    for m in recipients:
        try:
            e = hasattr(m.email, 'val') and m.email.val or m.email
            if e is not None:
                recps.append(e)
        except:
            logger.error(traceback.format_exc())
    if recps is None or len(recps) == 0:
        raise AttributeError("List of recipients cannot be None or empty")
    return ",".join(recps)


class SendEmail(threading.Thread):

    smtp_server = None

    def __init__(self):
        super(SendEmail, self).__init__()
        self.setDaemon(True)
        self.smtp_server = settings.EMAIL_SMTP_SERVER
        try:
            self.smtp_port = settings.EMAIL_SMTP_PORT
        except:
            pass
        try:
            self.smtp_user = settings.EMAIL_SMTP_USER
        except:
            pass
        try:
            self.smtp_password = settings.EMAIL_SMTP_PASSWORD
        except:
            pass
        try:
            self.smtp_tls = settings.EMAIL_SMTP_TLS
        except:
            pass
        self.allow_thread_timeout = False
        self.to_send = list()
        self.start()
    
    def run (self):
        """ this thread lives forever, pinging whatever connection exists to keep it's services alive """
        logger.debug("Starting sendemail thread...")
        while not (self.allow_thread_timeout): 
            try:
                from omeroweb.webclient.models import EmailToSend
                counter = EmailToSend.objects.count()
                logger.info("%i emails is waiting..." % (counter))
                if counter > 0:
                    email = None
                    try:
                        details = EmailToSend.objects.all()[0]
                        message = None
                        message = getattr(self, str(details.template))(details)
                        if message is not None:
                            logger.info("Sending notification...")
                            try:
                                smtp = smtplib.SMTP(self.smtp_server, self.smtp_port)
                            except:
                                logger.debug("settings.EMAIL_SMTP_PORT was not set, connecting on default port...")
                                smtp = smtplib.SMTP(self.smtp_server)
                            try:
                                if self.smtp_tls:
                                    smtp.starttls()
                                    logger.debug("settings.EMAIL_SMTP_TLS set")
                                else:
                                    logger.debug("settings.EMAIL_SMTP_TLS was not set, connecting...")
                            except:
                                logger.debug("settings.EMAIL_SMTP_TLS was not set, connecting...")
                            try:
                                smtp.login(self.smtp_user, self.smtp_password)
                            except:
                                logger.debug("settings.EMAIL_SMTP_USER and settings.EMAIL_SMTP_PASSWORD was not set, connecting without login details...")
                            smtp.sendmail(settings.EMAIL_SENDER_ADDRESS, details.recipients, message)
                            smtp.quit()
                            details.delete()
                            logger.info("Email was sent successful.")
                        else:
                            logger.debug("Message was not created.")
                    except:
                        logger.error("Email could not be sent. Please check settings.")
                        logger.error(traceback.format_exc())
                    logger.debug("sleep...")
                    time.sleep(SLEEPTIME)
                else:
                    self.allow_thread_timeout = True
            except:
                logger.error("!! something bad on the SENDER thread !!")
                logger.error(traceback.format_exc())
        self.seppuku()
        logger.debug("Thread death")

    def seppuku (self):
        logger.info("Sendemail will be closed")
        logger.info("Sendemail Deleted")

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
        
        msgText = MIMEText(error)
        msgAlternative.attach(msgText)
        
        msgText = MIMEText(error, 'html')
        msgAlternative.attach(msgText)
        self.to_send.append({"message": msgRoot.as_string(), "sender": settings.EMAIL_SENDER_ADDRESS, "recipients": [settings.ADMINS[0][1]]})
    
    def create_share(self, details):
        app = settings.WEBCLIENT_ROOT_BASE
        # Create the root message and fill in the from, to, and subject headers
        msgRoot = MIMEMultipart('related')
        try:
            msgRoot['Subject'] = 'OMERO.%s - %s shared some data with you' % (app, details.sender)
        except:
            msgRoot['Subject'] = 'OMERO.%s - unknown person shared some data with you' % (app)
        try:
            msgRoot['From'] = '%s <%s>' % (details.sender, details.sender_email)
        except:
            msgRoot['From'] = 'Unknown'
        #msgRoot['To'] = self.recipients
        msgRoot.preamble = 'This is a multi-part message in MIME format.'
        msgAlternative = MIMEMultipart('alternative')
        msgRoot.attach(msgAlternative)
        
        contentAlternative = details.template.content_txt % (details.host, details.share, details.blitz.id, details.sender)
        msgText = MIMEText(contentAlternative)
        msgAlternative.attach(msgText)
        content = details.template.content_html % (details.host, details.share, details.blitz.id, details.host, details.share, details.blitz.id, details.sender)
        msgText = MIMEText(content, 'html')
        msgAlternative.attach(msgText)

        fp = open(settings.STATIC_LOGO, 'rb')
        msgImage = MIMEImage(fp.read())
        fp.close()

        msgImage.add_header('Content-ID', '<image1>')
        msgRoot.attach(msgImage)
        return msgRoot.as_string()

    def add_comment_to_share(self, details):
        app = settings.WEBCLIENT_ROOT_BASE
        # Create the root message and fill in the from, to, and subject headers
        msgRoot = MIMEMultipart('related')
        msgRoot['Subject'] = 'OMERO.%s - new comment on share available' % (app)
        msgRoot['From'] = settings.EMAIL_SENDER_ADDRESS

        msgRoot.preamble = 'This is a multi-part message in MIME format.'
        msgAlternative = MIMEMultipart('alternative')
        msgRoot.attach(msgAlternative)
        
        contentAlternative = details.template.content_txt % (details.host, details.share, details.blitz.id)
        msgText = MIMEText(contentAlternative)
        msgAlternative.attach(msgText)
        
        content = details.template.content_html % (details.host, details.share, details.blitz.id, details.host, details.share, details.blitz.id)
        msgText = MIMEText(content, 'html')
        msgAlternative.attach(msgText)

        fp = open(settings.STATIC_LOGO, 'rb')
        msgImage = MIMEImage(fp.read())
        fp.close()

        msgImage.add_header('Content-ID', '<image1>')
        msgRoot.attach(msgImage)
        return msgRoot.as_string()
        
