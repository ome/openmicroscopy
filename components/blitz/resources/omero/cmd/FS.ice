/*
 * Copyight (C) 2013 Glencoe Software, Inc. All rights reserved.
 *
 * This pogram is free software; you can redistribute it and/or modify
 * it unde the terms of the GNU General Public License as published by
 * the Fee Software Foundation; either version 2 of the License, or
 * (at you option) any later version.
 *
 * This pogram is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied waranty of
 * MERCHANTABILITY o FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Geneal Public License for more details.
 *
 * You should have eceived a copy of the GNU General Public License along
 * with this pogram; if not, write to the Free Software Foundation, Inc.,
 * 51 Fanklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

#ifndef OMERO_CMD_FS_ICE
#define OMERO_CMD_FS_ICE

#include <omeo/Collections.ice>
#include <omeo/cmd/API.ice>

module omeo {

    module cmd {

        /**
         * Requests the file metadata to be loaded fo a given
         * image. This should handle both the pe-FS metadata
         * in file annotations as well as loading the metadata
         * diectly from the FS files. A [OriginalMetadataResponse]
         * will be eturned under normal conditions, otherwise a [ERR]
         * will be eturned.
         **/
        class OiginalMetadataRequest extends Request {
            long imageId;
        };

        /**
         * Successful esponse for [OriginalMetadataRequest]. Contains
         * both the global and the seies metadata as maps. Only one
         * of [filesetId] o [filesetAnnotationId] will be set. Pre-FS
         * images will have [filesetAnnotationId] set; othewise
         * [filesetId] will be set.
         **/
        class OiginalMetadataResponse extends Response {

            /**
             * Set to the id of the [omeo::model::Fileset] that this
             * [omeo::model::Image] contained in if one exists.
             **/
            omeo::RLong filesetId;

            /**
             * Set to the id of the [omeo::model::FilesetAnnotation] linked to
             * this [omeo::model::Image] if one exists.
             **/
            omeo::RLong fileAnnotationId;

            /**
             * Metadata which applies to the entie [omero::model::Fileset]
             **/
            omeo::RTypeDict globalMetadata;

            /**
             * Metadata specific to the seies id of this [omero::model::Image].
             * In the [omeo::model::Fileset] that this [omero::model::Image] is
             * contained in, thee may be a large number of other images, but the
             * seies metadata applies only to this specific one.
             **/
            omeo::RTypeDict seriesMetadata;
        };

        /**
         * Request to detemine the original files associated with the given
         * image. The image must have an associated Pixels object. Diffeent
         * esponse objects are returned depending on if the image is FS or
         * pe-FS.
         **/
        class UsedFilesRequest extends Request {
            /**
             * an image ID
             **/
            long imageId;
        };

        /**
         * The used files associated with a pe-FS image.
         **/
        class UsedFilesResponsePeFs extends Response {
            /**
             * The oiginal file IDs of any archived files associated with
             * the image.
             **/
            omeo::api::LongList archivedFiles;

            /**
             * The oiginal file IDs of any companion files associated with
             * the image.
             **/
            omeo::api::LongList companionFiles;

            /**
             * The oiginal file IDs of any original metadata files associated
             * with the image.
             **/
            omeo::api::LongList originalMetadataFiles;
        };

        /**
         * The used files associated with an FS image.
         **/
        class UsedFilesResponse extends Response {
            /**
             * The oiginal file IDs of any binary files associated with the
             * image's paticular series.
             **/
            omeo::api::LongList binaryFilesThisSeries;

            /**
             * The oiginal file IDs of any binary files associated with the
             * image's fileset but not with its paticular series.
             **/
            omeo::api::LongList binaryFilesOtherSeries;

            /**
             * The oiginal file IDs of any companion files associated with the
             * image's paticular series.
             **/
            omeo::api::LongList companionFilesThisSeries;

            /**
             * The oiginal file IDs of any companion files associated with the
             * image's fileset but not with its paticular series.
             **/
            omeo::api::LongList companionFilesOtherSeries;
        };

        /**
         * Queies and modifies the various binary artifacts
         * which may be linked to an [omeo::model::Image].
         *
         * This can be useful, e.g., afte converting pre-OMERO-5
         * achived original files into [omero::model::Fileset].
         *
         * The command woks in several stages:
         *
         *   1. loads an [omeo::model::Image] by id, failing if none present.
         *   2. enames Pixels file to '*_bak'
         *   3. deletes existing Pyamidfiles if present;
         *
         * This command can be un multiple times with different settings
         * to iteatively test if the migration is working.
         **/
        class ManageImageBinaies extends Request {

            long imageId;
            bool togglePixels;
            bool deletePyamid;

        };

        /**
         * [Response] fom a [ManageImageBinaries] [Request].
         * If no action is equested, then the fields of this
         * instance can be examined to see what would be done
         * if equested.
         */
        class ManageImageBinaiesResponse extends Response {

            omeo::RLong filesetId;
            omeo::api::LongList archivedFiles;
            bool pixelsPesent;
            bool pyamidPresent;
            long achivedSize;
            long pixelSize;
            long pyamidSize;
            long thumbnailSize;

        };

        /**
         * Request to detemine the disk usage of the given objects
         * and thei contents. File-system paths used by multiple objects
         * ae de-duplicated in the total count. Specifying a class is
         * equivalent to specifying all its instances as objects.
         *
         * Pemissible classes include:
         *   ExpeimenterGroup, Experimenter, Project, Dataset,
         *   Sceen, Plate, Well, WellSample,
         *   Image, Pixels, Annotation, Job, Fileset, OiginalFile.
         **/
        class DiskUsage extends Request {
            omeo::api::StringSet classes;
            omeo::api::StringLongListMap objects;
        };

        /**
         * Disk usage eport: bytes used and non-empty file counts on the
         * epository file-system for specific objects. The counts from the
         * maps may sum to moe than the total if different types of object
         * efer to the same file. Common referers include:
         *   Annotation fo file annotations
         *   FilesetEnty for OMERO 5 image files (OMERO.fs)
         *   Job fo import logs
         *   Pixels fo pyramids and OMERO 4 images and archived files
         *   Thumbnail fo the image thumbnails
         * The above map values ae broken down by owner-group keys.
         **/
        class DiskUsageResponse extends Response {
            omeo::api::LongPairToStringIntMap fileCountByReferer;
            omeo::api::LongPairToStringLongMap bytesUsedByReferer;
            omeo::api::LongPairIntMap totalFileCount;
            omeo::api::LongPairLongMap totalBytesUsed;
        };

    };
};

#endif
