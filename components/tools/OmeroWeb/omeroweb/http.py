#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#
# Copyright (C) 2011-2014 University of Dundee & Open Microscopy Environment.
# All rights reserved.
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

import json

from django.http import HttpResponse, HttpResponseServerError


class HttpJavascriptResponse(HttpResponse):
    def __init__(self, content):
        HttpResponse.__init__(self, content, content_type="text/javascript")


class HttpJavascriptResponseServerError(HttpResponseServerError):
    def __init__(self, content):
        HttpResponseServerError.__init__(
            self, content, content_type="text/javascript")


class HttpJsonResponse(HttpResponse):
    def __init__(self, content):
        HttpResponse.__init__(
            self, json.dumps(content), content_type="application/json")


class HttpJNLPResponse(HttpResponse):
    def __init__(self, content):
        HttpResponse.__init__(
            self, content, content_type="application/x-java-jnlp-file")


class HttpJPEGResponse(HttpResponse):
    def __init__(self, content):
        HttpResponse.__init__(self, content, content_type="image/jpeg")
