#!/usr/bin/env python

"""
   Test to reproduce an exception thrown on closing ThumbnailStore.
   After running this it is necessary to visually inspect the server
   log.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest
import test.integration.library as lib
import omero
import omero_Constants_ice
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI

class Test1048(lib.ITest):
    def testReproduce(self):
        c = self.client
        t = c.sf.createThumbnailStore()
        c.sf.closeOnDestroy()
        c.closeSession()

if __name__ == '__main__':
    unittest.main()
