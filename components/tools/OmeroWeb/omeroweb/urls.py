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

from django.conf.urls.defaults import *
from django.contrib import admin

from omeroweb.webadmin.models import Gateway
from omeroweb.webclient.models import Advice, CategoryAdvice

# make admin enable
admin.autodiscover()
admin.site.register(Gateway)
admin.site.register(Advice)
admin.site.register(CategoryAdvice)

# error handler
handler404 = "omeroweb.feedback.views.handler404"
handler500 = "omeroweb.feedback.views.handler500"

# url patterns
urlpatterns = patterns('',
    (r'^admin/(.*)', admin.site.root),
    
    (r'(?i)^webadmin/', include('omeroweb.webadmin.urls')),
    (r'(?i)^webclient/', include('omeroweb.webclient.urls')),
    (r'(?i)^feedback/', include('omeroweb.feedback.urls')),
    
    (r'^favicon\.ico$', 'django.views.generic.simple.redirect_to', {'url': '/static/images/ome.ico'}),
    
    
    #(r'^images/', include('omeroweb.weblitzviewer.urls')),
)
