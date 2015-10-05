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

from django.conf import settings
from django.conf.urls import url, patterns, include
from django.contrib.staticfiles.urls import staticfiles_urlpatterns
from django.shortcuts import redirect

from django.core.urlresolvers import reverse
from django.utils.functional import lazy
from django.views.generic import RedirectView
from django.views.decorators.cache import never_cache

# error handler
handler404 = "omeroweb.feedback.views.handler404"
handler500 = "omeroweb.feedback.views.handler500"

reverse_lazy = lazy(reverse, str)


def redirect_urlpatterns():
    """
    Helper function to return a URL pattern for index page http://host/.
    """
    if settings.INDEX_TEMPLATE is None:
        return patterns(
            '',
            url(r'^$', never_cache(
                RedirectView.as_view(url=reverse_lazy('webindex'))),
                name="index")
            )
    else:
        return patterns(
            '',
            url(r'^$', never_cache(
                RedirectView.as_view(url=reverse_lazy('webindex_custom'))),
                name="index"),
            )


# url patterns

urlpatterns = patterns(
    '',
    (r'^favicon\.ico$',
     lambda request: redirect('%swebgateway/img/ome.ico'
                              % settings.STATIC_URL)),

    (r'^(?i)webgateway/', include('omeroweb.webgateway.urls')),
    (r'^(?i)webadmin/', include('omeroweb.webadmin.urls')),
    (r'^(?i)webclient/', include('omeroweb.webclient.urls')),

    (r'^(?i)url/', include('omeroweb.webredirect.urls')),
    (r'^(?i)feedback/', include('omeroweb.feedback.urls')),

    url(r'^index/$', 'omeroweb.webclient.views.custom_index',
        name="webindex_custom"),
)

urlpatterns += redirect_urlpatterns()

for app in settings.ADDITIONAL_APPS:
    # Depending on how we added the app to INSTALLED_APPS in settings.py,
    # include the urls the same way
    if 'omeroweb.%s' % app in settings.INSTALLED_APPS:
        urlmodule = 'omeroweb.%s.urls' % app
    else:
        urlmodule = '%s.urls' % app
    regex = '^(?i)%s/' % app
    urlpatterns += patterns('', (regex, include(urlmodule)),)

if settings.DEBUG:
    urlpatterns += staticfiles_urlpatterns()
