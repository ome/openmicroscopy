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
import httplib, urllib

from email.MIMEMultipart import MIMEMultipart
from email.MIMEText import MIMEText
from email.MIMEImage import MIMEImage
from django.conf import settings

logger = logging.getLogger('sendfeedback')
SLEEPTIME = 30

class SendFeedback(threading.Thread):

    feedback_url = None
    message = None

    def __init__(self):
        super(SendFeedback, self).__init__()
        self.setDaemon(True)
        self.feedback_url = "users.openmicroscopy.org.uk:80"
        self.thread_timeout = False
        self.to_send = list()
        self.start()
    
    def run (self):
        """ this thread lives forever, pinging whatever connection exists to keep it's services alive """
        logger.info("Starting sendfeedback thread...")
        while not (self.thread_timeout):
            try:
                logger.debug("%i feedbacks in the queue." % (len(self.to_send)))
                if len(self.to_send) > 0:
                    try:
                        conn = httplib.HTTPConnection(self.feedback_url)
                        try:
                            try:
                                feedback = self.to_send[0]
                                p = {'error': feedback['error'], "type":feedback['app']}
                                if feedback['email'] is not None:
                                    p['email'] = feedback['email']
                                if feedback['comment'] is not None:
                                    p['comment'] = feedback['comment']
                                params = urllib.urlencode(p)
                                logger.debug("Sending...")
                                headers = {"Content-type": "application/x-www-form-urlencoded","Accept": "text/plain"}
                                conn.request("POST", "/~brain/omero/bugcollector.php", params, headers)
                                response = conn.getresponse()

                                if response.status == 200:
                                    logger.info(response.read())
                                else:
                                    logger.error("Feedback server error: %s" % response.reason)
                            except:
                                logger.error("Feedback could not be sent.")
                                logger.error(traceback.format_exc())
                        finally:
                            conn.close()
                            try:
                                self.to_send.remove(feedback)
                            except:
                                logger.error("Could not remove feedback from the queue.")
                    except Exception, x:
                        logger.error(x)
                logger.debug("sleep...")
                time.sleep(SLEEPTIME)
            except:
                logger.error("!! something bad on the SENDER keepalive thread !!")
                logger.error(traceback.format_exc())
                self.seppuku()
        logger.debug("Thread death")

    def seppuku (self):
        logger.debug("Thread will be closed")
        self.thread_timeout = True
        logger.debug("Thread Deleted")

    def __del__ (self):
        logger.debug("Garbage Collector KICK IN")

    def create_error_message(self, error, comment=None, email=None):
        self.to_send.append({"email": email, "comment":comment, "error": error, "app":"web_bugs"})
