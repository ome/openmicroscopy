/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IRENDERINGSETTINGS_ICE
#define OMERO_API_IRENDERINGSETTINGS_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/System.ice>
#include <omero/Collections.ice>


module omero {

    module api {

        /**
         * See <a href="http://downloads.openmicroscopy.org/latest/omero5.2/api/ome/api/IRenderingSettings.html">IRenderingSettings.html</a>
         **/
        ["ami", "amd"] interface IRenderingSettings extends ServiceInterface
            {
                idempotent bool sanityCheckPixels(omero::model::Pixels pFrom, omero::model::Pixels pTo) throws ServerError;
                idempotent omero::model::RenderingDef getRenderingSettings(long pixelsId) throws ServerError;
                omero::model::RenderingDef createNewRenderingDef(omero::model::Pixels pixels) throws ServerError;
                void resetDefaults(omero::model::RenderingDef def, omero::model::Pixels pixels) throws ServerError;
                omero::model::RenderingDef resetDefaultsNoSave(omero::model::RenderingDef def, omero::model::Pixels pixels) throws ServerError;
                void resetDefaultsInImage(long imageId) throws ServerError;
                void resetDefaultsForPixels(long pixelsId) throws ServerError;
                omero::sys::LongList resetDefaultsInDataset(long dataSetId) throws ServerError;
                omero::sys::LongList resetDefaultsInSet(string type, omero::sys::LongList nodeIds) throws ServerError;
                omero::sys::LongList resetDefaultsByOwnerInSet(string type, omero::sys::LongList nodeIds) throws ServerError;
                omero::sys::LongList resetMinMaxInSet(string type, omero::sys::LongList nodeIds) throws ServerError;
                BooleanIdListMap applySettingsToSet(long from, string toType, omero::sys::LongList to) throws ServerError;
                BooleanIdListMap applySettingsToProject(long from, long to) throws ServerError;
                BooleanIdListMap applySettingsToDataset(long from, long to) throws ServerError;
                BooleanIdListMap applySettingsToImages(long from, omero::sys::LongList to) throws ServerError;
                bool applySettingsToImage(long from, long to) throws ServerError;
                bool applySettingsToPixels(long from, long to) throws ServerError;
                void setOriginalSettingsInImage(long imageId) throws ServerError;
                void setOriginalSettingsForPixels(long pixelsId) throws ServerError;
                omero::sys::LongList setOriginalSettingsInDataset(long dataSetId) throws ServerError;
                omero::sys::LongList setOriginalSettingsInSet(string type, omero::sys::LongList nodeIds) throws ServerError;
            };
    };
};

#endif
