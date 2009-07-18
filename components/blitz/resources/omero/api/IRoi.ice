/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IROI_ICE
#define OMERO_API_IROI_ICE

#include <omero/API.ice>
#include <omero/model/Shape.ice>
#include <omero/ServerErrors.ice>

// Items for a separate service:
// -----------------------------
// Histograms
// Volumes, Velocities, Diffusions

module omero {

    module api {

        /**
         * Specifies filters used when querying the ROIs
         **/
        class RoiOptions
            {
                StringSet          shapes;
                omero::RInt        limit;
                omero::RInt        offset;
            };

        class RoiResult
            {
                RoiOptions         opts;
                RoiList            rois;
                StringStringMap    groups;

                // Indexes

                IntShapeListMap    byZ;
                IntShapeListMap    byT;
                StringShapeListMap byG;
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
         * for a given shape. All arrays are the same size.
         **/
        class ShapeStats
            {
                DoubleArray  min;
                DoubleArray  max;
                DoubleArray  sum;
                DoubleArray  mean;
                DoubleArray  stdDev;
                DoubleArray  pointsCount;
           };

        sequence<ShapeStats> ShapeStatsList;

        /**
         * Container for ShapeStats, one with the combined values,
         * and one per shape.
         */
        class RoiStats
            {
                ShapeStats combined;
                ShapeStatsList perShape;
            };

	["ami","amd"] interface IRoi extends ServiceInterface
	    {

                /*
                 * Returns a RoiResult with a single Roi member.
                 * Shape linkages are properly created.
                 * All Shapes are loaded, as is the Pixels and Image object.
                 * TODO: Annotations?
                 */
                RoiResult findByRoi(long roiId, RoiOptions opts) throws omero::ServerError;

                /**
                 * Returns all the Rois in an Image, indexed via Shape.
                 *
                 * Loads Rois as findByRoi.
                 */
                RoiResult findByImage(long imageId, RoiOptions opts) throws omero::ServerError;

		/**
		 * Find ROIs which intersect the given shape. If z/t/visible/locked are filled,
                 * only intersections on the given plane(s) or with the given properties are
                 * taken into account.
                 *
                 * Shape id is ignored, object should be properly loaded.
                 *
                 * Loads Rois as findByRoi.
                 *
		 **/
		RoiResult findByIntersection(long imageId, omero::model::Shape shape, RoiOptions opts) throws omero::ServerError;

		/**
		 * Find ROIs which intersect any of the given shape.
                 * Otherwise as findByIntersection.
		 **/
		RoiResult findByAnyIntersection(long imageId, ShapeList shape, RoiOptions opts) throws omero::ServerError;

                /**
                 * Calculate the points contained within a given shape
                 **/
                ShapePoints getPoints(long shapeId) throws omero::ServerError;

                /**
                 * Calculate stats for all the shapes within the given Roi.
                 */
                RoiStats getRoiStats(long roiId) throws omero::ServerError;

                /**
                 * Calculate the stats for the points within the given Shape.
                 **/
                ShapeStats getShapeStats(long shapeId) throws omero::ServerError;

                /**
                 * Calculate the stats for the points within the given Shapes.
                 **/
                ShapeStatsList getShapeStatsList(LongList shapeIdList) throws omero::ServerError;

	    };

    };

};

#endif
