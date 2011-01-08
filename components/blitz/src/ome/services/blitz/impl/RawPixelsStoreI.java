/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.List;

import ome.api.RawPixelsStore;
import ome.services.blitz.util.BlitzExecutor;
import omero.ServerError;
import omero.api.AMD_RawPixelsStore_calculateMessageDigest;
import omero.api.AMD_RawPixelsStore_getByteWidth;
import omero.api.AMD_RawPixelsStore_getCol;
import omero.api.AMD_RawPixelsStore_getHypercube;
import omero.api.AMD_RawPixelsStore_getPixelsId;
import omero.api.AMD_RawPixelsStore_getPlane;
import omero.api.AMD_RawPixelsStore_getPlaneOffset;
import omero.api.AMD_RawPixelsStore_getPlaneRegion;
import omero.api.AMD_RawPixelsStore_getPlaneSize;
import omero.api.AMD_RawPixelsStore_getRegion;
import omero.api.AMD_RawPixelsStore_getRow;
import omero.api.AMD_RawPixelsStore_getRowOffset;
import omero.api.AMD_RawPixelsStore_getRowSize;
import omero.api.AMD_RawPixelsStore_getStack;
import omero.api.AMD_RawPixelsStore_getStackOffset;
import omero.api.AMD_RawPixelsStore_getStackSize;
import omero.api.AMD_RawPixelsStore_getTimepoint;
import omero.api.AMD_RawPixelsStore_getTimepointOffset;
import omero.api.AMD_RawPixelsStore_getTimepointSize;
import omero.api.AMD_RawPixelsStore_getTotalSize;
import omero.api.AMD_RawPixelsStore_isFloat;
import omero.api.AMD_RawPixelsStore_isSigned;
import omero.api.AMD_RawPixelsStore_setPixelsId;
import omero.api.AMD_RawPixelsStore_prepare;
import omero.api.AMD_RawPixelsStore_setPlane;
import omero.api.AMD_RawPixelsStore_setRegion;
import omero.api.AMD_RawPixelsStore_setRow;
import omero.api.AMD_RawPixelsStore_setStack;
import omero.api.AMD_RawPixelsStore_setTimepoint;
import omero.api.AMD_StatefulServiceInterface_activate;
import omero.api.AMD_StatefulServiceInterface_close;
import omero.api.AMD_StatefulServiceInterface_passivate;
import omero.api.AMD_StatefulServiceInterface_getCurrentEventContext;
import omero.api._RawPixelsStoreOperations;
import Ice.Current;

/**
 * Implementation of the RawPixelsStore service.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.RawPixelsStore
 */
public class RawPixelsStoreI extends AbstractAmdServant implements
        _RawPixelsStoreOperations {

    public RawPixelsStoreI(RawPixelsStore service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void calculateMessageDigest_async(
            AMD_RawPixelsStore_calculateMessageDigest __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getByteWidth_async(AMD_RawPixelsStore_getByteWidth __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getPlaneOffset_async(AMD_RawPixelsStore_getPlaneOffset __cb,
            int z, int c, int t, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, z, c, t);

    }

    public void getPlaneRegion_async(AMD_RawPixelsStore_getPlaneRegion __cb,
            int z, int c, int t, int size, int offset, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, z, c, t, size, offset);

    }

    public void getPlaneSize_async(AMD_RawPixelsStore_getPlaneSize __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getPlane_async(AMD_RawPixelsStore_getPlane __cb, int z, int c,
            int t, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, z, c, t);

    }

    public void getHypercube_async(AMD_RawPixelsStore_getHypercube __cb,
            List<Integer> offset, List<Integer> size, List<Integer> step, 
            Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");
    }
    
    public void getRegion_async(AMD_RawPixelsStore_getRegion __cb, int size,
            long offset, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, size, offset);

    }

    public void getRowOffset_async(AMD_RawPixelsStore_getRowOffset __cb, int y,
            int z, int c, int t, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, y, z, c, t);

    }

    public void getRowSize_async(AMD_RawPixelsStore_getRowSize __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getRow_async(AMD_RawPixelsStore_getRow __cb, int y, int z,
            int c, int t, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, y, z, c, t);

    }

    public void getStackOffset_async(AMD_RawPixelsStore_getStackOffset __cb,
            int c, int t, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, c, t);

    }

    public void getStackSize_async(AMD_RawPixelsStore_getStackSize __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getStack_async(AMD_RawPixelsStore_getStack __cb, int c, int t,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, c, t);

    }

    public void getTimepointOffset_async(
            AMD_RawPixelsStore_getTimepointOffset __cb, int t, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, t);

    }

    public void getTimepointSize_async(
            AMD_RawPixelsStore_getTimepointSize __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getTimepoint_async(AMD_RawPixelsStore_getTimepoint __cb, int t,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, t);

    }

    public void getTotalSize_async(AMD_RawPixelsStore_getTotalSize __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void isFloat_async(AMD_RawPixelsStore_isFloat __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void isSigned_async(AMD_RawPixelsStore_isSigned __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void setPixelsId_async(AMD_RawPixelsStore_setPixelsId __cb,
            long pixelsId, boolean bypassOriginalFile, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, pixelsId, bypassOriginalFile);

    }

    public void getPixelsId_async(AMD_RawPixelsStore_getPixelsId __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }
    
	public void prepare_async(AMD_RawPixelsStore_prepare __cb,
            List<Long> pixelsIds, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, pixelsIds);

    }

    public void setPlane_async(AMD_RawPixelsStore_setPlane __cb, byte[] buf,
            int z, int c, int t, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, buf, z, c, t);

    }

    public void setRegion_async(AMD_RawPixelsStore_setRegion __cb, int size,
            long offset, byte[] buffer, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, size, offset, buffer);

    }

    public void setRow_async(AMD_RawPixelsStore_setRow __cb, byte[] buf, int y,
            int z, int c, int t, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, buf, y, z, c, t);

    }

    public void setStack_async(AMD_RawPixelsStore_setStack __cb, byte[] buf,
            int z, int c, int t, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, buf, z, c, t);

    }

    public void setTimepoint_async(AMD_RawPixelsStore_setTimepoint __cb,
            byte[] buf, int t, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, buf, t);

    }

    public void getCol_async(AMD_RawPixelsStore_getCol __cb, int x, int z,
            int c, int t, Current __current) throws ServerError
    {
        callInvokerOnRawArgs(__cb, __current, x, z, c, t);

    }

}
