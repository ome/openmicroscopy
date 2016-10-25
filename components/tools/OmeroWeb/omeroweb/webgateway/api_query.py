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

from omero.rtypes import unwrap, rlong
from django.conf import settings

from api_marshal import marshal_objects
from copy import deepcopy


def query_projects(conn, childCount=False,
                   group=None, owner=None,
                   page=1, limit=settings.PAGE,
                   normalize=False):
    """
    Query OMERO and marshal omero.model.Projects.

    Build a query based on a number of parameters,
    queries OMERO with the query service and
    marshals Projects with omero_marshal.

    @param conn:        BlitzGateway
    @param childCount:  If true, also load Dataset counts as omero:childCount
    @param group:       Filter by group Id
    @param owner:       Filter by owner Id
    @param page:        Pagination page. Default is 1
    @param limit:       Page size
    @param normalize:   If true, marshal groups and experimenters separately
    """
    qs = conn.getQueryService()
    params = omero.sys.ParametersI()
    if page:
        params.page((page-1) * limit, limit)
    ctx = deepcopy(conn.SERVICE_OPTS)

    # Set the desired group context and owner
    if group is None:
        group = -1
    ctx.setOmeroGroup(group)
    where_clause = ''
    if owner is not None and owner != -1:
        params.add('owner', rlong(owner))
        where_clause = 'where project.details.owner.id = :owner'

    withChildCount = ""
    if childCount:
        withChildCount = """, (select count(id) from ProjectDatasetLink pdl
                 where pdl.parent=project.id)"""

    # Need to load owners specifically, else can be unloaded if group != -1
    query = """
            select project %s from Project project
            join fetch project.details.owner
            %s
            order by lower(project.name), project.id
            """ % (withChildCount, where_clause)

    projects = []
    extras = {}
    if childCount:
        result = qs.projection(query, params, ctx)
        for p in result:
            project = unwrap(p[0])
            projects.append(project)
            extras[project.id.val] = {'omero:childCount': unwrap(p[1])}
    else:
        extras = None
        result = qs.findAllByQuery(query, params, ctx)
        for p in result:
            projects.append(p)

    return marshal_objects(projects, extras=extras, normalize=normalize)
