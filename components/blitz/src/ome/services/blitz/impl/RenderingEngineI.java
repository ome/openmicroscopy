/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import ome.services.blitz.util.BlitzExecutor;
import omeis.providers.re.RenderingEngine;
import omero.ServerError;
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
import omero.api.AMD_RenderingEngine_setQuantizationMap;
import omero.api.AMD_RenderingEngine_setQuantumStrategy;
import omero.api.AMD_RenderingEngine_setRGBA;
import omero.api.AMD_RenderingEngine_updateCodomainMap;
import omero.api.AMD_StatefulServiceInterface_close;
import omero.api.AMD_StatefulServiceInterface_getCurrentEventContext;
import omero.api._RenderingEngineOperations;
import omero.model.Family;
import omero.model.RenderingModel;
import omero.romio.CodomainMapContext;
import omero.romio.PlaneDef;
import Ice.Current;

/**
 * Implementation of the RenderingEngine service.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see omeis.providers.re.RenderingEngine
 */
public class RenderingEngineI extends AbstractAmdServant implements
        _RenderingEngineOperations {

    public RenderingEngineI(RenderingEngine service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void addCodomainMap_async(AMD_RenderingEngine_addCodomainMap __cb,
            CodomainMapContext mapCtx, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, mapCtx);

    }

    public void getAvailableFamilies_async(
            AMD_RenderingEngine_getAvailableFamilies __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getAvailableModels_async(
            AMD_RenderingEngine_getAvailableModels __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getChannelCurveCoefficient_async(
            AMD_RenderingEngine_getChannelCurveCoefficient __cb, int w,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, w);

    }

    public void getChannelFamily_async(
            AMD_RenderingEngine_getChannelFamily __cb, int w, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, w);

    }

    public void getChannelNoiseReduction_async(
            AMD_RenderingEngine_getChannelNoiseReduction __cb, int w,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, w);

    }

    public void getChannelStats_async(AMD_RenderingEngine_getChannelStats __cb,
            int w, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, w);

    }

    public void getChannelWindowEnd_async(
            AMD_RenderingEngine_getChannelWindowEnd __cb, int w,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, w);

    }

    public void getChannelWindowStart_async(
            AMD_RenderingEngine_getChannelWindowStart __cb, int w,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, w);

    }

    public void getCompressionLevel_async(
            AMD_RenderingEngine_getCompressionLevel __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getDefaultT_async(AMD_RenderingEngine_getDefaultT __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getDefaultZ_async(AMD_RenderingEngine_getDefaultZ __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getModel_async(AMD_RenderingEngine_getModel __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getPixelsTypeLowerBound_async(
            AMD_RenderingEngine_getPixelsTypeLowerBound __cb, int w,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, w);

    }

    public void getPixelsTypeUpperBound_async(
            AMD_RenderingEngine_getPixelsTypeUpperBound __cb, int w,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, w);

    }

    public void getPixels_async(AMD_RenderingEngine_getPixels __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getQuantumDef_async(AMD_RenderingEngine_getQuantumDef __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getRGBA_async(AMD_RenderingEngine_getRGBA __cb, int w,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, w);

    }

    public void isActive_async(AMD_RenderingEngine_isActive __cb, int w,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, w);

    }

    public void isPixelsTypeSigned_async(
            AMD_RenderingEngine_isPixelsTypeSigned __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void loadRenderingDef_async(
            AMD_RenderingEngine_loadRenderingDef __cb, long renderingDefId,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, renderingDefId);

    }

    public void load_async(AMD_RenderingEngine_load __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void lookupPixels_async(AMD_RenderingEngine_lookupPixels __cb,
            long pixelsId, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, pixelsId);

    }

    public void lookupRenderingDef_async(
            AMD_RenderingEngine_lookupRenderingDef __cb, long pixelsId,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, pixelsId);

    }

    public void removeCodomainMap_async(
            AMD_RenderingEngine_removeCodomainMap __cb,
            CodomainMapContext mapCtx, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, mapCtx);

    }

    public void renderAsPackedInt_async(
            AMD_RenderingEngine_renderAsPackedInt __cb, PlaneDef def,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, def);

    }

    public void renderCompressed_async(
            AMD_RenderingEngine_renderCompressed __cb, PlaneDef def,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, def);

    }

    public void renderProjectedAsPackedInt_async(
            AMD_RenderingEngine_renderProjectedAsPackedInt __cb, int algorithm,
            int timepoint, int stepping, int start, int end, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, algorithm, timepoint, stepping,
                start, end);

    }

    public void renderProjectedCompressed_async(
            AMD_RenderingEngine_renderProjectedCompressed __cb, int algorithm,
            int timepoint, int stepping, int start, int end, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, algorithm, timepoint, stepping,
                start, end);

    }

    public void render_async(AMD_RenderingEngine_render __cb, PlaneDef def,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, def);

    }

    public void resetDefaultsNoSave_async(
            AMD_RenderingEngine_resetDefaultsNoSave __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void resetDefaults_async(AMD_RenderingEngine_resetDefaults __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void saveCurrentSettings_async(
            AMD_RenderingEngine_saveCurrentSettings __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void setActive_async(AMD_RenderingEngine_setActive __cb, int w,
            boolean active, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, w, active);

    }

    public void setChannelWindow_async(
            AMD_RenderingEngine_setChannelWindow __cb, int w, double start,
            double end, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, w, start, end);

    }

    public void setCodomainInterval_async(
            AMD_RenderingEngine_setCodomainInterval __cb, int start, int end,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, start, end);

    }

    public void setCompressionLevel_async(
            AMD_RenderingEngine_setCompressionLevel __cb, float percentage,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, percentage);

    }

    public void setDefaultT_async(AMD_RenderingEngine_setDefaultT __cb, int t,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, t);

    }

    public void setDefaultZ_async(AMD_RenderingEngine_setDefaultZ __cb, int z,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, z);

    }

    public void setModel_async(AMD_RenderingEngine_setModel __cb,
            RenderingModel model, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, model);

    }

    public void setQuantizationMap_async(
            AMD_RenderingEngine_setQuantizationMap __cb, int w, Family fam,
            double coefficient, boolean noiseReduction, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, w, fam, coefficient,
                noiseReduction);

    }

    public void setQuantumStrategy_async(
            AMD_RenderingEngine_setQuantumStrategy __cb, int bitResolution,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, bitResolution);

    }

    public void setRGBA_async(AMD_RenderingEngine_setRGBA __cb, int w, int red,
            int green, int blue, int alpha, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, w, red, green, blue, alpha);

    }

    public void updateCodomainMap_async(
            AMD_RenderingEngine_updateCodomainMap __cb,
            CodomainMapContext mapCtx, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, mapCtx);

    }

    public void close_async(AMD_StatefulServiceInterface_close __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getCurrentEventContext_async(
            AMD_StatefulServiceInterface_getCurrentEventContext __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

}
