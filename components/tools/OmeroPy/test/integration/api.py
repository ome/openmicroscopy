#!/usr/bin/env python

"""
   Attempt to write a full api test.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import test.integration.library as lib
import omero
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI

class TestApi(lib.ITest):

    def testAdmin(self):
        a = self.client.sf.getAdminService()
        self.assert_(a.getEventContext() != None)

    def testThumbnail(self):
        q = self.client.sf.getQueryService()

        # Filter to only get one possible pixels
        f = omero.sys.Filter()
        f.offset = omero.RInt(0)
        f.limit  = omero.RInt(1)
        p = omero.sys.Parameters()
        p.theFilter = f

        pixel = q.findByQuery("select p from Pixels p join fetch p.thumbnails t", p)
        tstore = self.client.sf.createThumbnailStore()
        if not tstore.setPixelsId(pixel.id.val):
            tstore.resetDefaults()
            tstore.setPixelsId(pixel.id.val)
        tstore.getThumbnail(omero.RInt(16), omero.RInt(16))

if __name__ == '__main__':
    unittest.main()
