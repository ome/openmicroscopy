#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#
# Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
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

from django.http import HttpResponse

class HttpJavascriptRedirect(HttpResponse):
    def __init__(self,content):
        content = '<html><body onLoad="javascript:window.top.location.href=\'%s\'"></body></html>' % content
        HttpResponse.__init__(self,content)


class HttpJavascriptResponse(HttpResponse):
    def __init__(self,content):
        HttpResponse.__init__(self, content, content_type="text/javascript")


class HttpLoginRedirect(HttpResponse):
    def __init__(self,content): 
        content = """<html><body onLoad="top.location.replace('%s');"></body></html>""" % content
        HttpResponse.__init__(self,content)
