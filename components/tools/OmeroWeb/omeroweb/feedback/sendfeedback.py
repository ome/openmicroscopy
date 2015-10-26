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

import sys
import platform
import traceback
import logging
import urllib
import urllib2
import urlparse

from omero_version import omero_version

from django.conf import settings

logger = logging.getLogger(__name__)


class SendFeedback(object):

    conn = None

    def __init__(self, feedback_url):
        self.url = urlparse.urljoin(feedback_url, "/qa/initial/")

    def send_feedback(self, error=None, comment=None, email=None,
                      user_agent=""):
        try:
            p = {
                "app_name": settings.FEEDBACK_APP,
                "app_version": omero_version,
                "extra": "",
                "error": (error or ""),
                "email": (email or ""),
                "comment": comment
                }
            try:
                p['python_classpath'] = sys.path
            except:
                pass
            try:
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
            data = urllib.urlencode(p)
            headers = {
                "Content-type": "application/x-www-form-urlencoded",
                "Accept": "text/plain",
                "User-Agent": user_agent}
            request = urllib2.Request(self.url, data, headers)
            try:
                response = urllib2.urlopen(request)
                if response.code == 200:
                    logger.info(response.read())
                else:
                    logger.error(
                        "Feedback server error: %s" % response.reason)
                    raise Exception(
                        "Feedback server error: %s" % response.reason)
            except urllib2.URLError, e:
                logger.error(traceback.format_exc())
                raise Exception(
                    "Feedback server error: %s" % e.reason)
            finally:
                response.close()
        except Exception, x:
            logger.error(traceback.format_exc())
            raise Exception("Feedback server error: %s" % x.message)
