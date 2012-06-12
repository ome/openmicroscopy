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


    def doChange(self, obj_type, obj_id, group_id, test_should_pass=True, return_complete=True):
        """
        Performs the change-group action, waits on completion and checks that the 
        result is not an error.
        """
        prx = self.gateway.chgrpObject(obj_type, obj_id, group_id)
        
        if not return_complete:
            return prx
        
        cb = CmdCallbackI(self.gateway.c, prx)
        cb.loop(20, 500)

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

        image = self.image
        ctx = self.gateway.getAdminService().getEventContext()
        uuid = ctx.sessionUuid

        self.loginAsAdmin()
        gid = self.gateway.createGroup("chgrp-test-%s" % uuid, member_Ids=[ctx.userId], perms=COLLAB)
        self.loginAsAuthor()
        self.assertNotEqual(None, self.gateway.getObject("Image", image.id))

        # Do the Chgrp
        rsp = self.doChange("Image", image.getId(), gid)

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
        image = self.image
        dataset = image.getParent()
        ctx = self.gateway.getAdminService().getEventContext()
        uuid = ctx.sessionUuid

        self.loginAsAdmin()
        gid = self.gateway.createGroup("chgrp-test-%s" % uuid, member_Ids=[ctx.userId], perms=PRIVATE)
        self.loginAsAuthor()
        self.assertNotEqual(None, self.gateway.getObject("Image", image.id))

        # Do the Chgrp
        rsp = self.doChange("Dataset", dataset.getId(), gid)

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
        image = self.image
        dataset = image.getParent()
        project = dataset.getParent()
        ctx = self.gateway.getAdminService().getEventContext()
        uuid = ctx.sessionUuid

        self.loginAsAdmin()
        gid = self.gateway.createGroup("chgrp-test-%s" % uuid, member_Ids=[ctx.userId], perms=COLLAB)
        self.loginAsAuthor()
        self.assertNotEqual(None, self.gateway.getObject("Image", image.id))

        # Do the Chgrp
        rsp = self.doChange("Project", project.getId(), gid)

        # Image should no-longer be available in current group
        self.assertEqual(None, self.gateway.getObject("Image", image.id), "Image should not be available in original group")

        # Switch to new group - confirm that Project, Dataset, Image is there.
        self.gateway.setGroupForSession(gid)
        prj = self.gateway.getObject("Project", project.id)
        self.assertNotEqual(None, prj, "Project should be available in new group")

        ds = self.gateway.getObject("Dataset", dataset.id)
        self.assertNotEqual(None, ds, "Dataset should be available in new group")

        img = self.gateway.getObject("Image", image.id)
        self.assertNotEqual(None, img, "Image should be available in new group")
        self.assertEqual(img.getDetails().getGroup().id, gid, "Image group.id should match new group")


    def testTwoDatasetsChgrp(self):
        """
        Create a new group with the User as member. Image has 2 Dataset Parents.
        Test move one Dataset to new group. Image does not move. Move 2nd Dataset - Image moves.
        """
        image = self.image
        dataset = image.getParent()
        orig_gid = dataset.getDetails().getGroup().id
        update = self.gateway.getUpdateService()

        new_ds = omero.model.DatasetI()
        new_ds.name = rstring("chgrp-parent2")
        new_ds = update.saveAndReturnObject(new_ds)
        link = omero.model.DatasetImageLinkI()
        link.setParent(new_ds)
        link.setChild(image._obj)
        update.saveObject(link)

        ctx = self.gateway.getAdminService().getEventContext()
        uuid = ctx.sessionUuid

        self.loginAsAdmin()
        gid = self.gateway.createGroup("chgrp-test-%s" % uuid, member_Ids=[ctx.userId])
        self.loginAsAuthor()
        self.assertNotEqual(None, self.gateway.getObject("Dataset", dataset.id))

        # Do the Chgrp with one of the parents
        rsp = self.doChange("Dataset", new_ds.id.val, gid)

        # Dataset should no-longer be available in current group
        self.assertEqual(None, self.gateway.getObject("Dataset", new_ds.id.val), "Dataset should not be available in original group")
        self.assertNotEqual(None, self.gateway.getObject("Dataset", dataset.getId()), "Other Dataset should still be in original group")
        # But Image should
        img = self.gateway.getObject("Image", image.id)
        self.assertNotEqual(None, img, "Image should still be available in original group")

        # Do the Chgrp with the OTHER parent
        self.gateway.setGroupForSession(orig_gid)
        rsp = self.doChange("Dataset", dataset.id, gid)

        # Confirm that Dataset AND Image is now in new group
        self.gateway.setGroupForSession(gid)
        ctx = self.gateway.getAdminService().getEventContext()
        ds = self.gateway.getObject("Dataset", dataset.id)
        self.assertNotEqual(None, ds, "Dataset should now be available in new group")

        img = self.gateway.getObject("Image", image.id)
        self.assertNotEqual(None, img, "Image should now be available in new group")
        self.assertEqual(img.getDetails().getGroup().id, gid, "Image group.id should match new group")


    def testChgrpAsync(self):
        """
        Try to reproduce "race condition" bugs seen in web #8037 (fails to reproduce)
        """
        image = self.image
        ctx = self.gateway.getAdminService().getEventContext()
        uuid = ctx.sessionUuid

        self.loginAsAdmin()
        gid = self.gateway.createGroup("chgrp-test-%s" % uuid, member_Ids=[ctx.userId], perms=COLLAB)
        self.loginAsAuthor()
        original_group = ctx.groupId
        self.assertNotEqual(None, self.gateway.getObject("Image", image.id))

        # Do the Chgrp
        rsp = self.doChange("Image", image.getId(), gid, return_complete=False)
        
        while rsp.getResponse() is None:
            # while waiting, try various things to reproduce race condition seen in web.
            img = self.gateway.getObject("Image", image.id)
            c = BlitzGateway()
            c.connect(sUuid=uuid)
            #self.gateway.setGroupForSession(gid)

        # Image should no-longer be available in current group
        self.assertEqual(None, self.gateway.getObject("Image", image.id), "Image should not be available in original group")

        # Switch to new group - confirm that image is there.
        self.gateway.setGroupForSession(gid)
        img = self.gateway.getObject("Image", image.id)
        self.assertNotEqual(None, img, "Image should be available in new group")
        self.assertEqual(img.getDetails().getGroup().id, gid, "Image group.id should match new group")

if __name__ == '__main__':
    unittest.main()
