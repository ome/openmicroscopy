#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2008-2014 University of Dundee & Open Microscopy Environment.
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

from datetime import datetime


def parse_permissions_css(permissions, ownerid, conn):
    ''' Parse numeric permissions into a string of space separated
        CSS classes.

        @param permissions Permissions to parse
        @type permissions L{omero.rtypes.rmap}
        @param ownerid Owner Id for the object having Permissions
        @type ownerId Integer
        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
    '''

    restrictions = ('canEdit', 'canAnnotate', 'canLink', 'canDelete')
    permissionsCss = [r for r in restrictions if permissions.get(r)]
    if ownerid == conn.getUserId():
        permissionsCss.append("canChgrp")
    return ' '.join(permissionsCss)


def marshal_plate_acquisition(conn, row):
    ''' Given a PlateAcquisition row (list) marshals it into a
        dictionary.  Order and type of columns in row is:
          * id (rlong)
          * name (rstring)
          * details.owner.id (rlong)
          * details.permissions (dict)
          * startTime (rtime)
          * endTime (rtime)

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param row The ProjectAcquisition row to marshal
        @type row L{list}
    '''
    pa_id, name, owner_id, permissions, start_time, end_time = row
    plate_acquisition = dict()
    plate_acquisition['id'] = pa_id.val
    if name is not None:
        plate_acquisition['name'] = name.val
    elif start_time is not None and end_time is not None:
        start_time = datetime.fromtimestamp(start_time.val / 1000.0)
        end_time = datetime.fromtimestamp(end_time.val / 1000.0)
        plate_acquisition['name'] = '%s - %s' % (start_time, end_time)
    else:
        plate_acquisition['name'] = 'Run %d' % pa_id.val
    plate_acquisition['permsCss'] = \
        parse_permissions_css(permissions, owner_id.val, conn)
    plate_acquisition['isOwned'] = owner_id.val == conn.getUserId()
    return plate_acquisition


def marshal_datasets_for_projects(conn, project_ids):
    ''' Given a list of project ids, marshals the contained datasets, grouping
        by parent project.

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param project_ids The Project IDs to marshal
        @type project_ids list of longs
    '''
    if len(project_ids) == 0:
        return {}
    projects = {}
    qs = conn.getQueryService()
    params = omero.sys.ParametersI()
    params.addIds(project_ids)
    q = """
        select project.id,
               dataset.id,
               dataset.name,
               dataset.details.owner.id,
               project.details.permissions,
               project.details.owner.id,
               (select count(id) from DatasetImageLink dil
                  where dil.parent=dataset.id)
               from ProjectDatasetLink pdlink
               join pdlink.parent project
               join pdlink.child dataset
        where project.id in (:ids)
        order by dataset.name
        """
    for e in qs.projection(q, params, conn.SERVICE_OPTS):
        p = projects.setdefault(e[0].val, {'datasets': []})
        if 'permsCss' not in p:
            p['permsCss'] = parse_permissions_css(e[4].val, e[5].val, conn)
        d = {}
        d['id'] = e[1].val
        d['name'] = e[2].val
        d['isOwned'] = e[3].val == conn.getUserId()
        d['childCount'] = e[6].val
        p['datasets'].append(d)
    for p in projects.keys():
        projects[p]['childCount'] = len(projects[p]['datasets'])
    return projects


def marshal_datasets(conn, dataset_ids):
    ''' Marshal datasets with ids matching dataset_ids.

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param dataset_ids The dataset IDs to marshal
        @type dataset_ids list of longs
    '''
    if len(dataset_ids) == 0:
        return []
    datasets = []
    qs = conn.getQueryService()
    q = """
        select dataset.id,
               dataset.name,
               dataset.details.permissions,
               dataset.details.owner.id,
               (select count(id) from DatasetImageLink dil
                 where dil.parent=dataset.id)
               from Dataset dataset
        where dataset.id in (%s)
        order by dataset.name
        """ % ','.join((str(x) for x in dataset_ids))
    for e in qs.projection(q, None, conn.SERVICE_OPTS):
        d = {}
        d['id'] = e[0].val
        d['name'] = e[1].val
        d['isOwned'] = e[3].val == conn.getUserId()
        d['childCount'] = e[4].val
        d['permsCss'] = parse_permissions_css(e[2].val, e[3].val, conn)
        datasets.append(d)
    return datasets


def marshal_plates_for_screens(conn, screen_ids):
    ''' Given a list of screen ids, marshals the contained plates, grouping
        by parent screen.

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param screen_ids The screen IDs to marshal
        @type screen_ids list of longs
    '''
    if len(screen_ids) == 0:
        return {}
    screens = {}
    qs = conn.getQueryService()
    q = """
        select screen.id,
               plate.id,
               plate.name,
               plate.details.owner.id,
               plate.details.permissions,
               pa.id,
               pa.name,
               pa.details.owner.id,
               pa.details.permissions,
               pa.startTime,
               pa.endTime
               from Screen screen
               join screen.plateLinks splink
               join splink.child plate
               join plate.plateAcquisitions pa
        where screen.id in (%s)
        order by screen.name, plate.name, pa.id
        """ % ','.join((str(x) for x in screen_ids))
    for e in qs.projection(q, None, conn.SERVICE_OPTS):
        s = screens.setdefault(e[0].val, {'plateids': [], 'plates': {}})
        pid = e[1].val
        if pid in s['plateids']:
            p = s['plates'][pid]
        else:
            s['plateids'].append(pid)
            p = {}
            s['plates'][pid] = p
            p['permsCss'] = parse_permissions_css(e[4].val, e[3].val, conn)
            p['isOwned'] = e[3].val == conn.getUserId()
            p['id'] = e[1].val
            p['name'] = e[2].val
            p['plateacquisitions'] = []
        p['plateacquisitions'].append(
            marshal_plate_acquisition(conn, e[5:11])
        )
    for s in screens.keys():
        screens[s]['childCount'] = len(screens[s]['plates'])
        # keeping plates ordered
        screens[s]['plates'] = [screens[s]['plates'][x]
                                for x in screens[s]['plateids']]
        for p in screens[s]['plates']:
            p['plateAcquisitionsCount'] = len(p['plateacquisitions'])
    return screens


def marshal_plates(conn, plate_ids):
    ''' Marshal plates with ids matching plate_ids.

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param plate_ids The plate IDs to marshal
        @type plate_ids list of longs
    '''
    if len(plate_ids) == 0:
        return []
    plates = {}
    plateids = []
    qs = conn.getQueryService()
    q = """
        select plate.id,
               plate.name,
               plate.details.owner.id,
               plate.details.permissions,
               pa.id,
               pa.name,
               pa.details.owner.id,
               pa.details.permissions,
               pa.startTime,
               pa.endTime
               from Plate plate
               join plate.plateAcquisitions pa
        where plate.id in (%s)
        order by plate.name, pa.id
        """ % ','.join((str(x) for x in plate_ids))
    for e in qs.projection(q, None, conn.SERVICE_OPTS):
        pid = e[0].val
        if pid in plateids:
            p = plates[pid]
        else:
            plateids.append(pid)
            p = {}
            plates[pid] = p
            p['permsCss'] = parse_permissions_css(e[3].val, e[2].val, conn)
            p['isOwned'] = e[2].val == conn.getUserId()
            p['id'] = pid
            p['name'] = e[1].val
            p['plateacquisitions'] = []
        p['plateacquisitions'].append(
            marshal_plate_acquisition(conn, e[4:10])
        )
    # keeping plates ordered
    plates = [plates[x] for x in plateids]
    for p in plates:
        p['plateAcquisitionsCount'] = len(p['plateacquisitions'])
    return plates
