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

    private final OMEROWrapper reader;

    private final String path;
    
    private final int sizeX;
    
    private final int sizeY;
    
    private final int sizeZ;
    
    private final int sizeC;
    
    private final int sizeT;
    
    private final int rgbChannels;
    
    private final int pixelType;
    
    private final int pixelSize;

    private final int rowSize;

    private final int planeSize;

    private final int stackSize;
    
    private final int timepointSize;
        
    
    public BfPixelsStoreI(String path) throws IOException, FormatException {
        this.path = path;
        reader = new OMEROWrapper(new ImportConfig());
        reader.setId(path);
        
        /* Get some data that is widely used elsewhere.
         * As there are no setters this is reasonable here.
         */
        sizeX = reader.getSizeX();
        sizeY = reader.getSizeY();
        sizeZ = reader.getSizeZ();
        sizeC = reader.getSizeC();
        sizeT = reader.getSizeT();
        rgbChannels = reader.getRGBChannelCount();
        pixelType = reader.getPixelType();
        pixelSize = getBytesPerPixel(pixelType);
        rowSize = sizeX * pixelSize;
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
            __cb.ice_response(pixelSize);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }

    }

    public void getHypercube_async(AMD_RawPixelsStore_getHypercube __cb,
            List<Integer> offset, List<Integer> size, List<Integer> step, 
            Current __current) throws ServerError {
        if( size.get(0) < 1 ||  size.get(1) < 1 ||  size.get(2) < 1 
                ||  size.get(3) < 1 ||  size.get(4) < 1 )
        {
            throw new IllegalArgumentException("Invalid step size: steps sizes must be 1 or greater");
        }
        
        try {
            int cubeOffset = 0;
            int tStripes = (size.get(4) + step.get(4) - 1) / step.get(4);
            int cStripes = (size.get(3) + step.get(3) - 1) / step.get(3);
            int zStripes = (size.get(2) + step.get(2) - 1) / step.get(2);
            int yStripes = (size.get(1) + step.get(1) - 1) / step.get(1);
            int xStripes = (size.get(0) + step.get(0) - 1) / step.get(0);
            int tileRowSize = pixelSize * xStripes;
            int cubeSize = tileRowSize * yStripes * zStripes * cStripes * tStripes;
            byte[] cube = new byte[cubeSize];
            byte[] plane = new byte[planeSize];      
            for(int t = offset.get(4); t < size.get(4)+offset.get(4); t += step.get(4))
            {
                for(int c = offset.get(3); c < size.get(3)+offset.get(3); c += step.get(3))
                {
                    for(int z = offset.get(2); z < size.get(2)+offset.get(2); z += step.get(2))
                    {
                        getWholePlane(z,c,t,plane);
                        int rowOffset = offset.get(1)*rowSize;
                        if(step.get(0)==1)
                        {
                            int byteOffset = rowOffset + offset.get(0)*pixelSize;
                            for(int y = offset.get(1); y < size.get(1)+offset.get(1); y += step.get(1))
                            {
                                System.arraycopy(plane, byteOffset, cube, cubeOffset, tileRowSize);
                                cubeOffset += tileRowSize;
                                byteOffset += rowSize*step.get(1);
                            }
                        } 
                        else
                        {
                            for(int y = offset.get(1); y < size.get(1)+offset.get(1); y += step.get(1))
                            {
                                int byteOffset = offset.get(0)*pixelSize;
                                for(int x = offset.get(0); x < size.get(0)+offset.get(0); x += step.get(0))
                                {
                                    System.arraycopy(plane, rowOffset+byteOffset, cube, cubeOffset, pixelSize);
                                    cubeOffset += pixelSize;
                                    byteOffset += step.get(0)*pixelSize;
                                }
                                rowOffset += rowSize*step.get(1);
                            }
                        }
                        
                    }
                }
            }
            swapIfRequired(cube);
            __cb.ice_response(cube);
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }
    
    public void getCol_async(AMD_RawPixelsStore_getCol __cb, int x, int z,
            int c, int t, Current __current) throws ServerError {
        try {
            byte[] col = new byte[sizeX * pixelSize];
            byte[] plane = new byte[planeSize];
            getWholePlane(z,c,t,plane);
            for(int y = 0; y < sizeY; y++) { 
                System.arraycopy(plane, (y*rowSize)+(x*pixelSize), 
                        col, y*pixelSize, pixelSize);
            }
            swapIfRequired(col);
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
            getWholePlane(z,c,t,plane);
            swapIfRequired(plane);
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
            byte[] plane = new byte[planeSize];
            getWholePlane(z,c,t,plane);
            System.arraycopy(plane, y*rowSize, row, 0, rowSize);
            swapIfRequired(row);
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
            swapIfRequired(stack);
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
            swapIfRequired(timepoint);
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
     * Get a plane dealing with rgb/interleaving if necessary
     *     - using openPlane2d doesn't seem to work
     */
    /*
    private byte[] getWholePlane(int z, int c, int t, byte[] plane) 
            throws IOException, FormatException 
    {
        if(rgbChannels == 1) {
            int planeNumber = reader.getIndex(z, c, t);
            Plane2D plane2d = reader.openPlane2D(path, planeNumber, plane);
            plane = plane2d.getData().array(); 
        } else {
            // Separate channels as openPlane2D doesn't do this
            byte[] fullPlane = new byte[planeSize*rgbChannels];
            int planeNumber = reader.getIndex(z, 0, t);
            Plane2D plane2d = reader.openPlane2D(path, planeNumber, plane);
            fullPlane = plane2d.getData().array(); 
            System.arraycopy(fullPlane, c*planeSize, plane, 0, planeSize);
        }
        return plane;
    }
    */
    
    /*
     * Get a plane dealing with rgb/interleaving if necessary
     */
    private byte[] getWholePlane(int z, int c, int t, byte[] plane) 
            throws IOException, FormatException 
    {
        int planeNumber;
        if(rgbChannels == 1) {
            planeNumber = reader.getIndex(z, c, t);
            reader.openBytes(planeNumber, plane);
        } else {
            byte[] fullPlane = new byte[planeSize*rgbChannels];
            planeNumber = reader.getIndex(z, 0, t);
            reader.openBytes(planeNumber, fullPlane);
            if(reader.isInterleaved()) {
                for(int p = 0; p < planeSize; p += pixelSize) {
                    System.arraycopy(fullPlane, c*pixelSize + p*rgbChannels, 
                        plane, p, pixelSize);
                }
            } else {
                System.arraycopy(fullPlane, c*planeSize, plane, 0, planeSize);
            }
        }
        return plane;
    }
    
    
    /*
     * Get multiple planes
     */   
    private byte[] getWholeStack(int c, int t, byte[] stack) 
            throws IOException, FormatException 
    {
        byte[] plane = new byte[planeSize];        
        for(int z = 0; z < sizeZ; z++)
        {
            getWholePlane(z,c,t,plane);
            System.arraycopy(plane, 0, stack, z*planeSize, planeSize);
        }
        return stack;
    }
    
    /*
     * Get multiple stacks
     */   
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

    /**
     * cgb - stolen from ImportLibrary - slightly modified
     * 
     * Examines a byte array to see if it needs to be byte swapped and modifies
     * the byte array directly.
     * @param bytes The byte array to check and modify if required.
     * @return the <i>byteArray</i> either swapped or not for convenience.
     * @throws IOException if there is an error read from the file.
     * @throws FormatException if there is an error during metadata parsing.
     */
    private byte[] swapIfRequired(byte[] bytes)
        throws FormatException, IOException
    {
        // We've got nothing to do if the samples are only 8-bits wide.
        if (pixelSize == 1)
            return bytes;
        
        boolean isLittleEndian = reader.isLittleEndian();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int length;
        if (isLittleEndian) {
            if (pixelSize == 2) { // short/ushort
                ShortBuffer buf = buffer.asShortBuffer();
                length = buffer.limit() / 2;
                for (int i = 0; i < length; i++) {
                    buf.put(i, DataTools.swap(buf.get(i)));
                }
            } else if (pixelSize == 4) { // int/uint/float
                IntBuffer buf = buffer.asIntBuffer();
                length = buffer.limit() / 4;
                for (int i = 0; i < length; i++) {
                    buf.put(i, DataTools.swap(buf.get(i)));
                }
            } else if (pixelSize == 8) // long/double
            {
                LongBuffer buf = buffer.asLongBuffer();
                length = buffer.limit() / 8;
                for (int i = 0; i < length ; i++) {
                    buf.put(i, DataTools.swap(buf.get(i)));
                }
            } else {
                throw new FormatException(String.format(
                        "Unsupported sample bit width: %d", pixelSize));
            }
        }
        // We've got a big-endian file with a big-endian byte array.
        bytes = buffer.array();
        return bytes;
    } 
}
