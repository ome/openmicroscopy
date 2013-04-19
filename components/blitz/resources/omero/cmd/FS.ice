/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

#ifndef OMERO_CMD_FS_ICE
#define OMERO_CMD_FS_ICE

#include <omero/Collections.ice>
#include <omero/cmd/API.ice>

module omero {

    module cmd {

        /**
         * Requests the file metadata to be loaded for a given
         * image. This should handle both the pre-FS metadata
         * in file annotations as well as loading the metadata
         * directly from the FS files. A [OriginalMetadataResponse]
         * will be returned under normal conditions, otherwise a [ERR]
         * will be returned.
         **/
        class OriginalMetadataRequest extends Request {
            long imageId;
        };

        /**
         * Successful response for [OriginalMetadataRequest]. Contains
         * boths the global and the series metadata as maps. Only one
         * of [filesetId] or [filesetAnnotationId] will be set. Pre-FS
         * images will have [filesetAnnotationId] set; otherwise
         * [filesetId] will be set.
         **/
        class OriginalMetadataResponse extends Response {

            /**
             * Set to the id of the [omero::model::Fileset] that this
             * [omero::model::Image] contained in if one exists.
             **/
            omero::RLong filesetId;

            /**
             * Set to the id of the [omero::model::FilesetAnnotation] linked to
             * this [omero::model::Image] if one exists.
             **/
            omero::RLong fileAnnotationId;

            /**
             * Metadata which applies to the entire [omero::model::Fileset]
             **/
            omero::RTypeDict globalMetadata;

            /**
             * Metadata specific to the series id of this [omero::model::Image].
             * In the [omero::model::Fileset] that this [omero::model::Image] is
             * contained in, there may be a large number of other images, but the
             * series metadata applies only to this specific one.
             **/
            omero::RTypeDict seriesMetadata;
        };
    };
};

#endif
