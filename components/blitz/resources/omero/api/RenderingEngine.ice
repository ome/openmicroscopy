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
         * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/omeis/re/providers/RenderingEngine.html">RenderingEngine.html</a>
         **/
        ["ami", "amd"] interface RenderingEngine extends PyramidService
            {
                omero::romio::RGBBuffer render(omero::romio::PlaneDef def) throws ServerError;
                Ice::IntSeq renderAsPackedInt(omero::romio::PlaneDef def) throws ServerError;
                /**
                 * The method provided here is deprecated in OMERO 4.3.
                 * <code>renderAsPackedInt</code> should be used instead.
                 **/
                ["deprecated:renderAsPackedIntAsRGBA() is deprecated"] Ice::IntSeq renderAsPackedIntAsRGBA(omero::romio::PlaneDef def) throws ServerError;
                Ice::IntSeq renderProjectedAsPackedInt(omero::constants::projection::ProjectionType algorithm, int timepoint, int stepping, int start, int end) throws ServerError;
                Ice::ByteSeq renderCompressed(omero::romio::PlaneDef def) throws ServerError;
                Ice::ByteSeq renderProjectedCompressed(omero::constants::projection::ProjectionType algorithm, int timepoint, int stepping, int start, int end) throws ServerError;
                long getRenderingDefId() throws ServerError;
                void lookupPixels(long pixelsId) throws ServerError;
                bool lookupRenderingDef(long pixelsId) throws ServerError;
                void loadRenderingDef(long renderingDefId) throws ServerError;
                void setOverlays(omero::RLong tablesId, omero::RLong imageId, LongIntMap rowColorMap) throws ServerError;
                void load() throws ServerError;
                void setModel(omero::model::RenderingModel model) throws ServerError;
                omero::model::RenderingModel getModel() throws ServerError;
                int getDefaultZ() throws ServerError;
                int getDefaultT() throws ServerError;
                void setDefaultZ(int z) throws ServerError;
                void setDefaultT(int t) throws ServerError;
                omero::model::Pixels getPixels() throws ServerError;
                IObjectList getAvailableModels() throws ServerError;
                IObjectList getAvailableFamilies() throws ServerError;
                void setQuantumStrategy(int bitResolution) throws ServerError;
                void setCodomainInterval(int start, int end) throws ServerError;
                omero::model::QuantumDef getQuantumDef() throws ServerError;
                void setQuantizationMap(int w, omero::model::Family fam, double coefficient, bool noiseReduction) throws ServerError;
                omero::model::Family getChannelFamily(int w) throws ServerError;
                bool getChannelNoiseReduction(int w) throws ServerError;
                Ice::DoubleSeq getChannelStats(int w) throws ServerError;
                double getChannelCurveCoefficient(int w) throws ServerError;
                void setChannelWindow(int w, double start, double end) throws ServerError;
                double getChannelWindowStart(int w) throws ServerError;
                double getChannelWindowEnd(int w) throws ServerError;
                void setRGBA(int w, int red, int green, int blue, int alpha) throws ServerError;
                Ice::IntSeq getRGBA(int w) throws ServerError;
                void setActive(int w, bool active) throws ServerError;
                bool isActive(int w) throws ServerError;
                void addCodomainMap(omero::romio::CodomainMapContext mapCtx) throws ServerError;
                void updateCodomainMap(omero::romio::CodomainMapContext mapCtx) throws ServerError;
                void removeCodomainMap(omero::romio::CodomainMapContext mapCtx) throws ServerError;
                void saveCurrentSettings() throws ServerError;
                long saveAsNewSettings() throws ServerError;
                ["deprecated:resetDefaults() is deprecated"] void resetDefaults() throws ServerError;
                ["deprecated:resetDefaultsNoSave() is deprecated"] void resetDefaultsNoSave() throws ServerError;
                ["deprecated:resetDefaultsSettings() is deprecated"] long resetDefaultsSettings(bool save) throws ServerError;
                long resetDefaultSettings(bool save) throws ServerError;
                void setCompressionLevel(float percentage) throws ServerError;
                float getCompressionLevel() throws ServerError;
                bool isPixelsTypeSigned() throws ServerError;
                double getPixelsTypeUpperBound(int w) throws ServerError;
                double getPixelsTypeLowerBound(int w) throws ServerError;

            };

    };
};

#endif
