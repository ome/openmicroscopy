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

    url( r'^$', views.index, name="waindex" ),
    url( r'^login/$', views.login, name="walogin" ),
    url( r'^logout/$', views.logout, name="walogout" ),
    url( r'^forgottenpassword/$', views.forgotten_password, name="waforgottenpassword" ),
    url( r'^experimenters/$', views.experimenters, name="waexperimenters" ),
    url( r'^experimenter/(?P<action>[a-z]+)/$', views.manage_experimenter, name="wamanageexperimenteraction" ),
    url( r'^experimenter/(?P<action>[a-z]+)/(?P<eid>[0-9]+)/$', views.manage_experimenter, name="wamanageexperimenterid" ),
    url( r'^groups/$', views.groups, name="wagroups" ),
    url( r'^group/(?P<action>[a-z]+)/$', views.manage_group, name="wamanagegroupaction" ),
    url( r'^group/(?P<action>[a-z]+)/(?P<gid>[0-9]+)/$', views.manage_group, name="wamanagegroupid" ),
    url( r'^ldap/', views.ldap, name="waldap" ),
    url( r'^scripts/', views.scripts, name="wascripts" ),
    url( r'^script/([a-z]+)/$', views.manage_script, name="wamanagescriptaction" ),
    url( r'^script/([a-z]+)/([0-9]+)/$', views.manage_script, name="wamanagescriptid" ),
    url( r'^imports/$', views.imports, name="waimports" ),
    url( r'^imports/([a-z]+)/$', views.imports, name="waimportsaction" ),
    url( r'^myaccount/$', views.my_account, name="wamyaccount" ),
    url( r'^myaccount/(?P<action>[a-z]+)/$', views.my_account, name="wamyaccountaction" ),
    url( r'^drivespace/$', views.drivespace, name="wadrivespace"),

    url( r'^piechart/$', views.piechart, name="wapiechart"),
    url( r'^myphoto/$', views.myphoto, name="wamyphoto"),

    # static
    url( r'^static/(?P<path>.*)$', serve ,{ 'document_root': os.path.join(os.path.dirname(__file__), 'media').replace('\\','/') }, name="wastatic" ),
    url( r'^help/(?P<path>.*)$', serve ,{ 'document_root': os.path.join(os.path.dirname(__file__), 'help').replace('\\','/') }, name="wahelp" ),

)

