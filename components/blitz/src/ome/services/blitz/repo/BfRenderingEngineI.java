/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import java.util.Map;

import omero.RLong;
import omero.ServerError;
import omero.api.AMD_PyramidService_getResolutionLevel;
import omero.api.AMD_PyramidService_getResolutionLevels;
import omero.api.AMD_PyramidService_getTileSize;
import omero.api.AMD_PyramidService_hasPixelsPyramid;
import omero.api.AMD_PyramidService_setResolutionLevel;
import omero.api.AMD_RenderingEngine_addCodomainMap;
import omero.api.AMD_RenderingEngine_getAvailableFamilies;
import omero.api.AMD_RenderingEngine_getAvailableModels;
import omero.api.AMD_RenderingEngine_getChannelCurveCoefficient;
import omero.api.AMD_RenderingEngine_getChannelFamily;
import omero.api.AMD_RenderingEngine_getChannelNoiseReduction;
import omero.api.AMD_RenderingEngine_getChannelStats;
import omero.api.AMD_RenderingEngine_getChannelWindowEnd;
import omero.api.AMD_RenderingEngine_getChannelWindowStart;
import omero.api.AMD_RenderingEngine_getCompressionLevel;
import omero.api.AMD_RenderingEngine_getDefaultT;
import omero.api.AMD_RenderingEngine_getDefaultZ;
import omero.api.AMD_RenderingEngine_getModel;
import omero.api.AMD_RenderingEngine_getPixels;
import omero.api.AMD_RenderingEngine_getPixelsTypeLowerBound;
import omero.api.AMD_RenderingEngine_getPixelsTypeUpperBound;
import omero.api.AMD_RenderingEngine_getQuantumDef;
import omero.api.AMD_RenderingEngine_getRGBA;
import omero.api.AMD_RenderingEngine_isActive;
import omero.api.AMD_RenderingEngine_isPixelsTypeSigned;
import omero.api.AMD_RenderingEngine_load;
import omero.api.AMD_RenderingEngine_loadRenderingDef;
import omero.api.AMD_RenderingEngine_lookupPixels;
import omero.api.AMD_RenderingEngine_lookupRenderingDef;
import omero.api.AMD_RenderingEngine_removeCodomainMap;
import omero.api.AMD_RenderingEngine_render;
import omero.api.AMD_RenderingEngine_renderAsPackedInt;
import omero.api.AMD_RenderingEngine_renderAsPackedIntAsRGBA;
import omero.api.AMD_RenderingEngine_renderCompressed;
import omero.api.AMD_RenderingEngine_renderProjectedAsPackedInt;
import omero.api.AMD_RenderingEngine_renderProjectedCompressed;
import omero.api.AMD_RenderingEngine_resetDefaults;
import omero.api.AMD_RenderingEngine_resetDefaultsNoSave;
import omero.api.AMD_RenderingEngine_saveCurrentSettings;
import omero.api.AMD_RenderingEngine_setActive;
import omero.api.AMD_RenderingEngine_setChannelWindow;
import omero.api.AMD_RenderingEngine_setCodomainInterval;
import omero.api.AMD_RenderingEngine_setCompressionLevel;
import omero.api.AMD_RenderingEngine_setDefaultT;
import omero.api.AMD_RenderingEngine_setDefaultZ;
import omero.api.AMD_RenderingEngine_setModel;
import omero.api.AMD_RenderingEngine_setOverlays;
import omero.api.AMD_RenderingEngine_setQuantizationMap;
import omero.api.AMD_RenderingEngine_setQuantumStrategy;
import omero.api.AMD_RenderingEngine_setRGBA;
import omero.api.AMD_RenderingEngine_updateCodomainMap;
import omero.api.AMD_StatefulServiceInterface_activate;
import omero.api.AMD_StatefulServiceInterface_close;
import omero.api.AMD_StatefulServiceInterface_getCurrentEventContext;
import omero.api.AMD_StatefulServiceInterface_passivate;
import omero.api._RenderingEngineDisp;
import omero.constants.projection.ProjectionType;
import omero.model.Family;
import omero.model.RenderingModel;
import omero.romio.CodomainMapContext;
import omero.romio.PlaneDef;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;

/**
 *
 * @since Beta4.1
 */
public class BfRenderingEngineI extends _RenderingEngineDisp {

    private final static Log log = LogFactory.getLog(BfRenderingEngineI.class);

    public BfRenderingEngineI() {

    }

    public void addCodomainMap_async(AMD_RenderingEngine_addCodomainMap __cb,
            CodomainMapContext mapCtx, Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getAvailableFamilies_async(
            AMD_RenderingEngine_getAvailableFamilies __cb, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getAvailableModels_async(
            AMD_RenderingEngine_getAvailableModels __cb, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getChannelCurveCoefficient_async(
            AMD_RenderingEngine_getChannelCurveCoefficient __cb, int w,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getChannelFamily_async(
            AMD_RenderingEngine_getChannelFamily __cb, int w, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getChannelNoiseReduction_async(
            AMD_RenderingEngine_getChannelNoiseReduction __cb, int w,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getChannelStats_async(AMD_RenderingEngine_getChannelStats __cb,
            int w, Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getChannelWindowEnd_async(
            AMD_RenderingEngine_getChannelWindowEnd __cb, int w,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getChannelWindowStart_async(
            AMD_RenderingEngine_getChannelWindowStart __cb, int w,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getCompressionLevel_async(
            AMD_RenderingEngine_getCompressionLevel __cb, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getDefaultT_async(AMD_RenderingEngine_getDefaultT __cb,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getDefaultZ_async(AMD_RenderingEngine_getDefaultZ __cb,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getModel_async(AMD_RenderingEngine_getModel __cb,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getPixelsTypeLowerBound_async(
            AMD_RenderingEngine_getPixelsTypeLowerBound __cb, int w,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getPixelsTypeUpperBound_async(
            AMD_RenderingEngine_getPixelsTypeUpperBound __cb, int w,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getPixels_async(AMD_RenderingEngine_getPixels __cb,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getQuantumDef_async(AMD_RenderingEngine_getQuantumDef __cb,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getRGBA_async(AMD_RenderingEngine_getRGBA __cb, int w,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void isActive_async(AMD_RenderingEngine_isActive __cb, int w,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void isPixelsTypeSigned_async(
            AMD_RenderingEngine_isPixelsTypeSigned __cb, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub

    }

    public void loadRenderingDef_async(
            AMD_RenderingEngine_loadRenderingDef __cb, long renderingDefId,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void setOverlays_async(AMD_RenderingEngine_setOverlays __cb,
		RLong tableId, RLong imageId, Map<Long, Integer> rowColorMap,
		Current __current) throws ServerError {
	// TODO Auto-generated method stub

    }

    public void load_async(AMD_RenderingEngine_load __cb, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub

    }

    public void lookupPixels_async(AMD_RenderingEngine_lookupPixels __cb,
            long pixelsId, Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void lookupRenderingDef_async(
            AMD_RenderingEngine_lookupRenderingDef __cb, long pixelsId,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void removeCodomainMap_async(
            AMD_RenderingEngine_removeCodomainMap __cb,
            CodomainMapContext mapCtx, Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void renderAsPackedIntAsRGBA_async(
            AMD_RenderingEngine_renderAsPackedIntAsRGBA __cb, PlaneDef def,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void renderAsPackedInt_async(
            AMD_RenderingEngine_renderAsPackedInt __cb, PlaneDef def,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void renderCompressed_async(
            AMD_RenderingEngine_renderCompressed __cb, PlaneDef def,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void renderProjectedAsPackedInt_async(
            AMD_RenderingEngine_renderProjectedAsPackedInt __cb,
            ProjectionType algorithm, int timepoint, int stepping, int start,
            int end, Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void renderProjectedCompressed_async(
            AMD_RenderingEngine_renderProjectedCompressed __cb,
            ProjectionType algorithm, int timepoint, int stepping, int start,
            int end, Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void render_async(AMD_RenderingEngine_render __cb, PlaneDef def,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void resetDefaultsNoSave_async(
            AMD_RenderingEngine_resetDefaultsNoSave __cb, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub

    }

    public void resetDefaults_async(AMD_RenderingEngine_resetDefaults __cb,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void saveCurrentSettings_async(
            AMD_RenderingEngine_saveCurrentSettings __cb, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub

    }

    public void setActive_async(AMD_RenderingEngine_setActive __cb, int w,
            boolean active, Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void setChannelWindow_async(
            AMD_RenderingEngine_setChannelWindow __cb, int w, double start,
            double end, Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void setCodomainInterval_async(
            AMD_RenderingEngine_setCodomainInterval __cb, int start, int end,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void setCompressionLevel_async(
            AMD_RenderingEngine_setCompressionLevel __cb, float percentage,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void setDefaultT_async(AMD_RenderingEngine_setDefaultT __cb, int t,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void setDefaultZ_async(AMD_RenderingEngine_setDefaultZ __cb, int z,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void setModel_async(AMD_RenderingEngine_setModel __cb,
            RenderingModel model, Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void setQuantizationMap_async(
            AMD_RenderingEngine_setQuantizationMap __cb, int w, Family fam,
            double coefficient, boolean noiseReduction, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub

    }

    public void setQuantumStrategy_async(
            AMD_RenderingEngine_setQuantumStrategy __cb, int bitResolution,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void setRGBA_async(AMD_RenderingEngine_setRGBA __cb, int w, int red,
            int green, int blue, int alpha, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub

    }

    public void updateCodomainMap_async(
            AMD_RenderingEngine_updateCodomainMap __cb,
            CodomainMapContext mapCtx, Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void activate_async(AMD_StatefulServiceInterface_activate __cb,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void close_async(AMD_StatefulServiceInterface_close __cb,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void getCurrentEventContext_async(
            AMD_StatefulServiceInterface_getCurrentEventContext __cb,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    public void passivate_async(AMD_StatefulServiceInterface_passivate __cb,
            Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see omero.api._PyramidServiceOperations#getResolutionLevels_async(omero.api.AMD_PyramidService_getResolutionLevels, Ice.Current)
     */
    public void getResolutionLevels_async(
            AMD_PyramidService_getResolutionLevels __cb, Current __current)
            throws ServerError
    {
    }

    /* (non-Javadoc)
     * @see omero.api._PyramidServiceOperations#getTileSize_async(omero.api.AMD_PyramidService_getTileSize, Ice.Current)
     */
    public void getTileSize_async(AMD_PyramidService_getTileSize __cb,
            Current __current) throws ServerError
    {
    }

    /* (non-Javadoc)
     * @see omero.api._PyramidServiceOperations#hasPixelsPyramid_async(omero.api.AMD_PyramidService_hasPixelsPyramid, Ice.Current)
     */
    public void hasPixelsPyramid_async(AMD_PyramidService_hasPixelsPyramid __cb,
            Current __current) throws ServerError
    {
    }

    /* (non-Javadoc)
     * @see omero.api._PyramidServiceOperations#setResolutionLevel_async(omero.api.AMD_PyramidService_setResolutionLevel, int, Ice.Current)
     */
    public void setResolutionLevel_async(
            AMD_PyramidService_setResolutionLevel __cb, int resolutionLevel,
            Current __current) throws ServerError
    {
    }

    /* (non-Javadoc)
     * @see omero.api._PyramidServiceOperations#getResolutionLevel_async(omero.api.AMD_PyramidService_getResolutionLevel, Ice.Current)
     */
    public void getResolutionLevel_async(
            AMD_PyramidService_getResolutionLevel __cb, Current __current)
            throws ServerError
    {
    }
}