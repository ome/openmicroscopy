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

from integration.helpers import createTestImage
    
class TestGateway(lib.ITest):

    def testBasicUsage(self):
        gateway = self.client.sf.createGateway()
        gateway.getProjects([0],False)

        iid = createTestImage(self.client.sf)
        
        query = self.client.sf.getQueryService()

        params = omero.sys.Parameters()
        params.map = {}
        params.map["oid"] = rlong(iid)
        params.theFilter = omero.sys.Filter()
        params.theFilter.offset = rint(0)
        params.theFilter.limit = rint(1)
        pixel = query.findByQuery("select p from Pixels as p left outer join fetch p.image i where i.id=:oid", params)
        imgid = pixel.image.id.val
        gateway.getRenderedImage(pixel.id.val, 0, 0)

        gateway.close()
        
        
if __name__ == '__main__':
    unittest.main()
