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

class TestCreateImage (object):

    def createImageAndCheck(self, conn, sizeX=125, sizeY=125, sizeZ=1, sizeC=1, sizeT=1):

        def f(x, y):
            """
            create some fake pixel data tile (2D numpy array)
            """
            return (x * y)/(1 + x + y)

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
        img = conn.createImageFromNumpySeq (planeGen(planeCount), imageName, sizeZ=sizeZ, sizeC=sizeC, sizeT=sizeT)

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
        self.createImageAndCheck(conn, sizeT=10)

    def testCreateBigImages(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        conn = gatewaywrapper.gateway

        # self.createImageAndCheck(conn)
        self.createImageAndCheck(conn, sizeX=4096, sizeY=4096)

