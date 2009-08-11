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

    url( r'^$', views.index, name="admin_index" ),
    url( r'^login/$', views.login, name="admin_login" ),
    url( r'^logout/$', views.logout, name="admin_logout" ),
    url( r'^forgottenpassword/$', views.forgotten_password, name="admin_forgotten_password" ),
    url( r'^experimenters/$', views.experimenters, name="admin_experimenters" ),
    url( r'^experimenter/(?P<action>[a-z]+)/$', views.manage_experimenter, name="admin_manage_experimenter_action" ),
    url( r'^experimenter/(?P<action>[a-z]+)/(?P<eid>[0-9]+)/$', views.manage_experimenter, name="admin_manage_experimenter_id" ),
    url( r'^groups/$', views.groups, name="admin_groups" ),
    url( r'^group/(?P<action>[a-z]+)/$', views.manage_group, name="admin_manage_group_action" ),
    url( r'^group/(?P<action>[a-z]+)/(?P<gid>[0-9]+)/$', views.manage_group, name="admin_manage_group_id" ),
    url( r'^ldap/', views.ldap, name="admin_ldap" ),
    url( r'^scripts/', views.scripts, name="admin_scripts" ),
    url( r'^script/([a-z]+)/$', views.manage_script, name="admin_manage_script_action" ),
    url( r'^script/([a-z]+)/([0-9]+)/$', views.manage_script, name="admin_manage_script_id" ),
    url( r'^imports/$', views.imports, name="admin_imports" ),
    url( r'^imports/([a-z]+)/$', views.imports, name="admin_imports_action" ),
    url( r'^myaccount/$', views.my_account, name="admin_myaccount" ),
    url( r'^myaccount/(?P<action>[a-z]+)/$', views.my_account, name="admin_myaccount_action" ),
    url( r'^drivespace/$', views.drivespace, name="admin_drivespace"),

    url( r'^piechart/$', views.piechart, name="admin_piechart"),
    url( r'^myphoto/$', views.myphoto, name="admin_myphoto"),

    # static
    url( r'^static/(?P<path>.*)$', serve ,{ 'document_root': os.path.join(os.path.dirname(__file__), 'media').replace('\\','/') }, name="admin_static" ),
    url( r'^help/(?P<path>.*)$', serve ,{ 'document_root': os.path.join(os.path.dirname(__file__), 'help').replace('\\','/') }, name="admin_help" ),

)

