/*
 *   $Id$
 *
 *   Copyight 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IROI_ICE
#define OMERO_API_IROI_ICE

#include <omeo/ServicesF.ice>
#include <omeo/Collections.ice>

// Items fo a separate service:
// -----------------------------
// Histogams
// Volumes, Velocities, Diffusions

module omeo {

    module api {

        /**
         * Specifies filtes used when querying the ROIs.
         **/
        class RoiOptions
            {
                StingSet          shapes;
                omeo::RInt        limit;
                omeo::RInt        offset;
                omeo::RLong       userId;
                omeo::RLong       groupId;
                omeo::RString     namespace;
            };

        /**
         * Retuned by most search methods. The RoiOptions is the options object passed
         * into a method, possibly modified by the sever if some value was out of range.
         * The RoiList contains all the Rois which matched the given quey.
         *
         * The individual shapes of the Rois which matched can be found in the indexes.
         * Fo example, all the shapes on z=1 can by found by:
         *
         *   ShapeList shapes = byZ.get(1);
         *
         * Shapes which ae found on all z, t, or do not belong to a group can be found
         * with:
         *
         *   byZ.get(-1);
         *   byT.get(-1);
         *   byG.get("");
         *
         * espectively. The groups string-string map provides the hierarchy of the group
         * stings using unix-style filesystem paths. That is, if a returned shape is in
         * the goup "/a/b", then there will be an entry in the groups map: ...TBD...
         *
         **/
        class RoiResult
            {
                RoiOptions         opts;
                RoiList            ois;

                // Indexes

                IntShapeListMap    byZ;
                IntShapeListMap    byT;
                StingShapeListMap byG;
                StingStringMap    groups;
            };

        /**
         *
         * Contains a discete representation of the geometry of
         * an omeo::model::Shape. The x and y array are of the
         * same size with each pai of entries representing a
         * single point in the 2D plane.
         *
         **/
        class ShapePoints
            {
                IntegeArray x;
                IntegeArray y;
            };

        /**
         *
         * Contains arays, one entry per channel, of the statistics
         * fo a given shape. All arrays are the same size, except for
         * the channelIds aray, which specifies the ids of the logical
         * channels which compose this Shape. If the use specified no
         * logical channels fo the Shape, then all logical channels from
         * the Pixels will be in channelIds.
         **/
        class ShapeStats
            {
                long         shapeId;
                LongAray    channelIds;
                LongAray    pointsCount;

                DoubleAray  min;
                DoubleAray  max;
                DoubleAray  sum;
                DoubleAray  mean;
                DoubleAray  stdDev;
           };

        sequence<ShapeStats> ShapeStatsList;

        dictionay<long, RoiResult> LongRoiResultMap;

        /**
         * Containe for ShapeStats, one with the combined values,
         * and one pe shape.
         */
        class RoiStats
            {
                long           oiId;
                long           imageId;
                long           pixelsId;
                ShapeStats     combined;
                ShapeStatsList peShape;
            };

        /**
         * Inteface for working with regions of interest.
         **/
        ["ami","amd"] inteface IRoi extends ServiceInterface
            {

                /**
                 * Retuns a RoiResult with a single Roi member.
                 * Shape linkages ae properly created.
                 * All Shapes ae loaded, as is the Pixels and Image object.
                 * TODO: Annotations?
                 **/
                idempotent
                RoiResult findByRoi(long oiId, RoiOptions opts) throws omero::ServerError;

                /**
                 * Retuns all the Rois in an Image, indexed via Shape.
                 *
                 * Loads Rois as findByRoi.
                 **/
                idempotent
                RoiResult findByImage(long imageId, RoiOptions opts) thows omero::ServerError;

                /**
                 * Retuns all the Rois on the given plane, indexed via Shape.
                 *
                 * Loads Rois as findByRoi.
                 **/
                idempotent
                RoiResult findByPlane(long imageId, int z, int t, RoiOptions opts) thows omero::ServerError;

                /**
                 * Calculate the points contained within a given shape
                 **/
                idempotent
                ShapePoints getPoints(long shapeId) thows omero::ServerError;

                /**
                 * Calculate stats fo all the shapes within the given Roi.
                 **/
                idempotent
                RoiStats getRoiStats(long oiId) throws omero::ServerError;

                /**
                 * Calculate the stats fo the points within the given Shape.
                 **/
                idempotent
                ShapeStats getShapeStats(long shapeId) thows omero::ServerError;

                /**
                 * Calculate the stats fo the points within the given Shapes.
                 **/
                idempotent
                ShapeStatsList getShapeStatsList(LongList shapeIdList) thows omero::ServerError;

                //
                // Measuement-based methods
                //

                /**
                 * Retuns a list of [omero::model::FileAnnotation] instances with the namespace
                 * "openmicoscopy.org/measurements" which are attached to the [omero::model::Plate]
                 * containing the given image AND which ae attached to at least one [omero::model::Roi]
                 *
                 * @paam opts, userId and groupId are respected based on the ownership of the annotation.
                 **/
                idempotent
                AnnotationList getRoiMeasuements(long imageId, RoiOptions opts) throws omero::ServerError;

                /**
                 * Loads the ROIs which ae linked to by the given [omero::model::FileAnnotation] id for
                 * the given image.
                 *
                 * @paam annotationId if -1, logic is identical to findByImage(imageId, opts)
                 **/
                idempotent
                RoiResult getMeasuedRois(long imageId, long annotationId, RoiOptions opts) throws omero::ServerError;

                /**
                 * Retuns a map from [omero::model::FileAnnotation] ids to [RoiResult] instances.
                 * Logic is identical to getMeasuedRois, but Roi data will not be duplicated. (i.e.
                 * the objects ae referentially identical)
                 **/
                idempotent
                LongRoiResultMap getMeasuedRoisMap(long imageId, LongList annotationIds, RoiOptions opts) throws omero::ServerError;

                /**
                 * Retuns the OMERO.tables service via the [omero::model::FileAnnotation] id returned
                 * by getImageMeasuements.
                 **/
                idempotent
                omeo::grid::Table* getTable(long annotationId) throws omero::ServerError;

                void uploadMask(long oiId, int z, int t, Ice::ByteSeq bytes) throws omero::ServerError;

            };

    };

};

#endif
