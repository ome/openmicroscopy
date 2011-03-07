/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import java.io.IOException;
import java.io.Serializable;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import loci.common.DataTools;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelData;
import ome.model.enums.PixelsType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @since Beta4.1
 */
public class BfPixelBuffer implements PixelBuffer, Serializable {

    private final static Log log = LogFactory.getLog(BfPixelBuffer.class);

    //private final ImageReader reader = new ImageReader();
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

    private final Integer rowSize;

    private final Integer colSize;

    private final Integer planeSize;

    private final Integer stackSize;
    
    private final Integer timepointSize;    
    
    /**
     * We may want a constructor that takes the id of an imported file
     * or that takes a File object? 
     * There should ultimately be some sort of check here that the 
     * file is in a/the repository.
     */
    public BfPixelBuffer(String path) throws IOException, FormatException {
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
        colSize = sizeY * pixelSize;
        planeSize = sizeY * rowSize;
        stackSize = sizeZ * planeSize;
        timepointSize = sizeC * stackSize;
    }
    
    public byte[] calculateMessageDigest() throws IOException {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "Required SHA-1 message digest algorithm unavailable.");
        }

        for (int t = 0; t < sizeT; t++) {
            try {
                ByteBuffer buffer = getTimepoint(t).getData();
                md.update(buffer);
            } catch (DimensionsOutOfBoundsException e) {
                throw new RuntimeException(e);
            }
        }

        return md.digest();
    }

    public void checkBounds(Integer x, Integer y, Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        if (x != null && (x > sizeX - 1 || x < 0)) {
            throw new DimensionsOutOfBoundsException("X '" + x
                    + "' greater than sizeX '" + getSizeX() + "'.");
        }
        if (y != null && (y > sizeY - 1 || y < 0)) {
            throw new DimensionsOutOfBoundsException("Y '" + y
                    + "' greater than sizeY '" + getSizeY() + "'.");
        }

        if (z != null && (z > sizeZ - 1 || z < 0)) {
            throw new DimensionsOutOfBoundsException("Z '" + z
                    + "' greater than sizeZ '" + getSizeZ() + "'.");
        }

        if (c != null && (c > sizeC - 1 || c < 0)) {
            throw new DimensionsOutOfBoundsException("C '" + c
                    + "' greater than sizeC '" + getSizeC() + "'.");
        }

        if (t != null && (t > sizeT - 1 || t < 0)) {
            throw new DimensionsOutOfBoundsException("T '" + t
                    + "' greater than sizeT '" + getSizeT() + "'.");
        }
    }

    public void close() throws IOException {
        // TODO Auto-generated method stub
        
    }

    public int getByteWidth() {
        return pixelSize;
    }

    public PixelData getCol(Integer x, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        checkBounds(x, null, z, c, t);
        PixelData d;
        try {
            byte[] buffer = new byte[colSize];
            getWholeCol(x,z,c,t,buffer);
            d = new PixelData(getPixelsType(pixelType), ByteBuffer.wrap(buffer));
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return d;
    }

    public byte[] getColDirect(Integer x, Integer z, Integer c, Integer t,
            byte[] buffer) throws IOException, DimensionsOutOfBoundsException {
        checkBounds(x, null, z, c, t);
        try {
            if (buffer.length != colSize)
                throw new RuntimeException("Buffer size incorrect.");
            getWholeCol(x,z,c,t,buffer);
            swapIfRequired(buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public Integer getColSize() {
        return colSize;
    }

    public long getId() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getPath() {
        return path;
    }

    public PixelData getPlane(Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        checkBounds(null, null, z, c, t);
        PixelData d;
        try {
            byte[] buffer = new byte[planeSize];
            getWholePlane(z,c,t,buffer);
            d = new PixelData(getPixelsType(pixelType), ByteBuffer.wrap(buffer));
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return d;
    }

    public byte[] getPlaneDirect(Integer z, Integer c, Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException {
        checkBounds(null, null, z, c, t);
        try {
            if (buffer.length != planeSize)
                throw new RuntimeException("Buffer size incorrect.");
            getWholePlane(z,c,t,buffer);
            swapIfRequired(buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public Long getPlaneOffset(Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, null, z, c, t);
        return (long) getStackOffset(c,t) + z * planeSize;
    }

    public byte[] getPlaneRegionDirect(Integer z, Integer c, Integer t,
            Integer count, Integer offset, byte[] buffer) throws IOException,
            DimensionsOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    public Integer getPlaneSize() {
        return planeSize;
    }

    public PixelData getRegion(Integer size, Long offset) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] getRegionDirect(Integer size, Long offset, byte[] buffer)
            throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public PixelData getRow(Integer y, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        checkBounds(null, y, z, c, t);
        PixelData d;
        try {
            byte[] buffer = new byte[rowSize];
            getWholeRow(y,z,c,t,buffer);
            d = new PixelData(getPixelsType(pixelType), ByteBuffer.wrap(buffer));
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return d;
    }

    public byte[] getRowDirect(Integer y, Integer z, Integer c, Integer t,
            byte[] buffer) throws IOException, DimensionsOutOfBoundsException {
        checkBounds(null, y, z, c, t);
        try {
            if (buffer.length != rowSize)
                throw new RuntimeException("Buffer size incorrect.");
            getWholeRow(y,z,c,t,buffer);
            swapIfRequired(buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public Long getRowOffset(Integer y, Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, y, z, c, t);
        return (long) getPlaneOffset(z,c,t) + y * rowSize;
    }

    public Integer getRowSize() {
        return rowSize;
    }

    public int getSizeC() {
        return sizeC;
    }

    public int getSizeT() {
        return sizeT;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    public PixelData getStack(Integer c, Integer t) throws IOException,
            DimensionsOutOfBoundsException {
        checkBounds(null, null, null, c, t);
        PixelData d;
        try {
            byte[] buffer = new byte[colSize];
            getWholeStack(c,t,buffer);
            d = new PixelData(getPixelsType(pixelType), ByteBuffer.wrap(buffer));
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return d;
    }

    public byte[] getStackDirect(Integer c, Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException {
        checkBounds(null, null, null, c, t);
        try {
            if (buffer.length != stackSize)
                throw new RuntimeException("Buffer size incorrect.");
            getWholeStack(c,t,buffer);
            swapIfRequired(buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public Long getStackOffset(Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, null, null, c, t);
        return (long) getTimepointOffset(t) + c * stackSize;
    }

    public Integer getStackSize() {
        return stackSize;
    }

    public PixelData getTimepoint(Integer t) throws IOException,
            DimensionsOutOfBoundsException {
        checkBounds(null, null, null, null, t);
        PixelData d;
        try {
            byte[] buffer = new byte[timepointSize];
            getWholeTimepoint(t,buffer);
            d = new PixelData(getPixelsType(pixelType), ByteBuffer.wrap(buffer));
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return d;
    }

    public byte[] getTimepointDirect(Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException {
        checkBounds(null, null, null, null, t);
        try {
            if (buffer.length != timepointSize)
                throw new RuntimeException("Buffer size incorrect.");
            getWholeTimepoint(t,buffer);
            swapIfRequired(buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public Long getTimepointOffset(Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, null, null, null, t);
        return (long) t * timepointSize;
    }

    public Integer getTimepointSize() {
        return timepointSize;
    }

    public Integer getTotalSize() {
        return sizeT * timepointSize;
    }

    public boolean isFloat() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isSigned() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setPlane(ByteBuffer buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
throw new UnsupportedOperationException("Cannot write to repository");
        
    }

    public void setPlane(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
throw new UnsupportedOperationException("Cannot write to repository");
        
    }

    public void setRegion(Integer size, Long offset, byte[] buffer)
            throws IOException, BufferOverflowException {
throw new UnsupportedOperationException("Cannot write to repository");
        
    }

    public void setRegion(Integer size, Long offset, ByteBuffer buffer)
            throws IOException, BufferOverflowException {
throw new UnsupportedOperationException("Cannot write to repository");
        
    }

    public void setRow(ByteBuffer buffer, Integer y, Integer z, Integer c,
            Integer t) throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
throw new UnsupportedOperationException("Cannot write to repository");
        
    }

    public void setStack(ByteBuffer buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
throw new UnsupportedOperationException("Cannot write to repository");
        
    }

    public void setStack(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
        throw new UnsupportedOperationException("Cannot write to repository");
        
    }

    public void setTimepoint(ByteBuffer buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException, BufferOverflowException {
        throw new UnsupportedOperationException("Cannot write to repository");
        
    }

    public void setTimepoint(byte[] buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException, BufferOverflowException {
        throw new UnsupportedOperationException("Cannot write to repository");
        
    }

    public PixelData getHypercube(List<Integer> offset, List<Integer> size, 
            List<Integer> step) throws IOException, DimensionsOutOfBoundsException 
    {
        // only works for 5d at present
        checkBounds(offset.get(0),offset.get(1),offset.get(2),offset.get(3),offset.get(4));
        checkBounds(offset.get(0)+size.get(0),offset.get(1)+size.get(1),
                offset.get(2)+size.get(2),offset.get(3)+size.get(3),offset.get(4)+size.get(4));
        PixelData d;
        try {
            int tStripes = (size.get(4) + step.get(4) - 1) / step.get(4);
            int cStripes = (size.get(3) + step.get(3) - 1) / step.get(3);
            int zStripes = (size.get(2) + step.get(2) - 1) / step.get(2);
            int yStripes = (size.get(1) + step.get(1) - 1) / step.get(1);
            int xStripes = (size.get(0) + step.get(0) - 1) / step.get(0);
            int tileRowSize = pixelSize * xStripes;
            int cubeSize = tileRowSize * yStripes * zStripes * cStripes * tStripes;
            byte[] buffer = new byte[timepointSize];
            getWholeHypercube(offset,size,step,buffer);
            d = new PixelData(getPixelsType(pixelType), ByteBuffer.wrap(buffer));
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return d;
    }

    public byte[] getHypercubeDirect(List<Integer> offset, List<Integer> size, 
            List<Integer> step, byte[] buffer) 
            throws IOException, DimensionsOutOfBoundsException 
    {
        // only works for 5d at present
        checkBounds(offset.get(0),offset.get(1),offset.get(2),offset.get(3),offset.get(4));
        checkBounds(offset.get(0)+size.get(0),offset.get(1)+size.get(1),
                offset.get(2)+size.get(2),offset.get(3)+size.get(3),offset.get(4)+size.get(4));
        try {
            int tStripes = (size.get(4) + step.get(4) - 1) / step.get(4);
            int cStripes = (size.get(3) + step.get(3) - 1) / step.get(3);
            int zStripes = (size.get(2) + step.get(2) - 1) / step.get(2);
            int yStripes = (size.get(1) + step.get(1) - 1) / step.get(1);
            int xStripes = (size.get(0) + step.get(0) - 1) / step.get(0);
            int tileRowSize = pixelSize * xStripes;
            int cubeSize = tileRowSize * yStripes * zStripes * cStripes * tStripes;
            if (buffer.length != cubeSize) //test is more complex.
                throw new RuntimeException("Buffer size incorrect.");
            getWholeHypercube(offset,size,step,buffer);
            swapIfRequired(buffer);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }
                
    public PixelData getPlaneRegion(Integer x, Integer y, Integer width,
            Integer height, Integer z, Integer c, Integer t, Integer stride)
            throws IOException, DimensionsOutOfBoundsException
            {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * Helper methods, duplicated in BfPixelsStoreI and elsewhere.
     *    - needs refactoring!
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
     * Get partial plane
     */   
    private byte[] getWholeRow(int y, int z, int c, int t, byte[] row) 
            throws IOException, FormatException 
    {
        byte[] plane = new byte[planeSize];
        getWholePlane(z,c,t,plane);
        System.arraycopy(plane, y*rowSize, row, 0, rowSize);
        return row;
    }

    /*
     * Get partial plane
     */   
    private byte[] getWholeCol(int x, int z, int c, int t, byte[] col) 
            throws IOException, FormatException 
    {
        byte[] plane = new byte[planeSize];        
        getWholePlane(z,c,t,plane);
        for(int y = 0; y < sizeY; y++) { 
            System.arraycopy(plane, (y*rowSize)+(x*pixelSize), 
                    col, y*pixelSize, pixelSize);
        }
        return col;
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
    
    /*
     * 
     */   
    private byte[] getWholeHypercube(List<Integer> offset, List<Integer> size, 
            List<Integer> step, byte[] cube) throws IOException, FormatException {
        int cubeOffset = 0;
        int xStripes = (size.get(0) + step.get(0) - 1) / step.get(0);
        int tileRowSize = pixelSize * xStripes;
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
        return cube;
    }
    
    /**
     * cgb - created from the methods below?
     * 
     * Retrieves how many bytes per pixel the current plane or section has.
     * @return the number of bytes per pixel.
     */
    private PixelsType getPixelsType(int type)
    {
        switch(type) {
            case 0: return new PixelsType("int8");
            case 1: return new PixelsType("uint8");
            case 2: return new PixelsType("int16");
            case 3: return new PixelsType("uint16");
            case 4: return new PixelsType("int32");
            case 5: return new PixelsType("uint32");
            case 6: return new PixelsType("float");
            case 7: return new PixelsType("double");
        }
        throw new RuntimeException("Unknown type with id: '" + type + "'");
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