/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.List;

import loci.common.DataTools;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.Plane2D;
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
import omero.api.AMD_RawPixelsStore_prepare;
import omero.api.AMD_RawPixelsStore_save;
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

    private final BfPixelsWrapper reader;

    public BfPixelsStoreI(String path) throws IOException, FormatException {
        reader = new BfPixelsWrapper(path);
    }

    public void calculateMessageDigest_async(
            AMD_RawPixelsStore_calculateMessageDigest __cb, Current __current)
            throws ServerError {
        try {
            __cb.ice_response(reader.getMessageDigest());
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getByteWidth_async(AMD_RawPixelsStore_getByteWidth __cb,
            Current __current) throws ServerError {
        try {
            __cb.ice_response(reader.getByteWidth());
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getHypercube_async(AMD_RawPixelsStore_getHypercube __cb,
            List<Integer> offset, List<Integer> size, List<Integer> step,
            Current __current) throws ServerError {

        try {
            byte[] cube = new byte[reader.getCubeSize(offset,size,step)];
            reader.getHypercube(offset,size,step,cube);
            reader.swapIfRequired(cube);
            __cb.ice_response(cube);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getCol_async(AMD_RawPixelsStore_getCol __cb, int x, int z,
            int c, int t, Current __current) throws ServerError {
        try {
            byte[] col = new byte[reader.getColSize()];
            reader.getCol(x,z,c,t,col);
            reader.swapIfRequired(col);
            __cb.ice_response(col);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getPlaneOffset_async(AMD_RawPixelsStore_getPlaneOffset __cb,
            int z, int c, int t, Current __current) throws ServerError {
        try {
            __cb.ice_response(reader.getPlaneOffset(z,c,t));
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getPlaneRegion_async(AMD_RawPixelsStore_getPlaneRegion __cb,
            int z, int c, int t, int size, int offset, Current __current)
            throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getPlaneSize_async(AMD_RawPixelsStore_getPlaneSize __cb,
            Current __current) throws ServerError {
        try {
            __cb.ice_response(reader.getPlaneSize());
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getPlane_async(AMD_RawPixelsStore_getPlane __cb, int z, int c,
            int t, Current __current) throws ServerError {
        try {
            byte[] plane = new byte[reader.getPlaneSize()];
            reader.getPlane(z,c,t,plane);
            reader.swapIfRequired(plane);
            __cb.ice_response(plane);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getRegion_async(AMD_RawPixelsStore_getRegion __cb, int size,
            long offset, Current __current) throws ServerError {
        throw new UnsupportedOperationException(
                "Not yet supported, raise ticket to implement if required");
    }

    public void getRowOffset_async(AMD_RawPixelsStore_getRowOffset __cb, int y,
            int z, int c, int t, Current __current) throws ServerError {
        try {
            __cb.ice_response(reader.getRowOffset(y,z,c,t));
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getRowSize_async(AMD_RawPixelsStore_getRowSize __cb,
            Current __current) throws ServerError {
        try {
            __cb.ice_response(reader.getRowSize());
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getRow_async(AMD_RawPixelsStore_getRow __cb, int y, int z,
            int c, int t, Current __current) throws ServerError {
        try {
            byte[] row = new byte[reader.getRowSize()];
            reader.getRow(y,z,c,t,row);
            reader.swapIfRequired(row);
            __cb.ice_response(row);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getStackOffset_async(AMD_RawPixelsStore_getStackOffset __cb,
            int c, int t, Current __current) throws ServerError {
        try {
            __cb.ice_response(reader.getStackOffset(c,t));
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getStackSize_async(AMD_RawPixelsStore_getStackSize __cb,
            Current __current) throws ServerError {
        try {
            __cb.ice_response(reader.getStackSize());
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getStack_async(AMD_RawPixelsStore_getStack __cb, int c, int t,
            Current __current) throws ServerError {
        try {
            byte[] stack = new byte[reader.getStackSize()];
            reader.getStack(c,t,stack);
            reader.swapIfRequired(stack);
            __cb.ice_response(stack);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getTimepointOffset_async(
            AMD_RawPixelsStore_getTimepointOffset __cb, int t, Current __current)
            throws ServerError {
        try {
            __cb.ice_response(reader.getTimepointOffset(t));
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getTimepointSize_async(
            AMD_RawPixelsStore_getTimepointSize __cb, Current __current)
            throws ServerError {
        try {
            __cb.ice_response(reader.getTimepointSize());
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getTimepoint_async(AMD_RawPixelsStore_getTimepoint __cb, int t,
            Current __current) throws ServerError {
        try {
            byte[] timepoint = new byte[reader.getTimepointSize()];
            reader.getTimepoint(t,timepoint);
            reader.swapIfRequired(timepoint);
            __cb.ice_response(timepoint);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getTotalSize_async(AMD_RawPixelsStore_getTotalSize __cb,
            Current __current) throws ServerError {
        try {
            __cb.ice_response(reader.getTotalSize());
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void isFloat_async(AMD_RawPixelsStore_isFloat __cb, Current __current)
            throws ServerError {
        try {
            __cb.ice_response(reader.isFloat());
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void isSigned_async(AMD_RawPixelsStore_isSigned __cb,
            Current __current) throws ServerError {
        try {
            __cb.ice_response(reader.isSigned());
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void prepare_async(AMD_RawPixelsStore_prepare __cb,
            List<Long> pixelsIds, Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");
    }

    public void save_async(AMD_RawPixelsStore_save __cb, Current __current)
            throws ServerError {
        throw new UnsupportedOperationException("NYI");
    }

    public void setPixelsId_async(AMD_RawPixelsStore_setPixelsId __cb,
            long pixelsId, boolean bypassOriginalFile, Current __current)
            throws ServerError {
        throw new UnsupportedOperationException("Cannot write to repository");
    }

    public void getPixelsId_async(AMD_RawPixelsStore_getPixelsId __cb, Current __current)
            throws ServerError {
        throw new UnsupportedOperationException("NYI");
    }

    public void setPlane_async(AMD_RawPixelsStore_setPlane __cb, byte[] buf,
            int z, int c, int t, Current __current) throws ServerError {
        throw new UnsupportedOperationException("Cannot write to repository");
    }

    public void setRegion_async(AMD_RawPixelsStore_setRegion __cb, int size,
            long offset, byte[] buffer, Current __current) throws ServerError {
        throw new UnsupportedOperationException("Cannot write to repository");
    }

    public void setRow_async(AMD_RawPixelsStore_setRow __cb, byte[] buf, int y,
            int z, int c, int t, Current __current) throws ServerError {
        throw new UnsupportedOperationException("Cannot write to repository");
    }

    public void setStack_async(AMD_RawPixelsStore_setStack __cb, byte[] buf,
            int z, int c, int t, Current __current) throws ServerError {
        throw new UnsupportedOperationException("Cannot write to repository");
    }

    public void setTimepoint_async(AMD_RawPixelsStore_setTimepoint __cb,
            byte[] buf, int t, Current __current) throws ServerError {
        throw new UnsupportedOperationException("Cannot write to repository");
    }

    public void activate_async(AMD_StatefulServiceInterface_activate __cb,
            Current __current) throws ServerError {
        throw new UnsupportedOperationException("NYI");
    }

    public void close_async(AMD_StatefulServiceInterface_close __cb,
            Current __current) throws ServerError {
        try {
            reader.close();
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
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
