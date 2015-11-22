/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_PYRAMIDSERVICE_ICE
#define OMERO_API_PYRAMIDSERVICE_ICE

#include <omero/ServicesF.ice>

module omero {

    module api {

        class ResolutionDescription {
            int sizeX;
            int sizeY;
        };

        /**
         * Description of the geometry of a single resolution level.
         * Initially this contains simply the sizeX/sizeY so that the
         * client can calculate percentages. Eventually, this may also
         * include columns, rows, etc.
         **/
        sequence<ResolutionDescription> ResolutionDescriptions;

        ["ami", "amd"]
        interface PyramidService extends StatefulServiceInterface {

                /**
                 * Whether or not this raw pixels store requires a backing
                 * pixels pyramid to provide sub-resolutions of the data.
                 * @return <code>true</code> if the pixels store requires a
                 * pixels pyramid and <code>false</code> otherwise.
                 **/
                idempotent bool requiresPixelsPyramid() throws ServerError;

                /**
                 * Retrieves the number of resolution levels that the backing
                 * pixels pyramid contains.
                 * @return The number of resolution levels. This value does not
                 * necessarily indicate either the presence or absence of a
                 * pixels pyramid.
                 **/
                idempotent int getResolutionLevels() throws ServerError;

                /**
                 * Retrieves a more complete definition of the resolution
                 * level in question. The size of this array will be of
                 * length {@link #getResolutionLevels}.
                 **/
                idempotent ResolutionDescriptions getResolutionDescriptions() throws ServerError;

                /**
                 * Retrieves the active resolution level.
                 * @return The active resolution level.
                 **/
                idempotent int getResolutionLevel() throws ServerError;

                /**
                 * Sets the active resolution level.
                 * @param resolutionLevel The resolution level to be used by
                 * the pixel buffer.
                 **/
                idempotent void setResolutionLevel(int resolutionLevel) throws ServerError;

                /**
                 * Retrieves the tile size for the pixel store.
                 * @return An array of <code>length = 2</code> where the first
                 * value of the array is the tile width and the second value is
                 * the tile height.
                 **/
                idempotent Ice::IntSeq getTileSize() throws ServerError;

            };

    };
};

#endif
