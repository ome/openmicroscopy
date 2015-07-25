#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2014-2015 Glencoe Software, Inc.
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

"""
Simple integration tests for the "tree" module.
"""

import pytest
import library as lib

from omero.gateway import BlitzGateway
from omero.model import ProjectI, DatasetI, ScreenI, PlateI, \
    PlateAcquisitionI, TagAnnotationI, ProjectAnnotationLinkI, \
    DatasetAnnotationLinkI, ImageAnnotationLinkI, ScreenAnnotationLinkI, \
    PlateAnnotationLinkI, PlateAcquisitionAnnotationLinkI
from omero.rtypes import rstring, rtime
from omeroweb.webclient.tree import marshal_experimenter, \
    marshal_projects, marshal_datasets, marshal_images, marshal_plates, \
    marshal_screens, marshal_plate_acquisitions, marshal_orphaned, \
    marshal_tags, marshal_tagged, marshal_shares, marshal_discussions

from datetime import datetime


def unwrap(x):
    """Handle case where there is no value because attribute is None"""
    if x is not None:
        return x.val
    return None


def cmp_id(x, y):
    """Identifier comparator."""
    return cmp(unwrap(x.id), unwrap(y.id))


def cmp_name(x, y):
    """Name comparator."""
    return cmp(unwrap(x.name), unwrap(y.name))


def lower_or_none(x):
    """ Lower the case or `None`"""
    if x is not None:
        return x.lower()
    return None


def cmp_name_insensitive(x, y):
    """Case-insensitive name comparator."""
    return cmp(lower_or_none(unwrap(x.name)),
               lower_or_none(unwrap(y.name)))


def cmp_omename_insensitive(x, y):
    """Case-insensitive omeName comparator."""
    return cmp(lower_or_none(unwrap(x.omeName)),
               lower_or_none(unwrap(y.omeName)))


def cmp_textValue_insensitive(x, y):
    """Case-insensitive textValue comparator."""
    return cmp(lower_or_none(unwrap(x.textValue)),
               lower_or_none(unwrap(y.textValue)))


def get_connection(user, group_id=None):
    """
    Get a BlitzGateway connection for the given user's client
    """
    connection = BlitzGateway(client_obj=user[0])
    # Refresh the session context
    connection.getEventContext()
    if group_id is not None:
        connection.SERVICE_OPTS.setOmeroGroup(group_id)
    return connection


def get_update_service(user):
    """
    Get the update_service for the given user's client
    """
    return user[0].getSession().getUpdateService()


def get_perms(user, obj, dtype):
    """
    Get the permissions as a string from this list and in this order:
    'canEdit canAnnotate canLink canDelete canChgrp'
    according to the specified user (client, user)
    """
    qs = user[0].getSession().getQueryService()
    # user reloads obj so that permissions apply correctly
    obj = qs.get(dtype, obj.id.val, {'omero.group': '-1'})
    permissions = obj.details.permissions
    perms = []
    if permissions.canEdit():
        perms.append('canEdit')
    if permissions.canAnnotate():
        perms.append('canAnnotate')
    if permissions.canLink():
        perms.append('canLink')
    if permissions.canDelete():
        perms.append('canDelete')
    if obj.details.owner.id.val == user[1].id.val:
        perms.append('isOwned')
    # TODO Add chgrp permission to an admin user
    if obj.details.owner.id.val == user[1].id.val:
        perms.append('canChgrp')

    return ' '.join(perms)


def expected_experimenter(user):
    expected = {
        'id': user[1].id.val,
        'omeName': user[1].omeName.val,
        'firstName': user[1].firstName.val,
        'lastName': user[1].lastName.val
    }
    if user[1].email is not None:
        expected['email'] = user[1].email.val
    return expected


def expected_projects(user, projects):
    """ Marshal projects with permissions according to user """
    expected = []
    for project in projects:
        expected.append({
            'id': project.id.val,
            'name': project.name.val,
            'ownerId': project.details.owner.id.val,
            'childCount': len(project.linkedDatasetList()),
            'permsCss': get_perms(user, project, "Project")
        })
    return expected


def expected_datasets(user, datasets):
    expected = []
    for dataset in datasets:
        expected.append({
            'id': dataset.id.val,
            'name': dataset.name.val,
            'ownerId': dataset.details.owner.id.val,
            'childCount': len(dataset.linkedImageList()),
            'permsCss': get_perms(user, dataset, "Dataset")
        })
    return expected


# TODO Is there a way to test load_pixels when these fake images don't
# actually have any pixels?
def expected_images(user, images):
    expected = []
    for image in images:
        i = {
            'id': image.id.val,
            'name': image.name.val,
            'ownerId': image.details.owner.id.val,
            'permsCss': get_perms(user, image, "Image"),
        }
        if image.fileset is not None:
            i['filesetId'] = image.fileset.id.val
        expected.append(i)
    return expected


def expected_screens(user, screens):
    expected = []
    for screen in screens:
        expected.append({
            'id': screen.id.val,
            'name': screen.name.val,
            'ownerId': screen.details.owner.id.val,
            'childCount': len(screen.linkedPlateList()),
            'permsCss': get_perms(user, screen, "Screen")
        })
    return expected


def expected_plates(user, plates):
    expected = []
    for plate in plates:
        expected.append({
            'id': plate.id.val,
            'name': plate.name.val,
            'ownerId': plate.details.owner.id.val,
            'childCount': len(plate.copyPlateAcquisitions()),
            'permsCss': get_perms(user, plate, "Plate")
        })
    return expected


def expected_plate_acquisitions(user, plate_acquisitions):
    expected = []
    for acq in plate_acquisitions:

        if acq.name is not None:
            acq_name = acq.name.val
        elif acq.startTime is not None and acq.endTime is not None:
            start_time = datetime.utcfromtimestamp(acq.startTime.val / 1000.0)
            end_time = datetime.utcfromtimestamp(acq.endTime.val / 1000.0)
            acq_name = '%s - %s' % (start_time, end_time)
        else:
            acq_name = 'Run %d' % acq.id.val

        expected.append({
            'id': acq.id.val,
            'name': acq_name,
            'ownerId': acq.details.owner.id.val,
            'permsCss': get_perms(user, acq, "PlateAcquisition")
        })
    return expected


def expected_orphaned(user, images):
    return {
        'id': user[1].id.val,
        'childCount': len(images)
    }


def expected_tags(user, tags):
    expected = []
    for tag in tags:
        if tag.description is not None:
            description = tag.description.val
        else:
            description = None
        t = {
            'id': tag.id.val,
            'value': tag.textValue.val,
            'description': description,
            'ownerId': tag.details.owner.id.val,
            'childCount': len(tag.linkedAnnotationList()),
            'permsCss': get_perms(user, tag, "TagAnnotation")
        }

        if tag.ns is not None and tag.ns.val == \
                'openmicroscopy.org/omero/insight/tagset':
            t['set'] = True
        else:
            t['set'] = False
        expected.append(t)
    return expected


def expected_tagged(user, projects, datasets, images, screens, plates,
                    plate_acquisitions):
    return {
        'projects': expected_projects(user, projects),
        'datasets': expected_datasets(user, datasets),
        'images': expected_images(user, images),
        'screens': expected_screens(user, screens),
        'plates': expected_plates(user, plates),
        'acquisitions': expected_plate_acquisitions(user, plate_acquisitions)
    }


def expected_shares(user, shares):
    expected = []
    for share in shares:
        expected.append({
            'id': share.id.val,
            'ownerId': share.owner.id.val,
            'childCount': share.getItemCount().val
        })
    return expected


def expected_discussions(user, discussions):
    expected = []
    for discussion in discussions:
        expected.append({
            'id': discussion.id.val,
            'ownerId': discussion.owner.id.val
        })
    return expected


# @pytest.fixture(scope='function')
# def itest(request):
#     """
#     Returns a new L{test.integration.library.ITest} instance.  With
#     attached finalizer so that pytest will clean it up.
#     """
#     o = lib.ITest()
#     o.setup_method(None)

#     def finalizer():
#         o.teardown_method(None)
#     request.addfinalizer(finalizer)
#     return o


# # Create a read-only group
# @pytest.fixture(scope='function')
# def groupA(request, itest):
#     """Returns a new read-only group."""
#     return itest.new_group(perms='rwr---')


# # Create a read-only group
# @pytest.fixture(scope='function')
# def groupB(request, itest):
#     """Returns a new read-only group."""
#     return itest.new_group(perms='rwr---')


# Create a read-write group
# @pytest.fixture(scope='function')
# def groupC(request, itest):
#     """Returns a new read-only group."""
#     return itest.new_group(perms='rwrw--')


# Create users in the read-only group
# @pytest.fixture(scope='function')
# def userA(request, itest, groupA, groupB):
#     """Returns a new user in the groupA group and also add to groupB"""
#     user = itest.new_client_and_user(group=groupA)
#     itest.add_groups(user[1], [groupB])
#     return user


# @pytest.fixture(scope='function')
# def userB(request, itest, groupA):
#     """Returns another new user in the read-only group."""
#     return itest.new_client_and_user(group=groupA)


# @pytest.fixture(scope='function')
# def userC(request, itest, groupC):
#     """Returns a new user in the read-write group."""
#     return itest.new_client_and_user(group=groupC)


# @pytest.fixture(scope='function')
# def userD(request, itest, groupC):
#     """Returns another new user in the read-write group."""
#     return itest.new_client_and_user(group=groupC)


# Some names
@pytest.fixture(scope='module')
def names1(request):
    return ('Apple', 'bat')


@pytest.fixture(scope='module')
def names2(request):
    return ('Axe',)


@pytest.fixture(scope='module')
def names3(request):
    return ('Bark', 'custard')


@pytest.fixture(scope='module')
def names4(request):
    return ('Butter',)


# Projects
@pytest.fixture(scope='function')
def projects_userA_groupA(request, names1, userA,
                          project_hierarchy_userA_groupA):
    """
    Returns new OMERO Projects with required fields set and with names
    that can be used to exercise sorting semantics.
    """
    to_save = []
    for name in names1:
        project = ProjectI()
        project.name = rstring(name)
        to_save.append(project)
    projects = get_update_service(userA).saveAndReturnArray(to_save)
    projects.extend(project_hierarchy_userA_groupA[:2])
    projects.sort(cmp_name_insensitive)
    return projects


@pytest.fixture(scope='function')
def projects_userB_groupA(request, names2, userB):
    """
    Returns a new OMERO Project with required fields set and with a name
    that can be used to exercise sorting semantics.
    """
    to_save = []
    for name in names2:
        project = ProjectI()
        project.name = rstring(name)
        to_save.append(project)
    projects = get_update_service(userB).saveAndReturnArray(
        to_save)
    projects.sort(cmp_name_insensitive)
    return projects


@pytest.fixture(scope='function')
def projects_userA_groupB(request, names3, userA, groupB):
    """
    Returns new OMERO Projects with required fields set and with names
    that can be used to exercise sorting semantics.
    """
    to_save = []
    for name in names3:
        project = ProjectI()
        project.name = rstring(name)
        to_save.append(project)
    conn = get_connection(userA, groupB.id.val)
    projects = conn.getUpdateService().saveAndReturnArray(to_save,
                                                          conn.SERVICE_OPTS)

    projects.sort(cmp_name_insensitive)
    return projects


@pytest.fixture(scope='function')
def projects_groupA(request, projects_userA_groupA,
                    projects_userB_groupA):
    """
    Returns OMERO Projects for userA and userB in groupA
    """
    projects = projects_userA_groupA + projects_userB_groupA
    projects.sort(cmp_name_insensitive)
    return projects


@pytest.fixture(scope='function')
def projects_groupB(request, projects_userA_groupB):
    """
    Returns OMERO Projects for userA and userB in groupB
    """
    projects = projects_userA_groupB
    # projects.sort(cmp_name_insensitive)
    return projects


@pytest.fixture(scope='function')
def projects_userA(request, projects_userA_groupA,
                   projects_userA_groupB):
    """
    Returns OMERO Projects for userA in both groupA and groupB
    """
    projects = projects_userA_groupA + projects_userA_groupB
    projects.sort(cmp_name_insensitive)
    return projects


@pytest.fixture(scope='function')
def projects(request, projects_groupA,
             projects_groupB):
    """
    Returns OMERO Projects for both users in read-only group
    """
    projects = projects_groupA + projects_groupB
    projects.sort(cmp_name_insensitive)
    return projects


# Datasets
@pytest.fixture(scope='function')
def datasets_userA_groupA(request, userA):
    """
    Returns new OMERO Datasets without project parents for userA in groupA
    """
    to_save = []
    for name in ['Tiger', 'sparrow']:
        dataset = DatasetI()
        dataset.name = rstring(name)
        to_save.append(dataset)
    datasets = get_update_service(userA).saveAndReturnArray(to_save)
    datasets.sort(cmp_name_insensitive)
    return datasets


@pytest.fixture(scope='function')
def datasets_userB_groupA(request, userB):
    """
    Returns new OMERO Datasets without project parents for userB in groupA
    """
    to_save = []
    for name in ['lion', 'Zebra']:
        dataset = DatasetI()
        dataset.name = rstring(name)
        to_save.append(dataset)
    datasets = get_update_service(userB).saveAndReturnArray(to_save)
    datasets.sort(cmp_name_insensitive)
    return datasets


@pytest.fixture(scope='function')
def datasets_userA_groupB(request, userA, groupB):
    """
    Returns new OMERO Datasets without project parents for userA in groupB
    """
    to_save = []
    for name in ['Meerkat', 'anteater']:
        dataset = DatasetI()
        dataset.name = rstring(name)
        to_save.append(dataset)
    conn = get_connection(userA, groupB.id.val)
    datasets = conn.getUpdateService().saveAndReturnArray(to_save,
                                                          conn.SERVICE_OPTS)

    datasets.sort(cmp_name_insensitive)
    return datasets


@pytest.fixture(scope='function')
def datasets_groupA(request, datasets_userA_groupA,
                    datasets_userB_groupA):
    """
    Returns OMERO Datasets for userA and userB in groupA
    """
    datasets = datasets_userA_groupA + datasets_userB_groupA
    datasets.sort(cmp_name_insensitive)
    return datasets


@pytest.fixture(scope='function')
def datasets_groupB(request, datasets_userA_groupB):
    """
    Returns OMERO Datasets for userA and userB in groupB
    """
    datasets = datasets_userA_groupB
    # datasets.sort(cmp_name_insensitive)
    return datasets


@pytest.fixture(scope='function')
def datasets_userA(request, datasets_userA_groupA,
                   datasets_userA_groupB):
    """
    Returns OMERO Datasets for userA in groupA and groupB
    """
    datasets = datasets_userA_groupA + datasets_userA_groupB
    datasets.sort(cmp_name_insensitive)
    return datasets


@pytest.fixture(scope='function')
def datasets(request, datasets_groupA,
             datasets_groupB):
    """
    Returns OMERO Datasets for all users in all groups
    """
    datasets = datasets_groupA + datasets_groupB
    datasets.sort(cmp_name_insensitive)
    return datasets


# ### Images ###
# @pytest.fixture(scope='function')
# def images_userA_groupA(request, itest, userA):
#     """
#     Returns new OMERO Images for userA in groupA
#     """
#     to_save = []
#     for name in ['Neon', 'hydrogen', 'Helium', 'boron']:
#         image = itest.new_image(name=name)
#         to_save.append(image)

#     images = get_update_service(userA).saveAndReturnArray(to_save)
#     images.sort(cmp_name_insensitive)
#     return images


# @pytest.fixture(scope='function')
# def images_userB_groupA(request, itest, userB):
#     """
#     Returns new OMERO Images for userB in groupA
#     """
#     to_save = []
#     for name in ['Oxygen', 'nitrogen']:
#         image = itest.new_image(name=name)
#         to_save.append(image)

#     images = get_update_service(userB).saveAndReturnArray(to_save)
#     images.sort(cmp_name_insensitive)
#     return images


# @pytest.fixture(scope='function')
# def images_userA_groupB(request, itest, userA, groupB):
#     """
#     Returns new OMERO Images for userA in groupB
#     """
#     to_save = []
#     for name in ['Zinc', 'aluminium']:
#         image = itest.new_image(name=name)
#         to_save.append(image)

#     conn = get_connection(userA, groupB.id.val)
#     images = conn.getUpdateService().saveAndReturnArray(to_save,
#                                                         conn.SERVICE_OPTS)
#     images.sort(cmp_name_insensitive)
#     return images


@pytest.fixture(scope='function')
def images_groupA(request, images_userA_groupA,
                  images_userB_groupA):
    """
    Returns OMERO Images for userA and userB in groupA
    """
    images = images_userA_groupA + images_userB_groupA
    images.sort(cmp_name_insensitive)
    return images


@pytest.fixture(scope='function')
def images_groupB(request, images_userA_groupB):
    """
    Returns OMERO Images for userA and userB in groupB
    """
    images = images_userA_groupB
    # images.sort(cmp_name_insensitive)
    return images


@pytest.fixture(scope='function')
def images_userA(request, images_userA_groupA, images_userA_groupB):
    """
    Returns OMERO Images for userA in groupA and groupB
    """
    images = images_userA_groupA + images_userA_groupB
    images.sort(cmp_name_insensitive)
    return images


@pytest.fixture(scope='function')
def images(request, images_groupA, images_groupB):
    """
    Returns OMERO Images for all users in all groups
    """
    images = images_groupA + images_groupB
    images.sort(cmp_name_insensitive)
    return images


# @pytest.fixture(scope='function')
# def project_hierarchy_userA_groupA_x(request, itest, userA):
#     """
#     Returns OMERO Projects with Dataset Children with Image Children

#     Note: This returns a list of mixed objects in a specified order
#     """

#     # Create and name all the objects
#     projectA = ProjectI()
#     projectA.name = rstring('ProjectA')
#     projectB = ProjectI()
#     projectB.name = rstring('ProjectB')
#     datasetA = DatasetI()
#     datasetA.name = rstring('DatasetA')
#     datasetB = DatasetI()
#     datasetB.name = rstring('DatasetB')
#     imageA = itest.new_image(name='ImageA')
#     imageB = itest.new_image(name='ImageB')

#     # Link them together like so:
#     # projectA
#     #   datasetA
#     #       imageA
#     #       imageB
#     #   datasetB
#     #       imageB
#     # projectB
#     #   datasetB
#     #       imageB
#     projectA.linkDataset(datasetA)
#     projectA.linkDataset(datasetB)
#     projectB.linkDataset(datasetB)
#     datasetA.linkImage(imageA)
#     datasetA.linkImage(imageB)
#     datasetB.linkImage(imageB)

#     to_save = [projectA, projectB]
#     projects = get_update_service(userA).saveAndReturnArray(to_save)
#     projects.sort(cmp_name_insensitive)

#     datasets = projects[0].linkedDatasetList()
#     datasets.sort(cmp_name_insensitive)

#     images = datasets[0].linkedImageList()
#     images.sort(cmp_name_insensitive)

#     return projects + datasets + images


# Shares
@pytest.fixture(scope='function')
def shares_userA_owned(request, userA, userB, images_userA_groupA):
    """
    Returns OMERO Shares with Image Children for userA
    """
    conn = get_connection(userA)
    shares = []
    for name in ['ShareB', 'ShareA']:
        # It seems odd that unlike most services, createShare returns the id
        # of what was created instead of the share itself
        sid = conn.getShareService().createShare(name, rtime(None),
                                                 images_userA_groupA,
                                                 [userB[1]],
                                                 [], True)
        sh = conn.getShareService().getShare(sid)
        shares.append(sh)
    shares.sort(cmp_id)
    return shares


@pytest.fixture(scope='function')
def shares_userB_owned(request, userA, userB, images_userB_groupA):
    """
    Returns new OMERO Shares with Image Children for userB
    """
    conn = get_connection(userB)
    shares = []
    for name in ['ShareD', 'ShareC']:
        sid = conn.getShareService().createShare(name, rtime(None),
                                                 images_userB_groupA,
                                                 [userA[1]],
                                                 [], True)
        sh = conn.getShareService().getShare(sid)
        shares.append(sh)
    shares.sort(cmp_id)
    return shares


@pytest.fixture(scope='function')
def shares(request, shares_userA_owned, shares_userB_owned):
    """
    Returns OMERO Shares for userA and userB
    """
    shares = shares_userA_owned + shares_userB_owned
    shares.sort(cmp_id)
    return shares


# Discussions
@pytest.fixture(scope='function')
def discussions_userA_owned(request, userA, userB):
    """
    Returns OMERO Shares with Image Children for userA
    """
    conn = get_connection(userA)
    discussions = []
    for name in ['DiscussionB', 'DiscussionA']:
        sid = conn.getShareService().createShare(name, rtime(None),
                                                 [],
                                                 [userB[1]],
                                                 [], True)
        sh = conn.getShareService().getShare(sid)
        discussions.append(sh)
    discussions.sort(cmp_id)
    return discussions


@pytest.fixture(scope='function')
def discussions_userB_owned(request, userA, userB):
    """
    Returns new OMERO Shares with Image Children for userB
    """
    conn = get_connection(userB)
    discussions = []
    for name in ['DiscussionD', 'DiscussionC']:
        sid = conn.getShareService().createShare(name, rtime(None),
                                                 [],
                                                 [userA[1]],
                                                 [], True)
        sh = conn.getShareService().getShare(sid)
        discussions.append(sh)
    discussions.sort(cmp_id)
    return discussions


@pytest.fixture(scope='function')
def discussions(request, discussions_userA_owned, discussions_userB_owned):
    """
    Returns OMERO Shares for userA and userB
    """
    discussions = discussions_userA_owned + discussions_userB_owned
    discussions.sort(cmp_id)
    return discussions


# Screens
@pytest.fixture(scope='function')
def screens_userA_groupA(request, userA):
    """
    Returns new OMERO Screens for userA in groupA
    """
    to_save = []
    for name in ['France', 'albania']:
        screen = ScreenI()
        screen.name = rstring(name)
        to_save.append(screen)

    screens = get_update_service(userA).saveAndReturnArray(to_save)
    screens.sort(cmp_name_insensitive)
    return screens


@pytest.fixture(scope='function')
def screens_userB_groupA(request, userB):
    """
    Returns new OMERO Screens for userB in groupA
    """
    to_save = []
    for name in ['Canada', 'Australia']:
        screen = ScreenI()
        screen.name = rstring(name)
        to_save.append(screen)

    screens = get_update_service(userB).saveAndReturnArray(to_save)
    screens.sort(cmp_name_insensitive)
    return screens


@pytest.fixture(scope='function')
def screens_userA_groupB(request, userA, groupB):
    """
    Returns new OMERO Screens for userA in groupB
    """
    to_save = []
    for name in ['United States of America', 'United Kingom']:
        screen = ScreenI()
        screen.name = rstring(name)
        to_save.append(screen)

    conn = get_connection(userA, groupB.id.val)
    screens = conn.getUpdateService().saveAndReturnArray(to_save,
                                                         conn.SERVICE_OPTS)
    screens.sort(cmp_name_insensitive)
    return screens


@pytest.fixture(scope='function')
def screens_groupA(request, screens_userA_groupA, screens_userB_groupA):
    """
    Returns OMERO Screens for userA and userB in groupA
    """
    screens = screens_userA_groupA + screens_userB_groupA
    screens.sort(cmp_name_insensitive)
    return screens


@pytest.fixture(scope='function')
def screens_groupB(request, screens_userA_groupB):
    """
    Returns OMERO Screens for userA and userB in groupB
    """
    screens = screens_userA_groupB
    # screens.sort(cmp_name_insensitive)
    return screens


@pytest.fixture(scope='function')
def screens_userA(request, screens_userA_groupA, screens_userA_groupB):
    """
    Returns OMERO Screens for userA in groupA and groupB
    """
    screens = screens_userA_groupA + screens_userA_groupB
    screens.sort(cmp_name_insensitive)
    return screens


@pytest.fixture(scope='function')
def screens(request, screens_groupA, screens_groupB):
    """
    Returns OMERO Screens for all users in all groups
    """
    screens = screens_groupA + screens_groupB
    screens.sort(cmp_name_insensitive)
    return screens


# Plates
@pytest.fixture(scope='function')
def plates_userA_groupA(request, userA):
    """
    Returns new OMERO Plates for userA in groupA
    """
    to_save = []
    for name in ['New York', 'New Amsterdam']:
        plate = PlateI()
        plate.name = rstring(name)
        to_save.append(plate)

    plates = get_update_service(userA).saveAndReturnArray(to_save)
    plates.sort(cmp_name_insensitive)
    return plates


@pytest.fixture(scope='function')
def plates_userB_groupA(request, userB):
    """
    Returns new OMERO Plates for userB in groupA
    """
    to_save = []
    for name in ['Istanbul', 'Constantinople']:
        plate = PlateI()
        plate.name = rstring(name)
        to_save.append(plate)

    plates = get_update_service(userB).saveAndReturnArray(to_save)
    plates.sort(cmp_name_insensitive)
    return plates


@pytest.fixture(scope='function')
def plates_userA_groupB(request, userA, groupB):
    """
    Returns new OMERO Plates for userA in groupB
    """
    to_save = []
    for name in ['Mumbai', 'Bombay']:
        plate = PlateI()
        plate.name = rstring(name)
        to_save.append(plate)

    conn = get_connection(userA, groupB.id.val)
    plates = conn.getUpdateService().saveAndReturnArray(to_save,
                                                        conn.SERVICE_OPTS)
    plates.sort(cmp_name_insensitive)
    return plates


@pytest.fixture(scope='function')
def plates_groupA(request, plates_userA_groupA, plates_userB_groupA):
    """
    Returns OMERO Plates for userA and userB in groupA
    """
    plates = plates_userA_groupA + plates_userB_groupA
    plates.sort(cmp_name_insensitive)
    return plates


@pytest.fixture(scope='function')
def plates_groupB(request, plates_userA_groupB):
    """
    Returns OMERO Plates for userA and userB in groupB
    """
    plates = plates_userA_groupB
    # plates.sort(cmp_name_insensitive)
    return plates


@pytest.fixture(scope='function')
def plates_userA(request, plates_userA_groupA, plates_userA_groupB):
    """
    Returns OMERO Plates for userA in groupA and groupB
    """
    plates = plates_userA_groupA + plates_userA_groupB
    plates.sort(cmp_name_insensitive)
    return plates


@pytest.fixture(scope='function')
def plates(request, plates_groupA, plates_groupB):
    """
    Returns OMERO Plates for all users in all groups
    """
    plates = plates_groupA + plates_groupB
    plates.sort(cmp_name_insensitive)
    return plates


@pytest.fixture(scope='function')
def screen_hierarchy_userA_groupA(request, userA):
    """
    Returns OMERO Screens with Plate Children with Plate Acquisition Children

    Note: This returns a list of mixed objects in a specified order
    """

    # Create and name all the objects
    screenA = ScreenI()
    screenA.name = rstring('ScreenA')
    screenB = ScreenI()
    screenB.name = rstring('ScreenB')
    plateA = PlateI()
    plateA.name = rstring('PlateA')
    acqA = PlateAcquisitionI()
    acqA.name = rstring('AcqA')
    # No name for acqB
    acqNone = PlateAcquisitionI()
    # Only set startTime and endTime for acqC
    acqTime = PlateAcquisitionI()
    acqTime.startTime = rtime(0)
    acqTime.endTime = rtime(1)

    # Link them together like so:
    # screenA
    #   plateA
    #       acqA
    #       acqNone
    #       acqTime
    # screenB
    #   plateA
    #       acqA
    #       acqNone
    #       acqTime
    screenA.linkPlate(plateA)
    screenB.linkPlate(plateA)

    plateA.addPlateAcquisition(acqA)
    plateA.addPlateAcquisition(acqNone)
    plateA.addPlateAcquisition(acqTime)

    to_save = [screenA, screenB]
    screens = get_update_service(userA).saveAndReturnArray(to_save)
    screens.sort(cmp_name_insensitive)

    plates = screens[0].linkedPlateList()
    plates.sort(cmp_name_insensitive)

    acqs = plates[0].copyPlateAcquisitions()

    acqs.sort(cmp_id)

    return screens + plates + acqs


@pytest.fixture(scope='function')
def screen_hierarchy_userB_groupA(request, userB):
    """
    Returns OMERO Screens with Plate Children with Plate Acquisition Children

    Note: This returns a list of mixed objects in a specified order
    """

    # Create and name all the objects
    screenC = ScreenI()
    screenC.name = rstring('ScreenC')
    plateB = PlateI()
    plateB.name = rstring('PlateB')
    acqB = PlateAcquisitionI()
    acqB.name = rstring('AcqB')

    # Link them together like so:
    # screenC
    #   plateB
    #       acqB

    screenC.linkPlate(plateB)

    plateB.addPlateAcquisition(acqB)

    to_save = [screenC]
    screens = get_update_service(userB).saveAndReturnArray(to_save)
    screens.sort(cmp_name_insensitive)

    plates = screens[0].linkedPlateList()

    acqs = plates[0].copyPlateAcquisitions()

    return screens + plates + acqs


@pytest.fixture(scope='function')
def screen_hierarchy_userA_groupB(request, userA, groupB):
    """
    Returns OMERO Screens with Plate Children with Plate Acquisition Children

    Note: This returns a list of mixed objects in a specified order
    """

    # Create and name all the objects
    screenD = ScreenI()
    screenD.name = rstring('ScreenD')
    plateC = PlateI()
    plateC.name = rstring('PlateC')
    acqC = PlateAcquisitionI()
    acqC.name = rstring('AcqC')

    # Link them together like so:
    # screenD
    #   plateC
    #       acqC

    screenD.linkPlate(plateC)

    plateC.addPlateAcquisition(acqC)

    to_save = [screenD]
    conn = get_connection(userA, groupB.id.val)
    screens = conn.getUpdateService().saveAndReturnArray(to_save,
                                                         conn.SERVICE_OPTS)
    screens.sort(cmp_name_insensitive)

    plates = screens[0].linkedPlateList()

    acqs = plates[0].copyPlateAcquisitions()

    return screens + plates + acqs


@pytest.fixture(scope='function')
def tags_userA_groupA(request, userA, tagset_hierarchy_userA_groupA):
    """
    Returns new OMERO Tags
    """
    to_save = []
    for name in ['Jupiter', 'mars']:
        tag = TagAnnotationI()
        tag.textValue = rstring(name)
        to_save.append(tag)
    tags = get_update_service(userA).saveAndReturnArray(to_save)
    tags.extend(tagset_hierarchy_userA_groupA[:2])
    tags.sort(cmp_id)
    return tags


@pytest.fixture(scope='function')
def tags_userB_groupA(request, userB):
    """
    Returns new OMERO Tags for userB in groupA
    """
    to_save = []
    for name in ['venus', 'Earth']:
        tag = TagAnnotationI()
        tag.textValue = rstring(name)
        to_save.append(tag)

    tags = get_update_service(userB).saveAndReturnArray(to_save)
    tags.sort(cmp_id)
    return tags


@pytest.fixture(scope='function')
def tags_userA_groupB(request, userA, groupB):
    """
    Returns new OMERO Tags for userA in groupB
    """
    to_save = []
    for name in ['Saturn', 'Mercury']:
        tag = TagAnnotationI()
        tag.textValue = rstring(name)
        to_save.append(tag)

    conn = get_connection(userA, groupB.id.val)
    tags = conn.getUpdateService().saveAndReturnArray(to_save,
                                                      conn.SERVICE_OPTS)
    tags.sort(cmp_id)
    return tags


@pytest.fixture(scope='function')
def tags_groupA(request, tags_userA_groupA, tags_userB_groupA):
    """
    Returns OMERO Tags for userA and userB in groupA
    """
    tags = tags_userA_groupA + tags_userB_groupA
    tags.sort(cmp_id)
    return tags


@pytest.fixture(scope='function')
def tags_groupB(request, tags_userA_groupB):
    """
    Returns OMERO Tags for userA and userB in groupB
    """
    tags = tags_userA_groupB
    # tags.sort(cmp_name_insensitive)
    return tags


@pytest.fixture(scope='function')
def tags_userA(request, tags_userA_groupA, tags_userA_groupB):
    """
    Returns OMERO Tags for userA in groupA and groupB
    """
    tags = tags_userA_groupA + tags_userA_groupB
    tags.sort(cmp_id)
    return tags


@pytest.fixture(scope='function')
def tags(request, tags_groupA, tags_groupB):
    """
    Returns OMERO Tags for all users in all groups
    """
    tags = tags_groupA + tags_groupB
    tags.sort(cmp_id)
    return tags


@pytest.fixture(scope='function')
def tagset_hierarchy_userA_groupA(request, userA,
                                  project_hierarchy_userA_groupA,
                                  screen_hierarchy_userA_groupA):
    """
    Returns OMERO TagSets with Tag Children with Project Children

    Note: This returns a list of mixed objects in a specified order
    """

    project = project_hierarchy_userA_groupA[0]
    dataset = project_hierarchy_userA_groupA[2]
    image = project_hierarchy_userA_groupA[4]
    screen = screen_hierarchy_userA_groupA[0]
    plate = screen_hierarchy_userA_groupA[2]
    acq = screen_hierarchy_userA_groupA[3]

    # Create and name all the objects
    tagsetA = TagAnnotationI()
    tagsetA.textValue = rstring('TagsetA')
    tagsetA.ns = rstring('openmicroscopy.org/omero/insight/tagset')
    tagA = TagAnnotationI()
    tagA.textValue = rstring('TagA')

    # Link them together like so:
    # tagsetA
    #   tagA
    #       projectA

    tagsetA.linkAnnotation(tagA)

    to_save = [tagsetA]
    conn = get_connection(userA)
    tagsets = conn.getUpdateService().saveAndReturnArray(to_save)
    tagsets.sort(cmp_id)

    tags = tagsets[0].linkedAnnotationList()
    tags.sort(cmp_id)

    project_link = ProjectAnnotationLinkI()
    project_link.parent = project
    project_link.child = tags[0]

    dataset_link = DatasetAnnotationLinkI()
    dataset_link.parent = dataset
    dataset_link.child = tags[0]

    image_link = ImageAnnotationLinkI()
    image_link.parent = image
    image_link.child = tags[0]

    screen_link = ScreenAnnotationLinkI()
    screen_link.parent = screen
    screen_link.child = tags[0]

    plate_link = PlateAnnotationLinkI()
    plate_link.parent = plate
    plate_link.child = tags[0]

    acq_link = PlateAcquisitionAnnotationLinkI()
    acq_link.parent = acq
    acq_link.child = tags[0]

    to_save = [project_link, dataset_link, image_link, screen_link, plate_link,
               acq_link]

    links = conn.getUpdateService().saveAndReturnArray(to_save)

    # links is: project, dataset, image, screen, plate, acquisition
    return tagsets + tags + [link.parent for link in links]


@pytest.fixture(scope='function')
def tagset_hierarchy_userB_groupA(request, userA,
                                  project_hierarchy_userA_groupA,
                                  screen_hierarchy_userA_groupA):
    """
    Returns OMERO TagSets with Tag Children with Project Children

    Note: This returns a list of mixed objects in a specified order
    """

    project = project_hierarchy_userA_groupA[0]
    dataset = project_hierarchy_userA_groupA[2]
    image = project_hierarchy_userA_groupA[4]
    screen = screen_hierarchy_userA_groupA[0]
    plate = screen_hierarchy_userA_groupA[2]
    acq = screen_hierarchy_userA_groupA[3]

    # Create and name all the objects
    tagsetA = TagAnnotationI()
    tagsetA.textValue = rstring('TagsetA')
    tagsetA.ns = rstring('openmicroscopy.org/omero/insight/tagset')
    tagA = TagAnnotationI()
    tagA.textValue = rstring('TagA')

    # Link them together like so:
    # tagsetA
    #   tagA
    #       projectA

    tagsetA.linkAnnotation(tagA)

    to_save = [tagsetA]
    conn = get_connection(userA)
    tagsets = conn.getUpdateService().saveAndReturnArray(to_save)
    tagsets.sort(cmp_id)

    tags = tagsets[0].linkedAnnotationList()
    tags.sort(cmp_id)

    project_link = ProjectAnnotationLinkI()
    project_link.parent = project
    project_link.child = tags[0]

    dataset_link = DatasetAnnotationLinkI()
    dataset_link.parent = dataset
    dataset_link.child = tags[0]

    image_link = ImageAnnotationLinkI()
    image_link.parent = image
    image_link.child = tags[0]

    screen_link = ScreenAnnotationLinkI()
    screen_link.parent = screen
    screen_link.child = tags[0]

    plate_link = PlateAnnotationLinkI()
    plate_link.parent = plate
    plate_link.child = tags[0]

    acq_link = PlateAcquisitionAnnotationLinkI()
    acq_link.parent = acq
    acq_link.child = tags[0]

    to_save = [project_link, dataset_link, image_link, screen_link, plate_link,
               acq_link]

    links = conn.getUpdateService().saveAndReturnArray(to_save)

    # links is: project, dataset, image, screen, plate, acquisition
    return tagsets + tags + [link.parent for link in links]


# # Cross-linked project hierarchy
# @pytest.fixture(scope='function')
# def project_hierarchy_crosslink(request, itest, groupC, userC, userD):
#     """
#     Returns OMERO Projects with Dataset Children with Image Children

#     Note: This returns a list of mixed objects in a specified order
#     """

#     # Create and name a project as userC
#     projectA = ProjectI()
#     projectA.name = rstring('ProjectA')
#     projectA = get_update_service(userC).saveAndReturnObject(projectA)

#     # Create and name a dataset as userD
#     datasetA = DatasetI()
#     datasetA.name = rstring('DatasetA')
#     datasetA = get_update_service(userD).saveAndReturnObject(datasetA)

#     # Create and name an image as userC
#     imageA = itest.new_image(name='ImageA')
#     imageA = get_update_service(userC).saveAndReturnObject(imageA)

#     # Link them together like so:
#     # projectA
#     #   (UserC's Link)
#     #   datasetA
#     #       (UserD's Link)
#     #       imageA

#     # Link the project with the dataset
#     projectA.linkDataset(datasetA)
#     projectA = get_update_service(userC).saveAndReturnObject(projectA)
#     datasetA = projectA.linkedDatasetList()[0]

#     # Link the dataset with the image
#     datasetA.linkImage(imageA)
#     datasetA = get_update_service(userD).saveAndReturnObject(datasetA)
#     imageA = datasetA.linkedImageList()[0]

#     return [projectA, datasetA, imageA]


class TestTree(lib.ITest):
    """
    Tests to ensure that OMERO.web "tree" infrastructure is working
    correctly.

    These tests make __extensive__ use of pytest fixtures.  In particular
    the scoping semantics allowing re-use of instances populated by the
    *request fixtures.  It is recommended that the pytest fixture
    documentation be studied in detail before modifications or attempts to
    fix failing tests are made:

     * https://pytest.org/latest/fixture.html
    """

    # @classmethod
    # def setup_class(cls):
    #     """Returns a logged in Django test client."""
    #     super(TestTree, cls).setup_class()
    #     cls.names = ('Apple', 'bat', 'atom', 'Butter')

    # def setup_method(self, method):
    #     self.client = self.new_client(perms='rwr---')
    #     self.conn = BlitzGateway(client_obj=self.client)
    #     self.update = self.client.getSession().getUpdateService()

    # Create a read-only group
    @pytest.fixture(scope='function')
    def groupA(self):
        """Returns a new read-only group."""
        return self.new_group(perms='rwr---')

    # Create a read-only group
    @pytest.fixture(scope='function')
    def groupB(self):
        """Returns a new read-only group."""
        return self.new_group(perms='rwr---')

    # Create a read-write group
    @pytest.fixture()
    def groupC(self):
        """Returns a new read-only group."""
        return self.new_group(perms='rwrw--')

    # Create users in the read-only group
    @pytest.fixture()
    def userA(self, groupA, groupB):
        """Returns a new user in the groupA group and also add to groupB"""
        user = self.new_client_and_user(group=groupA)
        self.add_groups(user[1], [groupB])
        return user

    @pytest.fixture()
    def userB(self, groupA):
        """Returns another new user in the read-only group."""
        return self.new_client_and_user(group=groupA)

    @pytest.fixture()
    def userC(self, groupC):
        """Returns a new user in the read-write group."""
        return self.new_client_and_user(group=groupC)

    @pytest.fixture()
    def userD(self, groupC):
        """Returns another new user in the read-write group."""
        return self.new_client_and_user(group=groupC)
    ### Projects ###
    # @pytest.fixture()
    # def projects_userA_groupA(self, names1, userA,
    #                           project_hierarchy_userA_groupA):
    #     """
    #     Returns new OMERO Projects with required fields set and with names
    #     that can be used to exercise sorting semantics.
    #     """
    #     to_save = []
    #     for name in names1:
    #         project = ProjectI()
    #         project.name = rstring(name)
    #         to_save.append(project)
    #     projects = get_update_service(userA).saveAndReturnArray(to_save)
    #     projects.extend(project_hierarchy_userA_groupA[:2])
    #     projects.sort(cmp_name_insensitive)
    #     return projects

    # @pytest.fixture()
    # def projects_userB_groupA(self, names2, userB):
    #     """
    #     Returns a new OMERO Project with required fields set and with a name
    #     that can be used to exercise sorting semantics.
    #     """
    #     to_save = []
    #     for name in names2:
    #         project = ProjectI()
    #         project.name = rstring(name)
    #         to_save.append(project)
    #     projects = get_update_service(userB).saveAndReturnArray(
    #         to_save)
    #     projects.sort(cmp_name_insensitive)
    #     return projects

    ### Images ###
    @pytest.fixture()
    def images_userA_groupA(self, userA):
        """
        Returns new OMERO Images for userA in groupA
        """
        to_save = []
        for name in ['Neon', 'hydrogen', 'Helium', 'boron']:
            image = self.new_image(name=name)
            to_save.append(image)

        images = get_update_service(userA).saveAndReturnArray(to_save)
        images.sort(cmp_name_insensitive)
        return images

    @pytest.fixture()
    def image_pixels_userA(self, userA):
        """
        Returns a new image with pixels of fixed dimensions
        """
        sf = userA[0].sf
        image = self.createTestImage(sizeX=50, sizeY=50, sizeZ=5, session=sf)
        return image

    @pytest.fixture(scope='function')
    def images_userB_groupA(self, userB):
        """
        Returns new OMERO Images for userB in groupA
        """
        to_save = []
        for name in ['Oxygen', 'nitrogen']:
            image = self.new_image(name=name)
            to_save.append(image)

        images = get_update_service(userB).saveAndReturnArray(to_save)
        images.sort(cmp_name_insensitive)
        return images

    @pytest.fixture()
    def images_userA_groupB(self, userA, groupB):
        """
        Returns new OMERO Images for userA in groupB
        """
        to_save = []
        for name in ['Zinc', 'aluminium']:
            image = self.new_image(name=name)
            to_save.append(image)

        conn = get_connection(userA, groupB.id.val)
        images = conn.getUpdateService().saveAndReturnArray(to_save,
                                                            conn.SERVICE_OPTS)
        images.sort(cmp_name_insensitive)
        return images

    @pytest.fixture()
    def project_hierarchy_userA_groupA(self, userA):
        """
        Returns OMERO Projects with Dataset Children with Image Children

        Note: This returns a list of mixed objects in a specified order
        """

        # Create and name all the objects
        projectA = ProjectI()
        projectA.name = rstring('ProjectA')
        projectB = ProjectI()
        projectB.name = rstring('ProjectB')
        datasetA = DatasetI()
        datasetA.name = rstring('DatasetA')
        datasetB = DatasetI()
        datasetB.name = rstring('DatasetB')
        imageA = self.new_image(name='ImageA')
        imageB = self.new_image(name='ImageB')

        # Link them together like so:
        # projectA
        #   datasetA
        #       imageA
        #       imageB
        #   datasetB
        #       imageB
        # projectB
        #   datasetB
        #       imageB
        projectA.linkDataset(datasetA)
        projectA.linkDataset(datasetB)
        projectB.linkDataset(datasetB)
        datasetA.linkImage(imageA)
        datasetA.linkImage(imageB)
        datasetB.linkImage(imageB)

        to_save = [projectA, projectB]
        projects = get_update_service(userA).saveAndReturnArray(to_save)
        projects.sort(cmp_name_insensitive)

        datasets = projects[0].linkedDatasetList()
        datasets.sort(cmp_name_insensitive)

        images = datasets[0].linkedImageList()
        images.sort(cmp_name_insensitive)

        return projects + datasets + images

    # Cross-linked project hierarchy
    @pytest.fixture(scope='function')
    def project_hierarchy_crosslink(self, groupC, userC, userD):
        """
        Returns OMERO Projects with Dataset Children with Image Children

        Note: This returns a list of mixed objects in a specified order
        """

        # Create and name a project as userC
        projectA = ProjectI()
        projectA.name = rstring('ProjectA')
        projectA = get_update_service(userC).saveAndReturnObject(projectA)

        # Create and name a dataset as userD
        datasetA = DatasetI()
        datasetA.name = rstring('DatasetA')
        datasetA = get_update_service(userD).saveAndReturnObject(datasetA)

        # Create and name an image as userC
        imageA = self.new_image(name='ImageA')
        imageA = get_update_service(userC).saveAndReturnObject(imageA)

        # Link them together like so:
        # projectA
        #   datasetA
        #       imageA

        # Link the project with the dataset
        # link = ProjectDatasetLinkI()
        # link.setParent(ProjectI(projectA.getId(), False))
        # link.setChild(DatasetI(datasetA.getId(), False))
        # get_update_service(userC).saveObject(link)

        # Link the dataset with the image
        # link = DatasetImageLinkI()
        # link.setParent(DatasetI(datasetA.getId(), False))
        # link.setChild(ImageI(imageA.getId(), False))
        # get_update_service(userD).saveObject(link)

        # In order that we have child objects loaded, add links like this...
        projectA.linkDataset(datasetA)
        projectA = get_update_service(userC).saveAndReturnObject(projectA)
        # datasetA = projectA.linkedDatasetList()[0]

        datasetA.linkImage(imageA)
        datasetA = get_update_service(userD).saveAndReturnObject(datasetA)
        imageA = datasetA.linkedImageList()[0]

        return [projectA, datasetA, imageA]

    ### TESTS ###
    def test_marshal_experimenter(self, userA):
        """
        Test marshalling experimenter
        """
        conn = get_connection(userA)
        expected = expected_experimenter(userA)
        marshaled = marshal_experimenter(conn, userA[1].id.val)
        assert marshaled == expected

    # TODO Testing experimenters is difficult because the database of users
    # is a moving target

    # def test_marshal_experimenters(self, userA):
    #     conn = get_connection(userA)
    #     marshaled = marshal_experimenters(conn)
    #     for x in marshaled:
    #         print x
    #     assert False

    # TODO Testing groups is difficult for the same reason as experimenters

    # def test_marshal_groups(self, userA):
    #     conn = get_connection(userA)
    #     marshaled = marshal_groups(conn, member_id=userA[1].id.val)
    #     for x in marshaled:
    #         print x
    #     assert False

    def test_marshal_projects_no_results(self, userA):
        """
        Test marshalling projects where there are none
        """
        conn = get_connection(userA)
        assert marshal_projects(conn=conn, experimenter_id=-2) == []

    def test_marshal_projects_user(self, userA, projects_userA_groupA):
        """
        Test marshalling user's own projects in current group
        """
        conn = get_connection(userA)
        expected = expected_projects(userA, projects_userA_groupA)
        marshaled = marshal_projects(conn=conn,
                                     experimenter_id=userA[1].id.val)
        assert marshaled == expected

    def test_marshal_projects_another_user(self, userA, userB,
                                           projects_userB_groupA):
        """
        Test marshalling another user's projects in current group
        Project is Owned by userB. We are testing userA's perms.
        """
        conn = get_connection(userA)
        expected = expected_projects(userA, projects_userB_groupA)
        marshaled = marshal_projects(conn=conn,
                                     experimenter_id=userB[1].id.val)
        assert marshaled == expected

    def test_marshal_projects_another_group(self, userA, groupB,
                                            projects_userA_groupB):
        """
        Test marshalling user's projects in another group
        """
        conn = get_connection(userA)
        expected = expected_projects(userA, projects_userA_groupB)
        marshaled = marshal_projects(conn=conn,
                                     group_id=groupB.id.val,
                                     experimenter_id=userA[1].id.val)
        assert marshaled == expected

    def test_marshal_projects_all_groups(self, userA, projects_userA):
        """
        Test marshalling all projects for a user regardless of group
        """
        conn = get_connection(userA)
        expected = expected_projects(userA, projects_userA)
        marshaled = marshal_projects(conn=conn,
                                     group_id=-1,
                                     experimenter_id=userA[1].id.val)
        assert marshaled == expected

    def test_marshal_projects_all_users(self, userA, groupA, projects_groupA):
        """
        Test marshalling all projects for a group regardless of user
        """
        conn = get_connection(userA)
        expected = expected_projects(userA, projects_groupA)
        marshaled = marshal_projects(conn=conn,
                                     group_id=groupA.id.val,
                                     experimenter_id=-1)
        assert marshaled == expected

    def test_marshal_projects_all_groups_all_users(self, userA, projects):
        """
        Test marshalling all projects for all users regardless of group
        """
        conn = get_connection(userA)
        expected = expected_projects(userA, projects)
        marshaled = marshal_projects(conn=conn,
                                     group_id=-1,
                                     experimenter_id=-1)
        assert marshaled == expected

    # Datasets
    def test_marshal_datasets_no_results(self, userA):
        '''
        Test marshalling datasets where there are none
        '''
        conn = get_connection(userA)
        assert marshal_datasets(conn, -1) == []

    def test_marshal_datasets_user(self, userA, datasets_userA_groupA):
        """
        Test marshalling user's own datasets without project parents
        in current group
        """
        conn = get_connection(userA)
        expected = expected_datasets(userA, datasets_userA_groupA)
        marshaled = marshal_datasets(conn=conn,
                                     experimenter_id=userA[1].id.val)
        assert marshaled == expected

    def test_marshal_datasets_another_user(self, userA, userB,
                                           datasets_userB_groupA):
        """
        Test marshalling another user's datasets without project parents
        in current group
        """
        conn = get_connection(userA)
        expected = expected_datasets(userA, datasets_userB_groupA)
        marshaled = marshal_datasets(conn=conn,
                                     experimenter_id=userB[1].id.val)
        assert marshaled == expected

    def test_marshal_datasets_another_group(self, userA, groupB,
                                            datasets_userA_groupB):
        """
        Test marshalling user's own datasets without project parents
        in another group
        """
        conn = get_connection(userA)
        expected = expected_datasets(userA, datasets_userA_groupB)
        marshaled = marshal_datasets(conn=conn,
                                     experimenter_id=userA[1].id.val,
                                     group_id=groupB.id.val)
        assert marshaled == expected

    def test_marshal_datasets_all_groups(self, userA, datasets_userA):
        """
        Test marshalling all datasets without project parents for a
        user regardless of group
        """
        conn = get_connection(userA)
        expected = expected_datasets(userA, datasets_userA)
        marshaled = marshal_datasets(conn=conn,
                                     experimenter_id=userA[1].id.val,
                                     group_id=-1)
        assert marshaled == expected

    def test_marshal_datasets_all_users(self, userA, groupA, datasets_groupA):
        """
        Test marshalling all datasets for a group regardless of user
        """
        conn = get_connection(userA)
        expected = expected_datasets(userA, datasets_groupA)
        marshaled = marshal_datasets(conn=conn,
                                     group_id=groupA.id.val)
        assert marshaled == expected

    def test_marshal_datasets_all_groups_all_users(self, userA, datasets):
        """
        Test marshalling all datasets for all users regardless of group
        """
        conn = get_connection(userA)
        expected = expected_datasets(userA, datasets)
        marshaled = marshal_datasets(conn=conn,
                                     group_id=-1)
        assert marshaled == expected

    def test_marshal_datasets_project(self, userA,
                                      project_hierarchy_userA_groupA):
        """
        Test marshalling datasets for userA, groupA, projectA
        """
        conn = get_connection(userA)
        project = project_hierarchy_userA_groupA[0]
        datasets = project_hierarchy_userA_groupA[2:4]
        expected = expected_datasets(userA, datasets)
        marshaled = marshal_datasets(conn=conn,
                                     project_id=project.id.val)
        assert marshaled == expected

    def test_marshal_datasets_project_crosslink(self, userC,
                                                project_hierarchy_crosslink):
        """
        Test marshalling crosslinked datasets
        """
        conn = get_connection(userC)
        project = project_hierarchy_crosslink[0]
        dataset = project_hierarchy_crosslink[1]
        expected = expected_datasets(userC, [dataset])
        marshaled = marshal_datasets(conn=conn,
                                     project_id=project.id.val)
        assert marshaled == expected

    # Images
    def test_marshal_images_no_results(self, userA):
        '''
        Test marshalling images where there are none
        '''
        conn = get_connection(userA)
        assert marshal_images(conn, -1) == []

    def test_marshal_images_user(self, userA, images_userA_groupA):
        # TODO Add fixture to create plate images to ensure these are not
        # being returned as orphans
        """
        Test marshalling user's own orphaned images in current group
        """
        conn = get_connection(userA)
        expected = expected_images(userA, images_userA_groupA)
        marshaled = marshal_images(conn=conn,
                                   experimenter_id=userA[1].id.val)
        assert marshaled == expected

    def test_marshal_images_user_pixels(self, userA, image_pixels_userA):
        """
        Test marshalling image, loading pixels
        """
        conn = get_connection(userA)
        expected = expected_images(userA, [image_pixels_userA])
        expected[0]['sizeX'] = 50
        expected[0]['sizeY'] = 50
        expected[0]['sizeZ'] = 5
        marshaled = marshal_images(conn=conn,
                                   load_pixels=True,
                                   experimenter_id=userA[1].id.val)
        assert marshaled == expected

    def test_marshal_images_another_user(self, userA, userB,
                                         images_userB_groupA):
        """
        Test marshalling another user's orphaned images in current group
        """
        conn = get_connection(userA)
        expected = expected_images(userA, images_userB_groupA)
        marshaled = marshal_images(conn=conn,
                                   experimenter_id=userB[1].id.val)
        assert marshaled == expected

    def test_marshal_images_another_group(self, userA, groupB,
                                          images_userA_groupB):
        """
        Test marshalling user's own orphaned images in another group
        """
        conn = get_connection(userA)
        expected = expected_images(userA, images_userA_groupB)
        marshaled = marshal_images(conn=conn,
                                   experimenter_id=userA[1].id.val,
                                   group_id=groupB.id.val)
        assert marshaled == expected

    def test_marshal_images_all_groups(self, userA, images_userA):
        """
        Test marshalling all orphaned images for a user regardless of group
        """
        conn = get_connection(userA)
        expected = expected_images(userA, images_userA)
        marshaled = marshal_images(conn=conn,
                                   experimenter_id=userA[1].id.val,
                                   group_id=-1)
        assert marshaled == expected

    def test_marshal_images_all_users(self, userA, groupA, images_groupA):
        """
        Test marshalling all orphaned images for a group regardless of user
        """
        conn = get_connection(userA)
        expected = expected_images(userA, images_groupA)
        marshaled = marshal_images(conn=conn,
                                   group_id=groupA.id.val)
        assert marshaled == expected

    def test_marshal_images_all_groups_all_users(self, userA, images):
        """
        Test marshalling all orphaned images for all users regardless of group
        """
        conn = get_connection(userA)
        expected = expected_images(userA, images)
        marshaled = marshal_images(conn=conn,
                                   group_id=-1)
        assert marshaled == expected

    def test_marshal_images_dataset(self, userA,
                                    project_hierarchy_userA_groupA):
        """
        Test marshalling images for userA, groupA, datasetA
        """
        conn = get_connection(userA)
        dataset = project_hierarchy_userA_groupA[2]
        images = project_hierarchy_userA_groupA[4:6]
        expected = expected_images(userA, images)
        marshaled = marshal_images(conn=conn,
                                   dataset_id=dataset.id.val)
        assert marshaled == expected

    def test_marshal_images_dataset_crosslink(self, userC,
                                              project_hierarchy_crosslink):
        """
        Test marshalling crosslinked images
        """
        conn = get_connection(userC)
        dataset = project_hierarchy_crosslink[1]
        image = project_hierarchy_crosslink[2]
        expected = expected_images(userC, [image])
        marshaled = marshal_images(conn=conn,
                                   dataset_id=dataset.id.val)
        assert marshaled == expected

    def test_marshal_images_share(self, userA, shares_userA_owned,
                                  images_userA_groupA):
        """
        Test marshalling images for shareA
        """
        conn = get_connection(userA)
        share = shares_userA_owned[0]
        images = images_userA_groupA
        expected = expected_images(userA, images)
        marshaled = marshal_images(conn=conn,
                                   share_id=share.id.val)
        assert marshaled == expected

    # Screens
    def test_marshal_screens_no_results(self, userA):
        '''
        Test marshalling screens where there are none
        '''
        conn = get_connection(userA)
        assert marshal_screens(conn, -1) == []

    def test_marshal_screens_user(self, userA, screens_userA_groupA):
        """
        Test marshalling user's own orphaned screens in current group
        """
        conn = get_connection(userA)
        expected = expected_screens(userA, screens_userA_groupA)
        marshaled = marshal_screens(conn=conn,
                                    experimenter_id=userA[1].id.val)
        assert marshaled == expected

    def test_marshal_screens_another_user(self, userA, userB,
                                          screens_userB_groupA):
        """
        Test marshalling another user's orphaned screens in current group
        """
        conn = get_connection(userA)
        expected = expected_screens(userA, screens_userB_groupA)
        marshaled = marshal_screens(conn=conn,
                                    experimenter_id=userB[1].id.val)
        assert marshaled == expected

    def test_marshal_screens_another_group(self, userA, groupB,
                                           screens_userA_groupB):
        """
        Test marshalling user's own orphaned screens in another group
        """
        conn = get_connection(userA)
        expected = expected_screens(userA, screens_userA_groupB)
        marshaled = marshal_screens(conn=conn,
                                    experimenter_id=userA[1].id.val,
                                    group_id=groupB.id.val)
        assert marshaled == expected

    def test_marshal_screens_all_groups(self, userA, screens_userA):
        """
        Test marshalling all orphaned screens for a user regardless of group
        """
        conn = get_connection(userA)
        expected = expected_screens(userA, screens_userA)
        marshaled = marshal_screens(conn=conn,
                                    experimenter_id=userA[1].id.val,
                                    group_id=-1)
        assert marshaled == expected

    def test_marshal_screens_all_users(self, userA, groupA, screens_groupA):
        """
        Test marshalling all orphaned screens for a group regardless of user
        """
        conn = get_connection(userA)
        expected = expected_screens(userA, screens_groupA)
        marshaled = marshal_screens(conn=conn,
                                    group_id=groupA.id.val)
        assert marshaled == expected

    def test_marshal_screens_all_groups_all_users(self, userA, screens):
        """
        Test marshalling all orphaned screens for all users regardless of group
        """
        conn = get_connection(userA)
        expected = expected_screens(userA, screens)
        marshaled = marshal_screens(conn=conn,
                                    group_id=-1)
        assert marshaled == expected

    # Plates
    def test_marshal_plates_no_results(self, userA):
        '''
        Test marshalling plates where there are none
        '''
        conn = get_connection(userA)
        assert marshal_plates(conn=conn,
                              screen_id=-1) == []

    def test_marshal_plates_user(self, userA, plates_userA_groupA):
        """
        Test marshalling user's own orphaned plates in current group
        """
        conn = get_connection(userA)
        expected = expected_plates(userA, plates_userA_groupA)
        marshaled = marshal_plates(conn=conn,
                                   experimenter_id=userA[1].id.val)
        assert marshaled == expected

    def test_marshal_plates_another_user(self, userA, userB,
                                         plates_userB_groupA):
        """
        Test marshalling another user's orphaned plates in current group
        """
        conn = get_connection(userA)
        expected = expected_plates(userA, plates_userB_groupA)
        marshaled = marshal_plates(conn=conn,
                                   experimenter_id=userB[1].id.val)
        assert marshaled == expected

    def test_marshal_plates_another_group(self, userA, groupB,
                                          plates_userA_groupB):
        """
        Test marshalling user's own orphaned plates in another group
        """
        conn = get_connection(userA)
        expected = expected_plates(userA, plates_userA_groupB)
        marshaled = marshal_plates(conn=conn,
                                   experimenter_id=userA[1].id.val,
                                   group_id=groupB.id.val)
        assert marshaled == expected

    def test_marshal_plates_all_groups(self, userA, plates_userA):
        """
        Test marshalling all orphaned plates for a user regardless of group
        """
        conn = get_connection(userA)
        expected = expected_plates(userA, plates_userA)
        marshaled = marshal_plates(conn=conn,
                                   experimenter_id=userA[1].id.val,
                                   group_id=-1)
        assert marshaled == expected

    def test_marshal_plates_all_users(self, userA, groupA, plates_groupA):
        """
        Test marshalling all orphaned plates for a group regardless of user
        """
        conn = get_connection(userA)
        expected = expected_plates(userA, plates_groupA)
        marshaled = marshal_plates(conn=conn,
                                   group_id=groupA.id.val)
        assert marshaled == expected

    def test_marshal_plates_all_groups_all_users(self, userA, plates):
        """
        Test marshalling all orphaned plates for all users regardless of group
        """
        conn = get_connection(userA)
        expected = expected_plates(userA, plates)
        marshaled = marshal_plates(conn=conn,
                                   group_id=-1)
        assert marshaled == expected

    # PlateAcquisitions
    # There are no orphan PlateAcquisitions so all the tests are conducted
    # on data from hierarchies and there are fewer because querying cross-user
    # and cross-group have no real meaning here. Thus there is no need for
    # separate tests of the hierarchy either.

    def test_marshal_plate_acquisitions_no_results(self, userA):
        """
        Test marshalling plate acquisitions where there are none
        """
        conn = get_connection(userA)
        assert marshal_plate_acquisitions(conn, -1) == []

    def test_marshal_plate_acquisitions_user(self, userA,
                                             screen_hierarchy_userA_groupA):
        """
        Test marshalling user's own plate acquisitions in current group for
        plateA
        """
        conn = get_connection(userA)
        plate = screen_hierarchy_userA_groupA[2]
        acqs = screen_hierarchy_userA_groupA[3:6]
        expected = expected_plate_acquisitions(userA, acqs)
        marshaled = marshal_plate_acquisitions(conn=conn,
                                               plate_id=plate.id.val)
        assert marshaled == expected

    def test_marshal_plate_acquisitions_another_user(
            self, userA, screen_hierarchy_userB_groupA):
        """
        Test marshalling another user's plate acquisitions in current group
        for plateB
        """
        conn = get_connection(userA)
        plate = screen_hierarchy_userB_groupA[1]
        acqs = [screen_hierarchy_userB_groupA[2]]
        expected = expected_plate_acquisitions(userA, acqs)
        marshaled = marshal_plate_acquisitions(conn=conn,
                                               plate_id=plate.id.val)
        assert marshaled == expected

    def test_marshal_plate_acquisitions_another_group(
            self, userA, groupB, screen_hierarchy_userA_groupB):
        """
        Test marshalling user's own orphaned plate acquisitions in another
        group for plateC
        """
        conn = get_connection(userA)
        plate = screen_hierarchy_userA_groupB[1]
        acqs = [screen_hierarchy_userA_groupB[2]]
        expected = expected_plate_acquisitions(userA, acqs)
        marshaled = marshal_plate_acquisitions(conn=conn,
                                               plate_id=plate.id.val)
        assert marshaled == expected

    # Orphaned
    def test_marshal_orphaned_no_results(self, userA):
        '''
        Test marshalling orphaned container for unknown user
        '''
        conn = get_connection(userA)
        expected = {
            'id': -2L,
            'childCount': 0
        }
        marshaled = marshal_orphaned(conn=conn,
                                     experimenter_id=-2)
        assert marshaled == expected

    def test_marshal_orphaned(self, userA, images_userA_groupA):
        """
        Test marshalling user's orphaned container
        """
        conn = get_connection(userA)
        expected = expected_orphaned(userA, images_userA_groupA)
        marshaled = marshal_orphaned(conn=conn,
                                     experimenter_id=userA[1].id.val)
        assert marshaled == expected

    def test_marshal_orphaned_another_user(self, userA, userB,
                                           images_userB_groupA):
        """
        Test marshalling another user's orphaned container
        """
        conn = get_connection(userA)
        expected = expected_orphaned(userB, images_userB_groupA)
        marshaled = marshal_orphaned(conn=conn,
                                     experimenter_id=userB[1].id.val)
        assert marshaled == expected

    def test_marshal_orphaned_another_group(self, userA, groupB,
                                            images_userA_groupB):
        """
        Test marshalling user's orphaned container
        """
        conn = get_connection(userA)
        expected = expected_orphaned(userA, images_userA_groupB)
        marshaled = marshal_orphaned(conn=conn,
                                     experimenter_id=userA[1].id.val,
                                     group_id=groupB.id.val)
        assert marshaled == expected

    def test_marshal_orphaned_all_groups(self, userA, groupB,
                                         images_userA):
        """
        Test marshalling user's orphaned container

        """
        conn = get_connection(userA)
        expected = expected_orphaned(userA, images_userA)
        marshaled = marshal_orphaned(conn=conn,
                                     experimenter_id=userA[1].id.val,
                                     group_id=-1)
        assert marshaled == expected

    # Tags
    def test_marshal_tags_no_results(self, userA):
        conn = get_connection(userA)
        marshaled = marshal_tags(conn=conn,
                                 experimenter_id=-2)
        assert marshaled == []

    def test_marshal_tags_user(self, userA, tags_userA_groupA):
        """
        Test marshalling user's own tags in current group
        """
        conn = get_connection(userA)
        expected = expected_tags(userA, tags_userA_groupA)
        marshaled = marshal_tags(conn=conn,
                                 experimenter_id=userA[1].id.val)
        assert marshaled == expected

    def test_marshal_tags_another_user(self, userA, userB,
                                       tags_userB_groupA):
        """
        Test marshalling another user's tags in current group
        """
        conn = get_connection(userA)
        expected = expected_tags(userA, tags_userB_groupA)
        marshaled = marshal_tags(conn=conn,
                                 experimenter_id=userB[1].id.val)
        assert marshaled == expected

    def test_marshal_tags_another_group(self, userA, groupB,
                                        tags_userA_groupB):
        """
        Test marshalling user's tags in another group
        """
        conn = get_connection(userA)
        expected = expected_tags(userA, tags_userA_groupB)
        marshaled = marshal_tags(conn=conn,
                                 experimenter_id=userA[1].id.val,
                                 group_id=groupB.id.val)
        assert marshaled == expected

    def test_marshal_tags_all_groups(self, userA, tags_userA):
        """
        Test marshalling all tags for a user regardless of group
        """
        conn = get_connection(userA)
        expected = expected_tags(userA, tags_userA)
        marshaled = marshal_tags(conn=conn,
                                 experimenter_id=userA[1].id.val,
                                 group_id=-1)
        assert marshaled == expected

    def test_marshal_tags_all_users(self, userA, groupA, tags_groupA):
        """
        Test marshalling all tags for a group regardless of user
        """
        conn = get_connection(userA)
        expected = expected_tags(userA, tags_groupA)
        marshaled = marshal_tags(conn=conn,
                                 group_id=groupA.id.val)
        assert marshaled == expected

    def test_marshal_tags_all_groups_all_users(self, userA, tags):
        """
        Test marshalling all tags for all users regardless of group
        """
        conn = get_connection(userA)
        expected = expected_tags(userA, tags)
        marshaled = marshal_tags(conn=conn,
                                 group_id=-1)
        assert marshaled == expected

    def test_marshal_tags_tagset(self, userA, tagset_hierarchy_userA_groupA):
        """
        Test marshalling tags for userA in groupA in tagsetA
        """
        conn = get_connection(userA)
        tagset = tagset_hierarchy_userA_groupA[0]
        tags = [tagset_hierarchy_userA_groupA[1]]
        expected = expected_tags(userA, tags)
        marshaled = marshal_tags(conn=conn,
                                 experimenter_id=userA[1].id.val,
                                 tag_id=tagset.id.val)
        assert marshaled == expected

    # Tagged
    def test_marshal_tagged_no_results(self, userA):
        '''
        Test marshalling tagged where there are none
        '''
        conn = get_connection(userA)
        assert marshal_tagged(conn=conn, tag_id=-2) == {
            'projects': [],
            'datasets': [],
            'images': [],
            'screens': [],
            'plates': [],
            'acquisitions': []
        }

    def test_marshal_tagged_user(self, userA, tagset_hierarchy_userA_groupA):
        """
        Test marshalling tagged data for userA in groupA in tagsetA
        """
        conn = get_connection(userA)
        tag = tagset_hierarchy_userA_groupA[1]
        projects = [tagset_hierarchy_userA_groupA[2]]
        datasets = [tagset_hierarchy_userA_groupA[3]]
        images = [tagset_hierarchy_userA_groupA[4]]
        screens = [tagset_hierarchy_userA_groupA[5]]
        plates = [tagset_hierarchy_userA_groupA[6]]
        acqs = [tagset_hierarchy_userA_groupA[7]]
        expected = expected_tagged(userA, projects, datasets, images,
                                   screens, plates, acqs)
        marshaled = marshal_tagged(conn=conn,
                                   experimenter_id=userA[1].id.val,
                                   tag_id=tag.id.val)
        assert marshaled == expected

    # Share
    def test_marshal_shares_user(self, userA, shares):
        """
        Test marshalling shares a user is a member of
        """
        conn = get_connection(userA)
        expected = expected_shares(userA, shares)
        marshaled = marshal_shares(conn=conn,
                                   member_id=userA[1].id.val)
        assert marshaled == expected

    def test_marshal_shares_another_user(self, userA, userB, shares):
        """
        Test marshalling shares another user is a member of
        """
        conn = get_connection(userA)
        expected = expected_shares(userA, shares)
        marshaled = marshal_shares(conn=conn,
                                   member_id=userB[1].id.val)
        assert marshaled == expected

    def test_marshal_shares_user_owned(self, userA, shares_userA_owned):
        """
        Test marshalling shares owned by a user
        """
        conn = get_connection(userA)
        expected = expected_shares(userA, shares_userA_owned)
        marshaled = marshal_shares(conn=conn,
                                   owner_id=userA[1].id.val)
        assert marshaled == expected

    def test_marshal_shares_another_user_owned(self, userA, userB,
                                               shares_userB_owned):
        """
        Test marshalling shares owned by another user
        """
        conn = get_connection(userA)
        expected = expected_shares(userA, shares_userB_owned)
        marshaled = marshal_shares(conn=conn,
                                   owner_id=userB[1].id.val)
        assert marshaled == expected

    # Discussion #
    def test_marshal_discussions_user(self, userA, discussions):
        """
        Test marshalling discussions a user is a member of
        """
        conn = get_connection(userA)
        expected = expected_discussions(userA, discussions)
        marshaled = marshal_discussions(conn=conn,
                                        member_id=userA[1].id.val)
        assert marshaled == expected

    def test_marshal_discussions_another_user(self, userA, userB, discussions):
        """
        Test marshalling discussions another user is a member of
        """
        conn = get_connection(userA)
        expected = expected_discussions(userA, discussions)
        marshaled = marshal_discussions(conn=conn,
                                        member_id=userB[1].id.val)
        assert marshaled == expected

    def test_marshal_discussions_user_owned(self, userA,
                                            discussions_userA_owned):
        """
        Test marshalling discussions owned by a user
        """
        conn = get_connection(userA)
        expected = expected_discussions(userA, discussions_userA_owned)
        marshaled = marshal_discussions(conn=conn,
                                        owner_id=userA[1].id.val)
        assert marshaled == expected

    def test_marshal_discussions_another_user_owned(self, userA, userB,
                                                    discussions_userB_owned):
        """
        Test marshalling discussions owned by another user
        """
        conn = get_connection(userA)
        expected = expected_discussions(userA, discussions_userB_owned)
        marshaled = marshal_discussions(conn=conn,
                                        owner_id=userB[1].id.val)
        assert marshaled == expected
