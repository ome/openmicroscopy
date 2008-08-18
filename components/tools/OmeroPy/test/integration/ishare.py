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

class TestISShare(lib.ITest):

    def testBasicUsage(self):
        share = self.client.sf.getShareService()
        update = self.client.sf.getUpdateService()

        description = "my description"
        timeout = None
        objects = []
        experimenters = []
        guests = []
        enabled = omero.RTime()
        id = share.createShare(description, timeout, objects,
                               experimenters, guests, enabled)
        
        self.assert_(len(share.getContents(id)) == 0)
        
        d = omero.model.DatasetI()
        d.setName(omero.RString("d"))
        d = update.saveAndReturnObject(d)
        share.addObjects(id, [d])

        self.assert_(len(share.getContents(id)) == 1)
        
        ds = []
        for i in range(0,100):
            ds.append(omero.model.DatasetI())
            ds[i].setName(omero.RString("ds"))
        ds = update.saveAndReturnArray(ds)
        share.addObjects(id, ds)

        self.assert_(share.getContentSize(id) == 101)

    def testRetrieval(self):
        shs = self.root.sf.getShareService()
        shs.getAllShares(True)

if __name__ == '__main__':
    unittest.main()
