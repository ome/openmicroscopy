/*
 * OMERO Tile Loop
 *
 * Copyright 2011 Glencoe Software, Inc.  All Rights Reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */

#ifndef OMERO_UTIL_TILES_H
#define OMERO_UTIL_TILES_H

#include <omero/IceNoWarnPush.h>
#include <Ice/Ice.h>
#include <IceUtil/Handle.h>
#include <omero/API.h>
#include <omero/api/RawPixelsStore.h>
#include <omero/model/Pixels.h>
#include <omero/IceNoWarnPop.h>

#ifndef OMERO_CLIENT
#   ifdef OMERO_CLIENT_EXPORTS
#       define OMERO_CLIENT ICE_DECLSPEC_EXPORT
#   else
#       define OMERO_CLIENT ICE_DECLSPEC_IMPORT
#   endif
#endif

namespace omero {
    namespace util {
        class TileData;
        class TileLoopIteration;
        class TileLoop;
        class RPSTileLoop;
    }
}

namespace IceInternal {
  OMERO_CLIENT ::Ice::Object* upCast(::omero::util::TileData*);
  OMERO_CLIENT ::Ice::Object* upCast(::omero::util::TileLoopIteration*);
  OMERO_CLIENT ::Ice::Object* upCast(::omero::util::TileLoop*);
  OMERO_CLIENT ::Ice::Object* upCast(::omero::util::RPSTileLoop*);
}

namespace omero {
    namespace util {

        /**
         * Interface which must be returned from TileLoop.createData
         */
        typedef IceUtil::Handle<TileData> TileDataPtr;

        class OMERO_CLIENT TileData : virtual public IceUtil::Shared {
        private:
            // Preventing copy-construction and assigning by value.
            TileData& operator=(const TileData& rv);
            TileData(TileData&);
        public:
            TileData();
            virtual ~TileData() = 0;
            virtual Ice::ByteSeq getTile(int z, int c, int t, int x, int y, int w, int h) = 0;
            virtual void setTile(const Ice::ByteSeq& buffer, int z, int c, int t, int x, int y, int w, int h) = 0;
            virtual void close() = 0;
        };

        /**
         * Interface to be passed to forEachTile.
         */
        typedef IceUtil::Handle<TileLoopIteration> TileLoopIterationPtr;

        class OMERO_CLIENT TileLoopIteration : virtual public IceUtil::Shared {
        private:
            // Preventing copy-construction and assigning by value.
            TileLoopIteration& operator=(const TileLoopIteration& rv);
            TileLoopIteration(TileLoopIteration&);
        public:
            TileLoopIteration();
            virtual ~TileLoopIteration() = 0;
            virtual void run(const TileDataPtr& data, int z, int c, int t, int x, int y,
                             int tileWidth, int tileHeight, int tileCount) const = 0;
        };

        /**
         * Interface to be passed to forEachTile.
         */
        typedef IceUtil::Handle<TileLoop> TileLoopPtr;

        class OMERO_CLIENT TileLoop : virtual public IceUtil::Shared {
        private:
            // Preventing copy-construction and assigning by value.
            TileLoop& operator=(const TileLoop& rv);
            TileLoop(TileLoop&);
        public:
            TileLoop();
            virtual ~TileLoop() = 0;
            virtual TileDataPtr createData() = 0;
            virtual int forEachTile(int sizeX, int sizeY, int sizeZ, int sizeT, int sizeC,
                                   int tileHeight, int tileWidth, const TileLoopIterationPtr& iteration);
        };

        // Forward defs
        typedef IceUtil::Handle<RPSTileLoop> RPSTileLoopPtr;

        class OMERO_CLIENT RPSTileData : virtual public TileData {
        protected:
            RPSTileLoopPtr loop;
            omero::api::RawPixelsStorePrx rps;
        public:
            RPSTileData(const RPSTileLoopPtr& loop, const omero::api::RawPixelsStorePrx& rps);
            virtual ~RPSTileData();
            virtual Ice::ByteSeq getTile(int z, int c, int t, int x, int y, int w, int h);
            virtual void setTile(const Ice::ByteSeq& buffer, int z, int c, int t, int x, int y, int w, int h);
            virtual void close();
        };

        class OMERO_CLIENT RPSTileLoop : virtual public TileLoop {
        protected:
            omero::api::ServiceFactoryPrx session;
            omero::model::PixelsPtr pixels;
        public:
            using TileLoop::forEachTile;
            RPSTileLoop(const omero::api::ServiceFactoryPrx& session, const omero::model::PixelsPtr& pixels);
            virtual ~RPSTileLoop();
            virtual omero::api::ServiceFactoryPrx getSession();
            virtual omero::model::PixelsPtr getPixels();
            virtual void setPixels(const omero::model::PixelsPtr& pixels);
            virtual int forEachTile(int tileHeight, int tileWidth, const TileLoopIterationPtr& iteration);
            virtual TileDataPtr createData();
        };


    }

}

#endif // OMERO_UTIL_TILES_H
