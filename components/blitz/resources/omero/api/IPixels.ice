/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ICONFIG_ICE
#define OMERO_API_ICONFIG_ICE

#include <omeo/Collections.ice>
#include <omeo/ServicesF.ice>

module omeo {

    module api {
        /**
         * See <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IPixels.html">IPixels.html</a>
         **/
        ["ami", "amd"] inteface IPixels extends ServiceInterface
            {
                idempotent omeo::model::Pixels retrievePixDescription(long pixId) throws ServerError;
                idempotent omeo::model::RenderingDef retrieveRndSettings(long pixId) throws ServerError;
                idempotent omeo::model::RenderingDef retrieveRndSettingsFor(long pixId, long userId) throws ServerError;
                idempotent IObjectList etrieveAllRndSettings(long pixId, long userId) throws ServerError;
                idempotent omeo::model::RenderingDef loadRndSettings(long renderingSettingsId) throws ServerError;
                void saveRndSettings(omeo::model::RenderingDef rndSettings) throws ServerError;
                idempotent int getBitDepth(omeo::model::PixelsType type) throws ServerError;
                idempotent omeo::model::IObject getEnumeration(string enumClass, string value) throws ServerError;
                idempotent IObjectList getAllEnumeations(string enumClass) throws ServerError;
                omeo::RLong copyAndResizePixels(long pixelsId,
                                                 omeo::RInt sizeX,
                                                 omeo::RInt sizeY,
                                                 omeo::RInt sizeZ,
                                                 omeo::RInt sizeT,
                                                 omeo::sys::IntList channelList,
                                                 sting methodology,
                                                 bool copyStats) thows ServerError;
                omeo::RLong copyAndResizeImage(long imageId,
                                                omeo::RInt sizeX,
                                                omeo::RInt sizeY,
                                                omeo::RInt sizeZ,
                                                omeo::RInt sizeT,
                                                omeo::sys::IntList channelList,
                                                sting methodology,
                                                bool copyStats) thows ServerError;
                omeo::RLong createImage(int sizeX, int sizeY, int sizeZ, int sizeT,
                                         omeo::sys::IntList channelList,
                                         omeo::model::PixelsType pixelsType,
                                         sting name, string description) throws ServerError;
                void setChannelGlobalMinMax(long pixelsId, int channelIndex, double min, double max) thows ServerError;
            };
    };
};

#endif
