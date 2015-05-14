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


# TODO Remove this when fixed. Workaround for bug:
# https://trac.openmicroscopy.org.uk/ome/ticket/12474
def get_perms(conn, object_type, object_id, object_owner_id, object_group_id,
              cache):

    # Attempt to get permissions which have previously been recorded for this
    # group depending on if the object is owned or not
    perms = cache.get((object_group_id.val, object_owner_id.val))

    # If no cache, query an object to get the permissions for this group and
    # object ownership
    if perms is None:
        params = omero.sys.ParametersI()
        params.addId(object_id)
        q = '''
            select obj from %s obj where obj.id = :id
            ''' % object_type
        qs = conn.getQueryService()
        obj = qs.projection(q,
                            params,
                            conn.SERVICE_OPTS)[0][0].val

        perms_obj = obj.details.permissions

        # To be compatible with parse_permissions_css, convert the required
        # fields to a dictionary
        restrictions = ('canEdit', 'canAnnotate', 'canLink', 'canDelete')
        perms = {}
        for r in restrictions:
            if getattr(perms_obj, r)():
                perms[r] = True
        if (obj.details.owner.id.val == conn.getUserId()):
            perms['isOwned'] = True

        # Cache the result
        cache[(object_group_id.val, object_owner_id.val)] = perms

    return perms


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
    restrictions = ('canEdit', 'canAnnotate', 'canLink', 'canDelete', 'isOwned')
    permissionsCss = [r for r in restrictions if permissions.get(r)]
    if ownerid == conn.getUserId() or conn.isAdmin():
        permissionsCss.append("canChgrp")
    return ' '.join(permissionsCss)


def marshal_plate(conn, row):
    ''' Given a Plate row (list) marshals it into a dictionary.  Order and
        type of columns in row is:
          * id (rlong)
          * name (rstring)
          * details.owner.id (rlong)
          * details.permissions (dict)

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param row The Plate row to marshal
        @type row L{list}
    '''
    plate_id, name, owner_id, permissions = row
    plate = dict()
    plate['id'] = plate_id.val
    plate['name'] = name.val
    plate['permsCss'] = parse_permissions_css(permissions, owner_id.val, conn)
    plate['isOwned'] = owner_id.val == conn.getUserId()
    plate['plateAcquisitions'] = list()
    return plate


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
        @param row The PlateAcquisition row to marshal
        @type row L{list}
    '''
    pa_id, name, owner_id, permissions, start_time, end_time = row
    plate_acquisition = dict()
    plate_acquisition['id'] = pa_id.val
    if name is not None:
        plate_acquisition['name'] = name.val
    elif start_time is not None and end_time is not None:
        start_time = datetime.utcfromtimestamp(start_time.val / 1000.0)
        end_time = datetime.utcfromtimestamp(end_time.val / 1000.0)
        plate_acquisition['name'] = '%s - %s' % (start_time, end_time)
    else:
        plate_acquisition['name'] = 'Run %d' % pa_id.val
    plate_acquisition['permsCss'] = \
        parse_permissions_css(permissions, owner_id.val, conn)
    plate_acquisition['isOwned'] = owner_id.val == conn.getUserId()
    return plate_acquisition


def marshal_dataset(conn, row):
    ''' Given a Dataset row (list) marshals it into a dictionary.  Order
        and type of columns in row is:
          * id (rlong)
          * name (rstring)
          * details.owner.id (rlong)
          * details.permissions (dict)
          * child_count (rlong)

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param row The Dataset row to marshal
        @type row L{list}
    '''
    dataset_id, name, owner_id, permissions, child_count = row
    dataset = dict()
    dataset['id'] = dataset_id.val
    dataset['name'] = name.val
    dataset['isOwned'] = owner_id.val == conn.getUserId()
    dataset['childCount'] = child_count.val
    dataset['permsCss'] = \
        parse_permissions_css(permissions, owner_id.val, conn)
    return dataset


def marshal_projects(conn, experimenter_id):
    ''' Marshals projects and contained datasets for a given user.

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param experimenter_id The Experimenter (user) ID to marshal
        Projects for or `None` if we are not to filter by a specific user.
        @type experimenter_id L{long}
    '''
    projects = list()
    query_service = conn.getQueryService()
    params = omero.sys.ParametersI()
    where_clause = ''
    if experimenter_id is not None:
        params.addId(experimenter_id)
        where_clause = 'where project.details.owner.id = :id'

    # TODO Remove group.ids when fixed. Workaround for bug:
    # https://trac.openmicroscopy.org.uk/ome/ticket/12474
    q = """
        select project.id,
               project.name,
               project.details.owner.id,
               project.details.group.id,
               project.details.permissions,
               dataset.id,
               dataset.name,
               dataset.details.owner.id,
               dataset.details.group.id,
               dataset.details.permissions,
               (select count(id) from DatasetImageLink dil
                  where dil.parent = dataset.id)
               from Project as project
               left join project.datasetLinks as pdlink
               left join pdlink.child dataset
        %s
        order by lower(project.name), project.id, lower(dataset.name)
        """ % (where_clause)

    # TODO Remove this when fixed. Workaround for bug:
    # https://trac.openmicroscopy.org.uk/ome/ticket/12474
    cache = {}

    for row in query_service.projection(q, params, conn.SERVICE_OPTS):
        project_id, project_name, project_owner_id, project_group_id, \
            project_permissions, dataset_id, dataset_name, dataset_owner_id, \
            dataset_group_id, dataset_permissions, child_count = row

        # TODO Remove this when fixed. Workaround for bug:
        # https://trac.openmicroscopy.org.uk/ome/ticket/12474
        project_permissions = get_perms(conn, 'Project', project_id,
                                        project_owner_id,
                                        project_group_id, cache)

        if len(projects) == 0 or projects[-1]['id'] != project_id.val:
            is_owned = experimenter_id == conn.getUserId()
            perms_css = parse_permissions_css(
                project_permissions, experimenter_id, conn
            )
            project = {
                'id': project_id.val, 'name': project_name.val,
                'isOwned': is_owned, 'permsCss': perms_css, 'datasets': list()
            }
            projects.append(project)

        project = projects[-1]
        if dataset_id is not None:

            # TODO Remove this when fixed. Workaround for bug:
            # https://trac.openmicroscopy.org.uk/ome/ticket/12474
            dataset_permissions = get_perms(conn, 'Dataset', dataset_id,
                                            dataset_owner_id,
                                            dataset_group_id, cache)

            project['datasets'].append(
                marshal_dataset(conn, (dataset_id,
                                       dataset_name,
                                       dataset_owner_id,
                                       dataset_permissions,
                                       child_count)))

        project['childCount'] = len(project['datasets'])
    return projects


def marshal_datasets(conn, experimenter_id):
    ''' Marshal datasets for a given user.

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param experimenter_id The Experimenter (user) ID to marshal
        Datasets for or `None` if we are not to filter by a specific user.
        @type experimenter_id L{long}
    '''
    datasets = []
    params = omero.sys.ParametersI()
    where_clause = ''
    if experimenter_id is not None:
        params.addId(experimenter_id)
        where_clause = 'and dataset.details.owner.id = :id'
    qs = conn.getQueryService()
    # TODO Remove group.ids when fixed. Workaround for bug:
    # https://trac.openmicroscopy.org.uk/ome/ticket/12474
    q = """
        select dataset.id,
               dataset.name,
               dataset.details.owner.id,
               dataset.details.group.id,
               dataset.details.permissions,
               (select count(id) from DatasetImageLink dil
                 where dil.parent=dataset.id)
               from Dataset dataset
        where not exists (
            select pdlink from ProjectDatasetLink as pdlink
            where pdlink.child = dataset.id
        ) %s
        order by lower(dataset.name)
        """ % (where_clause)

    # TODO Remove this when fixed. Workaround for bug:
    # https://trac.openmicroscopy.org.uk/ome/ticket/12474
    cache = {}

    for e in qs.projection(q, params, conn.SERVICE_OPTS):
        # TODO Revert this when fixed. Workaround for bug:
        # https://trac.openmicroscopy.org.uk/ome/ticket/12474
        # datasets.append(marshal_dataset(conn, e[0:5]))
        dataset_id, name, owner_id, group_id, permissions, child_count = e[0:6]
        permissions = get_perms(conn, 'Dataset', dataset_id, owner_id,
                                group_id, cache)
        datasets.append(marshal_dataset(conn, (dataset_id,
                                               name,
                                               owner_id,
                                               permissions,
                                               child_count)))
    return datasets


def marshal_screens(conn, experimenter_id=None):
    ''' Marshals screens and contained plates and aquisitions for a given user.

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param experimenter_id The Experimenter (user) ID to marshal
        Screens for or `None` if we are not to filter by a specific user.
        @type experimenter_id L{long}
    '''
    screens = list()
    query_service = conn.getQueryService()
    params = omero.sys.ParametersI()
    where_clause = ''
    if experimenter_id is not None:
        params.addId(experimenter_id)
        where_clause = 'where screen.details.owner.id = :id'
    # TODO Remove group.ids when fixed. Workaround for bug:
    # https://trac.openmicroscopy.org.uk/ome/ticket/12474
    q = """
        select screen.id,
               screen.name,
               screen.details.owner.id,
               screen.details.group.id,
               screen.details.permissions,
               plate.id,
               plate.name,
               plate.details.owner.id,
               plate.details.group.id,
               plate.details.permissions,
               pa.id,
               pa.name,
               pa.details.owner.id,
               pa.details.group.id,
               pa.details.permissions,
               pa.startTime,
               pa.endTime
               from Screen screen
               left join screen.plateLinks splink
               left join splink.child plate
               left join plate.plateAcquisitions pa
        %s
        order by lower(screen.name), screen.id, lower(plate.name), pa.id
        """ % (where_clause)

    # TODO Remove this when fixed. Workaround for bug:
    # https://trac.openmicroscopy.org.uk/ome/ticket/12474
    cache = {}

    for row in query_service.projection(q, params, conn.SERVICE_OPTS):
        screen_id, screen_name, screen_owner_id, screen_group_id, \
            screen_permissions, plate_id, plate_name, plate_owner_id, \
            plate_group_id, plate_permissions, acquisition_id, \
            acquisition_name, acquisition_owner_id, acquisition_group_id, \
            acquisition_permissions, acquisition_start_time, \
            acquisition_end_time = row

        # TODO Remove this when fixed. Workaround for bug:
        # https://trac.openmicroscopy.org.uk/ome/ticket/12474
        screen_permissions = get_perms(conn, 'Screen', screen_id,
                                       screen_owner_id, screen_group_id, cache)

        if len(screens) == 0 or screen_id.val != screens[-1]['id']:
            is_owned = screen_owner_id.val == conn.getUserId()
            perms_css = parse_permissions_css(
                screen_permissions, screen_owner_id.val, conn
            )
            screen = {
                'id': screen_id.val, 'name': screen_name.val,
                'isOwned': is_owned, 'permsCss': perms_css,
                'plates': list()
            }
            screens.append(screen)
        screen = screens[-1]
        if plate_id is not None and (
                len(screen['plates']) == 0 or
                screen['plates'][-1]['id'] != plate_id.val
                ):

            # TODO Remove this when fixed. Workaround for bug:
            # https://trac.openmicroscopy.org.uk/ome/ticket/12474
            plate_permissions = get_perms(conn, 'Plate', plate_id,
                                          plate_owner_id, plate_group_id,
                                          cache)

            screen['plates'].append(marshal_plate(conn, (plate_id,
                                                         plate_name,
                                                         plate_owner_id,
                                                         plate_permissions)))

            screen['plates'][-1]['plateAcquisitionCount'] = 0
        if acquisition_id is not None:
            plate = screen['plates'][-1]

            # TODO Remove this when fixed. Workaround for bug:
            # https://trac.openmicroscopy.org.uk/ome/ticket/12474
            acquisition_permissions = get_perms(conn, 'PlateAcquisition',
                                                acquisition_id,
                                                acquisition_owner_id,
                                                acquisition_group_id,
                                                cache)

            plate['plateAcquisitions'].append(
                marshal_plate_acquisition(conn, (acquisition_id,
                                                 acquisition_name,
                                                 acquisition_owner_id,
                                                 acquisition_permissions,
                                                 acquisition_start_time,
                                                 acquisition_end_time)))

            plate['plateAcquisitionCount'] = len(plate['plateAcquisitions'])
        screen['childCount'] = len(screen['plates'])
    return screens


def marshal_plates(conn, experimenter_id):
    ''' Marshal plates for a given user.

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param experimenter_id The Experimenter (user) ID to marshal
        Plates for or `None` if we are not to filter by a specific user.
        @type experimenter_id L{long}
    '''
    plates = list()
    params = omero.sys.ParametersI()
    where_clause = ''
    if experimenter_id is not None:
        params.addId(experimenter_id)
        where_clause = 'and plate.details.owner.id = :id'
    query_service = conn.getQueryService()
    # TODO Remove group.ids when fixed. Workaround for bug:
    # https://trac.openmicroscopy.org.uk/ome/ticket/12474
    q = """
        select plate.id,
               plate.name,
               plate.details.owner.id,
               plate.details.group.id,
               plate.details.permissions,
               pa.id,
               pa.name,
               pa.details.owner.id,
               pa.details.group.id,
               pa.details.permissions,
               pa.startTime,
               pa.endTime
               from Plate plate
               left join plate.plateAcquisitions pa
        where not exists (
            select splink from ScreenPlateLink as splink
            where splink.child = plate.id
        ) %s
        order by lower(plate.name), plate.id, pa.id
        """ % (where_clause)

    # TODO Remove this when fixed. Workaround for bug:
    # https://trac.openmicroscopy.org.uk/ome/ticket/12474
    cache = {}

    for row in query_service.projection(q, params, conn.SERVICE_OPTS):
        plate_id, plate_name, plate_owner_id, plate_group_id, \
            plate_permissions, acquisition_id, acquisition_name, \
            acquisition_owner_id, acquisition_group_id, \
            acquisition_permissions, acquisition_start_time, \
            acquisition_end_time = row
        if len(plates) == 0 or plate_id.val != plates[-1]['id']:
            # TODO Remove this when fixed. Workaround for bug:
            # https://trac.openmicroscopy.org.uk/ome/ticket/12474
            plate_permissions = get_perms(conn, 'Plate', plate_id,
                                          plate_owner_id, plate_group_id,
                                          cache)

            plates.append(
                marshal_plate(conn, (plate_id,
                                     plate_name,
                                     plate_owner_id,
                                     plate_permissions)))

            plates[-1]['plateAcquisitionCount'] = 0
        plate = plates[-1]
        if acquisition_id is not None:
            # TODO Remove this when fixed. Workaround for bug:
            # https://trac.openmicroscopy.org.uk/ome/ticket/12474
            acquisition_permissions = get_perms(conn, 'PlateAcquisition',
                                                acquisition_id,
                                                acquisition_owner_id,
                                                acquisition_group_id,
                                                cache)

            plate['plateAcquisitions'].append(
                marshal_plate_acquisition(conn, (acquisition_id,
                                                 acquisition_name,
                                                 acquisition_owner_id,
                                                 acquisition_permissions,
                                                 acquisition_start_time,
                                                 acquisition_end_time)))

            plate['plateAcquisitionCount'] = len(plate['plateAcquisitions'])
    return plates
