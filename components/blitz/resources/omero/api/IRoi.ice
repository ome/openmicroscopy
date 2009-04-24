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

        class ShapeStats
            {
                double min;
                double max;
                double sum;
                double mean;
                double stdDev;
                double pointsCount;
           };

	["ami","amd"] interface IRoi extends ServiceInterface
	    {

		/**
		 * Find ROIs which intersect the given shape. If z/t/visible/locked are filled,
                 * only intersections on the given plane(s) or with the given properties are
                 * taken into account.
                 *
                 * Shape id is ignored, object should be properly loaded.
                 *
		 **/
		RoiResult findByIntersection(long imageId, omero::model::Shape shape, RoiOptions opts) throws omero::ServerError;

                /**
                 * Calculate the points contained within a given shape
                 **/
                ShapePoints getPoints(long shapeId) throws omero::ServerError;

                /**
                 * Calculate the stats for the points within the given shape;
                 **/
                ShapeStats getStats(long shapeId) throws omero::ServerError;
	    };

    };

};

#endif
