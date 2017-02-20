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
from omero.sys import ParametersI
from omero.rtypes import wrap


def get_wellsample_indices(conn, plate_id=None, plateacquisition_id=None):
    """
    Return min and max WellSample index for a Plate OR PlateAcquisition

    @param conn:        BlitzGateway
    @param plate_id:    Plate ID
    @param plateacquisition_id:    PlateAcquisition ID
    @return             A dict of parent_id: child_count
    """
    ctx = deepcopy(conn.SERVICE_OPTS)
    ctx.setOmeroGroup(-1)
    params = ParametersI()
    query = "select minIndex(ws), maxIndex(ws) from Well well " \
            "join well.wellSamples ws"
    if plate_id is not None:
        query += " where well.plate.id=:plate_id "
        params.add('plate_id', wrap(plate_id))
    elif plateacquisition_id is not None:
        query += " where ws.plateAcquisition.id=:plateacquisition_id"
        params.add('plateacquisition_id', wrap(plateacquisition_id))
    result = conn.getQueryService().projection(query, params, ctx)
    result = [r for r in unwrap(result)[0] if r is not None]
    return result


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

    return marshal_objects(objects, extras=extras, normalize=normalize)
