/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IROI_ICE
#define OMERO_API_IROI_ICE

#include <omero/ServicesF.ice>
#include <omero/Collections.ice>

// Items for a separate service:
// -----------------------------
// Histograms
// Volumes, Velocities, Diffusions

module omero {

    module api {

        /**
         * Specifies filters used when querying the ROIs.
         **/
        class RoiOptions
            {
                StringSet          shapes;
                omero::RInt        limit;
                omero::RInt        offset;
                omero::RLong       userId;
                omero::RLong       groupId;
            };

        /**
         * Returned by most search methods. The RoiOptions is the options object passed
         * into a method, possibly modified by the server if some value was out of range.
         * The RoiList contains all the Rois which matched the given query.
         *
         * The individual shapes of the Rois which matched can be found in the indexes.
         * For example, all the shapes on z=1 can by found by:
         *
         *   ShapeList shapes = byZ.get(1);
         *
         * Shapes which are found on all z or t can be found with:
         *
         *   byZ.get(-1);
         *   byT.get(-1);
         *
         * respectively.
         *
         **/
        class RoiResult
            {
                RoiOptions         opts;
                RoiList            rois;

                // Indexes

                IntShapeListMap    byZ;
                IntShapeListMap    byT;
            };

        /**
         *
         * Contains a discrete representation of the geometry of
         * an omero::model::Shape. The x and y array are of the
         * same size with each pair of entries representing a
         * single point in the 2D plane.
         *
         **/
        class ShapePoints
            {
                IntegerArray x;
                IntegerArray y;
            };

        /**
         *
         * Contains arrays, one entry per channel, of the statistics
         * for a given shape. All arrays are the same size, except for
         * the channelIds array, which specifies the ids of the logical
         * channels which compose this Shape. If the user specified no
         * logical channels for the Shape, then all logical channels from
         * the Pixels will be in channelIds.
         **/
        class ShapeStats
            {
                long         shapeId;
                LongArray    channelIds;
                LongArray    pointsCount;

                DoubleArray  min;
                DoubleArray  max;
                DoubleArray  sum;
                DoubleArray  mean;
                DoubleArray  stdDev;
           };

        sequence<ShapeStats> ShapeStatsList;

        dictionary<long, RoiResult> LongRoiResultMap;

        /**
         * Container for ShapeStats, one with the combined values,
         * and one per shape.
         */
        class RoiStats
            {
                long           roiId;
                long           imageId;
                long           pixelsId;
                ShapeStats     combined;
                ShapeStatsList perShape;
            };

        /**
         * Interface for working with regions of interest.
         **/
        ["ami","amd"] interface IRoi extends ServiceInterface
            {

                /**
                 * Returns a RoiResult with a single Roi member.
                 * Shape linkages are properly created.
                 * All Shapes are loaded, as is the Pixels and Image object.
                 * TODO: Annotations?
                 **/
                idempotent
                RoiResult findByRoi(long roiId, RoiOptions opts) throws omero::ServerError;

                /**
                 * Returns all the Rois in an Image, indexed via Shape.
                 *
                 * Loads Rois as findByRoi.
                 **/
                idempotent
                RoiResult findByImage(long imageId, RoiOptions opts) throws omero::ServerError;

                /**
                 * Returns all the Rois on the given plane, indexed via Shape.
                 *
                 * Loads Rois as findByRoi.
                 **/
                idempotent
                RoiResult findByPlane(long imageId, int z, int t, RoiOptions opts) throws omero::ServerError;

                /**
                 * Calculate the points contained within a given shape
                 **/
                idempotent
                ShapePoints getPoints(long shapeId) throws omero::ServerError;

                /**
                 * Calculate stats for all the shapes within the given Roi.
                 **/
                idempotent
                RoiStats getRoiStats(long roiId) throws omero::ServerError;

                /**
                 * Calculate the stats for the points within the given Shape.
                 **/
                idempotent
                ShapeStats getShapeStats(long shapeId) throws omero::ServerError;

                /**
                 * Calculate the stats for the points within the given Shapes.
                 **/
                idempotent
                ShapeStatsList getShapeStatsList(LongList shapeIdList) throws omero::ServerError;

                //
                // Measurement-based methods
                //

                /**
                 * Returns a list of [omero::model::FileAnnotation] instances with the namespace
                 * "openmicroscopy.org/measurements" which are attached to the [omero::model::Plate]
                 * containing the given image AND which are attached to at least one [omero::model::Roi]
                 *
                 * @param opts, userId and groupId are respected based on the ownership of the annotation.
                 **/
                idempotent
                AnnotationList getRoiMeasurements(long imageId, RoiOptions opts) throws omero::ServerError;

                /**
                 * Loads the ROIs which are linked to by the given [omero::model::FileAnnotation] id for
                 * the given image.
                 *
                 * @param annotationId if -1, logic is identical to findByImage(imageId, opts)
                 **/
                idempotent
                RoiResult getMeasuredRois(long imageId, long annotationId, RoiOptions opts) throws omero::ServerError;

                /**
                 * Returns a map from [omero::model::FileAnnotation] ids to [RoiResult] instances.
                 * Logic is identical to getMeasuredRois, but Roi data will not be duplicated. (i.e.
                 * the objects are referentially identical)
                 **/
                idempotent
                LongRoiResultMap getMeasuredRoisMap(long imageId, LongList annotationIds, RoiOptions opts) throws omero::ServerError;

                /**
                 * Returns the OMERO.tables service via the [omero::model::FileAnnotation] id returned
                 * by getImageMeasurements.
                 **/
                idempotent
                omero::grid::Table* getTable(long annotationId) throws omero::ServerError;

                void uploadMask(long roiId, int z, int t, Ice::ByteSeq bytes) throws omero::ServerError;

            };

    };

};

#endif
