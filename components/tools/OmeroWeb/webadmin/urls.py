#!/usr/bin/env python
# 
# WebAdmin urls resolver
# 
# Copyright (c) 2008 University of Dundee. All rights reserved.
# This file is distributed under the same license as the OMERO package.
# Use is subject to license terms supplied in LICENSE.txt
# 
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
# 
# Version: 1.0
#

import os.path

from django.conf.urls.defaults import *
from django.views.static import serve

from webadminforblitz.webadmin import views

# url patterns
urlpatterns = patterns('',

    ( r'^$', views.index ),
    ( r'login/$', views.login ),
    ( r'logout/$', views.logout ),
    ( r'experimenters/$', views.experimenters ),
    ( r'experimenter/(?P<action>[a-z]+)/$', views.manage_experimenter ),
    ( r'experimenter/(?P<action>[a-z]+)/(?P<eid>[0-9]+)/$', views.manage_experimenter ),
    ( r'groups/$', views.groups ),
    ( r'group/(?P<action>[a-z]+)/$', views.manage_group ),
    ( r'group/(?P<action>[a-z]+)/(?P<gid>[0-9]+)/$', views.manage_group ),
    ( r'ldap/', views.ldap ),
    ( r'scripts/', views.scripts ),
    ( r'script/([a-z]+)/$', views.manage_script ),
    ( r'script/([a-z]+)/([0-9]+)/$', views.manage_script ),
    ( r'imports/$', views.imports ),
    ( r'imports/([a-z]+)/$', views.imports ),
    ( r'myaccount/$', views.my_account ),
    ( r'myaccount/(?P<action>[a-z]+)/$', views.my_account ),
    ( r'drivespace/$', views.drivespace),

    # image generators
    ( r'piechart/$', views.piechart),

    # static
    ( r'^static/(?P<path>.*)$', serve ,{ 'document_root': os.path.join(os.path.dirname(__file__), 'media').replace('\\','/') } ),
    ( r'^help/(?P<path>.*)$', serve ,{ 'document_root': os.path.join(os.path.dirname(__file__), 'help').replace('\\','/') } ),

)

