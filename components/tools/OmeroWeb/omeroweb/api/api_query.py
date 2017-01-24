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

from api_marshal import marshal_objects
from copy import deepcopy
from time import time


def query_objects(conn, object_type,
                  group=None,
                  opts=None,
                  normalize=False):
    """
    Base query method, handles different object_types.

    Builds a query and adds common
    parameters and filters such as by owner or group.

    @param conn:        BlitzGateway
    @param object_type: Type to query. E.g. Project
    @param group:       Filter query by ExperimenterGroup ID
    @param opts:        Options dict for conn.buildQuery()
    @param normalize:   If true, marshal groups and experimenters separately
    """
    # buildQuery is used by conn.getObjects()
    query, params, wrapper = conn.buildQuery(object_type, opts=opts)
    print query
    # Set the desired group context
    ctx = deepcopy(conn.SERVICE_OPTS)
    if group is None:
        group = -1
    ctx.setOmeroGroup(group)

    qs = conn.getQueryService()

    objects = []
    extras = {}

    t_start = time()
    
    if opts is not None and opts.get('child_count'):
        result = qs.projection(query, params, ctx)
        for p in result:
            obj = unwrap(p[0])
            objects.append(obj)
            if len(p) > 1:
                # in case child_count not supported by conn.buildQuery()
                extras[obj.id.val] = {'omero:childCount': unwrap(p[1])}
        print 'Combined query took:', time() - t_start
    else:
        # extras = None
        result = qs.findAllByQuery(query, params, ctx)
        for obj in result:
            objects.append(obj)
        print 'first queriy...', time() - t_start
        obj_ids = [r.id.val for r in result]
        print 'obj_ids', len(obj_ids)
        params = ParametersI()
        params.add('ids', wrap(obj_ids))
        query = "select d.id, size(l) from Dataset d join d.imageLinks as l where d.id in (:ids) group by d.id"
        result = qs.projection(query, params, ctx)
        print 'found', len(result)
        for d in result:
            extras[d[0].val] = {'omero:childCount': unwrap(d[1])}

        print 'Separate queries took:', time() - t_start

    return marshal_objects(objects, extras=extras, normalize=normalize)
