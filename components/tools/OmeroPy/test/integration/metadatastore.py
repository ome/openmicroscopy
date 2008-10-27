#!/usr/bin/env python

"""
   Integration test focused on the omero.api.MetadataStore interface

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest
import test.integration.library as lib
import omero
import omero_api_MetadataStore_ice
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI
from omero.rtypes import *

class TestMetdataStore(lib.ITest):

    def testBasicUsage(self):

        ms = self.client.sf.createByName(omero.constants.METADATASTORE)
        ms = omero.api.MetadataStorePrx.checkedCast(ms)

        ms.setRoot(rlist([]))
        ms.setPixelsSizeX(rint(1), 1, 1)
        ms.setPixelsSizeY(rint(1), 1, 1)
        ms.setPixelsSizeZ(rint(1), 1, 1)
        ms.setPixelsSizeC(rint(1), 1, 1)
        ms.setPixelsSizeT(rint(1), 1, 1)
        ms.getProjects()

if __name__ == '__main__':
    unittest.main()
