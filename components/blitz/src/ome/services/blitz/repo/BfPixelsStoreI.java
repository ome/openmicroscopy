/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import java.io.IOException;
import java.util.List;

import loci.formats.FormatException;
import loci.formats.ImageReader;
import omero.ServerError;
import omero.api.AMD_RawPixelsStore_calculateMessageDigest;
import omero.api.AMD_RawPixelsStore_getByteWidth;
import omero.api.AMD_RawPixelsStore_getCol;
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
import omero.api.AMD_RawPixelsStore_prepare;
import omero.api.AMD_RawPixelsStore_setPixelsId;
import omero.api.AMD_RawPixelsStore_setPlane;
import omero.api.AMD_RawPixelsStore_setRegion;
import omero.api.AMD_RawPixelsStore_setRow;
import omero.api.AMD_RawPixelsStore_setStack;
import omero.api.AMD_RawPixelsStore_setTimepoint;
import omero.api.AMD_StatefulServiceInterface_activate;
import omero.api.AMD_StatefulServiceInterface_close;
import omero.api.AMD_StatefulServiceInterface_getCurrentEventContext;
import omero.api.AMD_StatefulServiceInterface_passivate;
import omero.api._RawPixelsStoreDisp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;

/**
 * 
 * @since Beta4.1
 */
public class BfPixelsStoreI extends _RawPixelsStoreDisp {

    private final static Log log = LogFactory.getLog(BfPixelsStoreI.class);

    private final ImageReader reader = new ImageReader();

    private final String path;

    public BfPixelsStoreI(String path) throws IOException, FormatException {
        this.path = path;
        reader.setId(path);
    }

    public void calculateMessageDigest_async(
            AMD_RawPixelsStore_calculateMessageDigest __cb, Current __current)
            throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getByteWidth_async(AMD_RawPixelsStore_getByteWidth __cb,
            Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getCol_async(AMD_RawPixelsStore_getCol __cb, int x, int z,
            int c, int t, Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getPlaneOffset_async(AMD_RawPixelsStore_getPlaneOffset __cb,
            int z, int c, int t, Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getPlaneRegion_async(AMD_RawPixelsStore_getPlaneRegion __cb,
            int z, int c, int t, int size, int offset, Current __current)
            throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getPlaneSize_async(AMD_RawPixelsStore_getPlaneSize __cb,
            Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getPlane_async(AMD_RawPixelsStore_getPlane __cb, int z, int c,
            int t, Current __current) throws ServerError {
        try {
            int planeNumber = reader.getIndex(z, c, t);
            byte[] buf = reader.openBytes(planeNumber);
            __cb.ice_response(buf);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }

    }

    public void getRegion_async(AMD_RawPixelsStore_getRegion __cb, int size,
            long offset, Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getRowOffset_async(AMD_RawPixelsStore_getRowOffset __cb, int y,
            int z, int c, int t, Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getRowSize_async(AMD_RawPixelsStore_getRowSize __cb,
            Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getRow_async(AMD_RawPixelsStore_getRow __cb, int y, int z,
            int c, int t, Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getStackOffset_async(AMD_RawPixelsStore_getStackOffset __cb,
            int c, int t, Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getStackSize_async(AMD_RawPixelsStore_getStackSize __cb,
            Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getStack_async(AMD_RawPixelsStore_getStack __cb, int c, int t,
            Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getTimepointOffset_async(
            AMD_RawPixelsStore_getTimepointOffset __cb, int t, Current __current)
            throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getTimepointSize_async(
            AMD_RawPixelsStore_getTimepointSize __cb, Current __current)
            throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getTimepoint_async(AMD_RawPixelsStore_getTimepoint __cb, int t,
            Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getTotalSize_async(AMD_RawPixelsStore_getTotalSize __cb,
            Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void isFloat_async(AMD_RawPixelsStore_isFloat __cb, Current __current)
            throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void isSigned_async(AMD_RawPixelsStore_isSigned __cb,
            Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void prepare_async(AMD_RawPixelsStore_prepare __cb,
            List<Long> pixelsIds, Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void setPixelsId_async(AMD_RawPixelsStore_setPixelsId __cb,
            long pixelsId, boolean bypassOriginalFile, Current __current)
            throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void setPlane_async(AMD_RawPixelsStore_setPlane __cb, byte[] buf,
            int z, int c, int t, Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void setRegion_async(AMD_RawPixelsStore_setRegion __cb, int size,
            long offset, byte[] buffer, Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void setRow_async(AMD_RawPixelsStore_setRow __cb, byte[] buf, int y,
            int z, int c, int t, Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void setStack_async(AMD_RawPixelsStore_setStack __cb, byte[] buf,
            int z, int c, int t, Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void setTimepoint_async(AMD_RawPixelsStore_setTimepoint __cb,
            byte[] buf, int t, Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void activate_async(AMD_StatefulServiceInterface_activate __cb,
            Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void close_async(AMD_StatefulServiceInterface_close __cb,
            Current __current) throws ServerError {
        
        try {
            reader.close(true);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
        throw new UnsupportedOperationException("NYI");

    }

    public void getCurrentEventContext_async(
            AMD_StatefulServiceInterface_getCurrentEventContext __cb,
            Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void passivate_async(AMD_StatefulServiceInterface_passivate __cb,
            Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

}