#!/usr/bin/env python

"""
   Tests for the Pixels service.

"""

import omero
import unittest
import integration.library as lib


class TestPixelsService(lib.ITest):
            
    def testCreateImage(self):
        """
        Create a new image 
        """
        pixelsService = self.client.sf.getPixelsService()
        queryService = self.client.sf.getQueryService()

        pixelsType = queryService.findByQuery("from PixelsType as p where p.value='int8'", None)
        self.assertTrue(pixelsType is not None)

        sizeX = 1
        sizeY = 1
        sizeZ = 1
        sizeT = 1
        channelList = range(3)
        iId = pixelsService.createImage(sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType, "testCreateImage", description=None)

if __name__ == '__main__':
    unittest.main()
