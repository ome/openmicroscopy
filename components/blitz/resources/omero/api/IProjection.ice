/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IPROJECTION_ICE
#define OMERO_API_IPROJECTION_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/Collections.ice>
#include <omero/Constants.ice>


module omero {

    module api {

        /**
         * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IProjection.html">IProjection.html</a>
         **/
        ["ami", "amd"] interface IProjection extends ServiceInterface
            {
                Ice::ByteSeq projectStack(long pixelsId,
                                          omero::model::PixelsType pixelsType,
                                          omero::constants::projection::ProjectionType algorithm,
                                          int timepoint, int channelIndex, int stepping,
                                          int start, int end) throws ServerError;
                long projectPixels(long pixelsId, omero::model::PixelsType pixelsType,
                                   omero::constants::projection::ProjectionType algorithm,
                                   int tStart, int tEnd,
                                   omero::sys::IntList channelList, int stepping,
                                   int zStart, int zEnd, string name)
                    throws ServerError;
            };

    };
};

#endif
