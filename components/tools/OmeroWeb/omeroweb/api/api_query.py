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


from omero.rtypes import unwrap, wrap
from omero.sys import ParametersI
from . import api_settings

from api_marshal import marshal_objects
from copy import deepcopy


MAX_LIMIT = max(1, api_settings.API_MAX_LIMIT)
DEFAULT_LIMIT = max(1, api_settings.API_LIMIT)


def get_child_counts(conn, link_class, parent_ids):
    """
    Count child links for the specified parent_ids.

    @param conn:        BlitzGateway
    @param link_class:  Type of link, e.g. 'ProjectDatasetLink'
    @param parent_ids:  List of Parent IDs
    @return             A dict of parent_id: child_count
    """
    ctx = deepcopy(conn.SERVICE_OPTS)
    ctx.setOmeroGroup(-1)
    params = ParametersI()
    params.add('ids', wrap(parent_ids))
    query = ("select chl.parent.id, count(chl.id) from %s chl"
             " where chl.parent.id in (:ids) group by chl.parent.id"
             % link_class)
    result = conn.getQueryService().projection(query, params, ctx)
    counts = {}
    for d in result:
        counts[d[0].val] = unwrap(d[1])
    return counts


def validate_opts(opts):
    """Check that opts dict has valid 'limit' and 'offset'."""
    if opts is None:
        opts = {}
    if opts.get('limit') is None or opts.get('limit') < 0:
        opts['limit'] = DEFAULT_LIMIT
    opts['limit'] = min(opts['limit'], MAX_LIMIT)
    if opts.get('offset') is None or opts.get('offset') < 0:
        opts['offset'] = 0
    return opts


def query_objects(conn, object_type,
                  group=None,
                  opts=None,
                  normalize=False):
    """
    Base query method, handles different object_types.

    Builds a query and adds common
    parameters and filters such as by owner or group.

    @param conn:        BlitzGateway
    @param object_type: Type to query, e.g. Project
    @param group:       Filter query by ExperimenterGroup ID
    @param opts:        Options dict for conn.buildQuery()
    @param normalize:   If true, marshal groups and experimenters separately
    """
    opts = validate_opts(opts)
    # buildQuery is used by conn.getObjects()
    query, params, wrapper = conn.buildQuery(object_type, opts=opts)
    # Set the desired group context
    ctx = deepcopy(conn.SERVICE_OPTS)
    if group is None:
        group = -1
    ctx.setOmeroGroup(group)

    qs = conn.getQueryService()

    objects = []
    extras = {}

    if opts['limit'] == 0:
        result = []
    else:
        result = qs.findAllByQuery(query, params, ctx)
    for obj in result:
        objects.append(obj)

    # Optionally get child counts...
    if opts and opts.get('child_count') and wrapper.LINK_CLASS:
        obj_ids = [r.id.val for r in result]
        counts = get_child_counts(conn, wrapper.LINK_CLASS, obj_ids)
        for obj_id in obj_ids:
            count = counts[obj_id] if obj_id in counts else 0
            extras[obj_id] = {'omero:childCount': count}

    # Query the count() of objects & add to 'meta' dict
    count_query, params = conn.buildCountQuery(object_type, opts=opts)
    result = qs.projection(count_query, params, ctx)

    meta = {}
    meta['offset'] = opts['offset']
    meta['limit'] = opts['limit']
    meta['maxLimit'] = MAX_LIMIT
    meta['totalCount'] = result[0][0].val

    marshalled = marshal_objects(objects, extras=extras, normalize=normalize)
    marshalled['meta'] = meta
    return marshalled
