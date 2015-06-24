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
import httplib
import urllib
import urlparse

from omero_version import omero_version

from django.conf import settings

logger = logging.getLogger(__name__)


class SendFeedback(object):

    conn = None

    def __init__(self, feedback_url):
        try:
            host = urlparse.urlparse(feedback_url).hostname
            port = urlparse.urlparse(feedback_url).port
            if feedback_url.startswith("https"):
                self.conn = httplib.HTTPSConnection(host=host, port=port)
            else:
                self.conn = httplib.HTTPConnection(host=host, port=port)
        except Exception, e:
            logger.error(e)
            raise e

    def send_feedback(self, error=None, comment=None, email=None,
                      user_agent=""):
        try:
            try:
                p = {
                    "app_name": settings.FEEDBACK_APP,
                    "app_version": omero_version,
                    "extra": "",
                    "error": (error or ""),
                    "email": (email or ""),
                    "comment": (comment or "")
                    }
                try:
                    import sys
                    p['python_classpath'] = sys.path
                except:
                    pass
                try:
                    import platform
                    try:
                        p['python_version'] = platform.python_version()
                    except:
                        pass
                    try:
                        p['os_name'] = platform.platform()
                    except:
                        pass
                    try:
                        p['os_arch'] = platform.machine()
                    except:
                        pass
                    try:
                        p['os_version'] = platform.release()
                    except:
                        pass
                except:
                    pass
                params = urllib.urlencode(p)
                headers = {
                    "Content-type": "application/x-www-form-urlencoded",
                    "Accept": "text/plain",
                    "User-Agent": user_agent}
                self.conn.request("POST", "/qa/initial/", params, headers)
                response = self.conn.getresponse()

                if response.status == 200:
                    logger.info(response.read())
                else:
                    logger.error(
                        "Feedback server error: %s" % response.reason)
                    raise Exception(
                        "Feedback server error: %s" % response.reason)
            except Exception, x:
                logger.error("Feedback could not be sent.")
                logger.error(traceback.format_exc())
                raise x
        finally:
            self.conn.close()
