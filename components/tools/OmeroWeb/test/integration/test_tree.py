#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2014 Glencoe Software, Inc.
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
import test.integration.library as lib

from omero.gateway import BlitzGateway
from omero.model import ProjectI, DatasetI, ScreenI, PlateI, \
    PlateAcquisitionI
from omero.rtypes import rstring
from omeroweb.webclient.tree import marshal_datasets, marshal_plates, \
    marshal_projects, marshal_screens


def cmp_id(x, y):
    """Identifier comparator."""
    return cmp(x.id.val, y.id.val)


def cmp_name(x, y):
    """Name comparator."""
    return cmp(x.name.val, y.name.val)


def cmp_name_insensitive(x, y):
    """Case-insensitive name comparator."""
    return cmp(x.name.val.lower(), y.name.val.lower())


@pytest.fixture(scope='function')
def itest(request):
    """
    Returns a new L{test.integration.library.ITest} instance.  With
    attached finalizer so that pytest will clean it up.
    """
    o = lib.ITest()
    o.setup_method(None)

    def finalizer():
        o.teardown_method(None)
    request.addfinalizer(finalizer)
    return o


@pytest.fixture(scope='function')
def client(request, itest):
    """Returns a new user client in a read-only group."""
    # Use group read-only permissions (not private) by default
    return itest.new_client(perms='rwr---')


@pytest.fixture(scope='function')
def conn(request, client):
    """Returns a new OMERO gateway."""
    return BlitzGateway(client_obj=client)


@pytest.fixture(scope='function')
def update_service(request, client):
    """Returns a new OMERO update service."""
    return client.getSession().getUpdateService()


@pytest.fixture(scope='module')
def names(request):
    return ('Apple', 'bat', 'atom', 'Butter')


@pytest.fixture(scope='function')
def projects(request, itest, update_service, names):
    """
    Returns four new OMERO Projects with required fields set and with names
    that can be used to exercise sorting semantics.
    """
    to_save = [ProjectI(), ProjectI(), ProjectI(), ProjectI()]
    for index, project in enumerate(to_save):
        project.name = rstring(names[index])
    return update_service.saveAndReturnArray(to_save)


@pytest.fixture(scope='function')
def projects_different_users(request, itest, conn):
    """
    Returns two new OMERO Projects created by different users with
    required fields set.
    """
    client = conn.c
    group = conn.getGroupFromContext()._obj
    projects = list()
    # User that has already been created by the "client" fixture
    user, name = itest.user_and_name(client)
    itest.add_experimenters(group, [user])
    for name in (rstring(itest.uuid()), rstring(itest.uuid())):
        client, user = itest.new_client_and_user(group=group)
        try:
            project = ProjectI()
            project.name = name
            update_service = client.getSession().getUpdateService()
            projects.append(update_service.saveAndReturnObject(project))
        finally:
            client.closeSession()
    return projects


@pytest.fixture(scope='function')
def project_dataset(request, itest, update_service):
    """
    Returns a new OMERO Project and linked Dataset with required fields set.
    """
    project = ProjectI()
    project.name = rstring(itest.uuid())
    dataset = DatasetI()
    dataset.name = rstring(itest.uuid())
    project.linkDataset(dataset)
    return update_service.saveAndReturnObject(project)


@pytest.fixture(scope='function')
def project_dataset_image(request, itest, update_service):
    """
    Returns a new OMERO Project, linked Dataset and linked Image populated
    by an L{test.integration.library.ITest} instance with required fields
    set.
    """
    project = ProjectI()
    project.name = rstring(itest.uuid())
    dataset = DatasetI()
    dataset.name = rstring(itest.uuid())
    image = itest.new_image(name=itest.uuid())
    dataset.linkImage(image)
    project.linkDataset(dataset)
    return update_service.saveAndReturnObject(project)


@pytest.fixture(scope='function')
def projects_datasets(request, itest, update_service, names):
    """
    Returns four new OMERO Projects and four linked Datasets with required
    fields set and with names that can be used to exercise sorting semantics.
    """
    projects = [ProjectI(), ProjectI(), ProjectI(), ProjectI()]
    for index, project in enumerate(projects):
        project.name = rstring(names[index])
        datasets = [DatasetI(), DatasetI(), DatasetI(), DatasetI()]
        for index, dataset in enumerate(datasets):
            dataset.name = rstring(names[index])
            project.linkDataset(dataset)
    return update_service.saveAndReturnArray(projects)


@pytest.fixture(scope='function')
def datasets(request, itest, update_service, names):
    """
    Returns four new OMERO Datasets with required fields set and with names
    that can be used to exercise sorting semantics.
    """
    to_save = [DatasetI(), DatasetI(), DatasetI(), DatasetI()]
    for index, dataset in enumerate(to_save):
        dataset.name = rstring(names[index])
    # Non-orphaned Dataset to catch issues with queries where non-orphaned
    # datasets are included in the results.
    project = ProjectI()
    project.name = rstring(itest.uuid())
    dataset = DatasetI()
    dataset.name = rstring(itest.uuid())
    project.linkDataset(dataset)
    update_service.saveAndReturnObject(project)
    return update_service.saveAndReturnArray(to_save)


@pytest.fixture(scope='function')
def datasets_different_users(request, itest, conn):
    """
    Returns two new OMERO Datasets created by different users with
    required fields set.
    """
    client = conn.c
    group = conn.getGroupFromContext()._obj
    datasets = list()
    # User that has already been created by the "client" fixture
    user, name = itest.user_and_name(client)
    itest.add_experimenters(group, [user])
    for name in (rstring(itest.uuid()), rstring(itest.uuid())):
        client, user = itest.new_client_and_user(group=group)
        try:
            dataset = DatasetI()
            dataset.name = name
            update_service = client.getSession().getUpdateService()
            datasets.append(update_service.saveAndReturnObject(dataset))
        finally:
            client.closeSession()
    return datasets


@pytest.fixture(scope='function')
def screens(request, itest, update_service, names):
    """
    Returns four new OMERO Screens with required fields set and with names
    that can be used to exercise sorting semantics.
    """
    to_save = [ScreenI(), ScreenI(), ScreenI(), ScreenI()]
    for index, screen in enumerate(to_save):
        screen.name = rstring(names[index])
    return update_service.saveAndReturnArray(to_save)


@pytest.fixture(scope='function')
def screens_different_users(request, itest, conn):
    """
    Returns two new OMERO Screens created by different users with
    required fields set.
    """
    client = conn.c
    group = conn.getGroupFromContext()._obj
    screens = list()
    # User that has already been created by the "client" fixture
    user, name = itest.user_and_name(client)
    itest.add_experimenters(group, [user])
    for name in (rstring(itest.uuid()), rstring(itest.uuid())):
        client, user = itest.new_client_and_user(group=group)
        try:
            screen = ScreenI()
            screen.name = name
            update_service = client.getSession().getUpdateService()
            screens.append(update_service.saveAndReturnObject(screen))
        finally:
            client.closeSession()
    return screens


@pytest.fixture(scope='function')
def screen_plate_run(request, itest, update_service):
    """
    Returns a new OMERO Screen, linked Plate, and linked PlateAcquisition
    with all required fields set.
    """
    screen = ScreenI()
    screen.name = rstring(itest.uuid())
    plate = PlateI()
    plate.name = rstring(itest.uuid())
    plate_acquisition = PlateAcquisitionI()
    plate.addPlateAcquisition(plate_acquisition)
    screen.linkPlate(plate)
    return update_service.saveAndReturnObject(screen)


@pytest.fixture(scope='function')
def screens_plates_runs(request, itest, update_service):
    """
    Returns a two new OMERO Screens, two linked Plates, and two linked
    PlateAcquisitions with all required fields set.
    """
    screens = [ScreenI(), ScreenI()]
    for screen in screens:
        screen.name = rstring(itest.uuid())
        plates = [PlateI(), PlateI()]
        for plate in plates:
            plate.name = rstring(itest.uuid())
            plate_acquisitions = [PlateAcquisitionI(), PlateAcquisitionI()]
            for plate_acquisition in plate_acquisitions:
                plate.addPlateAcquisition(plate_acquisition)
            screen.linkPlate(plate)
    return update_service.saveAndReturnArray(screens)


@pytest.fixture(scope='function')
def screen_plate(request, itest, update_service):
    """
    Returns a new OMERO Screen and linked Plate with required fields set.
    """
    screen = ScreenI()
    screen.name = rstring(itest.uuid())
    plate = PlateI()
    plate.name = rstring(itest.uuid())
    screen.linkPlate(plate)
    return update_service.saveAndReturnObject(screen)


@pytest.fixture(scope='function')
def screens_plates(request, itest, update_service, names):
    """
    Returns four new OMERO Screens and four linked Plates with required
    fields set and with names that can be used to exercise sorting semantics.
    """
    screens = [ScreenI(), ScreenI(), ScreenI(), ScreenI()]
    for index, screen in enumerate(screens):
        screen.name = rstring(names[index])
        plates = [PlateI(), PlateI(), PlateI(), PlateI()]
        for index, plate in enumerate(plates):
            plate.name = rstring(names[index])
            screen.linkPlate(plate)
    return update_service.saveAndReturnArray(screens)


@pytest.fixture(scope='function')
def plates_different_users(request, itest, conn):
    """
    Returns two new OMERO Plates created by different users with
    required fields set.
    """
    client = conn.c
    group = conn.getGroupFromContext()._obj
    plates = list()
    # User that has already been created by the "client" fixture
    user, name = itest.user_and_name(client)
    itest.add_experimenters(group, [user])
    for name in (rstring(itest.uuid()), rstring(itest.uuid())):
        client, user = itest.new_client_and_user(group=group)
        try:
            plate = PlateI()
            plate.name = name
            update_service = client.getSession().getUpdateService()
            plates.append(update_service.saveAndReturnObject(plate))
        finally:
            client.closeSession()
    return plates


@pytest.fixture(scope='function')
def plates_runs(request, itest, update_service, names):
    """
    Returns a four new Plates, and two linked PlateAcquisitions with required
    fields set and with names that can be used to exercise sorting semantics.
    """
    plates = [PlateI(), PlateI(), PlateI(), PlateI()]
    for index, plate in enumerate(plates):
        plate.name = rstring(names[index])
        plate_acquisitions = [PlateAcquisitionI(), PlateAcquisitionI()]
        for plate_acquisition in plate_acquisitions:
            plate.addPlateAcquisition(plate_acquisition)
    # Non-orphaned Plate to catch issues with queries where non-orphaned
    # plates are included in the results.
    screen = ScreenI()
    screen.name = rstring(itest.uuid())
    plate = PlateI()
    plate.name = rstring(itest.uuid())
    screen.linkPlate(plate)
    update_service.saveAndReturnObject(screen)
    return update_service.saveAndReturnArray(plates)


@pytest.fixture(scope='function')
def plate_run(request, itest, update_service):
    """
    Returns a new OMERO Plate and linked PlateAcquisition with all required
    fields set.
    """
    plate = PlateI()
    plate.name = rstring(itest.uuid())
    plate_acquisition = PlateAcquisitionI()
    plate.addPlateAcquisition(plate_acquisition)
    return update_service.saveAndReturnObject(plate)


@pytest.fixture(scope='function')
def plate(request, itest, update_service):
    """
    Returns a new OMERO Plate with all required fields set.
    """
    plate = PlateI()
    plate.name = rstring(itest.uuid())
    return update_service.saveAndReturnObject(plate)


class TestTree(object):
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

    def test_marshal_project_dataset(self, conn, project_dataset):
        project_id = project_dataset.id.val
        dataset, = project_dataset.linkedDatasetList()
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        expected = [{
            'id': project_id,
            'childCount': 1L,
            'name': project_dataset.name.val,
            'isOwned': True,
            'datasets': [{
                'childCount': 0L,
                'id': dataset.id.val,
                'isOwned': True,
                'name': dataset.name.val,
                'permsCss': perms_css
            }],
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn, conn.getUserId())
        assert marshaled == expected

    def test_marshal_projects_datasetsew(self, conn, projects_datasets):
        project_a, project_b, project_c, project_d = projects_datasets
        expected = list()
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        # The underlying query explicitly orders the Projects list by
        # case-insensitive name.
        for project in sorted(projects_datasets, cmp_name_insensitive):
            expected.append({
                'id': project.id.val,
                'isOwned': True,
                'name': project.name.val,
                'childCount': 4,
                'permsCss': perms_css
            })
            # The underlying query explicitly orders the Datasets list by
            # case-insensitive name.
            source = project.linkedDatasetList()
            source.sort(cmp_name_insensitive)
            datasets = list()
            for dataset in source:
                datasets.append({
                    'childCount': 0L,
                    'id': dataset.id.val,
                    'isOwned': True,
                    'name': dataset.name.val,
                    'permsCss': perms_css
                })
            expected[-1]['datasets'] = datasets

        marshaled = marshal_projects(conn, conn.getUserId())
        assert marshaled == expected

    def test_marshal_projects_different_users_as_other_user(
            self, conn, projects_different_users):
        project_a, project_b = projects_different_users
        expected = list()
        perms_css = ''
        # The underlying query explicitly orders the Projects list by
        # case-insensitive name.
        for project in sorted(projects_different_users, cmp_name_insensitive):
            expected.append({
                'id': project.id.val,
                'isOwned': False,
                'name': project.name.val,
                'childCount': 0,
                'permsCss': perms_css,
                'datasets': list()
            })

        conn.SERVICE_OPTS.setOmeroGroup(project_a.details.group.id.val)
        marshaled = marshal_projects(conn, None)
        assert marshaled == expected

    def test_marshal_projects_no_results(self, conn):
        assert marshal_projects(conn, -1) == []

    def test_marshal_datasets(self, conn, datasets):
        dataset_a, dataset_b, dataset_c, dataset_d = datasets
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': dataset_a.id.val,
            'isOwned': True,
            'name': 'Apple',
            'childCount': 0L,
            'permsCss': perms_css
        }, {
            'id': dataset_c.id.val,
            'isOwned': True,
            'name': 'atom',
            'childCount': 0L,
            'permsCss': perms_css
        }, {
            'id': dataset_b.id.val,
            'isOwned': True,
            'name': 'bat',
            'childCount': 0L,
            'permsCss': perms_css
        }, {
            'id': dataset_d.id.val,
            'isOwned': True,
            'name': 'Butter',
            'childCount': 0L,
            'permsCss': perms_css
        }]

        marshaled = marshal_datasets(conn, conn.getUserId())
        assert marshaled == expected

    def test_marshal_datasets_different_users_as_other_user(
            self, conn, datasets_different_users):
        dataset_a, dataset_b = datasets_different_users
        expected = list()
        perms_css = ''
        # The underlying query explicitly orders the Screens list by
        # case-insensitive name.
        for dataset in sorted(datasets_different_users, cmp_name_insensitive):
            expected.append({
                'id': dataset.id.val,
                'isOwned': False,
                'name': dataset.name.val,
                'childCount': 0L,
                'permsCss': perms_css,
            })

        conn.SERVICE_OPTS.setOmeroGroup(dataset_a.details.group.id.val)
        marshaled = marshal_datasets(conn, None)
        assert marshaled == expected

    def test_marshal_datasets_no_results(self, conn):
        assert marshal_datasets(conn, -1L) == []

    def test_marshal_screen_plate_run(self, conn, screen_plate_run):
        screen_id = screen_plate_run.id.val
        plate, = screen_plate_run.linkedPlateList()
        plate_acquisition, = plate.copyPlateAcquisitions()
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        expected = [{
            'id': screen_id,
            'childCount': 1,
            'isOwned': True,
            'name': screen_plate_run.name.val,
            'permsCss': perms_css,
            'plates': [{
                'id': plate.id.val,
                'isOwned': True,
                'name': plate.name.val,
                'plateAcquisitions': [{
                    'id': plate_acquisition.id.val,
                    'name': 'Run %d' % plate_acquisition.id.val,
                    'isOwned': True,
                    'permsCss': perms_css
                }],
                'plateAcquisitionCount': 1,
                'permsCss': perms_css
            }],
        }]

        marshaled = marshal_screens(conn, conn.getUserId())
        assert marshaled == expected

    def test_marshal_screens_plates_runs(self, conn, screens_plates_runs):
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        expected = []
        # The underlying query explicitly orders the Screens by name.
        for screen in sorted(screens_plates_runs, cmp_name):
            expected_screen = {
                'id': screen.id.val,
                'name': screen.name.val,
                'isOwned': True,
                'permsCss': perms_css,
                'childCount': 2,
                'plates': list()
            }
            # The underlying query explicitly orders the Plates by name.
            for plate in sorted(screen.linkedPlateList(), cmp_name):
                expected_plates = expected_screen['plates']
                expected_plates.append({
                    'id': plate.id.val,
                    'isOwned': True,
                    'name': plate.name.val,
                    'plateAcquisitions': list(),
                    'plateAcquisitionCount': 2,
                    'permsCss': perms_css
                })
                # The underlying query explicitly orders the PlateAcquisitions
                # by id.
                plate_acquisitions = \
                    sorted(plate.copyPlateAcquisitions(), cmp_id)
                for plate_acquisition in plate_acquisitions:
                    expected_plates[-1]['plateAcquisitions'].append({
                        'id': plate_acquisition.id.val,
                        'name': 'Run %d' % plate_acquisition.id.val,
                        'isOwned': True,
                        'permsCss': perms_css
                    })
            expected.append(expected_screen)
        marshaled = marshal_screens(conn, conn.getUserId())
        assert marshaled == expected

    def test_marshal_screens_no_results(self, conn):
        assert marshal_screens(conn, -1L) == []

    def test_marshal_screen_plate(self, conn, screen_plate):
        plate, = screen_plate.linkedPlateList()
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        expected = [
            {
                'id': screen_plate.id.val,
                'name': screen_plate.name.val,
                'isOwned': True,
                'permsCss': perms_css,
                'childCount': 1,
                'plates': [{
                    'id': plate.id.val,
                    'isOwned': True,
                    'name': plate.name.val,
                    'plateAcquisitions': list(),
                    'plateAcquisitionCount': 0,
                    'permsCss': perms_css
                }],
            }
        ]

        marshaled = marshal_screens(conn, conn.getUserId())
        assert marshaled == expected

    def test_marshal_plate_run(self, conn, plate_run):
        plate_id = plate_run.id.val
        plate_acquisition, = plate_run.copyPlateAcquisitions()
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        expected = [{
            'id': plate_id,
            'isOwned': True,
            'name': plate_run.name.val,
            'plateAcquisitions': [{
                'id': plate_acquisition.id.val,
                'name': 'Run %d' % plate_acquisition.id.val,
                'isOwned': True,
                'permsCss': perms_css
            }],
            'plateAcquisitionCount': 1,
            'permsCss': perms_css
        }]

        marshaled = marshal_plates(conn, conn.getUserId())
        assert marshaled == expected

    def test_marshal_plates_runs(self, conn, plates_runs):
        plate_a, plate_b, plate_c, plate_d = plates_runs
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        expected = list()
        # The underlying query explicitly orders the Plates by name.
        for plate in sorted(plates_runs, cmp_name_insensitive):
            plate_id = plate.id.val
            expected.append({
                'id': plate_id,
                'isOwned': True,
                'name': plate.name.val,
                'plateAcquisitions': list(),
                'plateAcquisitionCount': 2,
                'permsCss': perms_css
            })
            # The underlying query explicitly orders the PlateAcquisitions
            # by id.
            plate_acquisitions = \
                sorted(plate.copyPlateAcquisitions(), cmp_id)
            for plate_acquisition in plate_acquisitions:
                expected[-1]['plateAcquisitions'].append({
                    'id': plate_acquisition.id.val,
                    'name': 'Run %d' % plate_acquisition.id.val,
                    'isOwned': True,
                    'permsCss': perms_css
                })

        marshaled = marshal_plates(conn, conn.getUserId())
        assert marshaled == expected

    def test_marshal_plates_different_users_as_other_user(
            self, conn, plates_different_users):
        plate_a, plate_b = plates_different_users
        expected = list()
        perms_css = ''
        # The underlying query explicitly orders the Plates list by
        # case-insensitive name.
        for plate in sorted(plates_different_users, cmp_name_insensitive):
            expected.append({
                'id': plate.id.val,
                'isOwned': False,
                'name': plate.name.val,
                'plateAcquisitions': list(),
                'plateAcquisitionCount': 0,
                'permsCss': perms_css,
            })

        conn.SERVICE_OPTS.setOmeroGroup(plate_a.details.group.id.val)
        marshaled = marshal_plates(conn, None)
        assert marshaled == expected

    def test_marshal_plates_no_results(self, conn):
        assert marshal_plates(conn, -1L) == []

    def test_marshal_plate(self, conn, plate):
        plate_id = plate.id.val
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        expected = [{
            'id': plate_id,
            'isOwned': True,
            'name': plate.name.val,
            'plateAcquisitions': list(),
            'plateAcquisitionCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_plates(conn, conn.getUserId())
        assert marshaled == expected

    def test_marshal_project_dataset_image(self, conn, project_dataset_image):
        project_id = project_dataset_image.id.val
        dataset, = project_dataset_image.linkedDatasetList()
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        expected = [{
            'id': project_id,
            'isOwned': True,
            'name': project_dataset_image.name.val,
            'datasets': [{
                'childCount': 1L,
                'id': dataset.id.val,
                'isOwned': True,
                'name': dataset.name.val,
                'permsCss': perms_css
            }],
            'childCount': 1,
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn, conn.getUserId())
        assert marshaled == expected

    def test_marshal_projects(self, conn, projects):
        project_a, project_b, project_c, project_d = projects
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': project_a.id.val,
            'isOwned': True,
            'name': 'Apple',
            'datasets': list(),
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_c.id.val,
            'isOwned': True,
            'name': 'atom',
            'datasets': list(),
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_b.id.val,
            'isOwned': True,
            'name': 'bat',
            'datasets': list(),
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': project_d.id.val,
            'isOwned': True,
            'name': 'Butter',
            'datasets': list(),
            'childCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_projects(conn, conn.getUserId())
        assert marshaled == expected

    def test_marshal_screens(self, conn, screens):
        screen_a, screen_b, screen_c, screen_d = screens
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        # Order is important to test desired HQL sorting semantics.
        expected = [{
            'id': screen_a.id.val,
            'isOwned': True,
            'name': 'Apple',
            'plates': list(),
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': screen_c.id.val,
            'isOwned': True,
            'name': 'atom',
            'plates': list(),
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': screen_b.id.val,
            'isOwned': True,
            'name': 'bat',
            'plates': list(),
            'childCount': 0,
            'permsCss': perms_css
        }, {
            'id': screen_d.id.val,
            'isOwned': True,
            'name': 'Butter',
            'plates': list(),
            'childCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_screens(conn, conn.getUserId())
        assert marshaled == expected

    def test_marshal_screens_plates(self, conn, screens_plates):
        screen_a, screen_b, screen_c, screen_d = screens_plates
        expected = list()
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        # The underlying query explicitly orders the Screens list by
        # case-insensitive name.
        for screen in sorted(screens_plates, cmp_name_insensitive):
            expected.append({
                'id': screen.id.val,
                'isOwned': True,
                'name': screen.name.val,
                'childCount': 4,
                'permsCss': perms_css
            })
            # The underlying query explicitly orders the Plate list by
            # case-insensitive name.
            source = screen.linkedPlateList()
            source.sort(cmp_name_insensitive)
            plates = list()
            for plate in source:
                plates.append({
                    'id': plate.id.val,
                    'isOwned': True,
                    'name': plate.name.val,
                    'permsCss': perms_css,
                    'plateAcquisitions': list(),
                    'plateAcquisitionCount': 0
                })
            expected[-1]['plates'] = plates

        marshaled = marshal_screens(conn, conn.getUserId())
        assert marshaled == expected

    def test_marshal_screens_different_users_as_other_user(
            self, conn, screens_different_users):
        screen_a, screen_b = screens_different_users
        expected = list()
        perms_css = ''
        # The underlying query explicitly orders the Screens list by
        # case-insensitive name.
        for screen in sorted(screens_different_users, cmp_name_insensitive):
            expected.append({
                'id': screen.id.val,
                'isOwned': False,
                'name': screen.name.val,
                'childCount': 0,
                'permsCss': perms_css,
                'plates': list()
            })

        conn.SERVICE_OPTS.setOmeroGroup(screen_a.details.group.id.val)
        marshaled = marshal_screens(conn, None)
        assert marshaled == expected
