#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2008-2015 University of Dundee & Open Microscopy Environment.
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

# import time
import omero

from omero.rtypes import unwrap, rlong   # wrap   # rlong
from django.conf import settings
# from django.http import Http404
# from datetime import datetime
from copy import deepcopy


def build_clause(components, name='', join=','):
    ''' Build a string from a list of components.
        This is to simplify building where clauses in particular that
        may optionally have zero, one or more parts
    '''
    if not components:
        return ''

    return ' ' + name + ' ' + (' ' + join + ' ').join(components) + ' '


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
    restrictions = ('canEdit',
                    'canAnnotate',
                    'canLink',
                    'canDelete')
    permissionsCss = [r for r in restrictions if permissions.get(r)]
    if ownerid == conn.getUserId():
        permissionsCss.append("isOwned")
    if ownerid == conn.getUserId() or conn.isAdmin():
        permissionsCss.append("canChgrp")
    return ' '.join(permissionsCss)


def _marshal_project(conn, row):
    ''' Given a Project row (list) marshals it into a dictionary.  Order
        and type of columns in row is:
          * id (rlong)
          * name (rstring)
          * details.owner.id (rlong)
          * details.permissions (dict)
          * child_count (rlong)

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param row The Project row to marshal
        @type row L{list}
    '''
    project_id, name, owner_id, permissions, child_count = row
    project = dict()
    project['id'] = unwrap(project_id)
    project['name'] = unwrap(name)
    project['ownerId'] = unwrap(owner_id)
    project['childCount'] = unwrap(child_count)
    project['permsCss'] = \
        parse_permissions_css(permissions, unwrap(owner_id), conn)
    return project


def marshal_projects(conn, group_id=-1, experimenter_id=-1,
                     page=1, limit=settings.PAGE):
    ''' Marshals projects

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param group_id The Group ID to filter by or -1 for all groups,
        defaults to -1
        @type group_id L{long}
        @param experimenter_id The Experimenter (user) ID to filter by
        or -1 for all experimenters
        @type experimenter_id L{long}
        @param page Page number of results to get. `None` or 0 for no paging
        defaults to 1
        @type page L{long}
        @param limit The limit of results per page to get
        defaults to the value set in settings.PAGE
        @type page L{long}
    '''
    projects = []
    params = omero.sys.ParametersI()
    service_opts = deepcopy(conn.SERVICE_OPTS)

    # Set the desired group context
    if group_id is None:
        group_id = -1
    service_opts.setOmeroGroup(group_id)

    # Paging
    if page is not None and page > 0:
        params.page((page-1) * limit, limit)

    where_clause = ''
    if experimenter_id is not None and experimenter_id != -1:
        params.addId(experimenter_id)
        where_clause = 'where project.details.owner.id = :id'
    qs = conn.getQueryService()

    q = """
        select new map(project.id as id,
               project.name as name,
               project.details.owner.id as ownerId,
               project as project_details_permissions,
               (select count(id) from ProjectDatasetLink pdl
                where pdl.parent = project.id) as childCount)
        from Project project
        %s
        order by lower(project.name), project.id
        """ % (where_clause)

    for e in qs.projection(q, params, service_opts):
        e = unwrap(e)
        e = [e[0]["id"], e[0]["name"], e[0]["ownerId"],
             e[0]["project_details_permissions"], e[0]["childCount"]]
        projects.append(_marshal_project(conn, e[0:5]))
    return projects


def _marshal_dataset(conn, row):
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
    dataset['id'] = unwrap(dataset_id)
    dataset['name'] = unwrap(name)
    dataset['ownerId'] = unwrap(owner_id)
    dataset['childCount'] = unwrap(child_count)
    dataset['permsCss'] = \
        parse_permissions_css(permissions, unwrap(owner_id), conn)
    return dataset


def marshal_datasets(conn, project_id=None, orphaned=False, group_id=-1,
                     experimenter_id=-1, page=1, limit=settings.PAGE):
    ''' Marshals datasets

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param project_id The Project ID to filter by or `None` to
        not filter by a specific project.
        defaults to `None`
        @type project_id L{long}
        @param orphaned If this is to filter by orphaned data. Overridden
        by project_id.
        defaults to False
        @type orphaned Boolean
        @param group_id The Group ID to filter by or -1 for all groups,
        defaults to -1
        @type group_id L{long}
        @param experimenter_id The Experimenter (user) ID to filter by
        or -1 for all experimenters
        @type experimenter_id L{long}
        @param page Page number of results to get. `None` or 0 for no paging
        defaults to 1
        @type page L{long}
        @param limit The limit of results per page to get
        defaults to the value set in settings.PAGE
        @type page L{long}
    '''
    datasets = []
    params = omero.sys.ParametersI()
    service_opts = deepcopy(conn.SERVICE_OPTS)

    # Set the desired group context
    if group_id is None:
        group_id = -1
    service_opts.setOmeroGroup(group_id)

    # Paging
    if page is not None and page > 0:
        params.page((page-1) * limit, limit)

    where_clause = []
    if experimenter_id is not None and experimenter_id != -1:
        params.addId(experimenter_id)
        where_clause.append('dataset.details.owner.id = :id')

    qs = conn.getQueryService()
    q = """
        select new map(dataset.id as id,
               dataset.name as name,
               dataset.details.owner.id as ownerId,
               dataset as dataset_details_permissions,
               (select count(id) from DatasetImageLink dil
                 where dil.parent=dataset.id) as childCount)
               from Dataset dataset
        """

    # If this is a query to get datasets from a parent project
    if project_id:
        params.add('pid', rlong(project_id))
        q += 'join dataset.projectLinks plink'
        where_clause.append('plink.parent.id = :pid')

    # If this is a query to get datasets with no parent project
    elif orphaned:
        where_clause.append(
            """
            not exists (
                select pdlink from ProjectDatasetLink as pdlink
                where pdlink.child = dataset.id
            )
            """
        )

    q += """
        %s
        order by lower(dataset.name), dataset.id
        """ % build_clause(where_clause, 'where', 'and')

    for e in qs.projection(q, params, service_opts):
        e = unwrap(e)
        e = [e[0]["id"],
             e[0]["name"],
             e[0]["ownerId"],
             e[0]["dataset_details_permissions"],
             e[0]["childCount"]]
        datasets.append(_marshal_dataset(conn, e[0:5]))
    return datasets
