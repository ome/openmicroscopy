#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
   Tests for the stateful RawPixelsStore service.

"""

import omero
import pytest
import threading
import test.integration.library as lib

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

    @pytest.mark.long_running
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
                assert False, "Should throw!"
            except omero.MissingPyramidException, mpm:
                assert pix.id.val == mpm.pixelsID

            # Eventually, however, it should be generated
            i = 10
            success = False
            while i > 0 and not success:
                try:
                    rps.setPixelsId(pix.id.val, True)
                    success = True
                except omero.MissingPyramidException, mpm:
                    assert pix.id.val == mpm.pixelsID
                    backOff = mpm.backOff / 1000
                    event = concurrency.get_event("testRomio")
                    event.wait(backOff)  # seconds
                i -= 1
            assert success
        finally:
            rps.close()

    @pytest.mark.long_running
    def testRomioToPyramidWithNegOne(self):
        """
        Here we try the above but pass omero.group:-1
        to see if we can cause an exception.
        """
        all_context = {"omero.group": "-1"}

        from omero.util import concurrency
        pix = self.missing_pyramid(self.root)
        rps = self.root.sf.createRawPixelsStore(all_context)
        try:
            # First execution should certainly fail
            try:
                rps.setPixelsId(pix.id.val, True, all_context)
                assert False, "Should throw!"
            except omero.MissingPyramidException, mpm:
                assert pix.id.val == mpm.pixelsID

            # Eventually, however, it should be generated
            i = 10
            success = False
            while i > 0 and not success:
                try:
                    rps.setPixelsId(pix.id.val, True, all_context)
                    success = True
                except omero.MissingPyramidException, mpm:
                    assert pix.id.val == mpm.pixelsID
                    backOff = mpm.backOff / 1000
                    event = concurrency.get_event("testRomio")
                    event.wait(backOff)  # seconds
                i -= 1
            assert success
        finally:
            rps.close()

    @pytest.mark.long_running
    def testPyramidConcurrentAccess(self):
        """
        See ticket:11709
        """
        all_context = {"omero.group": "-1"}

        from omero.util import concurrency
        pix = self.missing_pyramid(self.root)
        rps = self.root.sf.createRawPixelsStore(all_context)
        try:
            # First execution should certainly fail
            try:
                rps.setPixelsId(pix.id.val, True, all_context)
                assert False, "Should throw!"
            except omero.MissingPyramidException, mpm:
                assert pix.id.val == mpm.pixelsID

            # Eventually, however, it should be generated
            i = 10
            success = False
            while i > 0 and not success:
                try:
                    rps.setPixelsId(pix.id.val, True, all_context)
                    success = True
                except omero.MissingPyramidException, mpm:
                    assert pix.id.val == mpm.pixelsID
                    backOff = mpm.backOff / 1000
                    event = concurrency.get_event("testRomio")
                    event.wait(backOff)  # seconds
                i -= 1
            assert success

            # Once it's generated, we should be able to concurrencly
            # access the file without exceptions
            event = concurrency.get_event("concurrenct_pyramids")
            root_sf = self.root.sf

            class T(threading.Thread):

                def run(self):
                    self.success = 0
                    self.failure = 0
                    while not event.isSet() and self.success < 10:
                        self.rps = root_sf.createRawPixelsStore(all_context)
                        try:
                            self.rps.setPixelsId(pix.id.val, True, all_context)
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
