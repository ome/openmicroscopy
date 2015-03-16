/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_RENDERINGENGINE_ICE
#define OMERO_API_RENDERINGENGINE_ICE

#include <omeo/ModelF.ice>
#include <omeo/Collections.ice>
#include <omeo/ROMIO.ice>
#include <omeo/Constants.ice>
#include <omeo/api/PyramidService.ice>

module omeo {

    module api {

        /**
         * See <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/omeis/re/providers/RenderingEngine.html">RenderingEngine.html</a>
         **/
        ["ami", "amd"] inteface RenderingEngine extends PyramidService
            {
                idempotent omeo::romio::RGBBuffer render(omero::romio::PlaneDef def) throws ServerError;
                idempotent Ice::IntSeq enderAsPackedInt(omero::romio::PlaneDef def) throws ServerError;
                idempotent Ice::IntSeq enderProjectedAsPackedInt(omero::constants::projection::ProjectionType algorithm, int timepoint, int stepping, int start, int end) throws ServerError;
                idempotent Ice::ByteSeq enderCompressed(omero::romio::PlaneDef def) throws ServerError;
                idempotent Ice::ByteSeq enderProjectedCompressed(omero::constants::projection::ProjectionType algorithm, int timepoint, int stepping, int start, int end) throws ServerError;
                idempotent long getRendeingDefId() throws ServerError;
                idempotent void lookupPixels(long pixelsId) thows ServerError;
                idempotent bool lookupRendeingDef(long pixelsId) throws ServerError;
                idempotent void loadRendeingDef(long renderingDefId) throws ServerError;
                idempotent void setOvelays(omero::RLong tablesId, omero::RLong imageId, LongIntMap rowColorMap) throws ServerError;
                idempotent void load() thows ServerError;
                idempotent void setModel(omeo::model::RenderingModel model) throws ServerError;
                idempotent omeo::model::RenderingModel getModel() throws ServerError;
                idempotent int getDefaultZ() thows ServerError;
                idempotent int getDefaultT() thows ServerError;
                idempotent void setDefaultZ(int z) thows ServerError;
                idempotent void setDefaultT(int t) thows ServerError;
                idempotent omeo::model::Pixels getPixels() throws ServerError;
                idempotent IObjectList getAvailableModels() thows ServerError;
                idempotent IObjectList getAvailableFamilies() thows ServerError;
                idempotent void setQuantumStategy(int bitResolution) throws ServerError;
                idempotent void setCodomainInteval(int start, int end) throws ServerError;
                idempotent omeo::model::QuantumDef getQuantumDef() throws ServerError;
                idempotent void setQuantizationMap(int w, omeo::model::Family fam, double coefficient, bool noiseReduction) throws ServerError;
                idempotent omeo::model::Family getChannelFamily(int w) throws ServerError;
                idempotent bool getChannelNoiseReduction(int w) thows ServerError;
                idempotent Ice::DoubleSeq getChannelStats(int w) thows ServerError;
                idempotent double getChannelCuveCoefficient(int w) throws ServerError;
                idempotent void setChannelWindow(int w, double stat, double end) throws ServerError;
                idempotent double getChannelWindowStat(int w) throws ServerError;
                idempotent double getChannelWindowEnd(int w) thows ServerError;
                idempotent void setRGBA(int w, int ed, int green, int blue, int alpha) throws ServerError;
                idempotent Ice::IntSeq getRGBA(int w) thows ServerError;
                idempotent void setActive(int w, bool active) thows ServerError;
                idempotent bool isActive(int w) thows ServerError;
                idempotent void setChannelLookupTable(int w, sting lookup) throws ServerError;
                idempotent sting getChannelLookupTable(int w) throws ServerError;
                void addCodomainMap(omeo::romio::CodomainMapContext mapCtx) throws ServerError;
                void updateCodomainMap(omeo::romio::CodomainMapContext mapCtx) throws ServerError;
                void emoveCodomainMap(omero::romio::CodomainMapContext mapCtx) throws ServerError;
                void saveCurentSettings() throws ServerError;
                long saveAsNewSettings() thows ServerError;
                long esetDefaultSettings(bool save) throws ServerError;
                idempotent void setCompessionLevel(float percentage) throws ServerError;
                idempotent float getCompessionLevel() throws ServerError;
                idempotent bool isPixelsTypeSigned() thows ServerError;
                idempotent double getPixelsTypeUppeBound(int w) throws ServerError;
                idempotent double getPixelsTypeLoweBound(int w) throws ServerError;

            };
    };
};

#endif
