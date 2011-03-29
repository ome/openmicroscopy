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

    def pix(self):
        image = self.new_image()
        pixels = omero.model.PixelsI()
        pixels.sizeX = rint(10)
        pixels.sizeY = rint(10)
        pixels.sizeZ = rint(10)
        pixels.sizeC = rint(3)
        pixels.sizeT = rint(50)
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
        bytes_per_plane = pix.sizeX.val * pix.sizeY.val # Assuming int8
        for z in range(pix.sizeZ.val):
            for c in range(pix.sizeC.val):
                for t in range(pix.sizeT.val):
                    rps.setPlane([5]*bytes_per_plane, z, c, t)

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

if __name__ == '__main__':
    unittest.main()
