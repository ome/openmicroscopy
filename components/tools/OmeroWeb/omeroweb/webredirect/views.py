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

''' A view functions is simply a Python function that takes a Web request and
returns a Web response. This response can be the HTML contents of a Web page,
or a redirect, or the 404 and 500 error, or an XML document, or an image...
or anything.'''

from django.http import HttpResponseRedirect
from django.core.urlresolvers import reverse
from omeroweb.feedback.views import handlerInternalError
import logging

logger = logging.getLogger(__name__)


def index(request, **kwargs):
    if request.GET.get('show', None) is not None:
        url = "?".join([
            reverse(viewname="webindex"),
            "show="+request.GET.get('show')
            ])
        return HttpResponseRedirect(url)
    else:
        return handlerInternalError(
            request,
            "Path was not recognized. URL should follow the pattern: %s%s" % (
                request.build_absolute_uri(reverse(viewname="webredirect")),
                ("?path=server=1|project=1|dataset=2|image=3:selected")))
