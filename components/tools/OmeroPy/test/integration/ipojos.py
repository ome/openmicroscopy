#!/usr/bin/env python

"""
   Integration test focused on the omero.api.IPojos interface
   a running server.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import test.integration.library as lib
import omero
import omero_RTypes_ice
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI

class TestIPojos(lib.ITest):

    def testFindAnnotations(self):
        ipojo = self.client.sf.getPojosService()
        i = ImageI()
        i.setName(omero.RString("name"))
        i = ipojo.createDataObject(i,None)

if __name__ == '__main__':
    unittest.main()
