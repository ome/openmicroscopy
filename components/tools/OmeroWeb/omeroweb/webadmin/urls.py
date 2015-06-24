#!/usr/bin/env python
# -*- coding: utf-8 -*-
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

from django.conf.urls import url, patterns

from omeroweb.webadmin import views
from omeroweb.webclient import views as webclient_views

# url patterns
urlpatterns = patterns(
    '',
    url(r'^$', views.index, name="waindex"),
    url(r'^login/$', webclient_views.login, name="walogin"),
    url(r'^logout/$', views.logout, name="walogout"),
    url(r'^forgottenpassword/$', views.forgotten_password,
        name="waforgottenpassword"),
    url(r'^experimenters/$', views.experimenters, name="waexperimenters"),
    url(r'^experimenter/(?P<action>[a-z]+)/(?:(?P<eid>[0-9]+)/)?$',
        views.manage_experimenter, name="wamanageexperimenterid"),
    url(r'^change_password/(?P<eid>[0-9]+)/$', views.manage_password,
        name="wamanagechangepasswordid"),
    url(r'^groups/$', views.groups, name="wagroups"),
    url(r'^group/(?P<action>((?i)new|create|edit|save))/'
        '(?:(?P<gid>[0-9]+)/)?$', views.manage_group, name="wamanagegroupid"),
    url(r'^group_owner/(?P<action>((?i)edit|save))/(?P<gid>[0-9]+)/$',
        views.manage_group_owner, name="wamanagegroupownerid"),
    url(r'^myaccount/(?:(?P<action>[a-z]+)/)?$',
        views.my_account, name="wamyaccount"),
    url(r'^stats/$', views.stats, name="wastats"),
    url(r'^drivespace_json/groups/$', views.drivespace_json,
        {'query': 'groups'}, name="waloaddrivespace_groups"),
    url(r'^drivespace_json/users/$', views.drivespace_json,
        {'query': 'users'}, name="waloaddrivespace_users"),
    url(r'^drivespace_json/group/(?P<groupId>[0-9]+)/$',
        views.drivespace_json, name="waloaddrivespace_group"),
    url(r'^drivespace_json/user/(?P<userId>[0-9]+)/$',
        views.drivespace_json, name="waloaddrivespace_user"),

    url(r'^change_avatar/(?P<eid>[0-9]+)/(?:(?P<action>[a-z]+)/)?$',
        views.manage_avatar, name="wamanageavatar"),
    url(r'^myphoto/$', views.myphoto, name="wamyphoto"),
    url(r'^email/$', views.email, name="waemail"),
)
