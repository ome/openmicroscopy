/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_RENDERINGENGINE_ICE
#define OMERO_API_RENDERINGENGINE_ICE

#include <omero/ModelF.ice>
#include <omero/Collections.ice>
#include <omero/ROMIO.ice>
#include <omero/Constants.ice>
#include <omero/api/PyramidService.ice>

module omero {

    module api {

        /**
         * See <a href="http://downloads.openmicroscopy.org/latest/omero5.2/api/omeis/providers/re/RenderingEngine.html">RenderingEngine.html</a>
         **/
        ["ami", "amd"] interface RenderingEngine extends PyramidService
            {
                idempotent omero::romio::RGBBuffer render(omero::romio::PlaneDef def) throws ServerError;
                idempotent Ice::IntSeq renderAsPackedInt(omero::romio::PlaneDef def) throws ServerError;
                idempotent Ice::IntSeq renderProjectedAsPackedInt(omero::constants::projection::ProjectionType algorithm, int timepoint, int stepping, int start, int end) throws ServerError;
                idempotent Ice::ByteSeq renderCompressed(omero::romio::PlaneDef def) throws ServerError;
                idempotent Ice::ByteSeq renderProjectedCompressed(omero::constants::projection::ProjectionType algorithm, int timepoint, int stepping, int start, int end) throws ServerError;
                idempotent long getRenderingDefId() throws ServerError;
                idempotent void lookupPixels(long pixelsId) throws ServerError;
                idempotent bool lookupRenderingDef(long pixelsId) throws ServerError;
                idempotent void loadRenderingDef(long renderingDefId) throws ServerError;
                ["deprecated: use omero::romio::PlaneDefWithMasks instead"] idempotent void setOverlays(omero::RLong tablesId, omero::RLong imageId, LongIntMap rowColorMap) throws ServerError;
                idempotent void load() throws ServerError;
                idempotent void setModel(omero::model::RenderingModel model) throws ServerError;
                idempotent omero::model::RenderingModel getModel() throws ServerError;
                idempotent int getDefaultZ() throws ServerError;
                idempotent int getDefaultT() throws ServerError;
                idempotent void setDefaultZ(int z) throws ServerError;
                idempotent void setDefaultT(int t) throws ServerError;
                idempotent omero::model::Pixels getPixels() throws ServerError;
                idempotent IObjectList getAvailableModels() throws ServerError;
                idempotent IObjectList getAvailableFamilies() throws ServerError;
                idempotent void setQuantumStrategy(int bitResolution) throws ServerError;
                idempotent void setCodomainInterval(int start, int end) throws ServerError;
                idempotent omero::model::QuantumDef getQuantumDef() throws ServerError;
                idempotent void setQuantizationMap(int w, omero::model::Family fam, double coefficient, bool noiseReduction) throws ServerError;
                idempotent omero::model::Family getChannelFamily(int w) throws ServerError;
                idempotent bool getChannelNoiseReduction(int w) throws ServerError;
                idempotent Ice::DoubleSeq getChannelStats(int w) throws ServerError;
                idempotent double getChannelCurveCoefficient(int w) throws ServerError;
                idempotent void setChannelWindow(int w, double start, double end) throws ServerError;
                idempotent double getChannelWindowStart(int w) throws ServerError;
                idempotent double getChannelWindowEnd(int w) throws ServerError;
                idempotent void setRGBA(int w, int red, int green, int blue, int alpha) throws ServerError;
                idempotent Ice::IntSeq getRGBA(int w) throws ServerError;
                idempotent void setActive(int w, bool active) throws ServerError;
                idempotent bool isActive(int w) throws ServerError;
                idempotent void setChannelLookupTable(int w, string lookup) throws ServerError;
                idempotent string getChannelLookupTable(int w) throws ServerError;
                void addCodomainMap(omero::romio::CodomainMapContext mapCtx) throws ServerError;
                void updateCodomainMap(omero::romio::CodomainMapContext mapCtx) throws ServerError;
                void removeCodomainMap(omero::romio::CodomainMapContext mapCtx) throws ServerError;
                void saveCurrentSettings() throws ServerError;
                long saveAsNewSettings() throws ServerError;
                long resetDefaultSettings(bool save) throws ServerError;
                idempotent void setCompressionLevel(float percentage) throws ServerError;
                idempotent float getCompressionLevel() throws ServerError;
                idempotent bool isPixelsTypeSigned() throws ServerError;
                idempotent double getPixelsTypeUpperBound(int w) throws ServerError;
                idempotent double getPixelsTypeLowerBound(int w) throws ServerError;

            };
    };
};

#endif
