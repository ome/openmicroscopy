/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

// Java imports
import java.util.List;

import ome.api.IRenderingSettings;
import ome.services.blitz.util.BlitzExecutor;
import omero.ServerError;
import omero.api.AMD_IRenderingSettings_applySettingsToCategory;
import omero.api.AMD_IRenderingSettings_applySettingsToDataset;
import omero.api.AMD_IRenderingSettings_applySettingsToImage;
import omero.api.AMD_IRenderingSettings_applySettingsToPixels;
import omero.api.AMD_IRenderingSettings_applySettingsToProject;
import omero.api.AMD_IRenderingSettings_applySettingsToSet;
import omero.api.AMD_IRenderingSettings_createNewRenderingDef;
import omero.api.AMD_IRenderingSettings_getRenderingSettings;
import omero.api.AMD_IRenderingSettings_resetDefaults;
import omero.api.AMD_IRenderingSettings_resetDefaultsInCategory;
import omero.api.AMD_IRenderingSettings_resetDefaultsInDataset;
import omero.api.AMD_IRenderingSettings_resetDefaultsInImage;
import omero.api.AMD_IRenderingSettings_resetDefaultsInSet;
import omero.api.AMD_IRenderingSettings_resetDefaultsNoSave;
import omero.api.AMD_IRenderingSettings_sanityCheckPixels;
import omero.api.AMD_IRenderingSettings_setOriginalSettingsInDataset;
import omero.api.AMD_IRenderingSettings_setOriginalSettingsInImage;
import omero.api.AMD_IRenderingSettings_setOriginalSettingsInSet;
import omero.api._IRenderingSettingsOperations;
import omero.model.IObject;
import omero.model.Pixels;
import omero.model.RenderingDef;
import Ice.Current;

/**
 * Implementation of the IRenderingSettings service.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.IRenderingSettings
 */
public class RenderingSettingsI extends AbstractAmdServant implements
        _IRenderingSettingsOperations {

    public RenderingSettingsI(IRenderingSettings service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void applySettingsToCategory_async(
            AMD_IRenderingSettings_applySettingsToCategory __cb, long from,
            long to, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, from, to);

    }

    public void applySettingsToDataset_async(
            AMD_IRenderingSettings_applySettingsToDataset __cb, long from,
            long to, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, from, to);

    }

    public void applySettingsToImage_async(
            AMD_IRenderingSettings_applySettingsToImage __cb, long from,
            long to, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, from, to);

    }

    public void applySettingsToPixels_async(
            AMD_IRenderingSettings_applySettingsToPixels __cb, long from,
            long to, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, from, to);

    }

    public void applySettingsToProject_async(
            AMD_IRenderingSettings_applySettingsToProject __cb, long from,
            long to, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, from, to);

    }

    public void applySettingsToSet_async(
            AMD_IRenderingSettings_applySettingsToSet __cb, long from,
            String toType, List<IObject> to, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, from, toType, to);

    }

    public void createNewRenderingDef_async(
            AMD_IRenderingSettings_createNewRenderingDef __cb, Pixels pixels,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, pixels);

    }

    public void getRenderingSettings_async(
            AMD_IRenderingSettings_getRenderingSettings __cb, long pixelsId,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, pixelsId);

    }

    public void resetDefaultsInCategory_async(
            AMD_IRenderingSettings_resetDefaultsInCategory __cb,
            long categoryId, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, categoryId);

    }

    public void resetDefaultsInDataset_async(
            AMD_IRenderingSettings_resetDefaultsInDataset __cb, long dataSetId,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, dataSetId);

    }

    public void resetDefaultsInImage_async(
            AMD_IRenderingSettings_resetDefaultsInImage __cb, long imageId,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, imageId);

    }

    public void resetDefaultsInSet_async(
            AMD_IRenderingSettings_resetDefaultsInSet __cb, String type,
            List<Long> noteIds, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, type, noteIds);

    }

    public void resetDefaultsNoSave_async(
            AMD_IRenderingSettings_resetDefaultsNoSave __cb, RenderingDef def,
            Pixels pixels, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, def, pixels);

    }

    public void resetDefaults_async(AMD_IRenderingSettings_resetDefaults __cb,
            RenderingDef def, Pixels pixels, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, def, pixels);

    }

    public void sanityCheckPixels_async(
            AMD_IRenderingSettings_sanityCheckPixels __cb, Pixels from,
            Pixels to, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, from, to);

    }

    public void setOriginalSettingsInDataset_async(
            AMD_IRenderingSettings_setOriginalSettingsInDataset __cb,
            long dataSetId, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, dataSetId);

    }

    public void setOriginalSettingsInImage_async(
            AMD_IRenderingSettings_setOriginalSettingsInImage __cb,
            long imageId, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, imageId);

    }

    public void setOriginalSettingsInSet_async(
            AMD_IRenderingSettings_setOriginalSettingsInSet __cb, String type,
            List<Long> noteIds, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, type, noteIds);

    }

}
