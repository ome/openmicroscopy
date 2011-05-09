#!/usr/bin/env python

"""
   Tests for the stateful RawPixelsStore service.

   Copyright 2011 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero
import unittest
import integration.library as lib

from omero.rtypes import rstring, rlong, rint
from omero.util.concurrency import get_event
from binascii import hexlify as hex


class TestRPS(lib.ITest):

    def pix(self, x=10, y=10, z=10, c=3, t=50):
        image = self.new_image()
        pixels = omero.model.PixelsI()
        pixels.sizeX = rint(x)
        pixels.sizeY = rint(y)
        pixels.sizeZ = rint(z)
        pixels.sizeC = rint(c)
        pixels.sizeT = rint(t)
        pixels.sha1 = rstring("")
        pixels.pixelsType = omero.model.PixelsTypeI()
        pixels.pixelsType.value = rstring("int8")
        pixels.dimensionOrder = omero.model.DimensionOrderI()
        pixels.dimensionOrder.value = rstring("XYZCT")
        image.addPixels(pixels)
        image = self.update.saveAndReturnObject(image)
        pixels = image.getPrimaryPixels()
        return pixels

    def write(self, pix, rps):
        if not rps.hasPixelsPyramid():
            # By plane
            bytes_per_plane = pix.sizeX.val * pix.sizeY.val # Assuming int8
            for z in range(pix.sizeZ.val):
                for c in range(pix.sizeC.val):
                    for t in range(pix.sizeT.val):
                        rps.setPlane([5]*bytes_per_plane, z, c, t)
        else:
            # By tile
            w, h = rps.getTileSize()
            bytes_per_tile = w * h # Assuming int8
            for z in range(pix.sizeZ.val):
                for c in range(pix.sizeC.val):
                    for t in range(pix.sizeT.val):
                        for x in range(0, pix.sizeX.val, w):
                            for y in range(0, pix.sizeY.val, h):

                                changed = False
                                if x+w > pix.sizeX.val:
                                    w = pix.sizeX.val - x
                                    changed = True
                                if y+h > pix.sizeY.val:
                                    h = pix.sizeY.val - y
                                    changed = True
                                if changed:
                                    bytes_per_tile = w * h # Again assuming int8

                                args = ([5]*bytes_per_tile, z, c, t, x, y, w, h)
                                rps.setTile(*args)

    def check_pix(self, pix):
        pix = self.query.get("Pixels", pix.id.val)
        self.assert_(pix.sha1.val != "")
        rps = self.client.sf.createRawPixelsStore()
        try:
            rps.setPixelsId(pix.id.val, True)
            sha1 = hex(rps.calculateMessageDigest())
            self.assertEquals(sha1, pix.sha1.val)
        finally:
            rps.close()

    def testTicket4737WithClose(self):
        pix = self.pix()
        rps = self.client.sf.createRawPixelsStore()
        try:
            rps.setPixelsId(pix.id.val, True)
            self.write(pix, rps)
        finally:
            rps.close() # save is automatic
        self.check_pix(pix)

    def testTicket4737WithSave(self):
        pix = self.pix()
        rps = self.client.sf.createRawPixelsStore()
        try:
            rps.setPixelsId(pix.id.val, True)
            self.write(pix, rps)
            pix = rps.save()
            self.check_pix(pix)
        finally:
            rps.close()
        self.check_pix(pix)

    def testBigPlane(self):
        pix = self.pix(x=4000, y=4000, z=1, t=1, c=1)
        rps = self.client.sf.createRawPixelsStore()
        try:
            rps.setPixelsId(pix.id.val, True)
            self.write(pix, rps)
        finally:
            rps.close()
        self.check_pix(pix)

    def testRomioToPyramid(self):
        """
        Here we create a pixels that is not big,
        then modify its metadata so that it IS big,
        in order to trick the service into throwing
        us a MissingPyramidException
        """
        pix = self.pix(x=1, y=1, z=4000, t=4000, c=1)
        rps = self.client.sf.createRawPixelsStore()
        print pix.id.val
        try:
            rps.setPixelsId(pix.id.val, True)
            for t in range(4000):
                rps.setTimepoint([5]*4000, t) # Assuming int8
            pix = rps.save()
        finally:
            rps.close()

        pix.sizeX = omero.rtypes.rint(4000)
        pix.sizeY = omero.rtypes.rint(4000)
        pix.sizeZ = omero.rtypes.rint(1)
        pix.sizeT = omero.rtypes.rint(1)
        pix = self.update.saveAndReturnObject(pix)

        rps = self.client.sf.createRawPixelsStore()
        try:
            # First execution should certainly fail
            try:
                rps.setPixelsId(pix.id.val, True)
                fail("Should throw!")
            except omero.MissingPyramidException, mpm:
                self.assertEquals(pix.id.val, mpm.pixelsID)

            # Eventually, however, it should be generated
            i = 10
            success = False
            while i > 0 and not success:
                try:
                    rps.setPixelsId(pix.id.val, True)
                    success = True
                except omero.MissingPyramidException, mpm:
                    self.assertEquals(pix.id.val, mpm.pixelsID)
            self.assert_(success)
        finally:
            rps.close()

if __name__ == '__main__':
    unittest.main()
