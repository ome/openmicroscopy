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


def marshal_projects(projects, extras=None, normalize=False):

    marshalled = []
    for i, project in enumerate(projects):
        encoder = get_encoder(project.__class__)
        p = encoder.encode(project)
        if extras is not None and i < len(extras):
            p.update(extras[i])
        marshalled.append(p)

    if not normalize:
        return {'data': marshalled}
    projects, objects = normalize_objects(marshalled)
    objects['data'] = projects
    return objects
