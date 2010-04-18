#!/usr/bin/env python

"""
   Tests for the stateful Gateway service.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest
import integration.library as lib
import omero
import omero_api_Gateway_ice
import omero_Constants_ice
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI
from omero.rtypes import *

# Common bits
params = omero.sys.Parameters()
params.theFilter = omero.sys.Filter()
params.theFilter.offset = rint(0)
params.theFilter.limit = rint(1)

class TestGateway(lib.ITest):

    def testBasicUsage(self):
        gateway = self.client.sf.createGateway()
        gateway.getProjects([0],False)
        query = self.client.sf.getQueryService()
        thumb = query.findByQuery("select t from Thumbnail t join fetch t.pixels", params)
        pixid = thumb.pixels.id.val
        imgid = thumb.pixels.image.id.val
        gateway.getRenderedImage(pixid, 0, 0)

if __name__ == '__main__':
    unittest.main()
