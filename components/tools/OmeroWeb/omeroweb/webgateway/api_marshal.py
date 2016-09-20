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


from omero_marshal import get_encoder


def normalize_objects(objects):
    """
    Takes a list of dicts generated from omero_marshal and
    normalizes the groups and owners into separate
    lists. omero:details will only retain group and owner IDs.
    """
    experimenters = {}
    groups = {}
    objs = []
    for o in objects:
        exp = o['omero:details']['owner']
        experimenters[exp['@id']] = exp
        o['omero:details']['owner'] = {'@id': exp['@id']}
        grp = o['omero:details']['group']
        groups[grp['@id']] = grp
        o['omero:details']['group'] = {'@id': grp['@id']}
        objs.append(o)
    experimenters = experimenters.values()
    groups = groups.values()
    return objs, {'experimenters': experimenters, 'experimenterGroups': groups}


def marshal_objects(objects, extras=None, normalize=False):
    """
    Marshals a list of OMERO.model objects using omero_marshal

    @param extras:      A list of dicts to add extra data to each object in turn
    @param normalize:   If true, normalize groups and owners into separate lists
    """

    marshalled = []
    for i, o in enumerate(objects):
        encoder = get_encoder(o.__class__)
        m = encoder.encode(o)
        if extras is not None and i < len(extras):
            m.update(extras[i])
        marshalled.append(m)

    if not normalize:
        return {'data': marshalled}
    data, extra_objs = normalize_objects(marshalled)
    extra_objs['data'] = data
    return extra_objs
