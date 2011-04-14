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


if __name__ == '__main__':
    unittest.main()
