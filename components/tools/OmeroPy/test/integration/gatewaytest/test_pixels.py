#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   gateway tests - Testing the gateway image wrapper.getPrimaryPixels() and
   the pixels wrapper

   Copyright 2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper
   - author_testimg_generated

"""

import omero
import pytest


class TestPixels (object):
    @pytest.fixture(autouse=True)
    def setUp(self, author_testimg):
        self.image = author_testimg

    def testReuseRawPixelsStore(self, gatewaywrapper,
                                author_testimg_generated):
        img1 = self.image
        img2 = author_testimg_generated
        rps = gatewaywrapper.gateway.createRawPixelsStore()
        rps.setPixelsId(img1.getPrimaryPixels().getId(), True,
                        {'omero.group': '-1'})
        assert rps.getByteWidth() > 0
        rps.setPixelsId(img2.getPrimaryPixels().getId(), True,
                        {'omero.group': '-1'})
        assert rps.getByteWidth() > 0

    def testPlaneInfo(self):

        image = self.image
        pixels = image.getPrimaryPixels()
        assert pixels.OMERO_CLASS == 'Pixels'
        assert pixels._obj.__class__ == omero.model.PixelsI
        sizeZ = image.getSizeZ()
        sizeC = image.getSizeC()
        sizeT = image.getSizeT()
        planeInfo = list(pixels.copyPlaneInfo())
        assert len(planeInfo) == sizeZ*sizeC*sizeT

        # filter by 1 or more dimension
        planeInfo = list(pixels.copyPlaneInfo(theC=0))
        for p in planeInfo:
            assert p.theC == 0
        planeInfo = list(pixels.copyPlaneInfo(theZ=1, theT=0))
        for p in planeInfo:
            assert p.theZ == 1
            assert p.theT == 0

    def testPixelsType(self):
        image = self.image
        pixels = image.getPrimaryPixels()

        pixelsType = pixels.getPixelsType()
        assert pixelsType.value == 'int16'
        assert pixelsType.bitSize == 16

    def testGetTile(self, gatewaywrapper):
        image = self.image
        pixels = image.getPrimaryPixels()

        sizeX = image.getSizeX()

        zctTileList = []
        tile = (50, 100, 10, 20)
        for z in range(2):
            for c in range(1):
                for t in range(1):
                    zctTileList.append((z, c, t, tile))

        lastTile = None
        tiles = pixels.getTiles(zctTileList)  # get a tile from every plane
        for tile in tiles:
            tile

        lastT = None
        for zctTile in zctTileList:
            z, c, t, Tile = zctTile
            tile = pixels.getTile(z, c, t, Tile)
        assert lastTile == lastT

        # try stacking tiles together - check it's the same as getting the
        # same region as 1 tile
        z, c, t = 0, 0, 0
        tile1 = pixels.getTile(z, c, t, (0, 0, 5, 3))
        tile2 = pixels.getTile(z, c, t, (5, 0, 5, 3))
        # should be same as tile1 and tile2 combined
        tile3 = pixels.getTile(z, c, t, (0, 0, 10, 3))
        from numpy import hstack
        stacked = hstack((tile1, tile2))
        # bit of a hacked way to compare arrays, but seems to work
        assert str(tile3) == str(stacked)

        # See whether a the first row and a tile of the first row
        # are equal (without using gateway)
        rfs = gatewaywrapper.gateway.createRawPixelsStore()
        try:
            rfs.setPixelsId(pixels.id, False)
            directRow = rfs.getRow(0, 0, 0, 0)
            directTile = rfs.getTile(0, 0, 0, 0, 0, sizeX, 1)
            assert directRow == directTile
        finally:
            rfs.close()

        # See whether a 2x2 tile is the same as the same region of a Plane.
        # See #11315
        testTile = pixels.getTile(0, 0, 0, tile=(0, 0, 2, 2))
        croppedPlane = pixels.getPlane(0, 0, 0)[0:2, 0:2]
        assert str(testTile) == str(croppedPlane), \
            "Tile and croppedPlane not equal"

    def testGetPlane(self):
        image = self.image
        pixels = image.getPrimaryPixels()

        sizeZ = image.getSizeZ()
        sizeC = image.getSizeC()
        sizeT = image.getSizeT()

        zctList = []
        for z in range(sizeZ):
            for c in range(sizeC):
                for t in range(sizeT):
                    zctList.append((z, c, t))

        # timing commented out below - typical times:
        # get 70 planes, using getPlanes() t1 = 3.99837493896 secs, getPlane()
        # t2 = 5.9151828289 secs. t1/t2 = 0.7
        # get 210 planes, using getPlanes() t1 = 12.3150248528 secs,
        # getPlane() t2 = 17.2735779285 secs t1/t2 = 0.7

        # test getPlanes()
        # import time
        # startTime = time.time()
        planes = pixels.getPlanes(zctList)  # get all planes
        for plane in planes:
            plane
        # t1 = time.time() - startTime
        # print "Getplanes = %s secs" % t1

        # test getPlane() which returns a single plane
        # startTime = time.time()
        for zct in zctList:
            z, c, t = zct
            pixels.getPlane(z, c, t)
        # t2 = time.time() - startTime
        # print "Get individual planes = %s secs" % t2
        # print "t1/t2", t1/t2

        pixels.getPlane(sizeZ-1, sizeC-1, sizeT-1)
        plane = pixels.getPlane()   # default is (0,0,0)
        firstPlane = pixels.getPlane(0, 0, 0)
        assert plane[0][0] == firstPlane[0][0]

    def testGetPlanesExceptionOnGetPlane(self):
        """
        Tests exception handling in the gateway.getPlanes generator.

        See #5156
        """
        image = self.image
        pixels = image.getPrimaryPixels()

        # Replace service creation with a mock
        pixels._prepareRawPixelsStore = lambda: MockRawPixelsStore(pixels)

        # Now, when we call, the first yield should succeed, the second should
        # fail
        found = 0
        try:
            for x in pixels.getPlanes(((0, 0, 0), (1, 1, 1))):
                found += 1
            raise AssertionError("Should throw")
        except AssertionError:
            raise
        except Exception as e:
            assert not e.close
            assert found == 1

    def testGetPlanesExceptionOnClose(self):
        """
        Tests exception handling in the gateway.getPlanes generator.

        See #5156
        """
        image = self.image
        pixels = image.getPrimaryPixels()

        # Replace service creation with a mock
        pixels._prepareRawPixelsStore = lambda: MockRawPixelsStore(
            pixels, good_calls=2, close_fails=True)

        # Now, when we call, the first yield should succeed, the second should
        # fail
        found = 0
        try:
            for x in pixels.getPlanes(((0, 0, 0), (1, 1, 1))):
                found += 1
            raise AssertionError("Should have failed on close")
        except AssertionError:
            raise
        except Exception as e:
            assert e.close
            assert found == 2

    def testGetPlanesExceptionOnBoth(self):
        """
        Tests exception handling in the gateway.getPlanes generator.

        In this test, both the getPlane and the close throw an exception.
        The exception from the getPlane method should be thrown, and the close
        logged (not tested here)

        See #5156
        """
        image = self.image
        pixels = image.getPrimaryPixels()

        # Replace service creation with a mock
        pixels._prepareRawPixelsStore = lambda: MockRawPixelsStore(
            pixels, good_calls=1, close_fails=True)

        # Now, when we call, the first yield should succeed, the second should
        # fail
        found = 0
        try:
            for x in pixels.getPlanes(((0, 0, 0), (1, 1, 1))):
                found += 1
            raise AssertionError("Should have failed on getPlane and close")
        except AssertionError:
            raise
        except Exception as e:
            assert not e.close
            assert found == 1

    def testGetHistogram(self, gatewaywrapper):
        """
        Tests we get data of the right size and close rawPixelsStore
        """
        image = self.image

        # Should be 0 services already open, but don't enforce it...
        current_services = gatewaywrapper.gateway.c.getStatefulServices()
        current_count = len(current_services)

        channels = [0, 1]
        binSize = 100
        theZ = 0
        theT = 0
        histogram = image.getHistogram(channels, binSize, theZ=theZ, theT=theT)
        assert histogram.keys() == channels
        assert len(histogram[0]) == binSize

        # ...as long as we haven't left additional services open
        services = gatewaywrapper.gateway.c.getStatefulServices()
        assert len(services) == current_count


class MockRawPixelsStore(object):

    """
    Mock which throws exceptions at given times.
    """

    def __init__(self, pixels, good_calls=1, close_fails=False):
        self.pixels = pixels
        self.good_calls = good_calls
        self.close_fails = close_fails

    def getPlane(self, *args):
        if self.good_calls == 0:
            e = Exception("MOCK EXCEPTION")
            e.close = False
            raise e
        else:
            self.good_calls -= 1
            return "0"*(2*self.pixels.getSizeX()*self.pixels.getSizeY())

    def close(self, *args):
        if self.close_fails:
            e = Exception("MOCK CLOSE EXCEPTION")
            e.close = True
            raise e
