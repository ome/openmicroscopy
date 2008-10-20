#!/usr/bin/env python
# 
# Main urls resolver
# 
# Copyright (c) 2008 University of Dundee. All rights reserved.
# This file is distributed under the same license as the OMERO package.
# Use is subject to license terms supplied in LICENSE.txt
# 
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
# 
# Version: 1.0
#

from django.conf.urls.defaults import *
from django.contrib import admin

from webadminforblitz.webadmin.models import Gateway

# make admin enable
admin.autodiscover()
admin.site.register(Gateway)

# error handler
handler404 = "webadminforblitz.webadmin.views.handler404"
handler500 = "webadminforblitz.webadmin.views.handler500"

# url patterns
urlpatterns = patterns('',
    (r'^admin/(.*)', admin.site.root),
    (r'^webadmin/', include('webadminforblitz.webadmin.urls')),
)
