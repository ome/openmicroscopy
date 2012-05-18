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

import os
from glob import glob

from django.conf import settings
from django.core import template_loader
from django.template import RequestContext as Context
from django.shortcuts import render_to_response
from django.http import HttpResponse
from django.core.urlresolvers import reverse

def index(request):
    template = settings.INDEX_TEMPLATE
    if template is None:
        template = 'webstart/index.html'
    return render_to_response(template,{'insight_url':request.build_absolute_uri(reverse("webstart_insight"))})

def insight(request):
    t = template_loader.get_template('webstart/insight.xml')
    
    codebase = request.build_absolute_uri(settings.STATIC_URL+'webstart/jars/')
    href = request.build_absolute_uri(reverse("webstart_insight"))

    pattern = os.path.abspath(os.path.join(settings.OMERO_HOME, "lib", "insight",  "*.jar").replace('\\','/'))
    jarlist = glob(pattern)
    jarlist = [os.path.basename(x) for x in jarlist]
    context = {'codebase': codebase, 'href': href, 'jarlist': jarlist}
    c = Context(request, context)
    return HttpResponse(t.render(c), content_type="application/x-java-jnlp-file")
