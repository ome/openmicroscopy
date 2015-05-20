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
import library as lib

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

    @classmethod
    def setup_class(cls):
        """Returns a logged in Django test client."""
        super(TestTree, cls).setup_class()
        cls.names = ('Apple', 'bat', 'atom', 'Butter')

    def setup_method(self, method):
        self.client = self.new_client(perms='rwr---')
        self.conn = BlitzGateway(client_obj=self.client)
        self.update = self.client.getSession().getUpdateService()

    @pytest.fixture
    def projects(self):
        """
        Returns four new OMERO Projects with required fields set and with
        names that can be used to exercise sorting semantics.
        """
        to_save = [ProjectI(), ProjectI(), ProjectI(), ProjectI()]
        for index, project in enumerate(to_save):
            project.name = rstring(self.names[index])
        return self.update.saveAndReturnArray(to_save)

    @pytest.fixture
    def project_dataset(self):
        """
        Returns a new OMERO Project and linked Dataset with required fields
        set.
        """
        project = ProjectI()
        project.name = rstring(self.uuid())
        dataset = DatasetI()
        dataset.name = rstring(self.uuid())
        project.linkDataset(dataset)
        return self.update.saveAndReturnObject(project)

    @pytest.fixture
    def project_dataset_image(self):
        """
        Returns a new OMERO Project, linked Dataset and linked Image populated
        by an L{test.integration.library.ITest} instance with required fields
        set.
        """
        project = ProjectI()
        project.name = rstring(self.uuid())
        dataset = DatasetI()
        dataset.name = rstring(self.uuid())
        image = self.new_image(name=self.uuid())
        dataset.linkImage(image)
        project.linkDataset(dataset)
        return self.update.saveAndReturnObject(project)

    @pytest.fixture
    def projects_datasets(self):
        """
        Returns four new OMERO Projects and four linked Datasets with required
        fields set and with names that can be used to exercise sorting
        semantics.
        """
        projects = [ProjectI(), ProjectI(), ProjectI(), ProjectI()]
        for index, project in enumerate(projects):
            project.name = rstring(self.names[index])
            datasets = [DatasetI(), DatasetI(), DatasetI(), DatasetI()]
            for index, dataset in enumerate(datasets):
                dataset.name = rstring(self.names[index])
                project.linkDataset(dataset)
        return self.update.saveAndReturnArray(projects)

    @pytest.fixture
    def datasets(self):
        """
        Returns four new OMERO Datasets with required fields set and with
        names that can be used to exercise sorting semantics.
        """
        to_save = [DatasetI(), DatasetI(), DatasetI(), DatasetI()]
        for index, dataset in enumerate(to_save):
            dataset.name = rstring(self.names[index])
        # Non-orphaned Dataset to catch issues with queries where non-orphaned
        # datasets are included in the results.
        project = ProjectI()
        project.name = rstring(self.uuid())
        dataset = DatasetI()
        dataset.name = rstring(self.uuid())
        project.linkDataset(dataset)
        self.update.saveAndReturnObject(project)
        return self.update.saveAndReturnArray(to_save)

    @pytest.fixture
    def datasets_different_users(self):
        """
        Returns two new OMERO Datasets created by different users with
        required fields set.
        """
        client = self.conn.c
        group = self.conn.getGroupFromContext()._obj
        datasets = list()
        # User that has already been created by the "client" fixture
        user, name = self.user_and_name(client)
        self.add_experimenters(group, [user])
        for name in (rstring(self.uuid()), rstring(self.uuid())):
            client, user = self.new_client_and_user(group=group)
            try:
                dataset = DatasetI()
                dataset.name = name
                update_service = client.getSession().getUpdateService()
                datasets.append(update_service.saveAndReturnObject(dataset))
            finally:
                client.closeSession()
        return datasets

    @pytest.fixture
    def screens(self):
        """
        Returns four new OMERO Screens with required fields set and with names
        that can be used to exercise sorting semantics.
        """
        to_save = [ScreenI(), ScreenI(), ScreenI(), ScreenI()]
        for index, screen in enumerate(to_save):
            screen.name = rstring(self.names[index])
        return self.update.saveAndReturnArray(to_save)

    @pytest.fixture
    def screens_different_users(self):
        """
        Returns two new OMERO Screens created by different users with
        required fields set.
        """
        client = self.conn.c
        group = self.conn.getGroupFromContext()._obj
        screens = list()
        # User that has already been created by the "client" fixture
        user, name = self.user_and_name(client)
        self.add_experimenters(group, [user])
        for name in (rstring(self.uuid()), rstring(self.uuid())):
            client, user = self.new_client_and_user(group=group)
            try:
                screen = ScreenI()
                screen.name = name
                update_service = client.getSession().getUpdateService()
                screens.append(update_service.saveAndReturnObject(screen))
            finally:
                client.closeSession()
        return screens

    @pytest.fixture
    def screen_plate_run(self):
        """
        Returns a new OMERO Screen, linked Plate, and linked PlateAcquisition
        with all required fields set.
        """
        screen = ScreenI()
        screen.name = rstring(self.uuid())
        plate = PlateI()
        plate.name = rstring(self.uuid())
        plate_acquisition = PlateAcquisitionI()
        plate.addPlateAcquisition(plate_acquisition)
        screen.linkPlate(plate)
        return self.update.saveAndReturnObject(screen)

    @pytest.fixture
    def screens_plates_runs(self):
        """
        Returns a two new OMERO Screens, two linked Plates, and two linked
        PlateAcquisitions with all required fields set.
        """
        screens = [ScreenI(), ScreenI()]
        for screen in screens:
            screen.name = rstring(self.uuid())
            plates = [PlateI(), PlateI()]
            for plate in plates:
                plate.name = rstring(self.uuid())
                plate_acquisitions = [
                    PlateAcquisitionI(), PlateAcquisitionI()]
                for plate_acquisition in plate_acquisitions:
                    plate.addPlateAcquisition(plate_acquisition)
                screen.linkPlate(plate)
        return self.update.saveAndReturnArray(screens)

    @pytest.fixture
    def screen_plate(self):
        """
        Returns a new OMERO Screen and linked Plate with required fields set.
        """
        screen = ScreenI()
        screen.name = rstring(self.uuid())
        plate = PlateI()
        plate.name = rstring(self.uuid())
        screen.linkPlate(plate)
        return self.update.saveAndReturnObject(screen)

    @pytest.fixture
    def screens_plates(self):
        """
        Returns four new OMERO Screens and four linked Plates with required
        fields set and with names that can be used to exercise sorting
        semantics.
        """
        screens = [ScreenI(), ScreenI(), ScreenI(), ScreenI()]
        for index, screen in enumerate(screens):
            screen.name = rstring(self.names[index])
            plates = [PlateI(), PlateI(), PlateI(), PlateI()]
            for index, plate in enumerate(plates):
                plate.name = rstring(self.names[index])
                screen.linkPlate(plate)
        return self.update.saveAndReturnArray(screens)

    @pytest.fixture
    def plates_runs(self):
        """
        Returns a four new Plates, and two linked PlateAcquisitions with
        required fields set and with names that can be used to exercise
        sorting semantics.
        """
        plates = [PlateI(), PlateI(), PlateI(), PlateI()]
        for index, plate in enumerate(plates):
            plate.name = rstring(self.names[index])
            plate_acquisitions = [PlateAcquisitionI(), PlateAcquisitionI()]
            for plate_acquisition in plate_acquisitions:
                plate.addPlateAcquisition(plate_acquisition)
        # Non-orphaned Plate to catch issues with queries where non-orphaned
        # plates are included in the results.
        screen = ScreenI()
        screen.name = rstring(self.uuid())
        plate = PlateI()
        plate.name = rstring(self.uuid())
        screen.linkPlate(plate)
        self.update.saveAndReturnObject(screen)
        return self.update.saveAndReturnArray(plates)

    @pytest.fixture
    def plate_run(self):
        """
        Returns a new OMERO Plate and linked PlateAcquisition with all
        required fields set.
        """
        plate = PlateI()
        plate.name = rstring(self.uuid())
        plate_acquisition = PlateAcquisitionI()
        plate.addPlateAcquisition(plate_acquisition)
        return self.update.saveAndReturnObject(plate)

    @pytest.fixture
    def plate(self):
        """
        Returns a new OMERO Plate with all required fields set.
        """
        plate = PlateI()
        plate.name = rstring(self.uuid())
        return self.update.saveAndReturnObject(plate)

    @pytest.fixture
    def plates_different_users(self):
        """
        Returns two new OMERO Plates created by different users with
        required fields set.
        """
        client = self.conn.c
        group = self.conn.getGroupFromContext()._obj
        plates = list()
        # User that has already been created by the "client" fixture
        user, name = self.user_and_name(client)
        self.add_experimenters(group, [user])
        for name in (rstring(self.uuid()), rstring(self.uuid())):
            client, user = self.new_client_and_user(group=group)
            try:
                plate = PlateI()
                plate.name = name
                update_service = client.getSession().getUpdateService()
                plates.append(update_service.saveAndReturnObject(plate))
            finally:
                client.closeSession()
        return plates

    @pytest.fixture
    def projects_different_users(self):
        """
        Returns two new OMERO Projects created by different users with
        required fields set.
        """
        client = self.conn.c
        group = self.conn.getGroupFromContext()._obj
        projects = list()
        # User that has already been created by the "client" fixture
        user, name = self.user_and_name(client)
        self.add_experimenters(group, [user])
        for name in (rstring(self.uuid()), rstring(self.uuid())):
            client, user = self.new_client_and_user(group=group)
            try:
                project = ProjectI()
                project.name = name
                update_service = client.getSession().getUpdateService()
                projects.append(update_service.saveAndReturnObject(project))
            finally:
                client.closeSession()
        return projects

    def test_marshal_project_dataset(self, project_dataset):
        project_id = project_dataset.id.val
        dataset, = project_dataset.linkedDatasetList()
        perms_css = 'canEdit canAnnotate canLink canDelete isOwned canChgrp'
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

        marshaled = marshal_projects(self.conn, self.conn.getUserId())
        assert marshaled == expected

    def test_marshal_projects_datasets(self, projects_datasets):
        project_a, project_b, project_c, project_d = projects_datasets
        expected = list()
        perms_css = 'canEdit canAnnotate canLink canDelete isOwned canChgrp'
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

        marshaled = marshal_projects(self.conn, self.conn.getUserId())
        assert marshaled == expected

    def test_marshal_projects_datasets_duplicates(self, projects_datasets):
        """
        Test that same-named Projects are not duplicated in marshaled data.
        See https://trac.openmicroscopy.org/ome/ticket/12771
        """
        # Re-name projects with all the same name
        for p in projects_datasets:
            p.name = rstring("Test_Duplicates")
        projects_datasets = self.update.saveAndReturnArray(projects_datasets)
        marshaled = marshal_projects(self.conn, self.conn.getUserId())
        assert len(marshaled) == len(projects_datasets)

    def test_marshal_projects_different_users_as_other_user(
            self, projects_different_users):
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

        self.conn.SERVICE_OPTS.setOmeroGroup(project_a.details.group.id.val)
        marshaled = marshal_projects(self.conn, None)
        assert marshaled == expected

    def test_marshal_projects_no_results(self):
        assert marshal_projects(self.conn, -1) == []

    def test_marshal_datasets(self, datasets):
        dataset_a, dataset_b, dataset_c, dataset_d = datasets
        perms_css = 'canEdit canAnnotate canLink canDelete isOwned canChgrp'
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

        marshaled = marshal_datasets(self.conn, self.conn.getUserId())
        assert marshaled == expected

    def test_marshal_datasets_different_users_as_other_user(
            self, datasets_different_users):
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

        self.conn.SERVICE_OPTS.setOmeroGroup(dataset_a.details.group.id.val)
        marshaled = marshal_datasets(self.conn, None)
        assert marshaled == expected

    def test_marshal_datasets_no_results(self):
        assert marshal_datasets(self.conn, -1L) == []

    def test_marshal_screen_plate_run(self, screen_plate_run):
        screen_id = screen_plate_run.id.val
        plate, = screen_plate_run.linkedPlateList()
        plate_acquisition, = plate.copyPlateAcquisitions()
        perms_css = 'canEdit canAnnotate canLink canDelete isOwned canChgrp'
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

        marshaled = marshal_screens(self.conn, self.conn.getUserId())
        assert marshaled == expected

    def test_marshal_screens_plates_runs(self, screens_plates_runs):
        perms_css = 'canEdit canAnnotate canLink canDelete isOwned canChgrp'
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
        marshaled = marshal_screens(self.conn, self.conn.getUserId())
        assert marshaled == expected

    def test_marshal_screens_no_results(self):
        assert marshal_screens(self.conn, -1L) == []

    def test_marshal_screen_plate(self, screen_plate):
        plate, = screen_plate.linkedPlateList()
        perms_css = 'canEdit canAnnotate canLink canDelete isOwned canChgrp'
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

        marshaled = marshal_screens(self.conn, self.conn.getUserId())
        assert marshaled == expected

    def test_marshal_plate_run(self, plate_run):
        plate_id = plate_run.id.val
        plate_acquisition, = plate_run.copyPlateAcquisitions()
        perms_css = 'canEdit canAnnotate canLink canDelete isOwned canChgrp'
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

        marshaled = marshal_plates(self.conn, self.conn.getUserId())
        assert marshaled == expected

    def test_marshal_plates_runs(self, plates_runs):
        plate_a, plate_b, plate_c, plate_d = plates_runs
        perms_css = 'canEdit canAnnotate canLink canDelete isOwned canChgrp'
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

        marshaled = marshal_plates(self.conn, self.conn.getUserId())
        assert marshaled == expected

    def test_marshal_plates_different_users_as_other_user(
            self, plates_different_users):
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

        self.conn.SERVICE_OPTS.setOmeroGroup(plate_a.details.group.id.val)
        marshaled = marshal_plates(self.conn, None)
        assert marshaled == expected

    def test_marshal_plates_no_results(self):
        assert marshal_plates(self.conn, -1L) == []

    def test_marshal_plate(self, plate):
        plate_id = plate.id.val
        perms_css = 'canEdit canAnnotate canLink canDelete isOwned canChgrp'
        expected = [{
            'id': plate_id,
            'isOwned': True,
            'name': plate.name.val,
            'plateAcquisitions': list(),
            'plateAcquisitionCount': 0,
            'permsCss': perms_css
        }]

        marshaled = marshal_plates(self.conn, self.conn.getUserId())
        assert marshaled == expected

    def test_marshal_project_dataset_image(self, project_dataset_image):
        project_id = project_dataset_image.id.val
        dataset, = project_dataset_image.linkedDatasetList()
        perms_css = 'canEdit canAnnotate canLink canDelete isOwned canChgrp'
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

        marshaled = marshal_projects(self.conn, self.conn.getUserId())
        assert marshaled == expected

    def test_marshal_projects(self, projects):
        project_a, project_b, project_c, project_d = projects
        perms_css = 'canEdit canAnnotate canLink canDelete isOwned canChgrp'
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

        marshaled = marshal_projects(self.conn, self.conn.getUserId())
        print marshaled
        assert marshaled == expected

    def test_marshal_screens(self, screens):
        screen_a, screen_b, screen_c, screen_d = screens
        perms_css = 'canEdit canAnnotate canLink canDelete isOwned canChgrp'
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

        marshaled = marshal_screens(self.conn, self.conn.getUserId())
        assert marshaled == expected

    def test_marshal_screens_plates(self, screens_plates):
        screen_a, screen_b, screen_c, screen_d = screens_plates
        expected = list()
        perms_css = 'canEdit canAnnotate canLink canDelete isOwned canChgrp'
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

        marshaled = marshal_screens(self.conn, self.conn.getUserId())
        assert marshaled == expected

    def test_marshal_screens_different_users_as_other_user(
            self, screens_different_users):
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

        self.conn.SERVICE_OPTS.setOmeroGroup(screen_a.details.group.id.val)
        marshaled = marshal_screens(self.conn, None)
        assert marshaled == expected
