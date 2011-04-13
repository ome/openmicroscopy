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
        rps.setPixelsId(pix.id.val, True)
        sha1 = hex(rps.calculateMessageDigest())
        self.assertEquals(sha1, pix.sha1.val)

    def testTicket4737WithClose(self):
        pix = self.pix()
        rps = self.client.sf.createRawPixelsStore()
        rps.setPixelsId(pix.id.val, True)
        self.write(pix, rps)
        rps.close() # save is automatic
        self.check_pix(pix)

    def testTicket4737WithSave(self):
        pix = self.pix()
        rps = self.client.sf.createRawPixelsStore()
        rps.setPixelsId(pix.id.val, True)
        self.write(pix, rps)
        pix = rps.save()
        self.check_pix(pix)
        rps.close()
        self.check_pix(pix)

    def testBigPlane(self):
        pix = self.pix(x=4000, y=4000, z=1, t=1, c=1)
        rps = self.client.sf.createRawPixelsStore()
        rps.setPixelsId(pix.id.val, True)
        self.write(pix, rps)
        rps.close()
        self.check_pix(pix)

if __name__ == '__main__':
    unittest.main()
