/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.List;

import ome.api.ThumbnailStore;
import ome.services.blitz.util.BlitzExecutor;
import omero.RInt;
import omero.ServerError;
import omero.api.AMD_ThumbnailStore_createThumbnail;
import omero.api.AMD_ThumbnailStore_createThumbnails;
import omero.api.AMD_ThumbnailStore_createThumbnailsByLongestSideSet;
import omero.api.AMD_ThumbnailStore_getRenderingDefId;
import omero.api.AMD_ThumbnailStore_getThumbnail;
import omero.api.AMD_ThumbnailStore_getThumbnailByLongestSide;
import omero.api.AMD_ThumbnailStore_getThumbnailByLongestSideDirect;
import omero.api.AMD_ThumbnailStore_getThumbnailByLongestSideSet;
import omero.api.AMD_ThumbnailStore_getThumbnailDirect;
import omero.api.AMD_ThumbnailStore_getThumbnailForSectionByLongestSideDirect;
import omero.api.AMD_ThumbnailStore_getThumbnailForSectionDirect;
import omero.api.AMD_ThumbnailStore_getThumbnailSet;
import omero.api.AMD_ThumbnailStore_isInProgress;
import omero.api.AMD_ThumbnailStore_resetDefaults;
import omero.api.AMD_ThumbnailStore_setPixelsId;
import omero.api.AMD_ThumbnailStore_setRenderingDefId;
import omero.api.AMD_ThumbnailStore_thumbnailExists;
import omero.api._ThumbnailStoreOperations;

import Ice.Current;

/**
 * Implementation of the ThumbnailStore service.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.ThumbnailStore
 */
public class ThumbnailStoreI extends AbstractCloseableAmdServant implements
        _ThumbnailStoreOperations {

    public ThumbnailStoreI(ThumbnailStore service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void createThumbnail_async(AMD_ThumbnailStore_createThumbnail __cb,
            RInt sizeX, RInt sizeY, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, sizeX, sizeY);

    }

    public void createThumbnails_async(
            AMD_ThumbnailStore_createThumbnails __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }
    
    public void createThumbnailsByLongestSideSet_async(
            AMD_ThumbnailStore_createThumbnailsByLongestSideSet __cb, RInt size,
            List<Long> pixelsIds, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, size, pixelsIds);

    }

    public void getThumbnailByLongestSideDirect_async(
            AMD_ThumbnailStore_getThumbnailByLongestSideDirect __cb, RInt size,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, size);

    }

    public void getThumbnailByLongestSideSet_async(
            AMD_ThumbnailStore_getThumbnailByLongestSideSet __cb, RInt size,
            List<Long> pixelsIds, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, size, pixelsIds);

    }

    public void getThumbnailByLongestSide_async(
            AMD_ThumbnailStore_getThumbnailByLongestSide __cb, RInt size,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, size);

    }

    public void getThumbnailDirect_async(
            AMD_ThumbnailStore_getThumbnailDirect __cb, RInt sizeX, RInt sizeY,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, sizeX, sizeY);

    }

    public void getThumbnailForSectionByLongestSideDirect_async(
            AMD_ThumbnailStore_getThumbnailForSectionByLongestSideDirect __cb,
            int theZ, int theT, RInt size, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, theZ, theT, size);

    }

    public void getThumbnailForSectionDirect_async(
            AMD_ThumbnailStore_getThumbnailForSectionDirect __cb, int theZ,
            int theT, RInt sizeX, RInt sizeY, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, theZ, theT, sizeX, sizeY);

    }

    public void getThumbnailSet_async(AMD_ThumbnailStore_getThumbnailSet __cb,
            RInt sizeX, RInt sizeY, List<Long> pixelsIds, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, sizeX, sizeY, pixelsIds);

    }

    public void getThumbnail_async(AMD_ThumbnailStore_getThumbnail __cb,
            RInt sizeX, RInt sizeY, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, sizeX, sizeY);

    }

    public void resetDefaults_async(AMD_ThumbnailStore_resetDefaults __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void setPixelsId_async(AMD_ThumbnailStore_setPixelsId __cb,
            long pixelsId, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, pixelsId);

    }

    public void isInProgress_async(AMD_ThumbnailStore_isInProgress __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void setRenderingDefId_async(
            AMD_ThumbnailStore_setRenderingDefId __cb, long renderingDefId,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, renderingDefId);

    }

    public void thumbnailExists_async(AMD_ThumbnailStore_thumbnailExists __cb,
            RInt sizeX, RInt sizeY, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, sizeX, sizeY);

    }

    public void getRenderingDefId_async(AMD_ThumbnailStore_getRenderingDefId __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    //
    // Close logic
    //

    @Override
    protected void preClose(Current current) throws Throwable {
        // no-op
    }

    @Override
    protected void postClose(Current current) {
        // no-op
    }
}
