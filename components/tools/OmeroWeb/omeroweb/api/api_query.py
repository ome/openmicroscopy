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

"""Helper functions for views that handle object trees."""


from omero.rtypes import unwrap
from django.conf import settings

from api_marshal import marshal_objects
from copy import deepcopy


def query_projects(conn, childCount=False,
                   group=None, owner=None,
                   page=1, limit=settings.PAGE,
                   normalize=False):
    """Query OMERO and marshal omero.model.Projects."""
    return query_objects(conn, 'Project',
                         childCount=childCount, group=group, owner=owner,
                         page=page, limit=limit, normalize=normalize)


def query_datasets(conn, project=None, childCount=False,
                   group=None, owner=None,
                   page=1, limit=settings.PAGE,
                   normalize=False):
    """Query OMERO and marshal omero.model.Datasets."""
    return query_objects(conn, 'Dataset', project=project,
                         childCount=childCount, group=group, owner=owner,
                         page=page, limit=limit, normalize=normalize)


def query_objects(conn, object_type,
                  project=None,
                  childCount=False,
                  group=None, owner=None,
                  page=1, limit=settings.PAGE,
                  normalize=False):
    """
    Base query method, handles different object_types.

    Builds a query and adds common
    parameters and filters such as by owner or group.

    @param conn:        BlitzGateway
    @param object_type: Type to query. E.g. Project
    @param childCount:  If true, also load Dataset counts as omero:childCount
    @param group:       Filter by group Id
    @param owner:       Filter by owner Id
    @param page:        Pagination page. Default is 1
    @param limit:       Page size
    @param normalize:   If true, marshal groups and experimenters separately
    """
    params = {'page': page,
              'limit': limit,
              'owner': owner,
              'child_count': childCount,
              'order_by': 'name'}

    if object_type == 'Dataset' and project is not None:
        params['project'] = project

    # buildQuery is used by conn.getObjects()
    query, params, wrapper = conn.buildQuery(object_type, params=params)

    # Set the desired group context
    ctx = deepcopy(conn.SERVICE_OPTS)
    if group is None:
        group = -1
    ctx.setOmeroGroup(group)

    qs = conn.getQueryService()

    projects = []
    extras = {}
    if childCount:
        result = qs.projection(query, params, ctx)
        for p in result:
            object = unwrap(p[0])
            projects.append(object)
            extras[object.id.val] = {'omero:childCount': unwrap(p[1])}
    else:
        extras = None
        result = qs.findAllByQuery(query, params, ctx)
        for p in result:
            projects.append(p)

    return marshal_objects(projects, extras=extras, normalize=normalize)
