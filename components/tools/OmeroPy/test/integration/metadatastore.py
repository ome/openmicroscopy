#!/usr/bin/env python

"""
   Integration test focused on the omero.api.MetadataStore interface

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest
import test.integration.library as lib
import omero
import omero_MetadataStore_ice
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI

class TestMetdataStore(lib.ITest):

    def testBasicUsage(self):

        ms = self.client.sf.createByName(omero.constants.METADATASTORE)
        ms = omero.api.MetadataStorePrx.checkedCast(ms)

        ms.setRoot(omero.RList([]))
        ms.setPixelsSizeX(omero.RInt(1), 1, 1)
        ms.setPixelsSizeY(omero.RInt(1), 1, 1)
        ms.setPixelsSizeZ(omero.RInt(1), 1, 1)
        ms.setPixelsSizeC(omero.RInt(1), 1, 1)
        ms.setPixelsSizeT(omero.RInt(1), 1, 1)
        ms.getProjects()
        ms.getExperimenterID()

if __name__ == '__main__':
    unittest.main()
