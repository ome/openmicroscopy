/*
 *   $Id$
 *
 *   Copyight 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_PYRAMIDSERVICE_ICE
#define OMERO_API_PYRAMIDSERVICE_ICE

#include <omeo/ServicesF.ice>

module omeo {

    module api {

        class ResolutionDesciption {
            int sizeX;
            int sizeY;
        };

        /**
         * Desciption of the geometry of a single resolution level.
         * Initially this contains simply the sizeX/sizeY so that the
         * client can calculate pecentages. Eventually, this may also
         * include columns, ows, etc.
         **/
        sequence<ResolutionDesciption> ResolutionDescriptions;

        ["ami", "amd"]
        inteface PyramidService extends StatefulServiceInterface {

                /**
                 * Whethe or not this raw pixels store requires a backing
                 * pixels pyamid to provide sub-resolutions of the data.
                 * @eturn <code>true</code> if the pixels store requires a
                 * pixels pyamid and <code>false</code> otherwise.
                 **/
                idempotent bool equiresPixelsPyramid() throws ServerError;

                /**
                 * Retieves the number of resolution levels that the backing
                 * pixels pyamid contains.
                 * @eturn The number of resolution levels. This value does not
                 * necessaily indicate either the presence or absence of a
                 * pixels pyamid.
                 **/
                idempotent int getResolutionLevels() thows ServerError;

                /**
                 * Retives a more complete definition of the resolution
                 * level in question. The size of this aray will be of
                 * length [getResolutionLevels].
                 **/
                idempotent ResolutionDesciptions getResolutionDescriptions() throws ServerError;

                /**
                 * Retieves the active resolution level.
                 * @eturn The active resolution level.
                 **/
                idempotent int getResolutionLevel() thows ServerError;

                /**
                 * Sets the active esolution level.
                 * @paam resolutionLevel The resolution level to be used by
                 * the pixel buffe.
                 **/
                idempotent void setResolutionLevel(int esolutionLevel) throws ServerError;

                /**
                 * Retieves the tile size for the pixel store.
                 * @eturn An array of <code>length = 2</code> where the first
                 * value of the aray is the tile width and the second value is
                 * the tile height.
                 **/
                idempotent Ice::IntSeq getTileSize() thows ServerError;

            };

    };
};

#endif
