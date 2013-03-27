#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""

   Copyright 2011 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""


class TileLoopIteration(object):
    """
    "Interface" which must be passed to forEachTile
    """
    def run(self, rps, z, c, t, x, y, tileWidth, tileHeight, tileCount):
        raise NotImplemented()


class TileData(object):
    """
    "Interface" which must be returned by concrete TileLoop
    implementations.
    """

    def getTile(self, z, c, t, x, y, w, h):
        """
        z: int
        c: int
        t: int
        y: int
        w: int
        h: int
        return: byte[]
        """
        raise NotImplementedError()

    def setTile(self, buffer, z, c, t, x, y, w, h):
        """
        buffer: byte[]
        z: int
        c: int
        t: int
        y: int
        w: int
        h: int
        """
        raise NotImplementedError();

    def close(self):
        raise NotImplementedError();


class TileLoop(object):

    def createData(self):
        """
        Subclasses must provide a fresh instance of {@link TileData}.
        The instance will be closed after the run of forEachTile.

        returns: TileData
        """
        raise NotImplementedError()

    def forEachTile(self, sizeX, sizeY, sizeZ, sizeC, sizeT,\
                           tileWidth, tileHeight, iteration):
        """
        Iterates over every tile in a given pixel based on the
        over arching dimensions and a requested maximum tile width and height.
        @param iteration Invoker to call for each tile.
        @param pixel Pixel instance
        @param tileWidth <b>Maximum</b> width of the tile requested. The tile
        request itself will be smaller than the original tile width requested if
        <code>x + tileWidth > sizeX</code>.
        @param tileHeight <b>Maximum</b> height of the tile requested. The tile
        request itself will be smaller if <code>y + tileHeight > sizeY</code>.
        @return The total number of tiles iterated over.
        """

        data = self.createData()

        try:
            tileCount = 0
            for t in range(0, sizeT):

                for c in range(0, sizeC):

                    for z in range(0, sizeZ):

                        for tileOffsetY in range(0, ((sizeY + tileHeight - 1) / tileHeight)):

                            for tileOffsetX in range(0, ((sizeX + tileWidth - 1) / tileWidth)):

                                x = tileOffsetX * tileWidth
                                y = tileOffsetY * tileHeight
                                w = tileWidth

                                if (w + x > sizeX):
                                    w = sizeX - x;

                                h = tileHeight
                                if (h + y > sizeY):
                                    h = sizeY - y

                                iteration.run(data, z, c, t, x, y, w, h, tileCount)
                                tileCount += 1
            return tileCount;

        finally:
            data.close()


class RPSTileData(TileData):
    """
    """
    def __init__(self, loop, rps):
        self.loop = loop
        self.rps = rps
        self.pixels = None

    def getTile(self, z, c, t, x, y, w, h):
        return self.rps.getTile(z, c, t, x, y, w, h)

    def setTile(self, buffer, z, c, t, x, y, w, h):
        self.rps.setTile(buffer, z, c, t, x, y, w, h)

    def close(self):
        pixels = self.rps.save()
        self.loop.setPixels(pixels)
        self.rps.close()


class RPSTileLoop(TileLoop):

    def __init__(self, session, pixels):
        self.session = session
        self.pixels = pixels

    def getSession(self):
        return self.session

    def getPixels(self):
        """
        After saving the binary data, the update event of the
        {@link Pixels} instance will be updated and therefore
        need to be reloaded. As a convenience the returned
        value is accessible here.
        """
        return self.pixels

    def setPixels(self, pixels):
        """
        Used by RPSTileData to set a reloaded Pixels instance
        for client use.
        """
        self.pixels = pixels

    def createData(self):
        rps = self.getSession().createRawPixelsStore()
        data = RPSTileData(self, rps)
        rps.setPixelsId(self.getPixels().getId().getValue(), False) # 'false' is ignored here.
        return data

    def forEachTile(self, tileWidth, tileHeight, iteration):
        """
        Iterates over every tile in a given pixel based on the
        over arching dimensions and a requested maximum tile width and height.
        @param iteration Invoker to call for each tile.
        @param pixel Pixel instance
        @param tileWidth <b>Maximum</b> width of the tile requested. The tile
        request itself will be smaller than the original tile width requested if
        <code>x + tileWidth > sizeX</code>.
        @param tileHeight <b>Maximum</b> height of the tile requested. The tile
        request itself will be smaller if <code>y + tileHeight > sizeY</code>.
        @return The total number of tiles iterated over.
        """

        if self.pixels is None or self.pixels.id is None:
            raise omero.ClientError("pixels instance must be managed!")
        elif not self.pixels.loaded:
            try:
                self.pixels = getSession().getPixelsService().retrievePixDescription(self.pixels.id.val)
            except:
                raise omero.ClientError("Failed to load %s\n%s" % (self.pixels.id.val, e))

        sizeX = self.pixels.getSizeX().getValue()
        sizeY = self.pixels.getSizeY().getValue()
        sizeZ = self.pixels.getSizeZ().getValue()
        sizeC = self.pixels.getSizeC().getValue()
        sizeT = self.pixels.getSizeT().getValue()

        return TileLoop.forEachTile(self, sizeX, sizeY, sizeZ, sizeC, sizeT, tileWidth, tileHeight, iteration);
