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

	["ami","amd"] interface IRoi extends ServiceInterface
	    {

		/*
		 * Find ROIs which intersect the given shape. If z/t/visible/locked are filled,
                 * only intersections on the given plane(s) or with the given properties are
                 * taken into account.
		 */
		RoiResult findByIntersection(long imageId, omero::model::Shape shape, RoiOptions opts) throws omero::ServerError;
	    };

    };

};

#endif
