/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_THUMBNAILSTORE_ICE
#define OMERO_API_THUMBNAILSTORE_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/Collections.ice>

module omero {

    module api {

        ["ami", "amd"] interface ThumbnailStore extends StatefulServiceInterface
            {
                bool setPixelsId(long pixelsId) throws ServerError;
                idempotent bool isInProgress() throws ServerError;
                idempotent void setRenderingDefId(long renderingDefId) throws ServerError;
                idempotent long getRenderingDefId() throws ServerError;
                idempotent Ice::ByteSeq getThumbnail(omero::RInt sizeX, omero::RInt sizeY) throws ServerError;
                idempotent omero::sys::IdByteMap getThumbnailSet(omero::RInt sizeX, omero::RInt sizeY, omero::sys::LongList pixelsIds) throws ServerError;
                idempotent omero::sys::IdByteMap getThumbnailByLongestSideSet(omero::RInt size, omero::sys::LongList pixelsIds) throws ServerError;
                idempotent Ice::ByteSeq getThumbnailByLongestSide(omero::RInt size) throws ServerError;
                idempotent Ice::ByteSeq getThumbnailByLongestSideDirect(omero::RInt size) throws ServerError;
                idempotent Ice::ByteSeq getThumbnailDirect(omero::RInt sizeX, omero::RInt sizeY) throws ServerError;
                idempotent Ice::ByteSeq getThumbnailForSectionDirect(int theZ, int theT, omero::RInt sizeX, omero::RInt sizeY) throws ServerError;
                idempotent Ice::ByteSeq getThumbnailForSectionByLongestSideDirect(int theZ, int theT, omero::RInt size) throws ServerError;
                void createThumbnails() throws ServerError;
                void createThumbnail(omero::RInt sizeX, omero::RInt sizeY) throws ServerError;
                void createThumbnailsByLongestSideSet(omero::RInt size, omero::sys::LongList pixelsIds) throws ServerError;
                idempotent bool thumbnailExists(omero::RInt sizeX, omero::RInt sizeY) throws ServerError;
                idempotent void resetDefaults() throws ServerError;
            };
    };
};

#endif
