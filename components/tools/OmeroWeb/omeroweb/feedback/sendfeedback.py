#!/usr/bin/env python
# -*- coding: utf-8 -*-
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
import logging
import httplib, urllib, urlparse

from omero_version import omero_version

from email.MIMEMultipart import MIMEMultipart
from email.MIMEText import MIMEText
from email.MIMEImage import MIMEImage

from django.conf import settings

logger = logging.getLogger(__name__)

class SendFeedback(object):

    conn = None
    
    def __init__(self, feedback_url):
        try:
            host = urlparse.urlparse(feedback_url).hostname
            if feedback_url.startswith("https"):
                self.conn = httplib.HTTPSConnection(host)
            else:
                self.conn = httplib.HTTPConnection(host)
        except Exception, e:
            logger.error(e)
            raise e
                
    def send_feedback(self, feedback):
        try:
            try:
                p = {'error': feedback['error'], "app_name":feedback['app_name'], "app_version":feedback['app_version'], "extra":""}
                if feedback['email'] is not None:
                    p['email'] = feedback['email']
                if feedback['comment'] is not None:
                    p['comment'] = feedback['comment']
                if feedback['env'] is not None:
                    try:
                        p['python_classpath'] = feedback['env']['path']
                    except:
                        pass
                    try:
                        p['python_version'] = feedback['env']['python_version']
                    except:
                        pass
                    try:
                        p['os_name'] = feedback['env']['platform']
                    except:
                        pass
                    try:
                        p['os_arch'] = feedback['env']['arch']
                    except:
                        pass
                    try:
                        p['os_version'] = feedback['env']['os_version']
                    except:
                        pass                        
                params = urllib.urlencode(p)
                headers = {"Content-type": "application/x-www-form-urlencoded","Accept": "text/plain"}
                self.conn.request("POST", "/qa/initial/", params, headers)
                response = self.conn.getresponse()

                if response.status == 200:
                    logger.info(response.read())
                else:
                    logger.error("Feedback server error: %s" % response.reason)
                    raise Exception("Feedback server error: %s" % response.reason)
            except Exception, x:
                logger.error("Feedback could not be sent.")
                logger.error(traceback.format_exc())
                raise x
        finally:
            self.conn.close()
    
    def send_comment(self, feedback):
        try:
            try:
                p = {"comment":feedback['comment'], "app_name":feedback['app_name'], "app_version":feedback['app_version'], "extra":""}
                if feedback['email'] is not None:
                    p['email'] = feedback['email']
                if feedback['env'] is not None:
                    try:
                        p['python_classpath'] = feedback['env']['path']
                    except:
                        pass
                    try:
                        p['python_version'] = feedback['env']['python_version']
                    except:
                        pass
                    try:
                        p['os_name'] = feedback['env']['platform']
                    except:
                        pass
                    try:
                        p['os_arch'] = feedback['env']['arch']
                    except:
                        pass
                    try:
                        p['os_version'] = feedback['env']['os_version']
                    except:
                        pass                        
                params = urllib.urlencode(p)
                headers = {"Content-type": "application/x-www-form-urlencoded","Accept": "text/plain"}
                self.conn.request("POST", "/qa/initial/", params, headers)
                response = self.conn.getresponse()

                if response.status == 200:
                    logger.info(response.read())
                else:
                    logger.error("Feedback server error: %s" % response.reason)
                    raise Exception("Feedback server error: %s" % response.reason)
            except Exception, x:
                logger.error("Feedback could not be sent.")
                logger.error(traceback.format_exc())
                raise x
        finally:
            self.conn.close()


    def give_feedback(self, error, comment=None, email=None):
        env = dict()
        try:
            import sys
            env['path'] = sys.path
        except:
            pass
        try:
            import platform
            env['platform'] = platform.platform()
        except:
            pass
        try:
            env['arch'] = platform.machine()
        except:
            pass
        try:
            env['os_version'] = platform.release()
        except:
            pass
        try:
            env['python_version'] = platform.python_version()
        except:
            pass
        if len(env) == 0:
            env = None
        self.send_feedback({"email": email, "comment":comment, "error": error, "app_name": 6, "app_version": omero_version, "env":env})

    def give_comment(self, comment, email=None):
        env = dict()
        try:
            import sys
            env['path'] = sys.path
        except:
            pass
        try:
            import platform
            env['platform'] = platform.platform()
        except:
            pass
        try:
            env['arch'] = platform.machine()
        except:
            pass
        try:
            env['os_version'] = platform.release()
        except:
            pass
        try:
            env['python_version'] = platform.python_version()
        except:
            pass
        if len(env) == 0:
            env = None
        self.send_comment({"email": email, "comment":comment, "app_name": 6, "app_version": omero_version, "env":env})
    
