#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
# All rights reserved.
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

"""Handles all 'api' urls."""

from django.conf.urls import url, patterns
from omeroweb.api import views
from omeroweb.webgateway.views import LoginView
from django.conf import settings
import re

versions = '|'.join([re.escape(v)
                    for v in settings.API_VERSIONS])

api_versions = url(r'^$', views.api_versions, name='api_versions')

api_base = url(r'^v(?P<api_version>%s)/$' % versions,
               views.api_base,
               name='api_base')
"""
GET various urls listed below
"""

api_token = url(r'^v(?P<api_version>%s)/token/$' % versions,
                views.api_token,
                name='api_token')
"""
GET the CSRF token for this session. Needs to be included
in header with all POST, PUT & DELETE requests
"""

api_servers = url(r'^v(?P<api_version>%s)/servers/$' % versions,
                  views.api_servers,
                  name='api_servers')
"""
GET list of available OMERO servers to login to.
"""

api_login = url(r'^v(?P<api_version>%s)/login/$' % versions,
                LoginView.as_view(),
                name='api_login')
"""
Login to OMERO. POST with 'username', 'password' and 'server' index
"""

api_save = url(r'^v(?P<api_version>%s)/m/save/$' % versions,
               views.SaveView.as_view(),
               name='api_save')
"""
POST to create a new object or PUT to update existing object.
In both cases content body encodes json data.
"""

api_projects = url(r'^v(?P<api_version>%s)/m/projects/$' % versions,
                   views.ProjectsView.as_view(),
                   name='api_projects')
"""
GET all projects, using omero-marshal to generate json
"""

api_project = url(
    r'^v(?P<api_version>%s)/m/projects/(?P<pid>[0-9]+)/$' % versions,
    views.ProjectView.as_view(),
    name='api_project')
"""
Project url to GET or DELETE a single Project
"""

api_datasets = url(r'^v(?P<api_version>%s)/m/datasets/$' % versions,
                   views.DatasetsView.as_view(),
                   name='api_datasets')
"""
GET all projects, using omero-marshal to generate json
"""

api_dataset = url(
    r'^v(?P<api_version>%s)/m/datasets/(?P<pid>[0-9]+)/$' % versions,
    views.DatasetView.as_view(),
    name='api_dataset')
"""
Dataset url to GET or DELETE a single Dataset
"""

api_project_datasets = url(
    r'^v(?P<api_version>%s)/m/projects/(?P<pid>[0-9]+)/datasets/$' % versions,
    views.DatasetsView.as_view(),
    name='api_project_datasets')
"""
GET Datasets in Project, using omero-marshal to generate json
"""

api_screens = url(r'^v(?P<api_version>%s)/m/screens/$' % versions,
                   views.ScreensView.as_view(),
                   name='api_screens')
"""
GET all projects, using omero-marshal to generate json
"""

api_screen = url(
    r'^v(?P<api_version>%s)/m/screens/(?P<pid>[0-9]+)/$' % versions,
    views.ScreenView.as_view(),
    name='api_screen')
"""
Dataset url to GET or DELETE a single Screen
"""

urlpatterns = patterns(
    '',
    api_versions,
    api_base,
    api_token,
    api_servers,
    api_login,
    api_save,
    api_projects,
    api_project,
    api_datasets,
    api_dataset,
    api_project_datasets,
    api_screens,
    api_screen,
)
