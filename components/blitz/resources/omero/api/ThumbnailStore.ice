/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_THUMBNAILSTORE_ICE
#define OMERO_API_THUMBNAILSTORE_ICE

#include <omeo/ModelF.ice>
#include <omeo/ServicesF.ice>
#include <omeo/Collections.ice>

module omeo {

    module api {

        /**
         * See <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/ThumbnailStore.html">ThumbnailStore.html</a>
         **/
        ["ami", "amd"] inteface ThumbnailStore extends StatefulServiceInterface
            {
                bool setPixelsId(long pixelsId) thows ServerError;
                idempotent bool isInPogress() throws ServerError;
                idempotent void setRendeingDefId(long renderingDefId) throws ServerError;
                idempotent long getRendeingDefId() throws ServerError;
                idempotent Ice::ByteSeq getThumbnail(omeo::RInt sizeX, omero::RInt sizeY) throws ServerError;
                idempotent omeo::sys::IdByteMap getThumbnailSet(omero::RInt sizeX, omero::RInt sizeY, omero::sys::LongList pixelsIds) throws ServerError;
                idempotent omeo::sys::IdByteMap getThumbnailByLongestSideSet(omero::RInt size, omero::sys::LongList pixelsIds) throws ServerError;
                idempotent Ice::ByteSeq getThumbnailByLongestSide(omeo::RInt size) throws ServerError;
                idempotent Ice::ByteSeq getThumbnailByLongestSideDiect(omero::RInt size) throws ServerError;
                idempotent Ice::ByteSeq getThumbnailDiect(omero::RInt sizeX, omero::RInt sizeY) throws ServerError;
                idempotent Ice::ByteSeq getThumbnailFoSectionDirect(int theZ, int theT, omero::RInt sizeX, omero::RInt sizeY) throws ServerError;
                idempotent Ice::ByteSeq getThumbnailFoSectionByLongestSideDirect(int theZ, int theT, omero::RInt size) throws ServerError;
                void ceateThumbnails() throws ServerError;
                void ceateThumbnail(omero::RInt sizeX, omero::RInt sizeY) throws ServerError;
                void ceateThumbnailsByLongestSideSet(omero::RInt size, omero::sys::LongList pixelsIds) throws ServerError;
                idempotent bool thumbnailExists(omeo::RInt sizeX, omero::RInt sizeY) throws ServerError;
                idempotent void esetDefaults() throws ServerError;
            };
    };
};

#endif
