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
import omeroweb.webclient.views

# url patterns
urlpatterns = patterns('',

    url( r'^$', views.index, name="waindex" ),
    url( r'^login/$', omeroweb.webclient.views.login, name="walogin" ),
    url( r'^logout/$', views.logout, name="walogout" ),
    url( r'^forgottenpassword/$', views.forgotten_password, name="waforgottenpassword" ),
    url( r'^experimenters/$', views.experimenters, name="waexperimenters" ),
    url( r'^experimenter/(?P<action>[a-z]+)/(?:(?P<eid>[0-9]+)/)?$', views.manage_experimenter, name="wamanageexperimenterid" ),
    url( r'^change_password/(?P<eid>[0-9]+)/$', views.manage_password, name="wamanagechangepasswordid" ),
    url( r'^groups/$', views.groups, name="wagroups" ),
    url( r'^group/(?P<action>((?i)new|create|edit|save|update|members))/(?:(?P<gid>[0-9]+)/)?$', views.manage_group, name="wamanagegroupid" ),
    url( r'^group_owner/(?P<action>((?i)edit|save))/(?P<gid>[0-9]+)/$', views.manage_group_owner, name="wamanagegroupownerid" ),
    url( r'^ldap/', views.ldap, name="waldap" ),
    #url( r'^enums/$', views.enums, name="waenums" ),
    #url( r'^enum/(?P<action>((?i)new|edit|delete|save|reset))/(?P<klass>[a-zA-Z]+)/(?:(?P<eid>[0-9]+)/)?$', views.manage_enum, name="wamanageenum" ),
    #url( r'^imports/$', views.imports, name="waimports" ),
    url( r'^myaccount/(?:(?P<action>[a-z]+)/)?$', views.my_account, name="wamyaccount" ),
    url( r'^drivespace/$', views.drivespace, {'template':'json'}, name="wadrivespace"),
    url( r'^load_drivespace/$', views.load_drivespace, name="waloaddrivespace"),

    url( r'^change_avatar/(?P<eid>[0-9]+)/(?:(?P<action>[a-z]+)/)?$', views.manage_avatar, name="wamanageavatar"),
    url( r'^myphoto/$', views.myphoto, name="wamyphoto"),

)

