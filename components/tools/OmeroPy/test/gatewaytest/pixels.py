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
        sizeZ = image.getSizeZ()
        sizeC = image.getSizeC()
        sizeT = image.getSizeT()
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

        sizeZ = image.getSizeZ()
        sizeC = image.getSizeC()
        sizeT = image.getSizeT()

        zctList = []
        for z in range(sizeZ):
            for c in range(sizeC):
                for t in range(sizeT):
                    zctList.append((z,c,t))

        # timing commented out below - typical times:
        # get 70 planes, using getPlanes() t1 = 3.99837493896 secs, getPlane() t2 = 5.9151828289 secs. t1/t2 = 0.7
        # get 210 planes, using getPlanes() t1 = 12.3150248528 secs, getPlane() t2 = 17.2735779285 secs t1/t2 = 0.7

        # test getPlanes()
        #import time
        #startTime = time.time()
        planes = pixels.getPlanes(zctList)  # get all planes
        for plane in planes:
            p = plane
        #t1 = time.time() - startTime
        #print "Getplanes = %s secs" % t1

        # test getPlane() which returns a single plane
        #startTime = time.time()
        for zct in zctList:
            z,c,t = zct
            p = pixels.getPlane(z,c,t)
        #t2 = time.time() - startTime
        #print "Get individual planes = %s secs" % t2
        #print "t1/t2", t1/t2

        lastPlane = pixels.getPlane(sizeZ-1, sizeC-1, sizeT-1)
        plane = pixels.getPlane()   # default is (0,0,0)
        firstPlane = pixels.getPlane(0,0,0)
        self.assertEqual(plane[0][0], firstPlane[0][0])

if __name__ == '__main__':
    unittest.main()
