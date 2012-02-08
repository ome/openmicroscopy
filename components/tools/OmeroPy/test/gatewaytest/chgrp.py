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

PRIVATE = 'rw----'
READONLY = 'rwr---'
COLLAB = 'rwrw--'

import gatewaytest.library as lib

class ChrgpTest (lib.GTest):

    def setUp (self):
        """ This is called at the start of tests """
        super(ChrgpTest, self).setUp()
        self.loginAsAuthor()
        self.image = self.getTestImage()


    def doChange(self, obj_type, obj_id, group_id, test_should_pass=True):
        """
        Performs the change-group action, waits on completion and checks that the 
        result is not an error.
        """
        prx = self.gateway.chgrpObject(obj_type, obj_id, group_id)

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
        rsp = self.doChange("Image", image.getId(), gid)
        
        # Image should no-longer be available in current group
        self.assertEqual(None, self.gateway.getObject("Image", image.id), "Image should not be available in original group")
        
        # Switch to new group - confirm that image is there.
        self.gateway.setGroupForSession(gid)
        img = self.gateway.getObject("Image", image.id)
        self.assertNotEqual(None, img, "Image should be available in new group")
        self.assertEqual(img.getDetails().getGroup().id, gid, "Image group.id should match new group")


if __name__ == '__main__':
    unittest.main()
