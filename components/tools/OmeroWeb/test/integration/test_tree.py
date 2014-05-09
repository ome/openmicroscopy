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
from omeroweb.webclient.tree import marshal_datasets_for_projects, \
    marshal_datasets, marshal_plates_for_screens, marshal_plates


def cmp_id(x, y):
    """Identifier comparator."""
    return cmp(x.id.val, y.id.val)


def cmp_name(x, y):
    """Name comparator."""
    return cmp(x.name.val, y.name.val)


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
    """Returns a new user client."""
    return itest.new_client()


@pytest.fixture(scope='function')
def conn(request, client):
    """Returns a new OMERO gateway."""
    return BlitzGateway(client_obj=client)


@pytest.fixture(scope='function')
def update_service(request, client):
    """Returns a new OMERO update service."""
    return client.getSession().getUpdateService()


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
def projects_datasets(request, itest, update_service):
    """
    Returns a two new OMERO Projects and two linked Datasets for each Project
    with required fields set.
    """
    projects = [ProjectI(), ProjectI()]
    for project in projects:
        project.name = rstring(itest.uuid())
        datasets = [DatasetI(), DatasetI()]
        for dataset in datasets:
            dataset.name = rstring(itest.uuid())
            project.linkDataset(dataset)
    return update_service.saveAndReturnArray(projects)


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
def datasets(request, itest, update_service):
    """
    Returns a two new OMERO Datasets with required fields set.
    """
    datasets = [DatasetI(), DatasetI()]
    for dataset in datasets:
        dataset.name = rstring(itest.uuid())
    return update_service.saveAndReturnArray(datasets)


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
        expected = {
            project_id: {
                'childCount': 1L,
                'datasets': [{
                    'childCount': 0L,
                    'id': dataset.id.val,
                    'isOwned': True,
                    'name': dataset.name.val,
                    'permsCss': perms_css
                }],
                'permsCss': perms_css
            }
        }

        marshaled = marshal_datasets_for_projects(conn, [project_id])
        assert marshaled == expected

    def test_marshal_projects_datasets(self, conn, projects_datasets):
        project_a, project_b = projects_datasets
        expected = dict()
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        for project in (project_a, project_b):
            datasets = list()
            expected[project.id.val] = {
                'childCount': 2L,
                'datasets': datasets,
                'permsCss': perms_css
            }
            # The underlying query explicitly orders the Datasets list by
            # name.
            for dataset in sorted(project.linkedDatasetList(), cmp_name):
                datasets.append({
                    'childCount': 0L,
                    'id': dataset.id.val,
                    'isOwned': True,
                    'name': dataset.name.val,
                    'permsCss': perms_css
                })

        marshaled = marshal_datasets_for_projects(
            conn, [project_a.id.val, project_b.id.val]
        )
        assert marshaled == expected

    def test_marshal_datasets_for_projects_no_results(self, conn):
        assert marshal_datasets_for_projects(conn, []) == {}

    def test_marshal_datasets(self, conn, datasets):
        dataset_a, dataset_b = datasets
        expected = list()
        # The underlying query explicitly orders the Datasets list by
        # name.
        for dataset in sorted((dataset_a, dataset_b), cmp_name):
            expected.append({
                'id': dataset.id.val,
                'name': dataset.name.val,
                'isOwned': True,
                'childCount': 0L,
                'permsCss': 'canEdit canAnnotate canLink canDelete canChgrp'
            })

        marshaled = marshal_datasets(
            conn, [dataset_a.id.val, dataset_b.id.val]
        )
        assert marshaled == expected

    def test_marshal_datasets_no_results(self, conn):
        assert marshal_datasets(conn, []) == []

    def test_marshal_screen_plate_run(self, conn, screen_plate_run):
        screen_id = screen_plate_run.id.val
        plate, = screen_plate_run.linkedPlateList()
        plate_acquisition, = plate.copyPlateAcquisitions()
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        expected = {
            screen_id: {
                'childCount': 1,
                'plates': [{
                    'id': plate.id.val,
                    'isOwned': True,
                    'name': plate.name.val,
                    'plateacquisitions': [{
                        'id': plate_acquisition.id.val,
                        'name': 'Run %d' % plate_acquisition.id.val,
                        'isOwned': True,
                        'permsCss': perms_css
                    }],
                    'plateAcquisitionsCount': 1,
                    'permsCss': perms_css
                }],
                'plateids': [plate.id.val]
            }
        }

        marshaled = marshal_plates_for_screens(conn, [screen_id])
        assert marshaled == expected

    def test_marshal_plates_for_screens_no_results(self, conn):
        assert marshal_plates_for_screens(conn, []) == {}

    def test_marshal_screen_plate(self, conn, screen_plate):
        screen_id = screen_plate.id.val
        plate, = screen_plate.linkedPlateList()
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        expected = {
            screen_id: {
                'childCount': 1,
                'plates': [{
                    'id': plate.id.val,
                    'isOwned': True,
                    'name': plate.name.val,
                    'plateacquisitions': list(),
                    'plateAcquisitionsCount': 0,
                    'permsCss': perms_css
                }],
                'plateids': [plate.id.val]
            }
        }

        marshaled = marshal_plates_for_screens(conn, [screen_id])
        assert marshaled == expected

    def test_marshal_plate_run(self, conn, plate_run):
        plate_id = plate_run.id.val
        plate_acquisition, = plate_run.copyPlateAcquisitions()
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        expected = [{
            'id': plate_id,
            'isOwned': True,
            'name': plate_run.name.val,
            'plateacquisitions': [{
                'id': plate_acquisition.id.val,
                'name': 'Run %d' % plate_acquisition.id.val,
                'isOwned': True,
                'permsCss': perms_css
            }],
            'plateAcquisitionsCount': 1,
            'permsCss': perms_css
        }]

        marshaled = marshal_plates(conn, [plate_id])
        assert marshaled == expected

    def test_marshal_plates_no_results(self, conn):
        assert marshal_plates(conn, []) == []

    def test_marshal_plate(self, conn, plate):
        plate_id = plate.id.val
        perms_css = 'canEdit canAnnotate canLink canDelete canChgrp'
        expected = [{
            'id': plate_id,
            'isOwned': True,
            'name': plate.name.val,
            'plateacquisitions': list(),
            'plateAcquisitionsCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_plates(conn, [plate_id])
        assert marshaled == expected
