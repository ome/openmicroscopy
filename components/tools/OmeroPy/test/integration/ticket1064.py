#!/usr/bin/env python

"""
   Integration test focused on the omero.api.IShare interface
   a running server.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest
import test.integration.library as lib
import omero
import omero_RTypes_ice
import omero_Constants_ice
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI

class TestTicket1064(lib.ITest):

    def testBasicUsage(self):
        share = self.client.sf.getShareService()
        update = self.client.sf.getUpdateService()
        admin = self.client.sf.getAdminService()
        
        cx = admin.getEventContext()
        print cx
        
        self.client.sf.closeOnDestroy()

if __name__ == '__main__':
    unittest.main()
