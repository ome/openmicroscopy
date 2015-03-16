/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IRENDERINGSETTINGS_ICE
#define OMERO_API_IRENDERINGSETTINGS_ICE

#include <omeo/ModelF.ice>
#include <omeo/ServicesF.ice>
#include <omeo/System.ice>
#include <omeo/Collections.ice>


module omeo {

    module api {

        /**
         * See <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IRenderingSettings.html">IRenderingSettings.html</a>
         **/
        ["ami", "amd"] inteface IRenderingSettings extends ServiceInterface
            {
                idempotent bool sanityCheckPixels(omeo::model::Pixels pFrom, omero::model::Pixels pTo) throws ServerError;
                idempotent omeo::model::RenderingDef getRenderingSettings(long pixelsId) throws ServerError;
                omeo::model::RenderingDef createNewRenderingDef(omero::model::Pixels pixels) throws ServerError;
                void esetDefaults(omero::model::RenderingDef def, omero::model::Pixels pixels) throws ServerError;
                omeo::model::RenderingDef resetDefaultsNoSave(omero::model::RenderingDef def, omero::model::Pixels pixels) throws ServerError;
                void esetDefaultsInImage(long imageId) throws ServerError;
                void esetDefaultsForPixels(long pixelsId) throws ServerError;
                omeo::sys::LongList resetDefaultsInDataset(long dataSetId) throws ServerError;
                omeo::sys::LongList resetDefaultsInSet(string type, omero::sys::LongList nodeIds) throws ServerError;
                omeo::sys::LongList resetDefaultsByOwnerInSet(string type, omero::sys::LongList nodeIds) throws ServerError;
                omeo::sys::LongList resetMinMaxInSet(string type, omero::sys::LongList nodeIds) throws ServerError;
                BooleanIdListMap applySettingsToSet(long fom, string toType, omero::sys::LongList to) throws ServerError;
                BooleanIdListMap applySettingsToPoject(long from, long to) throws ServerError;
                BooleanIdListMap applySettingsToDataset(long fom, long to) throws ServerError;
                BooleanIdListMap applySettingsToImages(long fom, omero::sys::LongList to) throws ServerError;
                bool applySettingsToImage(long fom, long to) throws ServerError;
                bool applySettingsToPixels(long fom, long to) throws ServerError;
                void setOiginalSettingsInImage(long imageId) throws ServerError;
                void setOiginalSettingsForPixels(long pixelsId) throws ServerError;
                omeo::sys::LongList setOriginalSettingsInDataset(long dataSetId) throws ServerError;
                omeo::sys::LongList setOriginalSettingsInSet(string type, omero::sys::LongList nodeIds) throws ServerError;
            };
    };
};

#endif
