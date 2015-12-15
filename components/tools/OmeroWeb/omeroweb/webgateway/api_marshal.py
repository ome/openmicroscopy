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

from omero.rtypes import unwrap, rlong, wrap
from django.conf import settings
# from django.http import Http404
# from datetime import datetime
from copy import deepcopy
from datetime import datetime


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
    if page:
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
    if page:
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


def _marshal_date(time):
    d = datetime.fromtimestamp(time/1000)
    return d.isoformat() + 'Z'


def _marshal_image(conn, row, row_pixels=None, share_id=None,
                   date=None, acqDate=None, thumbVersion=None):
    ''' Given an Image row (list) marshals it into a dictionary.  Order
        and type of columns in row is:
          * id (rlong)
          * name (rstring)
          * details.owner.id (rlong)
          * details.permissions (dict)
          * fileset_id (rlong)

        May also take a row_pixels (list) if X,Y,Z dimensions are loaded
          * pixels.sizeX (rlong)
          * pixels.sizeY (rlong)
          * pixels.sizeZ (rlong)

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param row The Image row to marshal
        @type row L{list}
        @param row_pixels The Image row pixels data to marshal
        @type row_pixels L{list}
    '''
    image_id, name, owner_id, permissions, fileset_id = row
    image = dict()
    image['id'] = unwrap(image_id)
    image['name'] = unwrap(name)
    image['ownerId'] = unwrap(owner_id)
    image['permsCss'] = parse_permissions_css(permissions,
                                              unwrap(owner_id), conn)
    fileset_id_val = unwrap(fileset_id)
    if fileset_id_val is not None:
        image['filesetId'] = fileset_id_val
    if row_pixels:
        sizeX, sizeY, sizeZ = row_pixels
        image['sizeX'] = unwrap(sizeX)
        image['sizeY'] = unwrap(sizeY)
        image['sizeZ'] = unwrap(sizeZ)
    if share_id is not None:
        image['shareId'] = share_id
    if date is not None:
        image['date'] = _marshal_date(unwrap(date))
    if acqDate is not None:
        image['acqDate'] = _marshal_date(unwrap(acqDate))
    if thumbVersion is not None:
        image['thumbVersion'] = thumbVersion
    return image


def _marshal_image_deleted(conn, image_id):
    ''' Given an Image id and marshals it into a dictionary.

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param image_id The image id to marshal
        @type image_id L{long}
    '''
    return {
        'id': unwrap(image_id),
        'deleted': True
    }


def marshal_images(conn, dataset_id=None, orphaned=False, share_id=None,
                   load_pixels=False, group_id=-1, experimenter_id=-1,
                   page=1, date=False, thumb_version=False,
                   limit=settings.PAGE):

    ''' Marshals images

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param dataset_id The Dataset ID to filter by or `None` to
        not filter by a specific dataset.
        defaults to `None`
        @type dataset_id L{long}
        @param orphaned If this is to filter by orphaned data. Overridden
        by dataset_id.
        defaults to False
        @type orphaned Boolean
        @param share_id The Share ID to filter by or `None` to
        not filter by a specific share.
        defaults to `None`
        @type share_id L{long}
        @param load_pixels Whether to load the X,Y,Z dimensions
        @type load_pixels Boolean
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
    images = []
    params = omero.sys.ParametersI()
    service_opts = deepcopy(conn.SERVICE_OPTS)

    # Set the desired group context
    if group_id is None:
        group_id = -1
    service_opts.setOmeroGroup(group_id)

    # Paging
    if page:
        params.page((page-1) * limit, limit)

    from_join_clauses = []
    where_clause = []
    if experimenter_id is not None and experimenter_id != -1:
        params.addId(experimenter_id)
        where_clause.append('image.details.owner.id = :id')
    qs = conn.getQueryService()

    extraValues = ""
    if load_pixels:
        extraValues = """
             ,
             pix.sizeX as sizeX,
             pix.sizeY as sizeY,
             pix.sizeZ as sizeZ
             """

    if date:
        extraValues += """,
            image.details.creationEvent.time as date,
            image.acquisitionDate as acqDate
            """

    q = """
        select new map(image.id as id,
               image.name as name,
               image.details.owner.id as ownerId,
               image as image_details_permissions,
               image.fileset.id as filesetId %s)
        """ % extraValues

    from_join_clauses.append('Image image')

    if load_pixels:
        # We use 'left outer join', since we still want images if no pixels
        from_join_clauses.append('left outer join image.pixels pix')

    # If this is a query to get images from a parent dataset
    if dataset_id is not None:
        params.add('did', rlong(dataset_id))
        from_join_clauses.append('join image.datasetLinks dlink')
        where_clause.append('dlink.parent.id = :did')

    # If this is a query to get images with no parent datasets (orphans)
    # At the moment the implementation assumes that a cross-linked
    # object is not an orphan. We may need to change that so that a user
    # see all the data that belongs to them that is not assigned to a container
    # that they own.
    elif orphaned:
        orphan_where = """
                        not exists (
                            select dilink from DatasetImageLink as dilink
                            where dilink.child = image.id

                        """
        # This is what is necessary if an orphan means that it has no
        # container that belongs to the image owner. This corresponds
        # to marshal_orphaned as well because of the child count
        # if experimenter_id is not None and experimenter_id != -1:
        #     orphan_where += ' and dilink.parent.details.owner.id = :id '

        orphan_where += ') '
        where_clause.append(orphan_where)

        # Also discount any images which are part of a screen. No need to
        # take owner into account on this because we don't want them in
        # orphans either way
        where_clause.append(
            """
            not exists (
                select ws from WellSample ws
                where ws.image.id = image.id
            )
            """
        )

    # If this is a query to get images in a share
    if share_id is not None:
        # Get the contents of the blob which contains the images in the share
        # Would be nice to do this without the ShareService, preferably as part
        # of the single query
        image_rids = [image_rid.getId().val
                      for image_rid
                      in conn.getShareService().getContents(share_id)
                      if isinstance(image_rid, omero.model.ImageI)]

        # If there are no images in the share, don't bother querying
        if not image_rids:
            return images

        params.add('iids', wrap(image_rids))
        where_clause.append('image.id in (:iids)')

    q += """
        %s %s
        order by lower(image.name), image.id
        """ % (' from ' + ' '.join(from_join_clauses),
               build_clause(where_clause, 'where', 'and'))

    for e in qs.projection(q, params, service_opts):
        e = unwrap(e)[0]
        d = [e["id"],
             e["name"],
             e["ownerId"],
             e["image_details_permissions"],
             e["filesetId"]]
        kwargs = {'conn': conn, 'row': d[0:5]}
        if load_pixels:
            d = [e["sizeX"], e["sizeY"], e["sizeZ"]]
            kwargs['row_pixels'] = d
        if date:
            kwargs['acqDate'] = e['acqDate']
            kwargs['date'] = e['date']

        # While marshalling the images, determine if there are any
        # images mentioned in shares that are not in the results
        # because they have been deleted
        if share_id is not None and image_rids and e["id"] in image_rids:
            image_rids.remove(e["id"])
            kwargs['share_id'] = share_id

        images.append(_marshal_image(**kwargs))

    # Load thumbnails separately
    # We want version of most recent thumbnail (max thumbId) owned by user
    if thumb_version:
        userId = conn.getUserId()
        iids = [i['id'] for i in images]
        params = omero.sys.ParametersI()
        params.addIds(iids)
        params.add('thumbOwner', wrap(userId))
        q = """select image.id, thumbs.version from Image image
            join image.pixels pix join pix.thumbnails thumbs
            where image.id in (:ids)
            and thumbs.id = (
                select max(t.id)
                from Thumbnail t
                where t.pixels = pix.id
                and t.details.owner.id = :thumbOwner
            )
            """
        thumbVersions = {}
        for t in qs.projection(q, params, service_opts):
            iid, tv = unwrap(t)
            thumbVersions[iid] = tv
        # For all images, set thumb version if we have it...
        for i in images:
            if i['id'] in thumbVersions:
                i['thumbVersion'] = thumbVersions[i['id']]

    # If there were any deleted images in the share, marshal and return
    # those
    if share_id is not None and image_rids:
        for image_rid in image_rids:
            images.append(_marshal_image_deleted(conn, image_rid))

    return images
