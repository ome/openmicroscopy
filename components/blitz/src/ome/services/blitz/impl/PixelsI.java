/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.List;

import ome.api.IPixels;
import ome.services.blitz.util.BlitzExecutor;
import omero.RInt;
import omero.ServerError;
import omero.api.AMD_IPixels_copyAndResizeImage;
import omero.api.AMD_IPixels_copyAndResizePixels;
import omero.api.AMD_IPixels_createImage;
import omero.api.AMD_IPixels_getAllEnumerations;
import omero.api.AMD_IPixels_getBitDepth;
import omero.api.AMD_IPixels_getEnumeration;
import omero.api.AMD_IPixels_loadRndSettings;
import omero.api.AMD_IPixels_retrievePixDescription;
import omero.api.AMD_IPixels_retrieveAllRndSettings;
import omero.api.AMD_IPixels_retrieveRndSettings;
import omero.api.AMD_IPixels_retrieveRndSettingsFor;
import omero.api.AMD_IPixels_saveRndSettings;
import omero.api.AMD_IPixels_setChannelGlobalMinMax;
import omero.api._IPixelsOperations;
import omero.model.PixelsType;
import omero.model.RenderingDef;

import Ice.Current;

/**
 * Implementation of the IPixels service.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.IPixels
 */
public class PixelsI extends AbstractAmdServant implements _IPixelsOperations {

    public PixelsI(IPixels service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void copyAndResizeImage_async(AMD_IPixels_copyAndResizeImage __cb,
            long imageId, RInt sizeX, RInt sizeY, RInt sizeZ, RInt sizeT,
            List<Integer> channelList, String methodology, boolean copyStats,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, imageId, sizeX, sizeY, sizeZ,
                sizeT, channelList, methodology, copyStats);
    }

    public void copyAndResizePixels_async(AMD_IPixels_copyAndResizePixels __cb,
            long pixelsId, RInt sizeX, RInt sizeY, RInt sizeZ, RInt sizeT,
            List<Integer> channelList, String methodology, boolean copyStats,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, pixelsId, sizeX, sizeY, sizeZ,
                sizeT, channelList, methodology, copyStats);
    }

    public void createImage_async(AMD_IPixels_createImage __cb, int sizeX,
            int sizeY, int sizeZ, int sizeT, List<Integer> channelList,
            PixelsType pixelsType, String name, String description,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, sizeX, sizeY, sizeZ, sizeT,
                channelList, pixelsType, name, description);
    }

    public void getAllEnumerations_async(AMD_IPixels_getAllEnumerations __cb,
            String enumClass, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, enumClass);
    }

    public void getBitDepth_async(AMD_IPixels_getBitDepth __cb,
            PixelsType type, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, type);
    }

    public void getEnumeration_async(AMD_IPixels_getEnumeration __cb,
            String enumClass, String value, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, enumClass, value);
    }

    public void loadRndSettings_async(AMD_IPixels_loadRndSettings __cb,
            long renderingSettingsId, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, renderingSettingsId);
    }

    public void retrievePixDescription_async(
            AMD_IPixels_retrievePixDescription __cb, long pixId,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, pixId);
    }

    public void retrieveRndSettings_async(AMD_IPixels_retrieveRndSettings __cb,
            long pixId, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, pixId);
    }

    public void retrieveRndSettingsFor_async(AMD_IPixels_retrieveRndSettingsFor __cb,
            long pixId, long userId, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, pixId, userId);
    }
    
    public void retrieveAllRndSettings_async(AMD_IPixels_retrieveAllRndSettings __cb,
            long pixId, long userId, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, pixId, userId);
    }
    
    public void saveRndSettings_async(AMD_IPixels_saveRndSettings __cb,
            RenderingDef rndSettings, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, rndSettings);
    }

    public void setChannelGlobalMinMax_async(
            AMD_IPixels_setChannelGlobalMinMax __cb, long pixelsId,
            int channelIndex, double min, double max, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, pixelsId, channelIndex, min, max);
    }

}
