#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Tests for the Pixels service.

"""

import omero
import omero.gateway
import unittest
import test.integration.library as lib


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
        channelList = range(1, 4)
        iId = pixelsService.createImage(sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType, "testCreateImage", description=None)

    def test9655(self):
        # Create an image without statsinfo objects and attempt
        # to retrieve it from the Rendering service.

        # Get the pixels
        image_id = self.testCreateImage()
        gateway = omero.gateway.BlitzGateway(client_obj=self.client)
        image = gateway.getObject("Image", image_id)
        pixels_id = image.getPrimaryPixels().id

        # Save the pixels
        rps = self.client.sf.createRawPixelsStore()
        rps.setPixelsId(pixels_id, False)
        rps.setPlane([0], 0, 0, 0)
        rps.save()
        rps.close()

        # Now use the RE to load
        re = self.client.sf.createRenderingEngine()
        re.lookupPixels(pixels_id)
        re.resetDefaults()
        re.lookupPixels(pixels_id)
        re.lookupRenderingDef(pixels_id)
        re.getPixels()

if __name__ == '__main__':
    unittest.main()
