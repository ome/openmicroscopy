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

from omeroweb.webadmin import views

# url patterns
urlpatterns = patterns('',

    ( r'^$', views.index ),
    ( r'^login/$', views.login ),
    ( r'^logout/$', views.logout ),
    ( r'^experimenters/$', views.experimenters ),
    ( r'^experimenter/(?P<action>[a-z]+)/$', views.manage_experimenter ),
    ( r'^experimenter/(?P<action>[a-z]+)/(?P<eid>[0-9]+)/$', views.manage_experimenter ),
    ( r'^groups/$', views.groups ),
    ( r'^group/(?P<action>[a-z]+)/$', views.manage_group ),
    ( r'^group/(?P<action>[a-z]+)/(?P<gid>[0-9]+)/$', views.manage_group ),
    ( r'^ldap/', views.ldap ),
    ( r'^scripts/', views.scripts ),
    ( r'^script/([a-z]+)/$', views.manage_script ),
    ( r'^script/([a-z]+)/([0-9]+)/$', views.manage_script ),
    ( r'^imports/$', views.imports ),
    ( r'^imports/([a-z]+)/$', views.imports ),
    ( r'^myaccount/$', views.my_account ),
    ( r'^myaccount/(?P<action>[a-z]+)/$', views.my_account ),
    ( r'^drivespace/$', views.drivespace),

    ( r'^piechart/$', views.piechart),
    ( r'^myphoto/$', views.myphoto),

    # static
    ( r'^static/(?P<path>.*)$', serve ,{ 'document_root': os.path.join(os.path.dirname(__file__), 'media').replace('\\','/') } ),
    ( r'^help/(?P<path>.*)$', serve ,{ 'document_root': os.path.join(os.path.dirname(__file__), 'help').replace('\\','/') } ),

)

