#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Tests for the stateful RawPixelsStore service.

   Copyright 2011-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero
import threading
import library as lib
import pytest
__import__("sys")

from omero.util.tiles import TileLoopIteration
from omero.util.tiles import RPSTileLoop
from binascii import hexlify as hex


class TestRPS(lib.ITest):

    def check_pix(self, pix):
        pix = self.query.get("Pixels", pix.id.val)
        assert pix.sha1.val != ""
        rps = self.client.sf.createRawPixelsStore()
        try:
            rps.setPixelsId(pix.id.val, True)
            sha1 = hex(rps.calculateMessageDigest())
            assert sha1 == pix.sha1.val
        finally:
            rps.close()

    def testTicket4737WithClose(self):
        pix = self.pix()
        rps = self.client.sf.createRawPixelsStore()
        try:
            rps.setPixelsId(pix.id.val, True)
            self.write(pix, rps)
        finally:
            rps.close()  # save is automatic
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

            def run(self, data, z, c, t, x, y,
                    tileWidth, tileHeight, tileCount):
                data.setTile(
                    [5] * tileWidth * tileHeight,
                    z, c, t, x, y, tileWidth, tileHeight)

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

    def testRomioToPyramid(self, tmpdir):
        """
        Here we create a pixels that is not big,
        then modify its metadata so that it IS big,
        in order to trick the service into throwing
        us a MissingPyramidException
        """
        from omero.util import concurrency
        pix = self.missing_pyramid()
        rps = self.sf.createRawPixelsStore()
        try:
            # First execution should certainly fail
            try:
                rps.setPixelsId(long(pix), True)
                assert False, "Should throw!"
            except omero.MissingPyramidException, mpm:
                assert long(pix) == mpm.pixelsID

            # Eventually, however, it should be generated
            i = 10
            success = False
            while i > 0 and not success:
                try:
                    rps.setPixelsId(long(pix), True)
                    success = True
                except omero.MissingPyramidException, mpm:
                    assert long(pix) == mpm.pixelsID
                    backOff = mpm.backOff / 1000
                    event = concurrency.get_event("testRomio")
                    event.wait(backOff)  # seconds
                i -= 1
            assert success
        finally:
            rps.close()

    def test2RomioToPyramidWithNegOne(self, tmpdir):
        """
        Here we try the above but pass omero.group:-1
        to see if we can cause an exception.
        """
        all_context = {"omero.group": "-1"}

        from omero.util import concurrency
        pix = self.missing_pyramid()
        rps = self.sf.createRawPixelsStore(all_context)
        try:
            # First execution should certainly fail
            try:
                rps.setPixelsId(long(pix), True, all_context)
                assert False, "Should throw!"
            except omero.MissingPyramidException, mpm:
                assert long(pix) == mpm.pixelsID

            # Eventually, however, it should be generated
            i = 10
            success = False
            while i > 0 and not success:
                try:
                    rps.setPixelsId(long(pix), True, all_context)
                    success = True
                except omero.MissingPyramidException, mpm:
                    assert long(pix) == mpm.pixelsID
                    backOff = mpm.backOff / 1000
                    event = concurrency.get_event("testRomio")
                    event.wait(backOff)  # seconds
                i -= 1
            assert success
        finally:
            rps.close()

    def testPyramidConcurrentAccess(self, tmpdir):
        """
        See ticket:11709
        """
        all_context = {"omero.group": "-1"}

        from omero.util import concurrency
        pix = self.missing_pyramid()
        rps = self.sf.createRawPixelsStore(all_context)
        try:
            # First execution should certainly fail
            try:
                rps.setPixelsId(long(pix), True, all_context)
                assert False, "Should throw!"
            except omero.MissingPyramidException, mpm:
                assert long(pix) == mpm.pixelsID

            # Eventually, however, it should be generated
            i = 10
            success = False
            while i > 0 and not success:
                try:
                    rps.setPixelsId(long(pix), True, all_context)
                    success = True
                except omero.MissingPyramidException, mpm:
                    assert long(pix) == mpm.pixelsID
                    backOff = mpm.backOff / 1000
                    event = concurrency.get_event("testRomio")
                    event.wait(backOff)  # seconds
                i -= 1
            assert success

            # Once it's generated, we should be able to concurrencly
            # access the file without exceptions
            event = concurrency.get_event("concurrenct_pyramids")
            sf = self.sf

            class T(threading.Thread):

                def run(self):
                    self.success = 0
                    self.failure = 0
                    while not event.isSet() and self.success < 10:
                        self.rps = sf.createRawPixelsStore(all_context)
                        try:
                            self.rps.setPixelsId(long(pix), True, all_context)
                            self.success += 1
                        except:
                            self.failure += 1
                            raise
                        finally:
                            self.rps.close()

            threads = [T() for x in range(10)]
            for t in threads:
                t.start()
            event.wait(10)  # 10 seconds
            event.set()
            for t in threads:
                t.join()

            total_successes = sum([t.success for t in threads])
            total_failures = sum([t.failure for t in threads])
            assert total_successes
            assert not total_failures

        finally:
            rps.close()


class TestTiles(lib.ITest):

    @pytest.mark.skipif("sys.version_info < (2,7)",
                        reason="This fails with Python < 2.7 and Ice >= 3.5")
    def testTiles(self):
        from omero.model import PixelsI
        from omero.sys import ParametersI
        from omero.util.tiles import RPSTileLoop
        from omero.util.tiles import TileLoopIteration
        from numpy import fromfunction

        sizeX = 4096
        sizeY = 4096
        sizeZ = 1
        sizeC = 1
        sizeT = 1
        tileWidth = 1024
        tileHeight = 1024
        imageName = "testStitchBig4K-1Ktiles"
        description = None
        tile_max = float(255)

        pixelsService = self.client.sf.getPixelsService()
        queryService = self.client.sf.getQueryService()

        query = "from PixelsType as p where p.value='int8'"
        pixelsType = queryService.findByQuery(query, None)
        channelList = range(sizeC)
        iId = pixelsService.createImage(
            sizeX, sizeY, sizeZ, sizeT,
            channelList, pixelsType, imageName, description)

        image = queryService.findByQuery(
            "select i from Image i join fetch i.pixels where i.id = :id",
            ParametersI().addId(iId))
        pid = image.getPrimaryPixels().getId().getValue()

        def f(x, y):
            """
            create some fake pixel data tile (2D numpy array)
            """
            return (x * y) / (1 + x + y)

        def mktile(w, h):
            tile = fromfunction(f, (w, h))
            tile = tile.astype(int)
            tile[tile > tile_max] = tile_max
            return list(tile.flatten())

        tile = fromfunction(f, (tileWidth, tileHeight)).astype(int)
        tile_min = float(tile.min())
        tile_max = min(tile_max, float(tile.max()))

        class Iteration(TileLoopIteration):

            def run(self, data, z, c, t, x, y,
                    tileWidth, tileHeight, tileCount):
                tile2d = mktile(tileWidth, tileHeight)
                data.setTile(tile2d, z, c, t, x, y, tileWidth, tileHeight)

        loop = RPSTileLoop(self.client.sf, PixelsI(pid, False))
        loop.forEachTile(256, 256, Iteration())

        c = 0
        pixelsService.setChannelGlobalMinMax(
            pid, c, tile_min, tile_max)
