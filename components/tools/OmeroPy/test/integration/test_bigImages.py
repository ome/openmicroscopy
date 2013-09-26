#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test for rendering engine, particularly rendering a 'region' of big images. 

   PYTHONPATH=$PYTHONPATH:/opt/Ice-3.3.1/python:.:test:build/lib ICE_CONFIG=/Users/will/Desktop/OMERO/etc/ice.config python test/integration/thumbnailPerms.py

"""

import omero
import unittest
import test.integration.library as lib
from omero.rtypes import *

try:
    from PIL import Image, ImageDraw, ImageFont     # see ticket:2597
except: #pragma: nocover
    try:
        import Image, ImageDraw, ImageFont          # see ticket:2597
    except:
        logger.error('No PIL installed')

try:
    import hashlib
    hash_sha1 = hashlib.sha1
except:
    import sha
    hash_sha1 = sha.new

from numpy import asarray

class TestFigureExportScripts(lib.ITest):

    def testRenderRegion(self):
        """
        Test attempts to compare a full image plane, cropped to a region, with a region retrieved from
        rendering engine. 
        Uses PIL to convert compressed strings into 2D numpy arrays for cropping and comparison.
        ** Although cropped images and retrieved regions APPEAR identical, there appear to be rounding
        or rendering errors, either in the 'renderCompressed' method of the rendering engine or in PIL **
        For this reason, there are small differences in the pixel values of rendered regions. 
        This functionality is also tested in Java.
        Therefore this test is not 'Activated' currently. 
        """

        print "testRenderRegion"

        session = self.root.sf
        
        sizeX = 4
        sizeY = 3
        sizeZ = 1
        sizeC = 1
        sizeT = 1
        print "sizeX", sizeX, "sizeY", sizeY
        image = self.createTestImage(sizeX, sizeY, sizeZ, sizeC, sizeT)
        print "Image ID", image.getId().getValue()
        pixelsId = image.getPrimaryPixels().id.val
        
        renderingEngine = session.createRenderingEngine()
        renderingEngine.lookupPixels(pixelsId)
        if not renderingEngine.lookupRenderingDef(pixelsId):
            renderingEngine.resetDefaults() 
        renderingEngine.lookupRenderingDef(pixelsId)
        renderingEngine.load()
        
        # turn all channels on
        for i in range(sizeC): 
            renderingEngine.setActive(i, True)

        regionDef = omero.romio.RegionDef()
        x = 0
        y = 0
        width = 2
        height = 2
        x2 = x+width
        y2 = y+height
        
        regionDef.x = x
        regionDef.y = y
        regionDef.width = width
        regionDef.height = height

        planeDef = omero.romio.PlaneDef()
        planeDef.z = long(0)
        planeDef.t = long(0)

        import StringIO
        
        # First, get the full rendered plane...
        img = renderingEngine.renderCompressed(planeDef)    # compressed String
        fullImage = Image.open(StringIO.StringIO(img))     # convert to numpy array...
        #fullImage.show()
        img_array = asarray(fullImage)   # 3D array, since each pixel is [r,g,b]
        print img_array.shape
        print img_array
        not_cropped = Image.fromarray(img_array)
        not_cropped.show()
        cropped = img_array[y:y2, x:x2, :]      # ... so we can crop to region
        cropped_img = Image.fromarray(cropped)
        cropped_img.show()
        # print the cropped array and hash
        print cropped.shape
        print cropped
        h = hash_sha1()
        h.update(cropped_img.tostring())
        hash = h.hexdigest()
        print hash
        
        # now get the region
        planeDef.region = regionDef
        img = renderingEngine.renderCompressed(planeDef)
        regionImage = Image.open(StringIO.StringIO(img))
        region_array = asarray(regionImage)   # 3D array, since each pixel is [r,g,b]
        # print the region and hash
        print region_array.shape
        print region_array
        regionImage.show()
        h = hash_sha1()
        h.update(regionImage.tostring())
        hash = h.hexdigest()
        print hash
        

if __name__ == '__main__':
    unittest.main()
