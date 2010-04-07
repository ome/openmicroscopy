/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ICONFIG_ICE
#define OMERO_API_ICONFIG_ICE

#include <omero/Collections.ice>
#include <omero/ServicesF.ice>

module omero {

    module api {
        /**
         * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IPixels.html">IPixels.html</a>
         **/
        ["ami", "amd"] interface IPixels extends ServiceInterface
            {
                idempotent omero::model::Pixels retrievePixDescription(long pixId) throws ServerError;
                idempotent omero::model::RenderingDef retrieveRndSettings(long pixId) throws ServerError;
                idempotent omero::model::RenderingDef retrieveRndSettingsFor(long pixId, long userId) throws ServerError;
                idempotent IObjectList retrieveAllRndSettings(long pixId, long userId) throws ServerError;
                idempotent omero::model::RenderingDef loadRndSettings(long renderingSettingsId) throws ServerError;
                void saveRndSettings(omero::model::RenderingDef rndSettings) throws ServerError;
                idempotent int getBitDepth(omero::model::PixelsType type) throws ServerError;
                idempotent omero::model::IObject getEnumeration(string enumClass, string value) throws ServerError;
                idempotent IObjectList getAllEnumerations(string enumClass) throws ServerError;
                omero::RLong copyAndResizePixels(long pixelsId,
                                                 omero::RInt sizeX,
                                                 omero::RInt sizeY,
                                                 omero::RInt sizeZ,
                                                 omero::RInt sizeT,
                                                 omero::sys::IntList channelList,
                                                 string methodology,
                                                 bool copyStats) throws ServerError;
                omero::RLong copyAndResizeImage(long imageId,
                                                omero::RInt sizeX,
                                                omero::RInt sizeY,
                                                omero::RInt sizeZ,
                                                omero::RInt sizeT,
                                                omero::sys::IntList channelList,
                                                string methodology,
                                                bool copyStats) throws ServerError;
                omero::RLong createImage(int sizeX, int sizeY, int sizeZ, int sizeT,
                                         omero::sys::IntList channelList,
                                         omero::model::PixelsType pixelsType,
                                         string name, string description) throws ServerError;
                void setChannelGlobalMinMax(long pixelsId, int channelIndex, double min, double max) throws ServerError;
            };
    };
};

#endif
