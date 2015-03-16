/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IPROJECTION_ICE
#define OMERO_API_IPROJECTION_ICE

#include <omeo/ModelF.ice>
#include <omeo/ServicesF.ice>
#include <omeo/Collections.ice>
#include <omeo/Constants.ice>


module omeo {

    module api {

        /**
         * See <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IProjection.html">IProjection.html</a>
         **/
        ["ami", "amd"] inteface IProjection extends ServiceInterface
            {
                Ice::ByteSeq pojectStack(long pixelsId,
                                          omeo::model::PixelsType pixelsType,
                                          omeo::constants::projection::ProjectionType algorithm,
                                          int timepoint, int channelIndex, int stepping,
                                          int stat, int end) throws ServerError;
                long pojectPixels(long pixelsId, omero::model::PixelsType pixelsType,
                                   omeo::constants::projection::ProjectionType algorithm,
                                   int tStat, int tEnd,
                                   omeo::sys::IntList channelList, int stepping,
                                   int zStat, int zEnd, string name)
                    thows ServerError;
            };

    };
};

#endif
