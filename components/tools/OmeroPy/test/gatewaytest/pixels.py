#!/usr/bin/env python

"""
   gateway tests - Testing the gateway image wrapper.getPrimaryPixels() and the pixels wrapper

"""

import unittest
import omero
import time

import gatewaytest.library as lib


class PixelsTest (lib.GTest):

    def setUp (self):
        super(PixelsTest, self).setUp()
        self.loginAsAuthor()
        self.TESTIMG = self.getTestImage()

    def testPlaneInfo(self):

        image = self.TESTIMG
        pixels = image.getPrimaryPixels()
        self.assertEqual(pixels.OMERO_CLASS, 'Pixels')
        self.assertEqual(pixels._obj.__class__, omero.model.PixelsI)
        sizeZ = image.z_count()
        sizeC = image.c_count()
        sizeT = image.t_count()
        planeInfo = list(pixels.copyPlaneInfo())
        self.assertEqual(len(planeInfo), sizeZ*sizeC*sizeT)

        # filter by 1 or more dimension
        planeInfo = list(pixels.copyPlaneInfo(theC=0))
        for p in planeInfo:
            self.assertEqual(p.theC, 0)
        planeInfo = list(pixels.copyPlaneInfo(theZ=1, theT=0))
        for p in planeInfo:
            self.assertEqual(p.theZ, 1)
            self.assertEqual(p.theT, 0)

    def testPixelsType(self):
        image = self.TESTIMG
        pixels = image.getPrimaryPixels()

        pixelsType = pixels.getPixelsType()
        self.assertEqual(pixelsType.value, 'int16')
        self.assertEqual(pixelsType.bitSize, 16)

    def testGetPlane(self):
        image = self.TESTIMG
        pixels = image.getPrimaryPixels()

        sizeZ = image.z_count()
        sizeC = image.c_count()
        sizeT = image.t_count()

        # get 70 planes, new RawPixelsStore created and closed each plane = 5.9151828289 secs
        # get 70 planes, one RawPixelsStore created and closed = 3.99837493896 secs

        # test getPlanes()
        planes = pixels.getPlanes(zStop=sizeZ, cStop=sizeC, tStop=sizeT)  # get all planes
        planeList = list(planes)
        self.assertEqual(len(planeList), sizeZ*sizeC*sizeT)

        planeList = list(pixels.getPlanes())
        self.assertEqual(len(planeList), 1)

        # test getPlane() which returns a single plane
        lastPlane = pixels.getPlane(sizeZ-1, sizeC-1, sizeT-1)
        plane = pixels.getPlane()   # default is (0,0,0)
        firstPlane = pixels.getPlane(0,0,0)
        self.assertEqual(plane[0][0], firstPlane[0][0])

if __name__ == '__main__':
    unittest.main()
