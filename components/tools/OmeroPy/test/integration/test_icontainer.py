#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2008-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
   Integration test focused on the omero.api.IContainer interface.

"""

import library as lib
import omero
from omero_model_CommentAnnotationI import CommentAnnotationI
from omero.rtypes import rstring
from uuid import uuid4


class TestIContainer(lib.ITest):

    DEFAULT_PERMS = "rw----"

    def testFindAnnotations(self):
        ipojo = self.client.sf.getContainerService()
        i = self.new_image(name="name")
        i = ipojo.createDataObject(i, None)

    def testFindAndCountAnnotationsForSharedData(self):

        # create users
        group = self.new_group(perms="rwra--")
        client1, user1 = self.new_client_and_user(group=group)

        # create image with comment annotation
        img = self.make_image(name='test1154-img-%s' % self.uuid(),
                              client=client1)
        ann1 = self.new_object(
            CommentAnnotationI, name="user comment - %s" % self.uuid())
        self.link(img, ann1, client=client1)

        # user retrives the annotations for image
        ipojo1 = client1.sf.getContainerService()
        coll_count = ipojo1.getCollectionCount(
            "Image", "ome.model.containers.Image_annotationLinks",
            [img.id.val], None)
        assert 1 == coll_count.get(img.id.val, [])

        # login as user2
        client2, user2 = self.new_client_and_user(group=group)
        ann2 = self.new_object(
            CommentAnnotationI, name="user2 comment - %s" % self.uuid())
        self.link(img, ann2, client=client2)

        # do they see the same vals?
        coll_count = ipojo1.getCollectionCount(
            "Image", "ome.model.containers.Image_annotationLinks",
            [img.id.val], None)
        assert 2 == coll_count.get(img.id.val, [])

    def testCreateAfterBlitzPort(self):
        ipojo = self.client.sf.getContainerService()
        i = self.new_image(name="name")
        i = ipojo.createDataObject(i, None)
        o = i.getDetails().owner
        assert -1 == o.sizeOfGroupExperimenterMap()


class TestSplitFilesets(lib.ITest):

    def checkSplitFilesets(self, dtypeIdsMap, expected):
        """
        To check we get the expected result from
        iContainer.getImagesBySplitFilesets() we do the query with dtype & ids
        and compare the returned data with the specified dict.
        """
        container = self.client.sf.getContainerService()
        result = container.getImagesBySplitFilesets(dtypeIdsMap, None)

        def cmpLists(listOne, listTwo):
            """ Returns True if both lists have the same items """
            if (len(listOne) != len(listTwo)):
                return False
            for one in listOne:
                if one not in listTwo:
                    return False
            return True

        # compare result with expected...
        assert set(result.keys()) == set(
            expected.keys()),  "Result should have expected Fileset IDs"
        for fsId, expectedDict in expected.items():
            assert cmpLists(
                expectedDict[True],
                result[fsId][True]), "True ImageIDs should match"
            assert cmpLists(
                expectedDict[False],
                result[fsId][False]), "False ImageIDs should match"

    def testFilesetSplitByImage(self):
        """
        Fileset of 2 Images, we test split using 1 Image ID
        """
        images = self.importMIF(2)

        # Lookup the fileset
        imgId = images[0].id.val
        filesetId = self.query.get('Image', imgId).fileset.id.val

        # Define what we expect & query split fileset
        expected = {filesetId: {True: [imgId], False: [images[1].id.val]}}
        self.checkSplitFilesets({'Image': [imgId]}, expected)

    def testFilesetNotSplitByImage(self):
        """
        Fileset of 2 Images with No split (query with both Image IDs)
        """
        images = self.importMIF(2)

        imgIds = [i.id.val for i in images]

        # Define what we expect & query split fileset
        expected = {}
        self.checkSplitFilesets({'Image': imgIds}, expected)

    def testFilesetSplitByDatasetAndProject(self):
        """
        Fileset of 2 Images, one in a Dataset. Test split using Dataset ID
        """
        # Dataset contains 1 image of a 2-image fileset
        images = self.importMIF(2)
        ds = self.make_dataset("testFilesetSplitByDataset")
        self.link(ds, images[0])

        # Dataset in Project
        pr = self.make_project("testFilesetSplitByProject")
        self.link(pr, ds)

        # Lookup the fileset
        imgId = images[0].id.val
        filesetId = self.query.get('Image', imgId).fileset.id.val

        # Define what we expect & query split fileset
        expected = {filesetId: {True: [imgId], False: [images[1].id.val]}}
        self.checkSplitFilesets({'Dataset': [ds.id.val]}, expected)
        # Expect same result if query via Project
        self.checkSplitFilesets({'Project': [pr.id.val]}, expected)

        # No split if we include the extra image ID
        expected = {}
        idsMap = {'Dataset': [ds.id.val], "Image": [images[1].id.val]}
        self.checkSplitFilesets(idsMap, expected)
        idsMap = {'Project': [pr.id.val], "Image": [images[1].id.val]}
        self.checkSplitFilesets(idsMap, expected)

    def testFilesetNotSplitByDatasets(self):
        """
        Fileset of 2 Images, both in different Datasets.
        Test Not split using Dataset IDs
        """
        # Datasets each contain 1 image of a 2-image fileset
        datasets = self.createDatasets(2, "testFilesetNotSplitByDatasets")
        images = self.importMIF(2)
        for i in range(2):
            self.link(datasets[i], images[i])

        # Another Dataset contains both images
        ds = self.make_dataset(name="testFilesetNotSplitByDatasets")
        for i in images:
            self.link(ds, i)

        # Lookup the fileset
        imgId = images[0].id.val
        filesetId = self.query.get('Image', imgId).fileset.id.val

        # No split if we pass in both Dataset IDs...
        dsIds = [d.id.val for d in datasets]
        expected = {}
        self.checkSplitFilesets({'Dataset': dsIds}, expected)
        # ...or the Dataset that contains both images
        self.checkSplitFilesets({'Dataset': [ds.id.val]}, expected)

        # confirm split if we choose one Dataset
        expected = {filesetId: {True: [imgId], False: [images[1].id.val]}}
        self.checkSplitFilesets({'Dataset': [datasets[0].id.val]}, expected)

    def testGetImagesBySplitFilesetsManyCases(self):
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()
        ipojo = self.client.sf.getContainerService()
        admin = self.client.sf.getAdminService()

        eventContext = admin.getEventContext()

        # entity hierarchy

        project_dataset_hierarchy = [(0, [0, 1])]
        dataset_image_hierarchy = [(0, [0, 1]), (1, [2, 6]), (2, [3, 4, 5])]
        screen_plate_hierarchy = [(0, [0, 1])]
        plate_well_hierarchy = [(0, [0, 1]), (1, [2, 6]), (2, [3, 4, 5])]
        well_image_hierarchy = [
            (0, [0]), (1, [1]), (2, [2]), (3, [3]), (4, [4]), (5, [5])]
        fileset_image_hierarchy = [(0, [0]), (1, [1, 2]), (2, [3, 4, 5])]

        # test data, input and expected output value, is by list index

        test_cases = [({'Image': [0, 1, 2, 3, 4, 5]}, {}),
                      ({'Image': [6]}, {}),
                      ({'Image': [0, 1]}, {1: {True: [1], False: [2]}}),
                      ({'Image': [3]}, {2: {True: [3], False: [4, 5]}}),
                      ({'Image': [5]}, {2: {True: [5], False: [3, 4]}}),
                      ({'Image': [3, 4]}, {2: {True: [3, 4], False: [5]}}),
                      ({'Image': [0, 1, 5, 6]}, {
                       1: {True: [1], False: [2]},
                       2: {True: [5], False: [3, 4]}}),
                      ({'Fileset': [0], 'Image': [0, 3, 4, 5, 6]}, {}),
                      ({'Fileset': [0, 1, 2], 'Image': [0]}, {}),
                      ({'Well': [0, 1, 2, 3, 4, 5]}, {}),
                      ({'Well': [6]}, {}),
                      ({'Well': [0, 1]}, {1: {True: [1], False: [2]}}),
                      ({'Well': [3]}, {2: {True: [3], False: [4, 5]}}),
                      ({'Well': [5]}, {2: {True: [5], False: [3, 4]}}),
                      ({'Well': [3, 4]}, {2: {True: [3, 4], False: [5]}}),
                      ({'Well': [0, 1, 5, 6]}, {
                       1: {True: [1], False: [2]},
                       2: {True: [5], False: [3, 4]}}),
                      ({'Image': [0, 1], 'Well': [5, 6]}, {
                       1: {True: [1], False: [2]},
                       2: {True: [5], False: [3, 4]}}),
                      ({'Fileset': [0], 'Well': [0, 3, 4, 5, 6]}, {}),
                      ({'Fileset': [0, 1, 2], 'Well': [0]}, {}),
                      ({'Fileset': [2]}, {}),
                      ({'Dataset': [0]}, {1: {True: [1], False: [2]}}),
                      ({'Dataset': [1]}, {1: {True: [2], False: [1]}}),
                      ({'Dataset': [2]}, {}),
                      ({'Dataset': [1], 'Image': [0, 1]}, {}),
                      ({'Project': [0]}, {}),
                      ({'Project': [0], 'Image': [3]},
                       {2: {True: [3], False: [4, 5]}}),
                      ({'Dataset': [0], 'Fileset': [1]}, {}),
                      ({'Plate': [0]}, {1: {True: [1], False: [2]}}),
                      ({'Plate': [1]}, {1: {True: [2], False: [1]}}),
                      ({'Plate': [2]}, {}),
                      ({'Plate': [1], 'Image': [0, 1]}, {}),
                      ({'Plate': [1], 'Well': [0, 1]}, {}),
                      ({'Plate': [1], 'Image': [0], 'Well': [1]}, {}),
                      ({'Screen': [0]}, {}),
                      ({'Screen': [0], 'Image': [3]},
                       {2: {True: [3], False: [4, 5]}}),
                      ({'Screen': [0], 'Well': [3]},
                       {2: {True: [3], False: [4, 5]}}),
                      ({'Screen': [0], 'Image': [3], 'Well': [3]},
                       {2: {True: [3], False: [4, 5]}}),
                      ({'Plate': [0], 'Fileset': [1]}, {})]

        # TODO: consider factoring some of the below out into library functions
        # for use by other tests

        # name entity lists

        projects = []
        datasets = []
        screens = []
        plates = []
        wells = []
        filesets = []
        images = []

        named_entities = {'Project': projects, 'Dataset': datasets,
                          'Screen': screens, 'Plate': plates,
                          'Well': wells, 'Fileset': filesets, 'Image': images}

        # note all test case input values

        all_inputs = {}
        for name in named_entities.keys():
            all_inputs[name] = []

        for input, expected in test_cases:
            for name, ids in input.items():
                all_inputs[name] += ids

        # create test entities named in test case input values

        parents = lambda hierarchy:     [
            from_index for from_index, to_indices in hierarchy]
        children = lambda hierarchy: sum(
            [to_indices for from_index, to_indices in hierarchy], [])

        for project_index in set(all_inputs['Project']
                                 + parents(project_dataset_hierarchy)):
            project = self.make_project(name='Project #%i' % project_index)
            projects.append(query.get('Project', project.id.val))
        for dataset_index in set(all_inputs['Dataset']
                                 + children(project_dataset_hierarchy)
                                 + parents(dataset_image_hierarchy)):
            dataset = self.make_dataset(name='Dataset #%i' % dataset_index)
            datasets.append(query.get('Dataset', dataset.id.val))
        for screen_index in set(all_inputs['Screen']
                                + parents(screen_plate_hierarchy)):
            screen = omero.model.ScreenI()
            screen.name = rstring('Screen #%i' % screen_index)
            screen.id = update.saveAndReturnObject(screen).id
            screens.append(query.get('Screen', screen.id.val))
        for plate_index in set(all_inputs['Plate']
                               + children(screen_plate_hierarchy)
                               + parents(plate_well_hierarchy)):
            plate = omero.model.PlateI()
            plate.name = rstring('Plate #%i' % plate_index)
            plate.id = update.saveAndReturnObject(plate).id
            plates.append(query.get('Plate', plate.id.val))
        for well_index in set(all_inputs['Well']
                              + children(plate_well_hierarchy)
                              + parents(well_image_hierarchy)):
            well = omero.model.WellI()
            wells.append(well)  # cannot save until attached to plate
        for fileset_index in set(all_inputs['Fileset']
                                 + parents(fileset_image_hierarchy)):
            fileset = omero.model.FilesetI()
            fileset.templatePrefix = rstring(
                '%s_%i/%s' % (eventContext.userName,
                              eventContext.userId, uuid4()))
            fileset.id = update.saveAndReturnObject(fileset).id
            filesets.append(query.get('Fileset', fileset.id.val))
        for image_index in set(all_inputs['Image']
                               + children(dataset_image_hierarchy)
                               + children(well_image_hierarchy)
                               + children(fileset_image_hierarchy)):
            image = self.make_image('Image #%i' % image_index)
            images.append(query.get('Image', image.id.val))

        # associate test entities

        for project_index, dataset_indices in project_dataset_hierarchy:
            for dataset_index in dataset_indices:
                self.link(projects[project_index], datasets[dataset_index])

        for dataset_index, image_indices in dataset_image_hierarchy:
            for image_index in image_indices:
                self.link(datasets[dataset_index], images[image_index])

        for screen_index, plate_indices in screen_plate_hierarchy:
            for plate_index in plate_indices:
                screen_plate = omero.model.ScreenPlateLinkI()
                screen_plate.parent = screens[screen_index]
                screen_plate.child = plates[plate_index]
                update.saveAndReturnObject(screen_plate)

        for plate_index, well_indices in plate_well_hierarchy:
            for well_index in well_indices:
                wells[well_index].plate = plates[plate_index]

        for well_index, image_indices in well_image_hierarchy:
            for image_index in image_indices:
                well_sample = omero.model.WellSampleI()
                well_sample.well = wells[well_index]
                well_sample.image = images[image_index]
                wells[well_index].addWellSample(well_sample)

        for well in named_entities['Well']:
            well.id = update.saveAndReturnObject(well).id

        for fileset_index, image_indices in fileset_image_hierarchy:
            for image_index in image_indices:
                images[image_index].fileset = filesets[fileset_index]
                update.saveAndReturnObject(images[image_index])

        # translate list indices into database IDs and check that test cases
        # run as expected

        for named_indices, fileset_split in test_cases:
            referenced = {}
            for name, indices in named_indices.items():
                referenced[name] = [
                    named_entities[name][index].id.val for index in indices]
            expected = {}
            for fileset_index, image_indices in fileset_split.items():
                fileset_id = filesets[fileset_index].id.val
                expected[fileset_id] = {}
                for included in [False, True]:
                    expected[fileset_id][included] = [
                        images[image_index].id.val
                        for image_index in image_indices[included]]
            if ipojo.getImagesBySplitFilesets(referenced, None) != expected:
                raise Exception(
                    'for referenced ' + str(named_indices)
                    + ' expected ' + str(fileset_split))
