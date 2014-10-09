#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (c) 2014 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

from numpy import fromfunction, int8
import pytest


class TestCreateImage (object):

    @pytest.fixture(autouse=True)
    def setUp(self, author_testimg):
        self.image = author_testimg
        assert self.image is not None, 'No test image found on database'

    def createTileImageAndCheck(self, conn, sizeX=4096, sizeY=4096,
                                sizeZ=1, sizeC=1, sizeT=1,
                                tileWidth=256, tileHeight=256):
        """
        Create a large tiled image by stitching a region of a small image.
        """

        def planeFromImageGen(tileSeq):
            tlist = [(0, t['c'], 0, (0, 0, t['w'], t['h'])) for t in tileSeq]
            p = self.image.getPrimaryPixels()
            planes = p.getTiles(tlist)
            for tile in planes:
                yield tile

        ts = conn.getTileSequence(sizeX, sizeY, sizeZ, sizeC, sizeT,
                                  tileWidth, tileHeight)

        imageName = "gatewaytest.test_create_tiled_image"
        img = conn.createImageFromTileSeq(planeFromImageGen(ts), imageName,
                                          sizeX, sizeY, sizeZ, sizeC, sizeT,
                                          tileWidth, tileHeight)

        assert img is not None
        assert img.getSizeX() == sizeX
        assert img.getSizeY() == sizeY
        assert img.getSizeZ() == sizeZ
        assert img.getSizeC() == sizeC
        assert img.getSizeT() == sizeT

    def testCreateTiledImages(self, gatewaywrapper):

        gatewaywrapper.loginAsAuthor()
        conn = gatewaywrapper.gateway
        self.createTileImageAndCheck(conn, sizeC=2)
        # truncated tiles right and bottom
        self.createTileImageAndCheck(conn, sizeX=4000, sizeY=3500)

    def createImageAndCheck(self, conn, sizeX=125, sizeY=125,
                            sizeZ=1, sizeC=1, sizeT=1):

        def f(x, y):
            """
            create some fake pixel data tile (2D numpy array)
            """
            return (x * y)/(1+abs((x + y) * (x + y)))

        def planeGen(count):
            tile_max = 255
            dtype = int8
            for p in range(count):
                # perform some manipulation on each plane
                plane = fromfunction(f, (sizeX, sizeY), dtype=dtype)
                # plane = plane.astype(int)
                plane[plane > tile_max] = tile_max
                plane[plane < 0] = 0
                yield plane

        imageName = "gatewaytest.test_create_image"
        planeCount = sizeC * sizeZ * sizeT
        img = conn.createImageFromNumpySeq(
            planeGen(planeCount), imageName,
            sizeZ=sizeZ, sizeC=sizeC, sizeT=sizeT)

        assert img is not None
        assert img.getSizeX() == sizeX
        assert img.getSizeY() == sizeY
        assert img.getSizeZ() == sizeZ
        assert img.getSizeC() == sizeC
        assert img.getSizeT() == sizeT

    def testCreateImages(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        conn = gatewaywrapper.gateway

        self.createImageAndCheck(conn)
        self.createImageAndCheck(conn, sizeZ=10)
        self.createImageAndCheck(conn, sizeC=3)
        self.createImageAndCheck(conn, sizeT=10)
        self.createImageAndCheck(conn, sizeT=2, sizeZ=3)

    def testCreateBigImages(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        conn = gatewaywrapper.gateway

        self.createImageAndCheck(conn, sizeX=4096, sizeY=4096)
