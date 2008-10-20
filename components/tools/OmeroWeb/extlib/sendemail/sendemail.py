#!/usr/bin/env python
# 
# Send email
# 
# Copyright (c) 2008 University of Dundee. All rights reserved.
# This file is distributed under the same license as the OMERO package.
# Use is subject to license terms supplied in LICENSE.txt
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
SLEEPTIME = 60

class SendEmial(threading.Thread):

    smtp_server = None
    message = None

    def __init__(self):
        super(SendEmial, self).__init__()
        self.setDaemon(True)
        self.smtp_server = settings.EMAIL_SMTP_SERVER
        self.thread_timeout = False
        self.to_send = list()
        self.start()

    def run (self):
        """ this thread lives forever, pinging whatever connection exists to keep it's services alive """
        logger.debug("Starting thread...")
        while not (self.thread_timeout):
            try:
                logger.debug("%i email in the queue." % (len(self.to_send)))
                if len(self.to_send) > 0:
                    try:
                        email = self.to_send[0]
                        logger.debug("Sending...")
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
                logger.debug("sleep...")
                time.sleep(SLEEPTIME)
            except:
                logger.error("!! something bad on the SENDER keepalive thread !!")
                logger.error(traceback.format_exc())
        self.seppuku()
        logger.debug("Thread death")

    def seppuku (self):
        logger.debug("Thread will be closed")
        self._timeout = 0
        logger.debug("Thread Deleted")

    def __del__ (self):
        logger.debug("Garbage Collector KICK IN")

    def create_error_message(self, app, error):
        # Create the root message and fill in the from, to, and subject headers
        msgRoot = MIMEMultipart('related')
        msgRoot['Subject'] = 'OMERO.%s - error message' % (app)
        msgRoot['From'] = settings.EMAIL_SENDER_ADDRESS
        msgRoot['To'] = settings.EMAIL_ADMIN_ADDRESS
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
        self.to_send.append({"message": msgRoot.as_string(), "sender": settings.EMAIL_SENDER_ADDRESS, "recipients": [settings.EMAIL_ADMIN_ADDRESS]})

