#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2008-2016 University of Dundee & Open Microscopy Environment.
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

import time
import omero

from omero.rtypes import rlong, unwrap, wrap
from django.conf import settings
from django.http import Http404
from datetime import datetime
from copy import deepcopy
from omero.gateway import _letterGridLabel


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


def _marshal_group(conn, row):
    ''' Given an ExperimenterGroup row (list) marshals it into a dictionary.
        Order and type of columns in row is:
          * id (rlong)
          * name (rstring)
          * permissions (dict)

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param row The Group row to marshal
        @type row L{list}
    '''
    group_id, name, permissions = row
    group = dict()
    group['id'] = unwrap(group_id)
    group['name'] = unwrap(name)
    group['perm'] = unwrap(unwrap(permissions)['perm'])

    return group


def marshal_groups(conn, member_id=-1, page=1, limit=settings.PAGE):
    ''' Marshals groups

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param member_id The ID of the experimenter to filter by
        or -1 for all
        defaults to -1
        @type member_id L{long}
        @param page Page number of results to get. `None` or 0 for no paging
        defaults to 1
        @type page L{long}
        @param limit The limit of results per page to get
        defaults to the value set in settings.PAGE
        @type page L{long}
    '''
    groups = []
    params = omero.sys.ParametersI()
    service_opts = deepcopy(conn.SERVICE_OPTS)
    service_opts.setOmeroGroup(-1)

    # Paging
    if page is not None and page > 0:
        params.page((page-1) * limit, limit)

    join_clause = ''
    where_clause = ''
    if member_id != -1:
        params.add('mid', rlong(member_id))
        join_clause = ' join grp.groupExperimenterMap grexp '
        where_clause = ' and grexp.child.id = :mid '

    qs = conn.getQueryService()
    q = """
        select grp.id,
               grp.name,
               grp.details.permissions
        from ExperimenterGroup grp
        %s
        where grp.name != 'user'
        %s
        order by lower(grp.name)
        """ % (join_clause, where_clause)
    for e in qs.projection(q, params, service_opts):
        groups.append(_marshal_group(conn, e[0:3]))
    return groups


def _marshal_experimenter(conn, row):
    ''' Given an Experimenter row (list) marshals it into a dictionary.  Order
        and type of columns in row is:
          * id (rlong)
          * omeName (rstring)
          * firstName (rstring)
          * lastName (rstring)
          * email (rstring)

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param row The Experimenter row to marshal
        @type row L{list}
    '''

    experimenter_id, ome_name, first_name, last_name, email = row
    experimenter = dict()
    experimenter['id'] = unwrap(experimenter_id)
    experimenter['omeName'] = unwrap(ome_name)
    experimenter['firstName'] = unwrap(first_name)
    experimenter['lastName'] = unwrap(last_name)
    # Email is not mandatory
    if email:
        experimenter['email'] = unwrap(email)
    return experimenter


def marshal_experimenters(conn, group_id=-1, page=1, limit=settings.PAGE):
    ''' Marshals experimenters, possibly filtered by group.

        To make this consistent with the other tree.py functions
        this will default to restricting the results by the calling
        experimenters group membership. e.g. if user is in groupA
        and groupB, then users from groupA and groupB will be
        returned.

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param group_id The Group ID to filter by or -1 for all groups,
        defaults to -1
        @type group_id L{long}
        @param page Page number of results to get. `None` or 0 for no paging
        defaults to 1
        @type page L{long}
        @param limit The limit of results per page to get
        defaults to the value set in settings.PAGE
        @type page L{long}
    '''
    experimenters = []
    params = omero.sys.ParametersI()
    service_opts = deepcopy(conn.SERVICE_OPTS)

    if group_id is None:
        group_id = -1

    # This does not actually restrict the results so the restriction to
    # a certain group is done in the query
    service_opts.setOmeroGroup(-1)

    # Paging
    if page is not None and page > 0:
        params.page((page-1) * limit, limit)

    where_clause = ''
    if group_id != -1:
        params.add('gid', rlong(group_id))
        where_clause = '''
                       join experimenter.groupExperimenterMap grexp
                       where grexp.parent.id = :gid
                           '''

    # Don't currently need this filtering
    # Restrict by the current user's group membership
    # else:
    #     params.add('eid', rlong(conn.getUserId()))
    #     where_clause = '''
    #                    join experimenter.groupExperimenterMap grexp
    #                    where grexp.child.id = :eid
    #                    '''

    qs = conn.getQueryService()
    q = """
        select experimenter.id,
               experimenter.omeName,
               experimenter.firstName,
               experimenter.lastName,
               experimenter.email
        from Experimenter experimenter %s
        order by lower(experimenter.omeName), experimenter.id
        """ % (where_clause)
    for e in qs.projection(q, params, service_opts):
        experimenters.append(_marshal_experimenter(conn, e[0:5]))
    return experimenters


def marshal_experimenter(conn, experimenter_id):
    ''' Marshals experimenter.

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param experimenter_id The Experimenter ID to get details for
        @type experimenter_id L{long}
    '''
    params = omero.sys.ParametersI()
    service_opts = deepcopy(conn.SERVICE_OPTS)
    service_opts.setOmeroGroup(-1)

    params.add('id', rlong(experimenter_id))
    qs = conn.getQueryService()
    q = """
        select experimenter.id,
               experimenter.omeName,
               experimenter.firstName,
               experimenter.lastName,
               experimenter.email
        from Experimenter experimenter
        where experimenter.id = :id
        """
    rows = qs.projection(q, params, service_opts)
    if len(rows) != 1:
        raise Http404("No Experimenter found with ID %s" % experimenter_id)
    return _marshal_experimenter(conn, rows[0][0:5])


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


def _marshal_date(time):
    try:
        d = datetime.fromtimestamp(time/1000)
        return d.isoformat() + 'Z'
    except ValueError:
        return ''


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
    if page is not None and page > 0:
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
    if thumb_version and len(images) > 0:
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


def _marshal_screen(conn, row):
    ''' Given a Screen row (list) marshals it into a dictionary.  Order and
        type of columns in row is:
          * id (rlong)
          * name (rstring)
          * details.owner.id (rlong)
          * details.permissions (dict)
          * child_count (rlong)

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param row The Screen row to marshal
        @type row L{list}
    '''

    screen_id, name, owner_id, permissions, child_count = row
    screen = dict()
    screen['id'] = unwrap(screen_id)
    screen['name'] = unwrap(name)
    screen['ownerId'] = unwrap(owner_id)
    screen['childCount'] = unwrap(child_count)
    screen['permsCss'] = \
        parse_permissions_css(permissions, unwrap(owner_id), conn)
    return screen


def marshal_screens(conn, group_id=-1, experimenter_id=-1, page=1,
                    limit=settings.PAGE):

    ''' Marshals screens

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
    screens = []
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
        where_clause = 'where screen.details.owner.id = :id'
    qs = conn.getQueryService()
    q = """
        select new map(screen.id as id,
               screen.name as name,
               screen.details.owner.id as ownerId,
               screen as screen_details_permissions,
               (select count(spl.id) from ScreenPlateLink spl
                where spl.parent=screen.id) as childCount)
               from Screen screen
               %s
               order by lower(screen.name), screen.id
        """ % where_clause

    for e in qs.projection(q, params, service_opts):
        e = unwrap(e)
        e = [e[0]["id"],
             e[0]["name"],
             e[0]["ownerId"],
             e[0]["screen_details_permissions"],
             e[0]["childCount"]]
        screens.append(_marshal_screen(conn, e[0:5]))

    return screens


def _marshal_plate(conn, row):
    ''' Given a Plate row (list) marshals it into a dictionary.  Order and
        type of columns in row is:
          * id (rlong)
          * name (rstring)
          * details.owner.id (rlong)
          * details.permissions (dict)
          * child_count (rlong)

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param row The Plate row to marshal
        @type row L{list}
    '''

    plate_id, name, owner_id, permissions, child_count = row
    plate = dict()
    plate['id'] = unwrap(plate_id)
    plate['name'] = unwrap(name)
    plate['ownerId'] = unwrap(owner_id)
    plate['childCount'] = unwrap(child_count)
    plate['permsCss'] = \
        parse_permissions_css(permissions, unwrap(owner_id), conn)
    return plate


def marshal_plates(conn, screen_id=None, orphaned=False, group_id=-1,
                   experimenter_id=-1, page=1, limit=settings.PAGE):
    ''' Marshals plates

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param screen_id The Screen ID to filter by or `None` to
        not filter by a specific screen.
        defaults to `None`
        @type screen_id L{long}
        @param orphaned If this is to filter by orphaned data. Overridden
        by dataset_id.
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
    plates = []
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
        where_clause.append('plate.details.owner.id = :id')

    qs = conn.getQueryService()
    q = """
        select new map(plate.id as id,
               plate.name as name,
               plate.details.owner.id as ownerId,
               plate as plate_details_permissions,
               (select count(pa.id) from PlateAcquisition pa
                where pa.plate.id=plate.id) as childCount)
        from Plate plate
        """

    # If this is a query to get plates from a parent screen
    if screen_id is not None:
        params.add('sid', rlong(screen_id))
        q += 'join plate.screenLinks slink'
        where_clause.append('slink.parent.id = :sid')
    # If this is a query to get plates with no parent screens
    elif orphaned:
        where_clause.append(
            """
            not exists (
                select splink from ScreenPlateLink as splink
                where splink.child = plate.id
            )
            """
        )

    q += """
        %s
        order by lower(plate.name), plate.id
        """ % build_clause(where_clause, 'where', 'and')

    for e in qs.projection(q, params, service_opts):
        e = unwrap(e)
        e = [e[0]["id"],
             e[0]["name"],
             e[0]["ownerId"],
             e[0]["plate_details_permissions"],
             e[0]["childCount"]]
        plates.append(_marshal_plate(conn, e[0:5]))

    return plates


def _marshal_plate_acquisition(conn, row):
    ''' Given a PlateAcquisition row (list) marshals it into a dictionary.
        Order and type of columns in row is:
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
    plate_acquisition['id'] = unwrap(pa_id)

    # If there is no defined name, base it on the start/end time if that
    # exists or finally default to an id based name
    if name is not None:
        plate_acquisition['name'] = unwrap(name)
    elif start_time is not None and end_time is not None:
        start_time = datetime.utcfromtimestamp(unwrap(start_time) / 1000.0)
        end_time = datetime.utcfromtimestamp(unwrap(end_time) / 1000.0)
        plate_acquisition['name'] = '%s - %s' % (start_time, end_time)
    else:
        plate_acquisition['name'] = 'Run %d' % unwrap(pa_id)

    plate_acquisition['ownerId'] = unwrap(owner_id)
    plate_acquisition['permsCss'] = \
        parse_permissions_css(permissions, unwrap(owner_id), conn)
    return plate_acquisition


def marshal_plate_acquisitions(conn, plate_id, page=1, limit=settings.PAGE):
    ''' Marshals plate acquisitions ('runs')

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param plate_id The Plate ID to filter by
        @type plate_id L{long}
        @param page Page number of results to get. `None` or 0 for no paging
        defaults to 1
        @type page L{long}
        @param limit The limit of results per page to get
        defaults to the value set in settings.PAGE
        @type page L{long}

    '''
    plate_acquisitions = []
    params = omero.sys.ParametersI()
    service_opts = deepcopy(conn.SERVICE_OPTS)

    service_opts.setOmeroGroup(-1)

    # Paging
    if page is not None and page > 0:
        params.page((page-1) * limit, limit)

    params.add('pid', rlong(plate_id))
    qs = conn.getQueryService()
    q = """
        select new map(pa.id as id,
               pa.name as name,
               pa.details.owner.id as ownerId,
               pa as pa_details_permissions,
               pa.startTime as startTime,
               pa.endTime as endTime)
        from PlateAcquisition pa
        where pa.plate.id = :pid
        order by pa.id
        """

    for e in qs.projection(q, params, service_opts):
        e = unwrap(e)
        e = [e[0]["id"],
             e[0]["name"],
             e[0]["ownerId"],
             e[0]["pa_details_permissions"],
             e[0]["startTime"],
             e[0]["endTime"]]
        plate_acquisitions.append(_marshal_plate_acquisition(conn, e[0:6]))

    return plate_acquisitions


def marshal_orphaned(conn, group_id=-1, experimenter_id=-1, page=1,
                     limit=settings.PAGE):
    ''' Marshals orphaned containers

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
    params = omero.sys.ParametersI()
    service_opts = deepcopy(conn.SERVICE_OPTS)

    # Set the desired group context
    if group_id is None:
        group_id = -1
    service_opts.setOmeroGroup(group_id)

    # Paging
    if page is not None and page > 0:
        params.page((page-1) * limit, limit)

    if experimenter_id is not None and experimenter_id != -1:
        params.addId(experimenter_id)

    qs = conn.getQueryService()

    # Count all the images that do not have Datasets as parents or are
    # not in a WellSample
    q = '''
        select count(image.id) from Image image
        where
        '''
    if experimenter_id is not None and experimenter_id != -1:
        q += '''
            image.details.owner.id = :id
            and
            '''

    q += '''
        not exists (
            select dilink from DatasetImageLink as dilink
            where dilink.child.id = image.id
        '''

    # This corresponse to the user specific orphan restriction described
    # in the orphan section of marshal_images
    # q += ' and dilink.parent.details.owner.id = :id '
    q += '''
        )
        and not exists (
                select ws from WellSample ws
                where ws.image.id = image.id
        )
        '''

    count = unwrap(qs.projection(q, params, service_opts)[0][0])
    orphaned = dict()
    # In orphans, record the id as the experimenter
    orphaned['id'] = experimenter_id or -1
    orphaned['childCount'] = count
    return orphaned


def _marshal_tag(conn, row):
    ''' Given a Tag row (list) marshals it into a dictionary.  Order
        and type of columns in row is:
          * id (rlong)
          * text_value (rstring)
          * description (rstring)
          * details.owner.id (rlong)
          * details.permissions (dict)
          * namespace (rstring)

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param row The Tag row to marshal
        @type row L{list}

    '''
    tag_id, text_value, description, owner_id, permissions, namespace, \
        child_count = row

    tag = dict()
    tag['id'] = unwrap(tag_id)
    tag['value'] = unwrap(text_value)
    desc = unwrap(description)
    if desc:
        tag['description'] = desc
    tag['ownerId'] = unwrap(owner_id)
    tag['permsCss'] = parse_permissions_css(permissions,
                                            unwrap(owner_id), conn)

    if namespace and unwrap(namespace) == \
            omero.constants.metadata.NSINSIGHTTAGSET:
        tag['set'] = True
    else:
        tag['set'] = False

    tag['childCount'] = unwrap(child_count)

    return tag


def marshal_tags(conn, tag_id=None, group_id=-1, experimenter_id=-1, page=1,
                 orphaned=False, limit=settings.PAGE):
    ''' Marshals tags

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param tag_id The tag ID to filter by
        @type tag_id L{long}
        defaults to `None`
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
    tags = []
    params = omero.sys.ParametersI()
    service_opts = deepcopy(conn.SERVICE_OPTS)

    # Set the desired group context
    if group_id is None:
        group_id = -1
    service_opts.setOmeroGroup(group_id)

    # Paging
    if page is not None and page > 0:
        params.page((page-1) * limit, limit)

    qs = conn.getQueryService()

    # Restricted by the specified tag set
    if tag_id is not None:
        params.add('tid', rlong(tag_id))

        q = '''
            select new map(aalink.child.id as id,
                   aalink.child.textValue as textValue,
                   aalink.child.description as description,
                   aalink.child.details.owner.id as ownerId,
                   aalink.child as tag_details_permissions,
                   aalink.child.ns as ns,
                   (select count(aalink2)
                    from AnnotationAnnotationLink aalink2
                    where aalink2.child.class=TagAnnotation
                    and aalink2.parent.id=aalink.child.id) as childCount)
            from AnnotationAnnotationLink aalink
            where aalink.parent.class=TagAnnotation
            and aalink.child.class=TagAnnotation
            and aalink.parent.id=:tid
            '''

        # Restricted by the specified user
        if experimenter_id is not None and experimenter_id != -1:
            params.addId(experimenter_id)
            q += '''
                and aalink.child.details.owner.id = :id
                '''
        # TODO Is ordering by id here (and below) the right thing to do?
        q += '''
            order by aalink.child.id
            '''
    # All
    else:
        where_clause = []
        q = '''
            select new map(tag.id as id,
                   tag.textValue as textValue,
                   tag.description as description,
                   tag.details.owner.id as ownerId,
                   tag as tag_details_permissions,
                   tag.ns as ns,
                   (select count(aalink2)
                    from AnnotationAnnotationLink aalink2
                    where aalink2.child.class=TagAnnotation
                    and aalink2.parent.id=tag.id) as childCount)
            from TagAnnotation tag
            '''

        # Orphaned tags are those not tagged by a 'tagset'
        if orphaned:
            where_clause.append(
                '''
                not exists (
                    select aalink from AnnotationAnnotationLink as aalink
                    where aalink.child = tag.id
                    and aalink.parent.ns = '%s'
                )
                ''' % omero.constants.metadata.NSINSIGHTTAGSET
            )
        # Restricted by the specified user
        if experimenter_id is not None and experimenter_id != -1:
            params.addId(experimenter_id)
            where_clause.append(
                '''
                tag.details.owner.id = :id
                '''
            )
        q += """
        %s
        order by tag.id
        """ % build_clause(where_clause, 'where', 'and')

    for e in qs.projection(q, params, service_opts):
        e = unwrap(e)
        e = [e[0]["id"],
             e[0]["textValue"],
             e[0]["description"],
             e[0]["ownerId"],
             e[0]["tag_details_permissions"],
             e[0]["ns"],
             e[0]["childCount"]]
        tags.append(_marshal_tag(conn, e[0:7]))

    return tags


# TODO This could be built into the standard container marshalling as a filter
# as basically this is just the same as running several of those queries. Park
# this for now, but revisit later
# This also has a slightly different interface to the other marshals in that it
# returns a dictionary of the tagged types. This would also disappear if the
# above marshalling functions had filter functions added as one of those would
# be called each per object type instead of this one for all
def marshal_tagged(conn, tag_id, group_id=-1, experimenter_id=-1, page=1,
                   load_pixels=False, date=False, limit=settings.PAGE):
    ''' Marshals tagged data

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param tag_id The tag ID to filter by
        @type tag_id L{long}
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
    tagged = {}
    params = omero.sys.ParametersI()
    service_opts = deepcopy(conn.SERVICE_OPTS)

    # Set the desired group context
    if group_id is None:
        group_id = -1
    service_opts.setOmeroGroup(group_id)

    # Paging
    if page is not None and page > 0:
        params.page((page-1) * limit, limit)

    qs = conn.getQueryService()

    common_clause = ""
    if experimenter_id is not None and experimenter_id != -1:
        params.addId(experimenter_id)
        common_clause += '''
                        and obj.details.owner.id = :id
                        '''
    # NB: Need to add lower(obj.name) to select so we can sort on it
    common_clause += '''
                    order by lower(obj.name), obj.id
                    '''

    params.add('tid', rlong(tag_id))

    # Projects
    q = '''
        select distinct new map(obj.id as id,
            obj.name as name,
            lower(obj.name) as lowername,
            obj.details.owner.id as ownerId,
            obj as project_details_permissions,
            (select count(id) from ProjectDatasetLink dil
                where dil.parent=obj.id) as childCount)
            from Project obj
            join obj.annotationLinks alink
            where alink.child.id=:tid
        %s
        ''' % common_clause

    projects = []
    for e in qs.projection(q, params, service_opts):
        e = unwrap(e)
        e = [e[0]["id"],
             e[0]["name"],
             e[0]["ownerId"],
             e[0]["project_details_permissions"],
             e[0]["childCount"]]
        projects.append(_marshal_project(conn, e[0:5]))
    tagged['projects'] = projects

    # Datasets
    q = '''
        select distinct new map(obj.id as id,
            obj.name as name,
            lower(obj.name) as lowername,
            obj.details.owner.id as ownerId,
            obj as dataset_details_permissions,
            (select count(id) from DatasetImageLink dil
                where dil.parent=obj.id) as childCount)
            from Dataset obj
            join obj.annotationLinks alink
            where alink.child.id=:tid
        %s
        ''' % common_clause

    datasets = []
    for e in qs.projection(q, params, service_opts):
        e = unwrap(e)
        e = [e[0]["id"],
             e[0]["name"],
             e[0]["ownerId"],
             e[0]["dataset_details_permissions"],
             e[0]["childCount"]]
        datasets.append(_marshal_dataset(conn, e[0:5]))
    tagged['datasets'] = datasets

    # Images
    extraValues = ""
    extraObjs = ""
    if load_pixels:
        extraValues = """
             ,
             pix.sizeX as sizeX,
             pix.sizeY as sizeY,
             pix.sizeZ as sizeZ
             """
        extraObjs = " left outer join obj.pixels pix"
    if date:
        extraValues += """,
            obj.details.creationEvent.time as date,
            obj.acquisitionDate as acqDate
            """

    q = """
        select distinct new map(obj.id as id,
               obj.name as name,
               lower(obj.name) as lowername,
               obj.details.owner.id as ownerId,
               obj as image_details_permissions,
               obj.fileset.id as filesetId %s)
            from Image obj %s
            join obj.annotationLinks alink
            where alink.child.id=:tid
        %s
        """ % (extraValues, extraObjs, common_clause)

    images = []
    for e in qs.projection(q, params, service_opts):
        e = unwrap(e)
        row = [e[0]["id"],
               e[0]["name"],
               e[0]["ownerId"],
               e[0]["image_details_permissions"],
               e[0]["filesetId"]]
        kwargs = {}
        if load_pixels:
            d = [e[0]["sizeX"], e[0]["sizeY"], e[0]["sizeZ"]]
            kwargs['row_pixels'] = d
        if date:
            kwargs['acqDate'] = e[0]['acqDate']
            kwargs['date'] = e[0]['date']
        images.append(_marshal_image(conn, row, **kwargs))
    tagged['images'] = images

    # Screens
    q = '''
        select distinct new map(obj.id as id,
            obj.name as name,
            lower(obj.name) as lowername,
            obj.details.owner.id as ownerId,
            obj as screen_details_permissions,
            (select count(id) from ScreenPlateLink spl
                where spl.parent=obj.id) as childCount)
            from Screen obj
            join obj.annotationLinks alink
            where alink.child.id=:tid
        %s
        ''' % common_clause

    screens = []
    for e in qs.projection(q, params, service_opts):
        e = unwrap(e)
        e = [e[0]["id"],
             e[0]["name"],
             e[0]["ownerId"],
             e[0]["screen_details_permissions"],
             e[0]["childCount"]]
        screens.append(_marshal_screen(conn, e[0:5]))
    tagged['screens'] = screens

    # Plate
    q = '''
        select distinct new map(obj.id as id,
            obj.name as name,
            lower(obj.name) as lowername,
            obj.details.owner.id as ownerId,
            obj as plate_details_permissions,
            (select count(id) from PlateAcquisition pa
                where pa.plate.id=obj.id) as childCount)
            from Plate obj
            join obj.annotationLinks alink
            where alink.child.id=:tid
        %s
        ''' % common_clause

    plates = []
    for e in qs.projection(q, params, service_opts):
        e = unwrap(e)
        e = [e[0]["id"],
             e[0]["name"],
             e[0]["ownerId"],
             e[0]["plate_details_permissions"],
             e[0]["childCount"]]
        plates.append(_marshal_plate(conn, e[0:5]))
    tagged['plates'] = plates

    # Plate Acquisitions
    q = '''
        select distinct new map(obj.id as id,
            obj.name as name,
            lower(obj.name) as lowername,
            obj.details.owner.id as ownerId,
            obj as plateacquisition_details_permissions,
            obj.startTime as startTime,
            obj.endTime as endTime)
        from PlateAcquisition obj
            join obj.annotationLinks alink
            where alink.child.id=:tid
        %s
        ''' % common_clause

    plate_acquisitions = []
    for e in qs.projection(q, params, service_opts):
        e = unwrap(e)
        e = [e[0]["id"],
             e[0]["name"],
             e[0]["ownerId"],
             e[0]["plateacquisition_details_permissions"],
             e[0]["startTime"],
             e[0]["endTime"]]
        plate_acquisitions.append(_marshal_plate_acquisition(conn, e[0:6]))
    tagged['acquisitions'] = plate_acquisitions

    # Wells
    q = '''
        select distinct new map(obj.id as id,
            obj.details.owner.id as ownerId,
            obj as well_details_permissions,
            obj.row as row,
            obj.column as column,
            plate.id as plateId,
            plate.columnNamingConvention as colnames,
            plate.rowNamingConvention as rownames)
        from Well obj
            join obj.annotationLinks alink
            join obj.plate plate
            where alink.child.id=:tid
        order by obj.row, obj.column
        '''
    # E.g. sort A1, A2, B1, B2

    wells = []
    for e in qs.projection(q, params, service_opts):
        e = unwrap(e)
        e = [e[0]["id"],
             e[0]["ownerId"],
             e[0]["well_details_permissions"],
             e[0]["row"],
             e[0]["column"],
             e[0]["plateId"],
             e[0]["rownames"],
             e[0]["colnames"]]
        wells.append(_marshal_well(conn, e[0:8]))
    tagged['wells'] = wells

    return tagged


def _marshal_well(conn, row):
    ''' Given a Well row (list) marshals it into a dictionary.  Order
        and type of columns in row is:
          * id (rlong)
          * name (rstring)
          * details.owner.id (rlong)
          * details.permissions (dict)

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param row The Well row to marshal
        @type row L{list}
    '''
    well_id, owner_id, perms, row, col, plateId, rownames, colnames = row
    well = dict()
    well['id'] = unwrap(well_id)
    well['ownerId'] = unwrap(owner_id)
    well['plateId'] = unwrap(plateId)
    well['permsCss'] = \
        parse_permissions_css(perms, unwrap(owner_id), conn)
    rowname = str(row + 1) if rownames == 'number' else _letterGridLabel(row)
    colname = _letterGridLabel(col) if colnames == 'letter' else str(col + 1)
    well['name'] = "%s%s" % (rowname, colname)
    return well


def _marshal_share(conn, row):
    ''' Given a Share row (list) marshals it into a dictionary.  Order
        and type of columns in row is:
          * id (rlong)
          * details.owner.id (rlong)
          * child_count (rlong)

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param row The Share row to marshal
        @type row L{list}
    '''
    share_id, active, expired, owner_id, child_count = row
    share = dict()
    share['id'] = unwrap(share_id)
    share['ownerId'] = unwrap(owner_id)
    share['childCount'] = unwrap(child_count)
    share['isOwned'] = False
    if unwrap(owner_id) == conn.getUserId() or conn.isAdmin():
        share['isOwned'] = True
    share['expired'] = False
    if unwrap(expired) < time.time():
        share['expired'] = True
    share['active'] = unwrap(active)
    return share


def marshal_shares(conn, member_id=-1, owner_id=-1,
                   page=1, limit=settings.PAGE):
    ''' Marshal shares for a given user.

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param member_id The Experimenter (user) ID membership to filter by
        @type member_id L{long}
        @param owner_id The Experimenter (user) ID ownership to filter by
        @type owner_id L{long}
        @param page Page number of results to get. `None` or 0 for no paging
        defaults to 1
        @type page L{long}
        @param limit The limit of results per page to get
        defaults to the value set in settings.PAGE
        @type page L{long}
    '''
    shares = []
    params = omero.sys.ParametersI()
    service_opts = deepcopy(conn.SERVICE_OPTS)
    where_clause = ''

    # Paging
    if page is not None and page > 0:
        params.page((page-1) * limit, limit)

    if member_id is not None and member_id != -1:
        params.add('mid', rlong(member_id))
        where_clause += ' and mem.child.id=:mid '

    if owner_id is not None and owner_id != -1:
        params.add('owid', rlong(owner_id))
        where_clause += ' and mem.parent.owner.id=:owid '

    qs = conn.getQueryService()
    q = '''
        select distinct mem.parent.id,
            mem.parent.active,
            extract(epoch from mem.parent.started)
                +(mem.parent.timeToLive/1000),
            mem.parent.owner.id,
            mem.parent.itemCount
        from ShareMember mem
        where mem.parent.itemCount > 0
        %s
        order by mem.parent.id
        ''' % where_clause

    for e in qs.projection(q, params, service_opts):
        shares.append(_marshal_share(conn, e[0:5]))
    return shares


def _marshal_discussion(conn, row):
    ''' Given a Discussion row (list) marshals it into a dictionary.  Order
        and type of columns in row is:
          * id (rlong)
          * details.owner.id (rlong)

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param row The Discussion row to marshal
        @type row L{list}
    '''
    discussion_id, active, expired, owner_id = row
    discussion = dict()
    discussion['id'] = unwrap(discussion_id)
    discussion['ownerId'] = unwrap(owner_id)
    discussion['isOwned'] = False
    if unwrap(owner_id) == conn.getUserId() or conn.isAdmin():
        discussion['isOwned'] = True
    discussion['expired'] = False
    if unwrap(expired) < time.time():
        discussion['expired'] = True
    discussion['active'] = unwrap(active)
    return discussion


def marshal_discussions(conn, member_id=-1, owner_id=-1,
                        page=1, limit=settings.PAGE):
    ''' Marshal discussion for a given user.

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param member_id The Experimenter (user) ID membership to filter by
        @type member_id L{long}
        @param owner_id The Experimenter (user) ID ownership to filter by
        @type owner_id L{long}
        @param page Page number of results to get. `None` or 0 for no paging
        defaults to 1
        @type page L{long}
        @param limit The limit of results per page to get
        defaults to the value set in settings.PAGE
        @type page L{long}
    '''
    discussions = []
    params = omero.sys.ParametersI()
    service_opts = deepcopy(conn.SERVICE_OPTS)
    where_clause = ''

    # Paging
    if page is not None and page > 0:
        params.page((page-1) * limit, limit)

    if member_id is not None and member_id != -1:
        params.add('mid', rlong(member_id))
        where_clause += ' and mem.child.id=:mid '

    if owner_id is not None and owner_id != -1:
        params.add('owid', rlong(owner_id))
        where_clause += ' and mem.parent.owner.id=:owid '

    qs = conn.getQueryService()
    q = '''
        select distinct mem.parent.id,
            mem.parent.active,
            extract(epoch from mem.parent.started)
                +(mem.parent.timeToLive/1000),
            mem.parent.owner.id,
            mem.parent.itemCount
        from ShareMember mem
        where mem.parent.itemCount = 0
        %s
        order by mem.parent.id
        ''' % where_clause

    for e in qs.projection(q, params, service_opts):
        discussions.append(_marshal_discussion(conn, e[0:4]))
    return discussions


def _marshal_annotation(conn, annotation, link=None):
    ''' Given an OMERO annotation, marshals it into a dictionary.
        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param row The Dataset row to marshal
        @type row L{list}
    '''
    ann = {}
    ownerId = annotation.details.owner.id.val
    ann['id'] = annotation.id.val
    ann['ns'] = unwrap(annotation.ns)
    ann['description'] = unwrap(annotation.description)
    ann['owner'] = {'id': ownerId}
    creation = annotation.details.creationEvent._time
    ann['date'] = _marshal_date(unwrap(creation))
    perms = annotation.details.permissions
    ann['permissions'] = {'canDelete': perms.canDelete(),
                          'canAnnotate': perms.canAnnotate(),
                          'canLink': perms.canLink(),
                          'canEdit': perms.canEdit()}

    if link is not None:
        ann['link'] = {}
        ann['link']['id'] = link.id.val
        ann['link']['owner'] = {'id': link.details.owner.id.val}
        # Parent (Well & Acquisition have no Name)
        if link.parent.isLoaded():
            ann['link']['parent'] = {'id': link.parent.id.val,
                                     'class': link.parent.__class__.__name__}
            if hasattr(link.parent, 'name'):
                ann['link']['parent']['name'] = unwrap(link.parent.name)
        linkCreation = link.details.creationEvent._time
        ann['link']['date'] = _marshal_date(unwrap(linkCreation))
        p = link.details.permissions
        ann['link']['permissions'] = {'canDelete': p.canDelete(),
                                      'canAnnotate': p.canAnnotate(),
                                      'canLink': p.canLink(),
                                      'canEdit': p.canEdit()}

    annClass = annotation.__class__.__name__
    ann['class'] = annClass
    if annClass == 'MapAnnotationI':
        kvs = [[kv.name, kv.value] for kv in annotation.getMapValue()]
        ann['values'] = kvs
    elif annClass == 'FileAnnotationI' and annotation.file:
        ann['file'] = {}
        ann['file']['id'] = annotation.file.id.val
        ann['file']['name'] = unwrap(annotation.file.name)
        ann['file']['size'] = unwrap(annotation.file.size)
        ann['file']['path'] = unwrap(annotation.file.path)
        ann['permissions']['canDownload'] = not perms.isRestricted(
            omero.constants.permissions.BINARYACCESS)

    else:
        for a in ['timeValue', 'termValue', 'longValue',
                  'doubleValue', 'boolValue', 'textValue']:
            if hasattr(annotation, a):
                ann[a] = unwrap(getattr(annotation, a))
    return ann


def init_params(group_id, page, limit):
    params = omero.sys.ParametersI()
    # Paging
    if page is not None and page > 0:
        params.page((page-1) * limit, limit)
    return params


def _marshal_exp_obj(experimenter):
    exp = {}
    exp['id'] = experimenter.id.val
    exp['omeName'] = experimenter.omeName.val
    exp['firstName'] = unwrap(experimenter.firstName)
    exp['lastName'] = unwrap(experimenter.lastName)
    return exp


def marshal_annotations(conn, project_ids=None, dataset_ids=None,
                        image_ids=None, screen_ids=None, plate_ids=None,
                        run_ids=None, well_ids=None, ann_type=None,
                        group_id=-1, page=1, limit=settings.PAGE):

    annotations = []
    qs = conn.getQueryService()
    service_opts = deepcopy(conn.SERVICE_OPTS)

    # Set the desired group context
    if group_id is None:
        group_id = -1
    service_opts.setOmeroGroup(group_id)

    where_clause = ['pa.id in (:ids)']
    # if experimenter_id is not None and experimenter_id != -1:
    #     params.addId('eid', rlong(experimenter_id))
    #     where_clause.append('dataset.details.owner.id = :eid')
    if ann_type == 'tag':
        where_clause.append('ch.class=TagAnnotation')
    elif ann_type == 'file':
        where_clause.append('ch.class=FileAnnotation')
    elif ann_type == 'comment':
        where_clause.append('ch.class=CommentAnnotation')
    elif ann_type == 'rating':
        where_clause.append('ch.class=LongAnnotation')
        where_clause.append("ch.ns='openmicroscopy.org/omero/insight/rating'")
    elif ann_type == 'map':
        where_clause.append('ch.class=MapAnnotation')
    elif ann_type == 'custom':
        where_clause.append('ch.class!=MapAnnotation')
        where_clause.append('ch.class!=TagAnnotation')
        where_clause.append('ch.class!=FileAnnotation')
        where_clause.append('ch.class!=CommentAnnotation')
        where_clause.append("""(ch.ns=null or
            ch.ns!='openmicroscopy.org/omero/insight/rating')""")

    dtypes = ["Project", "Dataset", "Image",
              "Screen", "Plate", "PlateAcquisition", "Well"]
    obj_ids = [project_ids, dataset_ids, image_ids,
               screen_ids, plate_ids, run_ids, well_ids]

    experimenters = {}

    for dtype, ids in zip(dtypes, obj_ids):
        if ids is None or len(ids) == 0:
            continue
        params = init_params(group_id, page, limit)
        params.addIds(ids)
        q = """
            select oal from %sAnnotationLink as oal
            join fetch oal.details.creationEvent
            join fetch oal.details.owner
            left outer join fetch oal.child as ch
            left outer join fetch oal.parent as pa
            join fetch ch.details.creationEvent
            join fetch ch.details.owner
            left outer join fetch ch.file as file
            where %s order by ch.ns
            """ % (dtype, ' and '.join(where_clause))

        for link in qs.findAllByQuery(q, params, service_opts):
            ann = link.child
            d = _marshal_annotation(conn, ann, link)
            annotations.append(d)
            exp = _marshal_exp_obj(link.details.owner)
            experimenters[exp['id']] = exp
            exp = _marshal_exp_obj(ann.details.owner)
            experimenters[exp['id']] = exp

    experimenters = experimenters.values()
    # sort by id mostly for testing
    experimenters.sort(key=lambda x: x['id'])

    return annotations, experimenters
