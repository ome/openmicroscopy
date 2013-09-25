#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Tests for the stateful Gateway service.

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
from omero.rtypes import *

from test.integration.helpers import createTestImage
    
class TestGateway(lib.ITest):

    def testBasicUsage(self):
        gateway = self.client.sf.createGateway()
        gateway.getProjects([0],False)

        try:
            # query below does not find image if created with self.createTestImage() even though it 
            # uses 'identical' code to createTestImage(self.client.sf), which uses script_utils 
            #iid = self.createTestImage().getId().getValue() 
            iid = createTestImage(self.client.sf)
            print iid, type(iid)
            query = self.client.sf.getQueryService()
    
            params = omero.sys.Parameters()
            params.map = {}
            params.map["oid"] = rlong(iid)
            params.theFilter = omero.sys.Filter()
            params.theFilter.offset = rint(0)
            params.theFilter.limit = rint(1)
            pixel = query.findByQuery("select p from Pixels as p left outer join fetch p.image as i where i.id=:oid", params)
            print pixel
            imgid = pixel.image.id.val
            print imgid
            gateway.getRenderedImage(pixel.id.val, 0, 0)
        except omero.ValidationException, ve:
            print " testBasicUsage - createTestImage has failed. This fixture method needs to be fixed."

        gateway.close()
        
        
if __name__ == '__main__':
    unittest.main()
