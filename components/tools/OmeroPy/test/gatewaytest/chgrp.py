#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
#                      All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import omero
import unittest
from omero.rtypes import *
from omero.cmd import Chgrp, State, ERR, OK
from omero.callbacks import CmdCallbackI
from omero.gateway import BlitzGateway

PRIVATE = 'rw----'
READONLY = 'rwr---'
COLLAB = 'rwrw--'

import gatewaytest.library as lib

class ChgrpTest (lib.GTest):

    def setUp (self):
        """ This is called at the start of tests """
        super(ChgrpTest, self).setUp()
        self.loginAsAuthor()
        self.image = self.getTestImage()


    def doChange(self, obj_type, obj_ids, group_id, container_id=None, test_should_pass=True, return_complete=True):
        """
        Performs the change-group action, waits on completion and checks that the 
        result is not an error.
        """
        prx = self.gateway.chgrpObjects(obj_type, obj_ids, group_id, container_id)
        
        if not return_complete:
            return prx
        
        cb = CmdCallbackI(self.gateway.c, prx)
        for i in range(10):
            cb.loop(20, 500)
            if prx.getResponse() != None:
                break

        self.assertNotEqual(prx.getResponse(), None)

        status = prx.getStatus()
        rsp = prx.getResponse()

        if test_should_pass:
            if isinstance(rsp, ERR):
                self.fail("Found ERR when test_should_pass==true: %s (%s) params=%s" % (rsp.category, rsp.name, rsp.parameters))
            self.assertFalse(State.FAILURE in prx.getStatus().flags)
        else:
            if isinstance(rsp, OK):
                self.fail("Found OK when test_should_pass==false: %s", rsp)
            self.assertTrue(State.FAILURE in prx.getStatus().flags)
        return rsp


    def testImageChgrp(self):
        """
        Create a new group with the User as member. Test move the Image to new group.
        """
        image = self.createTestImage()
        ctx = self.gateway.getAdminService().getEventContext()
        uuid = ctx.sessionUuid

        self.loginAsAdmin()
        gid = self.gateway.createGroup("chgrp-test-%s" % uuid, member_Ids=[ctx.userId], perms=COLLAB)
        self.loginAsAuthor()
        self.assertNotEqual(None, self.gateway.getObject("Image", image.id))

        # Do the Chgrp
        rsp = self.doChange("Image", [image.getId()], gid)

        # Image should no-longer be available in current group
        self.assertEqual(None, self.gateway.getObject("Image", image.id), "Image should not be available in original group")

        # Switch to new group - confirm that image is there.
        self.gateway.setGroupForSession(gid)
        img = self.gateway.getObject("Image", image.id)
        self.assertNotEqual(None, img, "Image should be available in new group")
        self.assertEqual(img.getDetails().getGroup().id, gid, "Image group.id should match new group")


    def testDatasetChgrp(self):
        """
        Create a new group with the User as member. Test move the Dataset/Image to new group.
        """
        dataset = self.createPDTree(dataset="testDatasetChgrp")
        image = self.createTestImage(dataset=dataset)
        ctx = self.gateway.getAdminService().getEventContext()
        uuid = ctx.sessionUuid

        self.loginAsAdmin()
        gid = self.gateway.createGroup("chgrp-test-%s" % uuid, member_Ids=[ctx.userId], perms=PRIVATE)
        self.loginAsAuthor()
        self.assertNotEqual(None, self.gateway.getObject("Image", image.id))

        # Do the Chgrp
        rsp = self.doChange("Dataset", [dataset.id], gid)

        # Dataset should no-longer be available in current group
        self.assertEqual(None, self.gateway.getObject("Dataset", dataset.id), "Dataset should not be available in original group")

        # Switch to new group - confirm that Dataset, Image is there.
        self.gateway.setGroupForSession(gid)
        ds = self.gateway.getObject("Dataset", dataset.id)
        self.assertNotEqual(None, ds, "Dataset should be available in new group")

        img = self.gateway.getObject("Image", image.id)
        self.assertNotEqual(None, img, "Image should be available in new group")
        self.assertEqual(img.getDetails().getGroup().id, gid, "Image group.id should match new group")


    def testPDIChgrp(self):
        """
        Create a new group with the User as member. Test move the Project/Dataset/Image to new group.
        """
        link = self.createPDTree(project="testPDIChgrp", dataset="testPDIChgrp")
        dataset = link.getChild()   # DatasetWrapper
        project = link.parent   # omero.model.ProjectI - link.getParent() overwritten - returns None
        image = self.createTestImage(dataset=dataset)

        ctx = self.gateway.getAdminService().getEventContext()
        uuid = ctx.sessionUuid

        self.loginAsAdmin()
        gid = self.gateway.createGroup("chgrp-test-%s" % uuid, member_Ids=[ctx.userId], perms=COLLAB)
        self.loginAsAuthor()
        self.assertNotEqual(None, self.gateway.getObject("Image", image.id))

        # Do the Chgrp
        rsp = self.doChange("Project", [project.id.val], gid)

        # Image should no-longer be available in current group
        self.assertEqual(None, self.gateway.getObject("Image", image.id), "Image should not be available in original group")

        # Switch to new group - confirm that Project, Dataset, Image is there.
        self.gateway.setGroupForSession(gid)
        prj = self.gateway.getObject("Project", project.id.val)
        self.assertNotEqual(None, prj, "Project should be available in new group")

        ds = self.gateway.getObject("Dataset", dataset.id)
        self.assertNotEqual(None, ds, "Dataset should be available in new group")

        img = self.gateway.getObject("Image", image.id)
        self.assertNotEqual(None, img, "Image should be available in new group")
        self.assertEqual(img.getDetails().getGroup().id, gid, "Image group.id should match new group")


    def testTwoDatasetsChgrpToProject(self):
        """
        Create a new group with the User as member. Image has 2 Dataset Parents.
        Test move one Dataset to new group. Image does not move. Move 2nd Dataset - Image moves.
        """
        dataset = self.createPDTree(dataset="testTwoDatasetsChgrpToProject")
        image = self.createTestImage(dataset=dataset)
        orig_gid = dataset.details.group.id.val

        new_ds = self.createPDTree(dataset="testTwoDatasetsChgrp-parent2")
        update = self.gateway.getUpdateService()
        link = omero.model.DatasetImageLinkI()
        link.setParent(omero.model.DatasetI(new_ds.id, False))
        link.setChild(omero.model.ImageI(image.id, False))
        update.saveObject(link)

        ctx = self.gateway.getAdminService().getEventContext()
        uuid = ctx.sessionUuid

        self.loginAsAdmin()
        gid = self.gateway.createGroup("chgrp-test-%s" % uuid, member_Ids=[ctx.userId])
        self.loginAsAuthor()
        self.assertNotEqual(None, self.gateway.getObject("Dataset", dataset.id))

        # create Project in destination group
        self.gateway.setGroupForSession(gid)
        p = omero.model.ProjectI()
        p.name = rstring("testTwoDatasetsChgrpToProject")
        p = self.gateway.getUpdateService().saveAndReturnObject(p)
        self.assertEqual(p.details.group.id.val, gid, "Project should be created in target group")
        self.gateway.setGroupForSession(orig_gid)   # switch back

        # Do the Chgrp with one of the parents
        rsp = self.doChange("Dataset", [new_ds.id], gid)

        # Dataset should no-longer be available in current group
        self.assertEqual(None, self.gateway.getObject("Dataset", new_ds.id), "Dataset should not be available in original group")
        self.assertNotEqual(None, self.gateway.getObject("Dataset", dataset.getId()), "Other Dataset should still be in original group")
        # But Image should
        img = self.gateway.getObject("Image", image.id)
        self.assertNotEqual(None, img, "Image should still be available in original group")

        # Do the Chgrp with the OTHER parent
        self.gateway.setGroupForSession(gid)    # switch BEFORE doChange to allow Project link Save
        rsp = self.doChange("Dataset", [dataset.id], gid, container_id=p.id.val)

        # Confirm that Dataset AND Image is now in new group
        ctx = self.gateway.getAdminService().getEventContext()
        ds = self.gateway.getObject("Dataset", dataset.id)
        projects = list(ds.listParents())
        self.assertEqual(len(projects), 1, "Dataset should have one parent Project in new group")
        self.assertEqual(projects[0].getId(), p.id.val, "Check Dataset parent is Project created above")
        self.assertNotEqual(None, ds, "Dataset should now be available in new group")
        self.assertEqual(ds.getDetails().getGroup().id, gid, "Dataset group.id should match new group")

        img = self.gateway.getObject("Image", image.id)
        self.assertNotEqual(None, img, "Image should now be available in new group")
        self.assertEqual(img.getDetails().getGroup().id, gid, "Image group.id should match new group")


    def testMultiDatasetDoAll(self):
        """
        Need to enable chgrp independently of EventContext group being the destination group.
        Other tests that do not set omero.group require this for DoAll Save to work.
        """
        ctx = self.gateway.getAdminService().getEventContext()
        uuid = ctx.sessionUuid
        update = self.gateway.getUpdateService()

        new_ds = omero.model.DatasetI()
        new_ds.name = rstring("testMultiDatasetDoAll")
        new_ds = update.saveAndReturnObject(new_ds)
        
        new_ds2 = omero.model.DatasetI()
        new_ds2.name = rstring("testMultiDatasetDoAll2")
        new_ds2 = update.saveAndReturnObject(new_ds2)
        
        # new group
        self.loginAsAdmin()
        gid = self.gateway.createGroup("testMultiDatasetDoAll-%s" % uuid, member_Ids=[ctx.userId])
        self.loginAsAuthor()
        
        # create Project in new group
        self.gateway.SERVICE_OPTS.setOmeroGroup(gid)
        p = omero.model.ProjectI()
        p.name = rstring("testMultiChgrp")
        p = self.gateway.getUpdateService().saveAndReturnObject(p, self.gateway.SERVICE_OPTS)
        self.assertEqual(p.details.group.id.val, gid, "Project should be created in target group")
        
        # Test that this works whichever group you're in
        self.gateway.SERVICE_OPTS.setOmeroGroup(ctx.groupId)
        dsIds = [new_ds.id.val, new_ds2.id.val]
        
        # Chgrp
        rsp = self.doChange("Dataset", dsIds, gid, container_id=p.id.val)
        
        # Check all objects in destination group
        self.gateway.SERVICE_OPTS.setOmeroGroup(-1)     # we can get objects from either group...
        p = self.gateway.getObject("Project", p.id.val)
        datasets = list(p.listChildren())
        self.assertEqual(len(datasets), 2, "Project should have 2 new Datasets")
        for d in datasets:
            self.assertEqual(d.details.group.id.val, gid, "Dataset should be in new group")
            self.assertTrue(d.getId() in dsIds, "Checking Datasets by ID")

if __name__ == '__main__':
    unittest.main()
