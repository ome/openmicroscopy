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

import os.path

from django.conf.urls.defaults import *
from django.views.static import serve

# error handler
handler404 = "omeroweb.feedback.views.handler404"
handler500 = "omeroweb.feedback.views.handler500"

# url patterns
urlpatterns = patterns('',
    
    (r'^favicon\.ico$', 'django.views.generic.simple.redirect_to', {'url': '/appmedia/omeroweb/images/ome.ico'}),
    (r'^appmedia/webgateway/(?P<path>.*)$', 'django.views.static.serve', {'document_root': os.path.join(os.path.dirname(__file__), 'webgateway/media')}),
    url( r'^appmedia/omeroweb/(?P<path>.*)$', serve ,{ 'document_root': os.path.join(os.path.dirname(__file__), 'media', 'omeroweb').replace('\\','/') }, name="webstatic" ),
    url(r'^appmedia/(?P<path>.*)$', serve, {'document_root': os.path.join(os.path.dirname(__file__), 'media')}, name="static"),
    
    (r'(?i)^webadmin/', include('omeroweb.webadmin.urls')),
    (r'(?i)^webclient/', include('omeroweb.webclient.urls')),
    (r'(?i)^feedback/', include('omeroweb.feedback.urls')),
    (r'(?i)^webgateway/', include('omeroweb.webgateway.urls')),
    (r'(?i)^webtest/', include('omeroweb.webtest.urls')),    
       
    (r'(?i)^webemdb/', include('omeroweb.webemdb.urls')),
    (r'(?i)^webmobile/', include('omeroweb.webmobile.urls')),
)
