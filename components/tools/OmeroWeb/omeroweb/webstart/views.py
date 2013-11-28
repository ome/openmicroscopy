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

import os
from glob import glob

from django.conf import settings
from django.template import loader as template_loader
from django.template import RequestContext as Context
from django.shortcuts import render_to_response
from django.http import HttpResponse
from django.core.urlresolvers import reverse
from django.views.decorators.cache import never_cache

from omero_version import omero_version

def index(request):
    template = settings.INDEX_TEMPLATE
    if not isinstance(template, basestring):
        template = 'webstart/index.html'
    
    insight_url = None
    if settings.WEBSTART:
        insight_url = request.build_absolute_uri(reverse("webstart_insight"))
    
    return render_to_response(template,{'insight_url':insight_url, "version": omero_version})

@never_cache
def insight(request):
    t = template_loader.get_template('webstart/insight.xml')
    
    codebase = request.build_absolute_uri(settings.STATIC_URL+'webstart/jars/')
    href = request.build_absolute_uri(reverse("webstart_insight"))

    pattern = os.path.abspath(os.path.join(settings.OMERO_HOME, "lib", "insight",  "*.jar").replace('\\','/'))
    jarlist = glob(pattern)
    jarlist = [os.path.basename(x) for x in jarlist]

    # ticket:9478 put insight jar at the start of the list if available
    # This can be configured via omero.web.webstart_jar to point to a
    # custom value.
    idx = jarlist.index(settings.WEBSTART_JAR)
    if idx > 0:
        jarlist.pop(idx)
        jarlist.insert(0, settings.WEBSTART_JAR)
    
	idy = jarlist.index(settings.NANOXML_JAR)
	if idy > 0:
		jarlist.pop(idy)
		jarlist.insert(len(jarlist)-1, settings.NANOXML_JAR)
		
    context = {'codebase': codebase, 'href': href, 'jarlist': jarlist,
               'icon': settings.WEBSTART_ICON,
               'heap': settings.WEBSTART_HEAP,
               'host': settings.WEBSTART_HOST,
               'port': settings.WEBSTART_PORT,
               'class': settings.WEBSTART_CLASS,
               'title': settings.WEBSTART_TITLE,
               'vendor': settings.WEBSTART_VENDOR,
               'homepage': settings.WEBSTART_HOMEPAGE,
              }

    c = Context(request, context)
    return HttpResponse(t.render(c), content_type="application/x-java-jnlp-file")
    