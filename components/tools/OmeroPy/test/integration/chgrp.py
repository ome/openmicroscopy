#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
#                      All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import omero
import integration.library as lib
import unittest
from omero.rtypes import *
from omero.cmd import Chgrp, State, ERR, OK

class TestChgrp(lib.ITest):
    
    def doChange(self, chgrp, test_should_pass=True):
        
        sf = self.client.sf

        prx = sf.submit(chgrp)

        self.assertFalse(State.FAILURE in prx.getStatus().flags)

        from omero.callbacks import CmdCallbackI 

        cb = CmdCallbackI(self.client, prx)
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

        sf = self.client.sf
        pixID = self.import_image()[0]

        pixels = sf.getQueryService().get("Pixels", pixID)
        imageId = pixels.getImage().getId().getValue()
        print "imageId", imageId
        to_group = 1
        uuid = sf.getAdminService().getEventContext().sessionUuid

        chgrp = Chgrp(uuid, "/Image", imageId, None, to_group)
        rsp = self.doChange(chgrp, True)

        image = sf.getQueryService().get("Image", imageId)
        self.asserEqual(image.getDetails().getGroup().getId().getValue(), to_group)


if __name__ == '__main__':
    unittest.main()
