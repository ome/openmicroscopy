/*
 * ome.io.nio.PixelBuffer
 *
 *   Copyright 2007-2013 Glencoe Software Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.nio;

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ome.conditions.ApiUsageException;
import ome.model.core.Pixels;
import ome.util.PixelData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class implementation of the PixelBuffer interface for standard "proprietary"
 * ROMIO/OMEIS data format.
 *
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:chris@glencoesoftware.com">chris@glencoesoftware.com</a>
 * @version $Revision$
 * @since 3.0
 * @see PixelBuffer
 */
public class RomioPixelBuffer extends AbstractBuffer implements PixelBuffer {

    /** The logger for this particular class */
    private static Logger log = LoggerFactory.getLogger(RomioPixelBuffer.class);

    /** Default maximum buffer size for planar data transfer. (1MB) */
    public static final int MAXIMUM_BUFFER_SIZE = 1048576;

    /** Reference to the pixels. */
    private Pixels pixels;

    private RandomAccessFile file;

    private FileChannel channel;

    /** The size of a row. */
    private Integer rowSize;
    
    /** The size of a column. */
    private Integer colSize;

    /** The size of a plane. */
    private Long planeSize;

    /** The size of a stack. */
    private Long stackSize;

    /** The size of a timepoint. */
    private Long timepointSize;

    /** The total size. */
    private Long totalSize;

    /**
     * Whether or not any of the mutators to write data can be called.
     * If not, an internal exception will be raised since it represents
     * a programming error.
     */
    private final boolean permitModification;

    /**
     * Creates a new instance. {@link #permitModification} defaults to false.
     * 
     * @param path The path to the file.
     * @param pixels The pixels object to handle.
     */
    public RomioPixelBuffer(String path, Pixels pixels) {
        this(path, pixels, false);
    }

    /**
     * Creates a new instance, with manual setting of {@link #permitModification}.
     *
     * @param path The path to the file.
     * @param pixels The pixels object to handle.
     */
    public RomioPixelBuffer(String path, Pixels pixels, boolean permitModification) {
        super(path);
        if (pixels == null) {
            throw new NullPointerException(
                    "Expecting a not-null pixels element.");
        }
        this.pixels = pixels;
        this.permitModification = permitModification;
    }

    private void throwIfReadOnly() {
        if (!permitModification) {
            throw new ApiUsageException("Write-method not permitted.");
        }
    }
    
    /**
     * Converts a Long value to an Integer safely.
     * @param v Long value to convert.
     * @throws ApiUsageException If the conversion would cause an overflow
     * or an underflow.
     */
    public static Integer safeLongToInteger(Long v) {
        if (v > Integer.MAX_VALUE) {
            throw new ApiUsageException(String.format(
                    "Converting Long %d to Integer is an overflow.", v));
        }
        if (v < Integer.MIN_VALUE) {
            throw new ApiUsageException(String.format(
                    "Converting Long %d to Integer is an underflow.", v));
        }
        return v.intValue();
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#checkBounds(Integer, Integer, Integer, Integer, Integer)
	 */
    public void checkBounds(Integer x, Integer y, Integer z, Integer c, 
    		Integer t)
            throws DimensionsOutOfBoundsException {
    	if (x != null && (x > getSizeX() - 1 || x < 0)) {
            throw new DimensionsOutOfBoundsException("X '" + x
                    + "' greater than sizeX '" + getSizeX() + "'.");
        }
        if (y != null && (y > getSizeY() - 1 || y < 0)) {
            throw new DimensionsOutOfBoundsException("Y '" + y
                    + "' greater than sizeY '" + getSizeY() + "'.");
        }

        if (z != null && (z > getSizeZ() - 1 || z < 0)) {
            throw new DimensionsOutOfBoundsException("Z '" + z
                    + "' greater than sizeZ '" + getSizeZ() + "'.");
        }

        if (c != null && (c > getSizeC() - 1 || c < 0)) {
            throw new DimensionsOutOfBoundsException("C '" + c
                    + "' greater than sizeC '" + getSizeC() + "'.");
        }

        if (t != null && (t > getSizeT() - 1 || t < 0)) {
            throw new DimensionsOutOfBoundsException("T '" + t
                    + "' greater than sizeT '" + getSizeT() + "'.");
        }
    }

    private FileChannel getFileChannel() throws FileNotFoundException {
        if (channel == null) {
            file = new RandomAccessFile(getPath(), "rw");
            channel = file.getChannel();
        }

        return channel;
    }

    /**
     * Closes the buffer, cleaning up file state.
     * 
     * @throws IOException
     *             if an I/O error occurs.
     */
    public void close() throws IOException {
        if (channel != null) {
            try {
                channel.close();
            } catch (Exception e) {
                log.error("Error closing channel", e);
            } finally {
                channel = null;
            }
        }

        if (file != null) {
            try {
                file.close();
            } catch (Exception e) {
                log.error("Error closing file", e);
            } finally {
                file = null;
            }
        }
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getPlaneSize()
	 */
    public Long getPlaneSize() {
        if (planeSize == null) {
            planeSize = (long) getSizeX() * (long) getSizeY() * getByteWidth();
        }

        return planeSize;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getRowSize()
	 */
    public Integer getRowSize() {
        if (rowSize == null) {
            rowSize = getSizeX() * getByteWidth();
        }

        return rowSize;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getColSize()
	 */
    public Integer getColSize() {
        if (colSize == null) {
            colSize = getSizeY() * getByteWidth();
        }

        return colSize;
    }
    
    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getStackSize()
	 */
    public Long getStackSize() {
        if (stackSize == null) {
            stackSize = getPlaneSize() * getSizeZ();
        }

        return stackSize;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getTimepointSize()
	 */
    public Long getTimepointSize() {
        if (timepointSize == null) {
            timepointSize = getStackSize() * getSizeC();
        }

        return timepointSize;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getTotalSize()
	 */
    public Long getTotalSize() {
        if (totalSize == null) {
            totalSize = getTimepointSize() * getSizeT();
        }

        return totalSize;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getRowOffset(Integer, Integer, Integer, Integer)
	 */
    public Long getRowOffset(Integer y, Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, y, z, c, t);

        Integer rowSize = getRowSize();
        Long timepointSize = getTimepointSize();
        Long stackSize = getStackSize();
        Long planeSize = getPlaneSize();

        return (long) rowSize * y + timepointSize * t
                + stackSize * c + planeSize * z;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getPlaneOffset(Integer, Integer, Integer)
	 */
    public Long getPlaneOffset(Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, null, z, c, t);

        Long timepointSize = getTimepointSize();
        Long stackSize = getStackSize();
        Long planeSize = getPlaneSize();

        return timepointSize * t + stackSize * c
                + planeSize * z;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getStackOffset(Integer, Integer)
	 */
    public Long getStackOffset(Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, null, null, c, t);

        Long timepointSize = getTimepointSize();
        Long stackSize = getStackSize();

        return timepointSize * t + stackSize * c;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getTimepointOffset(Integer)
	 */
    public Long getTimepointOffset(Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, null, null, null, t);
        Long timepointSize = getTimepointSize();
        return (long) timepointSize * t;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getRegion(Integer, Long)
	 */
    public PixelData getRegion(Integer size, Long offset)
            throws IOException {
        FileChannel fileChannel = getFileChannel();

        /*
         * fileChannel should not be "null" as it will throw an exception if
         * there happens to be an error.
         */

        MappedByteBuffer b = fileChannel.map(MapMode.READ_ONLY, offset, size);
        return new PixelData(pixels.getPixelsType().getValue(), b);
    }
    
    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getRegionDirect(Integer, Long, byte[])
	 */
    public byte[] getRegionDirect(Integer size, Long offset, byte[] buffer)
    		throws IOException
    {
		if (buffer.length != size)
			throw new ApiUsageException("Buffer size incorrect.");
		final PixelData pd = getRegion(size, offset);
		pd.getData().get(buffer);
		pd.dispose();
		return buffer;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getRow(Integer, Integer, Integer, Integer)
	 */
    public PixelData getRow(Integer y, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
    	//dimension check getRowOffset
        Long offset = getRowOffset(y, z, c, t);
        Integer size = getRowSize();

        return getRegion(size, offset);
    }
    
    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getCol(Integer, Integer, Integer, Integer)
	 */
    public PixelData getCol(Integer x, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
    	//Dimension check in plane.
        PixelData plane = getPlane(z, c, t);
        Integer sizeY = getSizeY();
        Integer sizeX = getSizeX();
        Integer colSize = getColSize();
        ByteBuffer buf = ByteBuffer.wrap(new byte[colSize]);
        PixelData column = new PixelData(pixels.getPixelsType().getValue(), buf);
        int offset;
        double value;
        for (int i = 0; i < sizeY; i++) {
            offset = (i * sizeX) + x;
            value = plane.getPixelValue(offset);
            column.setPixelValue(i, value);
        }
        plane.dispose();
        return column;
    }
    
    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getRowDirect(Integer, Integer, Integer, Integer, byte[])
	 */
    public byte[] getRowDirect(Integer y, Integer z, Integer c, Integer t,
    		byte[] buffer) throws IOException, DimensionsOutOfBoundsException
    {
		if (buffer.length != getRowSize())
			throw new ApiUsageException("Buffer size incorrect.");
		final PixelData pd = getRow(y, z, c, t);
		pd.getData().get(buffer);
		pd.dispose();
		return buffer;
    }
    
    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getColDirect(Integer, Integer, Integer, Integer, byte[])
	 */
    public byte[] getColDirect(Integer x, Integer z, Integer c, Integer t, 
            byte[] buffer) throws IOException, DimensionsOutOfBoundsException
    {
        PixelData plane = getPlane(z, c, t);
        Integer sizeY = getSizeY();
        Integer sizeX = getSizeX();
        ByteBuffer buf = ByteBuffer.wrap(buffer);
        PixelData column = new PixelData(pixels.getPixelsType().getValue(), buf);
        int offset;
        double value;
        for (int i = 0; i < sizeY; i++) {
            offset = (i * sizeX) + x;
            value = plane.getPixelValue(offset);
            column.setPixelValue(i, value);
        }
        plane.dispose();
        return buffer;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getHypercube(List, List, List)
	 */
    public PixelData getHypercube(List<Integer> offset, List<Integer> size, 
            List<Integer> step) throws IOException, DimensionsOutOfBoundsException 
    {
        byte[] buffer = new byte[
                safeLongToInteger(getHypercubeSize(offset,size,step))];
        getHypercubeDirect(offset,size,step,buffer);
        return new PixelData(pixels.getPixelsType().getValue(), ByteBuffer.wrap(buffer));
	}
                
    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getHypercubeDirect(List, List, List, byte[])
	 */
    public byte[] getHypercubeDirect(List<Integer> offset, List<Integer> size, 
            List<Integer> step, byte[] buffer) 
            throws IOException, DimensionsOutOfBoundsException 
    {
        if (buffer.length != getHypercubeSize(offset, size, step))
            throw new RuntimeException("Buffer size incorrect.");
        getWholeHypercube(offset,size,step,buffer);
        return buffer;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getPlaneRegionDirect(Integer, Integer, Integer, Integer, 
     * Integer, byte[])
	 */
	public byte[] getPlaneRegionDirect(Integer z, Integer c, Integer t,
			Integer count, Integer offset, byte[] buffer)
		throws IOException, DimensionsOutOfBoundsException
	{
		final PixelData pd = getPlane(z, c, t);
		final ByteBuffer b = pd.getData();
		b.position(offset * getByteWidth());
		b.get(buffer, 0, count * getByteWidth());
		pd.dispose();
		return buffer;
	}

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getPlane(Integer, Integer, Integer)
	 */
    public PixelData getPlane(Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        log.info("Retrieving plane: " + z + "x" + c + "x" + t);
        Long offset = getPlaneOffset(z, c, t);
        Integer size = safeLongToInteger(getPlaneSize());
        PixelData region = getRegion(size, offset);

        byte[] nullPlane = PixelsService.nullPlane;

        for (int i = 0; i < PixelsService.NULL_PLANE_SIZE; i++) {
            if (region.getData().get(i) != nullPlane[i]) {
                return region;
            }
        }

        region.dispose();
        return null; // All of the nullPlane bytes match, non-filled plane
    }
    
    /**
     * Implemented as specified by {@link ByteBuffer} I/F.
     * @see PixelBuffer#getPlaneRegion(Integer, Integer, Integer, Integer, 
     * Integer, Integer, Integer, Integer)
	 */
    public PixelData getPlaneRegion(Integer x, Integer y, Integer width, 
    		Integer height, Integer z, Integer c, Integer t, Integer stride)
            throws IOException, DimensionsOutOfBoundsException {
    	if (stride == null || stride < 0) stride = 0;
    	checkBounds(x, y, z, c, t);
    	checkBounds(x+width-1, y+height-1, null, null, null);
    	
    	PixelData plane = getPlane(z, c, t);
    	Integer size;
    	ByteBuffer buf;
    	PixelData region = null;
    	int offset;
    	
    	if (stride == 0) {
    		size =  width*height*getByteWidth();
            buf = ByteBuffer.wrap(new byte[size]);
            region = new PixelData(pixels.getPixelsType().getValue(), buf);
            for (int i = 0; i < height; i++) {
            	for (int j = 0; j < width; j++) {
            		offset = (i+y)*getSizeX()+x+j;
            		region.setPixelValue(i*width+j, plane.getPixelValue(offset));
            	}
            }
            plane.dispose();
            return region;
    	}
    	stride++;
    	int w = width/stride;
    	size = width*height*getByteWidth()/(stride*stride);
        buf = ByteBuffer.wrap(new byte[size]);
        region = new PixelData(pixels.getPixelsType().getValue(), buf);
    	int k = 0;
    	int l = 0;
    	for (int i = 0; i < height; i = i+stride) {
    		l = 0;
        	for (int j = 0; j < width; j = j+stride) {
        		offset = (i+y)*getSizeX()+x+j;
        		region.setPixelValue(k*w+l, plane.getPixelValue(offset));
        		l++;
        	}
        	k++;
        }
        plane.dispose();
        return region;
    }
    
    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getPlaneDirect(Integer, Integer, Integer, byte[])
	 */
    public byte[] getPlaneDirect(Integer z, Integer c, Integer t, byte[] buffer)
    		throws IOException, DimensionsOutOfBoundsException
    {
		if (buffer.length != getPlaneSize())
			throw new ApiUsageException("Buffer size incorrect.");
		final PixelData pd = getPlane(z, c, t);
		pd.getData().get(buffer);
		pd.dispose();
		return buffer;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getStack(Integer, Integer)
	 */
    public PixelData getStack(Integer c, Integer t) throws IOException,
            DimensionsOutOfBoundsException {
        Long offset = getStackOffset(c, t);
        Integer size = safeLongToInteger(getStackSize());

        return getRegion(size, offset);
    }
    
    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getStackDirect(Integer, Integer, byte[])
	 */
    public byte[] getStackDirect(Integer c, Integer t, byte[] buffer)
    		throws IOException, DimensionsOutOfBoundsException
    {
		if (buffer.length != getStackSize())
			throw new ApiUsageException("Buffer size incorrect.");
		final PixelData pd = getStack(c, t);
		pd.getData().get(buffer);
		pd.dispose();
		return buffer;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getTimepoint(Integer)
	 */
    public PixelData getTimepoint(Integer t) throws IOException,
            DimensionsOutOfBoundsException {
        Long offset = getTimepointOffset(t);
        Integer size = safeLongToInteger(getTimepointSize());

        return getRegion(size, offset);
    }
    
    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getTimepointDirect(Integer, byte[])
	 */
    public byte[] getTimepointDirect(Integer t, byte[] buffer)
    		throws IOException, DimensionsOutOfBoundsException
    {
		if (buffer.length != getTimepointSize())
			throw new ApiUsageException("Buffer size incorrect.");
		final PixelData pd = getTimepoint(t);
		pd.getData().get(buffer);
		pd.dispose();
		return buffer;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#setRegion(Integer, Long, byte[])
	 */
    public void setRegion(Integer size, Long offset, byte[] buffer)
            throws IOException {
        throwIfReadOnly();
    	ByteBuffer buf = MappedByteBuffer.wrap(buffer);
    	buf.limit(size);
    	setRegion(size, offset, buf);
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#setRegion(Integer, Long, ByteBuffer)
	 */
    public void setRegion(Integer size, Long offset, ByteBuffer buffer)
            throws IOException {
        throwIfReadOnly();
        FileChannel fileChannel = getFileChannel();

        /*
         * fileChannel should not be "null" as it will throw an exception if
         * there happens to be an error.
         */
        fileChannel.write(buffer, offset);
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#setRow(ByteBuffer, Integer, Integer, Integer, Integer)
	 */
    public void setRow(ByteBuffer buffer, Integer y, Integer z, Integer c,
            Integer t) throws IOException, DimensionsOutOfBoundsException {
        throwIfReadOnly();
        Long offset = getRowOffset(y, z, c, t);
        Integer size = getRowSize();

        setRegion(size, offset, buffer);
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#setPlane(ByteBuffer, Integer, Integer, Integer)
	 */
    public void setPlane(ByteBuffer buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        throwIfReadOnly();
        Long offset = getPlaneOffset(z, c, t);
        Integer size = safeLongToInteger(getPlaneSize());
        if (buffer.limit() != size)
        {
        	// Handle the size mismatch.
        	if (buffer.limit() < size)
        		throw new BufferUnderflowException();
        	throw new BufferOverflowException();
        }

        setRegion(size, offset, buffer);
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#setPlane(byte[], Integer, Integer, Integer)
	 */
    public void setPlane(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        throwIfReadOnly();
    	setPlane(ByteBuffer.wrap(buffer), z, c, t);
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#setStack(ByteBuffer, Integer, Integer, Integer)
	 */
    public void setStack(ByteBuffer buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        throwIfReadOnly();
        Long offset = getStackOffset(c, t);
        Integer size = safeLongToInteger(getStackSize());
        if (buffer.limit() != size)
        {
        	// Handle the size mismatch.
        	if (buffer.limit() < size)
        		throw new BufferUnderflowException();
        	throw new BufferOverflowException();
        }

        setRegion(size, offset, buffer);
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#setStack(byte[], Integer, Integer, Integer)
	 */
    public void setStack(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        throwIfReadOnly();
    	setStack(MappedByteBuffer.wrap(buffer), z, c, t);
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#setTimepoint(ByteBuffer, Integer)
	 */
    public void setTimepoint(ByteBuffer buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException {
        throwIfReadOnly();
        Long offset = getTimepointOffset(t);
        Integer size = safeLongToInteger(getTimepointSize());
        if (buffer.limit() != size)
        {
        	// Handle the size mismatch.
        	if (buffer.limit() < size)
        		throw new BufferUnderflowException();
        	throw new BufferOverflowException();
        }

        setRegion(size, offset, buffer);
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#setTimepoint(ByteBuffer, Integer)
	 */
    public void setTimepoint(byte[] buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException {
        throwIfReadOnly();
    	setTimepoint(MappedByteBuffer.wrap(buffer), t);
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#calculateMessageDigest()
	 */
    public byte[] calculateMessageDigest() throws IOException {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "Required SHA-1 message digest algorithm unavailable.");
        }

        for (int t = 0; t < getSizeT(); t++) {
            for (int c = 0; c < getSizeC(); c++) {
                for (int z = 0; z < getSizeZ(); z++) {
                    try {
                        final PixelData pd = getPlane(z, c, t);
                        md.update(pd.getData());
                        pd.dispose();
                    } catch (DimensionsOutOfBoundsException e) {
                        // This better not happen. :)
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return md.digest();
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getByteWidth()
     */
    public int getByteWidth() {
        return PixelData.getBitDepth(pixels.getPixelsType().getValue()) / 8;
    }
    
    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#isSigned()
	 */
	public boolean isSigned()
	{
		PixelData d = new PixelData(pixels.getPixelsType().getValue(), null);
		return d.isSigned();
	}
	
	/**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#isFloat()
	 */
	public boolean isFloat()
	{
		PixelData d = new PixelData(pixels.getPixelsType().getValue(), null);
		return d.isFloat();
	}
    
    //
    // Delegate methods to ease work with pixels
    //

	/**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getSizeC()
	 */
    public int getSizeC() {
        return pixels.getSizeC();
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getSizeT()
	 */
    public int getSizeT() {
        return pixels.getSizeT();
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getSizeX()
	 */
    public int getSizeX() {
        return pixels.getSizeX();
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getSizeY()
	 */
    public int getSizeY() {
        return pixels.getSizeY();
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getSizeZ()
	 */
    public int getSizeZ() {
        return pixels.getSizeZ();
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getId()
	 */
    public long getId() {
        return pixels.getId();
    }

    public String getSha1() {
        return pixels.getSha1();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTile(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public PixelData getTile(Integer z, Integer c, Integer t, Integer x,
            Integer y, Integer w, Integer h) throws IOException
    {
        return getPlaneRegion(x, y, w, h, z, c, t, 0);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTileDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
     */
    public byte[] getTileDirect(Integer z, Integer c, Integer t, Integer x,
            Integer y, Integer w, Integer h, byte[] buffer) throws IOException
    {
        List<Integer> offset = Arrays.asList(new Integer[]{x,y,z,c,t});
        List<Integer> size = Arrays.asList(new Integer[]{w,h,1,1,1});
        List<Integer> step = Arrays.asList(new Integer[]{1,1,1,1,1});
        return getHypercubeDirect(offset, size, step, buffer);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setTile(byte[], java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public void setTile(byte[] buffer, Integer z, Integer c, Integer t, Integer x, Integer y,
            Integer w, Integer h) throws IOException,
            BufferOverflowException
    {
        if (x != 0)
        {
            throw new UnsupportedOperationException(
                    "ROMIO pixel buffer only supports 0 offseted tile writes.");
        }
        if (w != getSizeX())
        {
            throw new UnsupportedOperationException(
                    "ROMIO pixel buffer only supports full row writes.");
        }
        try
        {
            long offset = getPlaneOffset(z, c, t);
            offset += getByteWidth() * getSizeX() * y;
            setRegion(buffer.length, offset, buffer);
        }
        catch (DimensionsOutOfBoundsException e)
        {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getResolutionLevel()
     */
    public int getResolutionLevel()
    {
        return 0;
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getResolutionLevels()
     */
    public int getResolutionLevels()
    {
        return 1;
    }

    public List<List<Integer>> getResolutionDescriptions()
    {
        List<Integer> sizes = Arrays.asList(getSizeX(), getSizeY());
        List<List<Integer>> rv = new ArrayList<List<Integer>>();
        rv.add(sizes);
        return rv;
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTileSize()
     */
    public Dimension getTileSize()
    {
        int width = getSizeX();
        int height = Math.min(getSizeY(),
                (MAXIMUM_BUFFER_SIZE / getByteWidth()) / getSizeX());
        return new Dimension(width, height);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setResolutionLevel(int)
     */
    public void setResolutionLevel(int resolutionLevel)
    {
        throw new UnsupportedOperationException(
                "Cannot set resolution levels on a ROMIO pixel buffer.");
    }
    
    public Long getHypercubeSize(List<Integer> offset, List<Integer> size, List<Integer> step)
            throws DimensionsOutOfBoundsException {
        // only works for 5d at present
        checkCubeBounds(offset, size, step);
        int tStripes = (size.get(4) + step.get(4) - 1) / step.get(4);
        int cStripes = (size.get(3) + step.get(3) - 1) / step.get(3);
        int zStripes = (size.get(2) + step.get(2) - 1) / step.get(2);
        int yStripes = (size.get(1) + step.get(1) - 1) / step.get(1);
        int xStripes = (size.get(0) + step.get(0) - 1) / step.get(0);
        long tileRowSize = getByteWidth() * (long) xStripes;
        long cubeSize = tileRowSize * yStripes * zStripes * cStripes * tStripes;

        return cubeSize;
    }

    /*
     * Temporary helpers. May be factored out.
     * This code is repeated in bfPixelWrapper and so needs refactoring.
     */
    private byte[] getWholeHypercube(List<Integer> offset, List<Integer> size,
            List<Integer> step, byte[] cube) throws IOException {
        int cubeOffset = 0;
        int xStripes = (size.get(0) + step.get(0) - 1) / step.get(0);
        int pixelSize = getByteWidth();
        int tileRowSize = pixelSize * xStripes;
        byte[] plane = new byte[safeLongToInteger(getPlaneSize())];
        for(int t = offset.get(4); t < size.get(4)+offset.get(4); t += step.get(4))
        {
            for(int c = offset.get(3); c < size.get(3)+offset.get(3); c += step.get(3))
            {
                for(int z = offset.get(2); z < size.get(2)+offset.get(2); z += step.get(2))
                {
                    getPlaneDirect(z,c,t,plane);
                    int rowOffset = offset.get(1)*getRowSize();
                    if(step.get(0)==1)
                    {
                        int byteOffset = rowOffset + offset.get(0)*pixelSize;
                        for(int y = offset.get(1); y < size.get(1)+offset.get(1); y += step.get(1))
                        {
                            System.arraycopy(plane, byteOffset, cube, cubeOffset, tileRowSize);
                            cubeOffset += tileRowSize;
                            byteOffset += getRowSize()*step.get(1);
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
                            rowOffset += getRowSize()*step.get(1);
                        }
                    }

                }
            }
        }
        return cube;
    }
    
    private void checkCubeBounds(List<Integer> offset, List<Integer> size, List<Integer> step)
            throws DimensionsOutOfBoundsException {
        // At the moment the array must contain 5 values
        if(offset.size()!=5 || size.size()!=5 || step.size()!=5)
        {
            throw new DimensionsOutOfBoundsException(
                    "Invalid List length: each list must contain 5 elements XYZCT");
        }
        checkBounds(offset.get(0),offset.get(1),offset.get(2),offset.get(3),offset.get(4));
        checkBounds(offset.get(0)+size.get(0)-1,offset.get(1)+size.get(1)-1,
                offset.get(2)+size.get(2)-1,offset.get(3)+size.get(3)-1,offset.get(4)+size.get(4)-1);
        if(step.get(0) < 1 ||  step.get(1) < 1 ||  step.get(2) < 1
                ||  step.get(3) < 1 ||  step.get(4) < 1)
        {
            throw new DimensionsOutOfBoundsException(
                    "Invalid step size: steps sizes must be 1 or greater");
        }
    }

}
