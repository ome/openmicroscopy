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

import datetime


def parse_permissions_css(permissions, ownerid, conn):
    ''' Parse numeric permissions into a string of space separated
        CSS classes.

        @param permissions Permissions to parse
        @type permissions Integer
        @param ownerid Owner Id for the object having Permissions
        @type ownerId Integer
        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
    '''
    permissions = omero.model.PermissionsI(permissions)
    permissionsCss = []
    if permissions.canEdit():
        permissionsCss.append("canEdit")
    if permissions.canAnnotate():
        permissionsCss.append("canAnnotate")
    if permissions.canLink():
        permissionsCss.append("canLink")
    if permissions.canDelete():
        permissionsCss.append("canDelete")
    if ownerid == conn.getUserId():
        permissionsCss.append("canChgrp")
    return ' '.join(permissionsCss)


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
    q = """
        select project.id,
               dataset.id,
               dataset.name,
               dataset.details.owner.id,
               project.details.permissions.perm1,
               project.details.owner.id,
               (select count(id) from DatasetImageLink dil
                  where dil.parent=dataset.id)
               from ProjectDatasetLink pdlink
               join pdlink.parent project
               join pdlink.child dataset
        where project.id in (%s)
        order by dataset.name
        """ % ','.join((str(x) for x in project_ids))
    for e in qs.projection(q, None, conn.SERVICE_OPTS):
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
               dataset.details.permissions.perm1,
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
               plate.details.permissions.perm1,
               pa.id,
               pa.name,
               pa.details.owner.id,
               pa.details.permissions.perm1,
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
        pa = {}
        pa['id'] = e[5].val
        if e[6] is not None:
            pa['name'] = e[6].val
        else:
            if e[9] is not None and e[10] is not None:
                pa['name'] = "%s - %s" % \
                    (datetime.fromtimestamp(e[9].val/1000),
                     datetime.fromtimestamp(e[10].val/1000))
            else:
                pa['name'] = 'Run %d' % pa['id']
        pa['permsCss'] = parse_permissions_css(e[8].val, e[7].val, conn)
        pa['isOwned'] = e[7].val == conn.getUserId()
        p['plateacquisitions'].append(pa)
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
               plate.details.permissions.perm1,
               pa.id,
               pa.name,
               pa.details.owner.id,
               pa.details.permissions.perm1,
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
        pa = {}
        pa['id'] = e[4].val
        if e[5] is not None:
            pa['name'] = e[5].val
        else:
            if e[8] is not None and e[9] is not None:
                pa['name'] = "%s - %s" % \
                    (datetime.fromtimestamp(e[8].val/1000),
                     datetime.fromtimestamp(e[9].val/1000))
            else:
                pa['name'] = 'Run %d' % pa['id']
        pa['permsCss'] = parse_permissions_css(e[7].val, e[6].val, conn)
        pa['isOwned'] = e[6].val == conn.getUserId()
        p['plateacquisitions'].append(pa)
    # keeping plates ordered
    plates = [plates[x] for x in plateids]
    for p in plates:
        p['plateAcquisitionsCount'] = len(p['plateacquisitions'])
    return plates
