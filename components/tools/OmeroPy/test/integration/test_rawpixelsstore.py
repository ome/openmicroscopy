#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Tests for the stateful RawPixelsStore service.

   Copyright 2011 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero
import unittest
import test.integration.library as lib

from omero.rtypes import rstring, rlong, rint
from omero.util.concurrency import get_event
from omero.util.tiles import *
from binascii import hexlify as hex


class TestRPS(lib.ITest):

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

    def testTicket4737WithForEachTile(self):
        pix = self.pix()
        class Iteration(TileLoopIteration):
            def run(self, data, z, c, t, x, y, tileWidth, tileHeight, tileCount):
                data.setTile([5]*tileWidth*tileHeight, z, c, t, x, y, tileWidth, tileHeight)

        loop = RPSTileLoop(self.client.getSession(), pix)
        loop.forEachTile(256, 256, Iteration())
        pix = self.query.get("Pixels", pix.id.val)
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
        from omero.util import concurrency
        pix = self.missing_pyramid(self.root)
        rps = self.root.sf.createRawPixelsStore()
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
                    backOff = mpm.backOff/1000
                    event = concurrency.get_event("testRomio")
                    event.wait(backOff) # seconds
                i -=1
            self.assert_(success)
        finally:
            rps.close()

    def testRomioToPyramidWithNegOne(self):
        """
        Here we try the above but pass omero.group:-1
        to see if we can cause an exception.
        """
        all_context = {"omero.group":"-1"}

        from omero.util import concurrency
        pix = self.missing_pyramid(self.root)
        rps = self.root.sf.createRawPixelsStore(all_context)
        try:
            # First execution should certainly fail
            try:
                rps.setPixelsId(pix.id.val, True, all_context)
                fail("Should throw!")
            except omero.MissingPyramidException, mpm:
                self.assertEquals(pix.id.val, mpm.pixelsID)

            # Eventually, however, it should be generated
            i = 10
            success = False
            while i > 0 and not success:
                try:
                    rps.setPixelsId(pix.id.val, True, all_context)
                    success = True
                except omero.MissingPyramidException, mpm:
                    self.assertEquals(pix.id.val, mpm.pixelsID)
                    backOff = mpm.backOff/1000
                    event = concurrency.get_event("testRomio")
                    event.wait(backOff) # seconds
                i -=1
            self.assert_(success)
        finally:
            rps.close()

if __name__ == '__main__':
    unittest.main()
