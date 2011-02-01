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
    
    private final int sizeX;
    
    private final int sizeY;
    
    private final int sizeZ;
    
    private final int sizeC;
    
    private final int sizeT;
    
    private final int pixelType;
    
    private final int rowSize;

    private final int planeSize;

    private final int stackSize;
    
    private final int timepointSize;
        
    
    public BfPixelsStoreI(String path) throws IOException, FormatException {
        this.path = path;
        reader.setId(path);
        
        /* Get some data that is widely used elsewhere.
         * As there are no setters this is reasonable here.
         * Could this be done using a getMetadata method in the reader?
         */
        sizeX = reader.getSizeX();
        sizeY = reader.getSizeY();
        sizeZ = reader.getSizeZ();
        sizeC = reader.getSizeC();
        sizeT = reader.getSizeT();
        pixelType = reader.getPixelType();
        rowSize = sizeX * getBytesPerPixel(pixelType);
        planeSize = sizeY * rowSize;
        stackSize = sizeZ * planeSize;
        timepointSize = sizeC * stackSize;
    }

    public void calculateMessageDigest_async(
            AMD_RawPixelsStore_calculateMessageDigest __cb, Current __current)
            throws ServerError {
        throw new UnsupportedOperationException("NYI");

    }

    public void getByteWidth_async(AMD_RawPixelsStore_getByteWidth __cb,
            Current __current) throws ServerError {
        try {
            __cb.ice_response(getBytesPerPixel(pixelType));
        } catch (Exception e) {
            __cb.ice_exception(e);
        }

    }

    public void getHypercube_async(AMD_RawPixelsStore_getHypercube __cb,
            List<Integer> offset, List<Integer> size, List<Integer> step, 
            Current __current) throws ServerError {
        /* Assuming XYZCT order. 
         * Initial pass to get 5d cube of contiguous pixels only. 
         *      treating step[0..4] as 1
         */
        int planeNumber;
        int cubeOffset = 0;
        
        if( size.get(0) < 1 ||  size.get(1) < 1 ||  size.get(2) < 1 
                ||  size.get(3) < 1 ||  size.get(4) < 1 )
        {
            throw new IllegalArgumentException("Invalid step size: steps sizes must be 1 or greater");
        }
        
        try {
            int tileRowSize = size.get(0)*getBytesPerPixel(pixelType);
            int tileColSize = size.get(1)*getBytesPerPixel(pixelType);
            int tileSize = tileRowSize*size.get(1);
            int cubeSize = tileSize*size.get(2)*size.get(3)*size.get(4);
            byte[] cube = new byte[cubeSize];
            byte[] tile = new byte[tileSize];        
            byte[] tileRow = new byte[tileRowSize];        
            byte[] tileCol = new byte[tileColSize];        
            for(int t = offset.get(4); t < size.get(4); t += step.get(4))
            {
                for(int c = offset.get(3); c < size.get(3); c += step.get(3))
                {
                    for(int z = offset.get(2); z < size.get(2); z += step.get(2))
                    {
                        planeNumber = reader.getIndex(z, c, t);
                        if(step.get(0) == 1 && step.get(1) == 1)
                        {
                            reader.openBytes(planeNumber, tile, 
                                    offset.get(0), offset.get(1), size.get(0), size.get(1));
                            System.arraycopy(tile, 0, cube, cubeOffset, tileSize);
                            cubeOffset += tileSize;
                        } 
                        else if(step.get(0) == 1 && step.get(1) > 1)
                        {
                            for(int y = offset.get(1); y < size.get(1); y += step.get(1))
                            {
                                reader.openBytes(planeNumber, tileRow, 
                                        offset.get(0), y, size.get(0), 1);
                                System.arraycopy(tileRow, 0, cube, cubeOffset, tileRowSize);
                                cubeOffset += tileRowSize;
                            }
                        }
                        else if(step.get(0) > 1 && step.get(1) == 1)
                        {
                            for(int x = offset.get(0); x < size.get(0); x += step.get(0))
                            {
                                reader.openBytes(planeNumber, tileRow, 
                                        x, offset.get(1), 1, size.get(1));
                                System.arraycopy(tileCol, 0, cube, cubeOffset, tileColSize);
                                cubeOffset += tileColSize;
                            }
                        }
                        else
                        {
                            throw new IllegalArgumentException("Invalid step size: X step and Y step cannot both be 1");
                        }
                    }
                }
            }
            byte[] returnCube = new byte[cubeOffset];
            System.arraycopy(cube, 0, returnCube, 0, cubeOffset);
            __cb.ice_response(returnCube);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }
    
    public void getCol_async(AMD_RawPixelsStore_getCol __cb, int x, int z,
            int c, int t, Current __current) throws ServerError {
        try {
            int colSize = sizeX * getBytesPerPixel(pixelType);
            byte[] col = new byte[colSize];
            int planeNumber = reader.getIndex(z, c, t);
            reader.openBytes(planeNumber, col, x, 0, 1, colSize);
            __cb.ice_response(col);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getPlaneOffset_async(AMD_RawPixelsStore_getPlaneOffset __cb,
            int z, int c, int t, Current __current) throws ServerError {
        try {
            long offset = planeOffset(z,c,t);
            __cb.ice_response(offset);
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
            __cb.ice_response(planeSize);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }

    }

    public void getPlane_async(AMD_RawPixelsStore_getPlane __cb, int z, int c,
            int t, Current __current) throws ServerError {
        try {
            byte[] plane = new byte[planeSize];
            int planeNumber = reader.getIndex(z, c, t);
            reader.openBytes(planeNumber, plane);
            __cb.ice_response(plane);
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
        try {
            long offset = rowOffset(y,z,c,t);
            __cb.ice_response(offset);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }
    
    public void getRowSize_async(AMD_RawPixelsStore_getRowSize __cb,
            Current __current) throws ServerError {
        try {
            __cb.ice_response(rowSize);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getRow_async(AMD_RawPixelsStore_getRow __cb, int y, int z,
            int c, int t, Current __current) throws ServerError {
        try {
            if(!(y < sizeY)) {
                throw new IllegalArgumentException("Invalid Y index: " + y + "/" + sizeY);
            }
            byte[] row = new byte[rowSize];
            int planeNumber = reader.getIndex(z, c, t);
            reader.openBytes(planeNumber, row, 0, y, rowSize, 1);
            __cb.ice_response(row);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getStackOffset_async(AMD_RawPixelsStore_getStackOffset __cb,
            int c, int t, Current __current) throws ServerError {
        try {
            long offset = stackOffset(c,t);
            __cb.ice_response(offset);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getStackSize_async(AMD_RawPixelsStore_getStackSize __cb,
            Current __current) throws ServerError {
        try {
            __cb.ice_response(stackSize);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getStack_async(AMD_RawPixelsStore_getStack __cb, int c, int t,
            Current __current) throws ServerError {
        try {
            byte[] stack = new byte[stackSize];
            getWholeStack(c,t,stack);
            __cb.ice_response(stack);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getTimepointOffset_async(
            AMD_RawPixelsStore_getTimepointOffset __cb, int t, Current __current)
            throws ServerError {
        try {
            long offset = timepointOffset(t);
            __cb.ice_response(offset);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getTimepointSize_async(
            AMD_RawPixelsStore_getTimepointSize __cb, Current __current)
            throws ServerError {
        try {
            __cb.ice_response(timepointSize);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getTimepoint_async(AMD_RawPixelsStore_getTimepoint __cb, int t,
            Current __current) throws ServerError {
        try {
            byte[] timepoint = new byte[timepointSize];
            getWholeTimepoint(t,timepoint);
            __cb.ice_response(timepoint);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void getTotalSize_async(AMD_RawPixelsStore_getTotalSize __cb,
            Current __current) throws ServerError {
        try {
            int totalSize = sizeT * timepointSize;
            __cb.ice_response(totalSize);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void isFloat_async(AMD_RawPixelsStore_isFloat __cb, Current __current)
            throws ServerError {
        try {
            boolean floatPixelType = isFloatPixelType(pixelType);
            __cb.ice_response(floatPixelType);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void isSigned_async(AMD_RawPixelsStore_isSigned __cb,
            Current __current) throws ServerError {
        try {
            boolean signedPixelType = isSignedPixelType(pixelType);
            __cb.ice_response(signedPixelType);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void prepare_async(AMD_RawPixelsStore_prepare __cb,
            List<Long> pixelsIds, Current __current) throws ServerError {
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
            reader.close(true);
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

    /**
     * Helper methods
     */
    
    /*
     * Calculate offsets
     */
    private long timepointOffset(int t) throws IllegalArgumentException {
        if(!(t < sizeT)) {
            throw new IllegalArgumentException("Invalid T index: " + t + "/" + sizeT);
        }
        return t * timepointSize;
    }

    private long stackOffset(int c, int t) throws IllegalArgumentException {
        if(!(c < sizeC)) {
            throw new IllegalArgumentException("Invalid C index: " + c + "/" + sizeC);
        }
        return timepointOffset(t) + c * stackSize;
    }

    private long planeOffset(int z, int c, int t) throws IllegalArgumentException {
        if(!(z < sizeZ)) {
            throw new IllegalArgumentException("Invalid Z index: " + z + "/" + sizeZ);
        }
        return stackOffset(c,t) + z * planeSize;
    }

    private long rowOffset(int y, int z, int c, int t) throws IllegalArgumentException {
        if(!(y < sizeY)) {
            throw new IllegalArgumentException("Invalid Y index: " + y + "/" + sizeY);
        }
        return planeOffset(z,c,t) + y * rowSize;
    }

    /*
     * Get multiple planes
     */
    
    private byte[] getWholeStack(int c, int t, byte[] stack) throws IOException, FormatException {
        byte[] plane = new byte[planeSize];        
        for(int z = 0; z < sizeZ; z++)
        {
            int planeNumber = reader.getIndex(z, c, t);
            reader.openBytes(planeNumber, plane);
            System.arraycopy(plane, 0, stack, z*planeSize, planeSize);
        }
        return stack;
    }
    
    private byte[] getWholeTimepoint(int t, byte[] timepoint) throws IOException, FormatException {
        byte[] stack = new byte[stackSize];      
        for(int c = 0; c < sizeC; c++)
        {
            getWholeStack(c, t, stack);
            System.arraycopy(stack, 0, timepoint, c*stackSize, stackSize);
        }
        return timepoint;
    }
    
    
    /**
     * cgb - stolen from ImportLibrary - is there a better way to do this?
     * 
     * Retrieves how many bytes per pixel the current plane or section has.
     * @return the number of bytes per pixel.
     */
    private int getBytesPerPixel(int type)
    {
        switch(type) {
            case 0:
            case 1:
                return 1;  // INT8 or UINT8
            case 2:
            case 3:
                return 2;  // INT16 or UINT16
            case 4:
            case 5:
            case 6:
                return 4;  // INT32, UINT32 or FLOAT
            case 7:
                return 8;  // DOUBLE
        }
        throw new RuntimeException("Unknown type with id: '" + type + "'");
    }

    /**
     * cgb - based on above - is there a better way to do this?
     * 
     * Retrieves how many bytes per pixel the current plane or section has.
     * @return the number of bytes per pixel.
     */
    private boolean isSignedPixelType(int type)
    {
        switch(type) {
            case 0:
            case 2:
            case 4:
            case 6:
            case 7:
                return true; // INT8, INT16, INT32, FLOAT or DOUBLE
            case 1:
            case 3:
            case 5:
                return false;  // UINT8, UINT16 or UINT32
        }
        throw new RuntimeException("Unknown type with id: '" + type + "'");
    }
    
    /**
     * cgb - based on above - is there a better way to do this?
     * 
     * Retrieves how many bytes per pixel the current plane or section has.
     * @return the number of bytes per pixel.
     */
    private boolean isFloatPixelType(int type)
    {
        switch(type) {
            case 6:
            case 7:
                return true; // FLOAT or DOUBLE
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return false;  // INT8, UINT8, INT16, UINT16, INT32 or UINT32
        }
        throw new RuntimeException("Unknown type with id: '" + type + "'");
    }

    
}
