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

''' Helper functions for views that handle object trees '''

import omero

from omero.rtypes import unwrap
from django.conf import settings

from api_marshal import marshal_projects


def query_projects(conn, childCount=False,
                   page=1, limit=settings.PAGE,
                   normalize=False):

    qs = conn.getQueryService()
    params = omero.sys.ParametersI()
    if page:
        params.page((page-1) * limit, limit)
    ctx = {'omero.group': '-1'}

    withChildCount = ""
    if childCount:
        withChildCount = """, (select count(id) from ProjectDatasetLink pdl
                 where pdl.parent=project.id)"""
    query = "select project %s from Project project" % withChildCount

    query += " order by lower(project.name), project.id"

    projects = []
    extras = []
    if childCount:
        result = qs.projection(query, params, ctx)
        for p in result:
            projects.append(unwrap(p[0]))
            extras.append({'childCount': unwrap(p[1])})
    else:
        extras = None
        result = qs.findAllByQuery(query, params, ctx)
        for p in result:
            projects.append(p)

    return marshal_projects(projects, extras=extras, normalize=normalize)
