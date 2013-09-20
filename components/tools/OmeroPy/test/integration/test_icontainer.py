#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test focused on the omero.api.IPojos interface
   a running server.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import test.integration.library as lib
import omero, unittest
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI
from omero_model_ImageAnnotationLinkI import ImageAnnotationLinkI
from omero_model_CommentAnnotationI import CommentAnnotationI
from omero.rtypes import rstring, rtime

class TestIContainer(lib.ITest):

    def testFindAnnotations(self):
        ipojo = self.client.sf.getContainerService()
        i = ImageI()
        i.setName(rstring("name"))
        i.setAcquisitionDate(rtime(0))
        i = ipojo.createDataObject(i,None)

    def testFindAndCountAnnotationsForSharedData(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        query = self.root.sf.getQueryService()
        update = self.root.sf.getUpdateService()
        admin = self.root.sf.getAdminService()
        ipojo = self.root.sf.getContainerService()
        
        ### create new users
        #group1
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("group1_%s" % uuid)
        gid = admin.createGroup(new_gr1)
        
        #new user1
        new_exp = ExperimenterI()
        new_exp.omeName = rstring("user1_%s" % uuid)
        new_exp.firstName = rstring("New")
        new_exp.lastName = rstring("Test")
        
        defaultGroup = admin.getGroup(gid)
        listOfGroups = list()
        listOfGroups.append(admin.lookupGroup("user"))
        
        eid = admin.createExperimenterWithPassword(new_exp, rstring("ome"), defaultGroup, listOfGroups)
        
        #new user2
        new_exp2 = ExperimenterI()
        new_exp2.omeName = rstring("user2_%s" % uuid)
        new_exp2.firstName = rstring("New2")
        new_exp2.lastName = rstring("Test2")
        
        defaultGroup = admin.getGroup(gid)
        listOfGroups = list()
        listOfGroups.append(admin.lookupGroup("user"))
        
        eid2 = admin.createExperimenterWithPassword(new_exp2, rstring("ome"), defaultGroup, listOfGroups)
    
        ## get users
        user1 = admin.getExperimenter(eid)
        user2 = admin.getExperimenter(eid2)
        
        ## login as user1
        cl1 = self.new_client(user=user1, password="ome")
        update1 = cl1.sf.getUpdateService()
        ipojo1 = cl1.sf.getContainerService()
        
        # create image
        img = ImageI()
        img.setName(rstring('test1154-img-%s' % (uuid)))
        img.setAcquisitionDate(rtime(0))

        # default permission 'rw----':
        img = update1.saveAndReturnObject(img)
        img.unload()

        ann1 = CommentAnnotationI()
        ann1.textValue = rstring("user comment - %s" % uuid)
        l_ann1 = ImageAnnotationLinkI()
        l_ann1.setParent(img)
        l_ann1.setChild(ann1)
        update1.saveObject(l_ann1)

        #user retrives the annotations for image
        coll_count = ipojo1.getCollectionCount("Image", "ome.model.containers.Image_annotationLinks", [img.id.val], None)
        self.assertEquals(1, coll_count.get(img.id.val, []))
        #self.assertEquals(1, len(ipojo1.findAnnotations("Image", [img.id.val], None, None).get(img.id.val, [])))

        ## login as user2
        cl2 = self.new_client(user=user2, password="ome")
        update2 = cl1.sf.getUpdateService()
        
        ann = CommentAnnotationI()
        ann.textValue = rstring("user2 comment - %s" % uuid)
        l_ann = ImageAnnotationLinkI()
        l_ann.setParent(img)
        l_ann.setChild(ann)
        update2.saveObject(l_ann)
        
        #do they see the same vals?
        #print ipojo1.getCollectionCount("Image", "ome.model.containers.Image_annotationLinks", [img.id.val], None)
        #print ipojo.getCollectionCount("Image", "ome.model.containers.Image_annotationLinks", [img.id.val], None)
        #print len(ipojo1.findAnnotations("Image", [img.id.val], None, None).get(img.id.val, []))
        #print len(ipojo.findAnnotations("Image", [img.id.val], None, None).get(img.id.val, []))
        coll_count = ipojo1.getCollectionCount("Image", "ome.model.containers.Image_annotationLinks", [img.id.val], None)
        self.assertEquals(2, coll_count.get(img.id.val, []))
        #anns = ipojo1.findAnnotations("Image", [img.id.val], None, None).get(img.id.val, [])
        #self.assertEquals(2, len(anns))
        
        #self.assert_(anns[0].details.permissions == 'rw----')
        #self.assert_(anns[1].details.permissions == 'rw----')
        
        cl1.sf.closeOnDestroy()
        cl2.sf.closeOnDestroy()
    
    def testCreateAfterBlitzPort(self):
        ipojo = self.client.sf.getContainerService()
        i = ImageI()
        i.setName(rstring("name"))
        i.setAcquisitionDate(rtime(0))
        i = ipojo.createDataObject(i,None)
        o = i.getDetails().owner
        self.assertEquals( -1, o.sizeOfGroupExperimenterMap() )


class TestSplitFilesets(lib.ITest):

    def checkSplitFilesets(self, client, dtypeIdsMap, expected):
        """
        To check we get the expected result from iContainer.getImagesBySplitFilesets()
        we do the query with dtype & ids and compare the returned data with
        the specified dict.
        """
        container = client.sf.getContainerService()
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
        self.assertEqual(set(result.keys()), set(expected.keys()), "Result should have expected Fileset IDs")
        for fsId, expectedDict in expected.items():
            self.assertTrue(cmpLists(expectedDict[True], result[fsId][True]), "True ImageIDs should match")
            self.assertTrue(cmpLists(expectedDict[False], result[fsId][False]), "False ImageIDs should match")

    def testFilesetSplitByImage(self):
        """
        Fileset of 2 Images, we test split using 1 Image ID
        """
        client, user = self.new_client_and_user(perms="rw----")
        images = self.importMIF(2, client=client)

        # Lookup the fileset
        imgId = images[0].id.val
        query = client.sf.getQueryService()
        filesetId =  query.get('Image', imgId).fileset.id.val

        # Define what we expect & query split fileset
        expected = {filesetId: {True: [imgId], False: [images[1].id.val]}}
        self.checkSplitFilesets(client, {'Image': [imgId]}, expected)

    def testFilesetNotSplitByImage(self):
        """
        Fileset of 2 Images with No split (query with both Image IDs)
        """
        client, user = self.new_client_and_user(perms="rw----")
        images = self.importMIF(2, client=client)

        # Lookup the fileset
        imgIds = [i.id.val for i in images]
        query = client.sf.getQueryService()
        filesetId =  query.get('Image', imgIds[0]).fileset.id.val

        # Define what we expect & query split fileset
        expected = {}
        self.checkSplitFilesets(client, {'Image': imgIds}, expected)

    def testFilesetSplitByDatasetAndProject(self):
        """
        Fileset of 2 Images, one in a Dataset. Test split using Dataset ID
        """
        client, user = self.new_client_and_user(perms="rw----")
        update = client.sf.getUpdateService()
        query = client.sf.getQueryService()

        # Dataset contains 1 image of a 2-image fileset
        images = self.importMIF(2, client=client)
        ds = omero.model.DatasetI()
        ds.name = omero.rtypes.rstring("testFilesetSplitByDataset")
        ds = update.saveAndReturnObject(ds)
        link = omero.model.DatasetImageLinkI()
        link.setParent(ds.proxy())
        link.setChild(images[0].proxy())
        link = update.saveAndReturnObject(link)

        # Dataset in Project
        pr = omero.model.ProjectI()
        pr.name = omero.rtypes.rstring("testFilesetSplitByProject")
        pr = update.saveAndReturnObject(pr)
        link = omero.model.ProjectDatasetLinkI()
        link.setParent(pr.proxy())
        link.setChild(ds.proxy())
        link = update.saveAndReturnObject(link)

        # Lookup the fileset
        imgId = images[0].id.val
        filesetId =  query.get('Image', imgId).fileset.id.val

        # Define what we expect & query split fileset
        expected = {filesetId: {True: [imgId], False: [images[1].id.val]}}
        self.checkSplitFilesets(client, {'Dataset': [ds.id.val]}, expected)
        # Expect same result if query via Project
        self.checkSplitFilesets(client, {'Project': [pr.id.val]}, expected)

        # No split if we include the extra image ID
        expected = {}
        idsMap = {'Dataset': [ds.id.val], "Image": [images[1].id.val]}
        self.checkSplitFilesets(client, idsMap, expected)
        idsMap = {'Project': [pr.id.val], "Image": [images[1].id.val]}
        self.checkSplitFilesets(client, idsMap, expected)

    def testFilesetNotSplitByDatasets(self):
        """
        Fileset of 2 Images, both in different Datasets. Test Not split using Dataset IDs
        """
        client, user = self.new_client_and_user(perms="rw----")
        update = client.sf.getUpdateService()
        query = client.sf.getQueryService()

        # Datasets each contain 1 image of a 2-image fileset
        datasets = self.createDatasets(2, "testFilesetNotSplitByDatasets", client=client)
        images = self.importMIF(2, client=client)
        for i in range(2):
            link = omero.model.DatasetImageLinkI()
            link.setParent(datasets[i].proxy())
            link.setChild(images[i].proxy())
            link = update.saveAndReturnObject(link)

        # Another Dataset contains both images
        ds = omero.model.DatasetI()
        ds.name = omero.rtypes.rstring("testFilesetNotSplitByDatasets")
        ds = update.saveAndReturnObject(ds)
        for i in images:
            link = omero.model.DatasetImageLinkI()
            link.setParent(ds.proxy())
            link.setChild(i.proxy())
            link = update.saveAndReturnObject(link)

        # Lookup the fileset
        imgId = images[0].id.val
        filesetId =  query.get('Image', imgId).fileset.id.val

        # No split if we pass in both Dataset IDs...
        dsIds = [d.id.val for d in datasets]
        expected = {}
        self.checkSplitFilesets(client, {'Dataset': dsIds}, expected)
        # ...or the Dataset that contains both images
        self.checkSplitFilesets(client, {'Dataset': [ds.id.val]}, expected)

        # confirm split if we choose one Dataset
        expected = {filesetId: {True: [imgId], False: [images[1].id.val]}}
        self.checkSplitFilesets(client, {'Dataset': [datasets[0].id.val]}, expected)

    def testGetImagesBySplitFilesetsManyCases(self):
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()
        ipojo = self.client.sf.getContainerService()

        # entity hierarchy

        project_dataset_hierarchy = [(0, [0,1])]
        dataset_image_hierarchy = [(0, [0,1]), (1, [2,6]), (2, [3,4,5])]
        screen_plate_hierarchy = [(0, [0,1])]
        plate_well_hierarchy = [(0, [0,1]), (1, [2,6]), (2, [3,4,5])]
        well_image_hierarchy = [(0, [0]), (1, [1]), (2, [2]), (3, [3]), (4, [4]), (5, [5])]
        fileset_image_hierarchy = [(0, [0]), (1, [1,2]), (2, [3,4,5])]

        # test data, input and expected output value, is by list index

        test_cases = [({'Image': [0,1,2,3,4,5]}, {}),
                      ({'Image': [6]}, {}),
                      ({'Image': [0,1]}, {1: {True: [1], False: [2]}}),
                      ({'Image': [3]}, {2: {True: [3], False: [4,5]}}),
                      ({'Image': [5]}, {2: {True: [5], False: [3,4]}}),
                      ({'Image': [3,4]}, {2: {True: [3,4], False: [5]}}),
                      ({'Image': [0,1,5,6]}, {1: {True: [1], False: [2]}, 2: {True: [5], False: [3,4]}}),
                      ({'Fileset': [0], 'Image': [0,3,4,5,6]}, {}),
                      ({'Fileset': [0,1,2], 'Image': [0]}, {}),
                      ({'Well': [0,1,2,3,4,5]}, {}),
                      ({'Well': [6]}, {}),
                      ({'Well': [0,1]}, {1: {True: [1], False: [2]}}),
                      ({'Well': [3]}, {2: {True: [3], False: [4,5]}}),
                      ({'Well': [5]}, {2: {True: [5], False: [3,4]}}),
                      ({'Well': [3,4]}, {2: {True: [3,4], False: [5]}}),
                      ({'Well': [0,1,5,6]}, {1: {True: [1], False: [2]}, 2: {True: [5], False: [3,4]}}),
                      ({'Image': [0,1], 'Well': [5,6]}, {1: {True: [1], False: [2]}, 2: {True: [5], False: [3,4]}}),
                      ({'Fileset': [0], 'Well': [0,3,4,5,6]}, {}),
                      ({'Fileset': [0,1,2], 'Well': [0]}, {}),
                      ({'Fileset': [2]}, {}),
                      ({'Dataset': [0]}, {1: {True: [1], False: [2]}}),
                      ({'Dataset': [1]}, {1: {True: [2], False: [1]}}),
                      ({'Dataset': [2]}, {}),
                      ({'Dataset': [1], 'Image': [0,1]}, {}),
                      ({'Project': [0]}, {}),
                      ({'Project': [0], 'Image': [3]}, {2: {True: [3], False: [4,5]}}),
                      ({'Dataset': [0], 'Fileset': [1]}, {}),
                      ({'Plate': [0]}, {1: {True: [1], False: [2]}}),
                      ({'Plate': [1]}, {1: {True: [2], False: [1]}}),
                      ({'Plate': [2]}, {}),
                      ({'Plate': [1], 'Image': [0,1]}, {}),
                      ({'Plate': [1], 'Well': [0,1]}, {}),
                      ({'Plate': [1], 'Image': [0], 'Well': [1]}, {}),
                      ({'Screen': [0]}, {}),
                      ({'Screen': [0], 'Image': [3]}, {2: {True: [3], False: [4,5]}}),
                      ({'Screen': [0], 'Well': [3]}, {2: {True: [3], False: [4,5]}}),
                      ({'Screen': [0], 'Image': [3], 'Well': [3]}, {2: {True: [3], False: [4,5]}}),
                      ({'Plate': [0], 'Fileset': [1]}, {})]

        # TODO: consider factoring some of the below out into library functions for use by other tests

        # name entity lists

        projects = []
        datasets = []
        screens  = []
        plates   = []
        wells    = []
        filesets = []
        images   = []

        named_entities = {'Project': projects, 'Dataset': datasets, 'Screen': screens, 'Plate': plates,
                          'Well': wells, 'Fileset': filesets, 'Image': images}

        # note all test case input values

        all_inputs = {}
        for name in named_entities.keys():
            all_inputs[name] = []

        for input, expected in test_cases:
            for name, ids in input.items():
                all_inputs[name] += ids

        # create test entities named in test case input values

        parents  = lambda hierarchy:     [ from_index for from_index, to_indices in hierarchy ]
        children = lambda hierarchy: sum([ to_indices for from_index, to_indices in hierarchy ], [])

        for project_index in set(all_inputs['Project'] + parents(project_dataset_hierarchy)):
            project = omero.model.ProjectI()
            project.name = rstring('Project #%i' % project_index)
            project.id = update.saveAndReturnObject(project).id
            projects.append(query.get('Project', project.id.val))
        for dataset_index in set(all_inputs['Dataset'] + children(project_dataset_hierarchy) + parents(dataset_image_hierarchy)):
            dataset = omero.model.DatasetI()
            dataset.name = rstring('Dataset #%i' % dataset_index)
            dataset.id = update.saveAndReturnObject(dataset).id
            datasets.append(query.get('Dataset', dataset.id.val))
        for screen_index in set(all_inputs['Screen'] + parents(screen_plate_hierarchy)):
            screen = omero.model.ScreenI()
            screen.name = rstring('Screen #%i' % screen_index)
            screen.id = update.saveAndReturnObject(screen).id
            screens.append(query.get('Screen', screen.id.val))
        for plate_index in set(all_inputs['Plate'] + children(screen_plate_hierarchy) + parents(plate_well_hierarchy)):
            plate = omero.model.PlateI()
            plate.name = rstring('Plate #%i' % plate_index)
            plate.id = update.saveAndReturnObject(plate).id
            plates.append(query.get('Plate', plate.id.val))
        for well_index in set(all_inputs['Well'] + children(plate_well_hierarchy) + parents(well_image_hierarchy)):
            well = omero.model.WellI()
            wells.append(well)  # cannot save until attached to plate
        for fileset_index in set(all_inputs['Fileset'] + parents(fileset_image_hierarchy)):
            fileset = omero.model.FilesetI()
            fileset.id = update.saveAndReturnObject(fileset).id
            filesets.append(query.get('Fileset', fileset.id.val))
        for image_index in set(all_inputs['Image'] + children(dataset_image_hierarchy)
                                                   + children(well_image_hierarchy)
                                                   + children(fileset_image_hierarchy)):
            image = omero.model.ImageI()
            image.name = rstring('Image #%i' % image_index)
            image.acquisitionDate = rtime(0L)
            image.id = update.saveAndReturnObject(image).id
            images.append(query.get('Image', image.id.val))

        # associate test entities

        for project_index, dataset_indices in project_dataset_hierarchy:
            for dataset_index in dataset_indices:
                project_dataset = omero.model.ProjectDatasetLinkI()
                project_dataset.parent = projects[project_index]
                project_dataset.child = datasets[dataset_index]
                update.saveAndReturnObject(project_dataset)

        for dataset_index, image_indices in dataset_image_hierarchy:
            for image_index in image_indices:
                dataset_image = omero.model.DatasetImageLinkI()
                dataset_image.parent = datasets[dataset_index]
                dataset_image.child = images[image_index]
                update.saveAndReturnObject(dataset_image)

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

        # translate list indices into database IDs and check that test cases run as expected

        for named_indices, fileset_split in test_cases:
            referenced = {}
            for name, indices in named_indices.items():
                referenced[name] = [ named_entities[name][index].id.val for index in indices ]
            expected = {}
            for fileset_index, image_indices in fileset_split.items():
                fileset_id = filesets[fileset_index].id.val
                expected[fileset_id] = {}
                for included in [False, True]:
                    expected[fileset_id][included] = [ images[image_index].id.val for image_index in image_indices[included] ]
            if ipojo.getImagesBySplitFilesets(referenced, None) != expected:
                raise Exception('for referenced ' + str(named_indices) + ' expected ' + str(fileset_split))

if __name__ == '__main__':
    unittest.main()
