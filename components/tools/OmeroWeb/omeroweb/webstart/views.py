#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#
#
# Copyright (c) 2008-2014 University of Dundee.
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
import sys
import traceback
from glob import glob

from django.conf import settings
from django.template import loader as template_loader
from django.template import RequestContext as Context
from django.core.urlresolvers import reverse
from django.views.decorators.cache import never_cache

from omeroweb.http import HttpJNLPResponse
from omero_version import omero_version

from decorators import login_required, render_response


@never_cache
@login_required()
@render_response()
def custom_index(request, conn=None, **kwargs):
    context = {"version": omero_version}

    if settings.INDEX_TEMPLATE is not None:
        try:
            template_loader.get_template(settings.INDEX_TEMPLATE)
            context['template'] = settings.INDEX_TEMPLATE
        except Exception:
            context['template'] = 'webstart/start.html'
            context["error"] = traceback.format_exception(*sys.exc_info())[-1]
    else:
        context['template'] = 'webstart/start.html'

    return context


@never_cache
@login_required()
@render_response()
def index(request, conn=None, **kwargs):
    context = {"version": omero_version}

    if settings.WEBSTART_TEMPLATE is not None:
        try:
            template_loader.get_template(settings.WEBSTART_TEMPLATE)
            context['template'] = settings.WEBSTART_TEMPLATE
        except Exception:
            context['template'] = 'webstart/index.html'
            context["error"] = traceback.format_exception(*sys.exc_info())[-1]
    else:
        context['template'] = 'webstart/index.html'

    return context


@never_cache
def insight(request):
    t = template_loader.get_template('webstart/insight.xml')

    codebase = request.build_absolute_uri(settings.STATIC_URL+'webstart/jars/')
    href = request.build_absolute_uri(reverse("webstart_insight"))

    pattern = os.path.abspath(os.path.join(
        settings.OMERO_HOME, "lib", "insight",  "*.jar").replace('\\', '/'))
    jarlist = glob(pattern)
    jarlist = [os.path.basename(x) for x in jarlist]

    # ticket:9478 put insight jar at the start of the list if available
    # This can be configured via omero.web.webstart_jar to point to a
    # custom value.
    idx = jarlist.index(settings.WEBSTART_JAR)
    if idx > 0:
        jarlist.pop(idx)
        jarlist.insert(0, settings.WEBSTART_JAR)

    context = {
        'codebase': codebase, 'href': href, 'jarlist': jarlist,
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
    return HttpJNLPResponse(t.render(c))
