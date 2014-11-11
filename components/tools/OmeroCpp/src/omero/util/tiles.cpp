/*
 * OMERO Tile Utilities
 *
 * Copyright 2011 Glencoe Software, Inc.  All Rights Reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */

#include <omero/util/tiles.h>
#include <omero/ClientErrors.h>
#include <omero/IceNoWarnPush.h>
#include <omero/api/IPixels.h>
#include <omero/IceNoWarnPop.h>

namespace omero {
    namespace util {

        //
        // TileLoopIteration
        //

        TileLoopIteration::TileLoopIteration() {
        }

        TileLoopIteration::~TileLoopIteration() {
        }

        //
        // TileData
        //

        TileData::TileData() {
        }

        TileData::~TileData() {
        }

        //
        // TileLoop
        //

        TileLoop::TileLoop() {
        }

        TileLoop::~TileLoop() {
        }

        int TileLoop::forEachTile(int sizeX, int sizeY, int sizeZ, int sizeT, int sizeC,
                                  int tileHeight, int tileWidth, const TileLoopIterationPtr& iteration) {

            TileDataPtr data = createData();
            int x, y, w, h;
            int tileCount = 0;
            for (int t = 0; t < sizeT; t++)
            {
                for (int c = 0; c < sizeC; c++)
                {
                    for (int z = 0; z < sizeZ; z++)
                    {
                        for (int tileOffsetY = 0;
                            tileOffsetY < (sizeY + tileHeight - 1) / tileHeight;
                            tileOffsetY++)
                        {
                            for (int tileOffsetX = 0;
                                tileOffsetX < (sizeX + tileWidth - 1) / tileWidth;
                                tileOffsetX++)
                            {
                                x = tileOffsetX * tileWidth;
                                y = tileOffsetY * tileHeight;
                                w = tileWidth;
                                if (w + x > sizeX)
                                {
                                    w = sizeX - x;
                                }
                                h = tileHeight;
                                if (h + y > sizeY)
                                {
                                    h = sizeY - y;
                                }
                                iteration->run(data, z, c, t, x, y, w, h, tileCount);
                                tileCount++;
                            }
                        }
                    }
                }

            }
            return tileCount;
        }

        //
        // RPSTileData
        //

        RPSTileData::RPSTileData(const RPSTileLoopPtr& loop,
                                 const omero::api::RawPixelsStorePrx& rps) : TileData(), loop(loop), rps(rps) {
        }

        RPSTileData::~RPSTileData() {
        }

        Ice::ByteSeq RPSTileData::getTile(int z, int c, int t, int x, int y, int w, int h) {
            return rps->getTile(z, c, t, x, y, w, h);
        }

        void RPSTileData::setTile(const Ice::ByteSeq& buffer, int z, int c, int t, int x, int y, int w, int h) {
            rps->setTile(buffer, z, c, t, x, y, w, h);
        }

        void RPSTileData::close() {
            omero::model::PixelsPtr pixels = rps->save();
            loop->setPixels(pixels);
            rps->close(); // TODO: this should be a wrapper which calls close
        }

        //
        // RPSTileLoop
        //

        RPSTileLoop::RPSTileLoop(const omero::api::ServiceFactoryPrx& session,
                                 const omero::model::PixelsPtr& pixels) : TileLoop(), session(session), pixels(pixels) {

            if (!this->pixels || !this->pixels->getId()) {
                throw omero::ClientError(__FILE__, __LINE__, "pixels instance must be managed!");
            }

        }

        RPSTileLoop::~RPSTileLoop() {
        }

        omero::model::PixelsPtr RPSTileLoop::getPixels() {
            return this->pixels;
        }

        void RPSTileLoop::setPixels(const omero::model::PixelsPtr& pixels) {
            this->pixels = pixels;
        }

        TileDataPtr RPSTileLoop::createData() {
            omero::api::RawPixelsStorePrx rps = getSession()->createRawPixelsStore();
            rps->setPixelsId(getPixels()->getId()->getValue(), false); // 'false' is ignored here.
            return new RPSTileData(this, rps);
        }

        omero::api::ServiceFactoryPrx RPSTileLoop::getSession() {
            return session;
        }

        int RPSTileLoop::forEachTile(int tileHeight, int tileWidth, const TileLoopIterationPtr& iteration) {

            if (!pixels->isLoaded()) {
                pixels = getSession()->getPixelsService()->retrievePixDescription(pixels->getId()->getValue());
            }

            int sizeX = pixels->getSizeX()->getValue();
            int sizeY = pixels->getSizeY()->getValue();
            int sizeZ = pixels->getSizeZ()->getValue();
            int sizeC = pixels->getSizeC()->getValue();
            int sizeT = pixels->getSizeT()->getValue();

            return TileLoop::forEachTile(sizeX, sizeY, sizeZ, sizeT, sizeC, tileWidth, tileHeight, iteration);
        }

    }
}

