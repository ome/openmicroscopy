/*
 * ome.io.nio.PixelBuffer
 *
 *   Copyright 2007 Glencoe Software Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.conditions.ApiUsageException;
import ome.model.core.Pixels;

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
    private static Log log = LogFactory.getLog(RomioPixelBuffer.class);

    /** Reference to the pixels. */
    private Pixels pixels;

    private RandomAccessFile file;

    private FileChannel channel;

    /** The size of a row. */
    private Integer rowSize;
    
    /** The size of a column. */
    private Integer colSize;

    /** The size of a plane. */
    private Integer planeSize;

    /** The size of a stack. */
    private Integer stackSize;

    /** The size of a timepoint. */
    private Integer timepointSize;

    /** The total size. */
    private Integer totalSize;

    /**
     * Creates a new instance.
     * 
     * @param path The path to the file.
     * @param pixels The pixels object to handle.
     */
    public RomioPixelBuffer(String path, Pixels pixels) {
        super(path);
        if (pixels == null) {
            throw new NullPointerException(
                    "Expecting a not-null pixels element.");
        }

        this.pixels = pixels;
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
    public Integer getPlaneSize() {
        if (planeSize == null) {
            planeSize = getSizeX() * getSizeY() * getByteWidth();
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
    public Integer getStackSize() {
        if (stackSize == null) {
            stackSize = getPlaneSize() * getSizeZ();
        }

        return stackSize;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getTimepointSize()
	 */
    public Integer getTimepointSize() {
        if (timepointSize == null) {
            timepointSize = getStackSize() * getSizeC();
        }

        return timepointSize;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getTotalSize()
	 */
    public Integer getTotalSize() {
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
        Integer timepointSize = getTimepointSize();
        Integer stackSize = getStackSize();
        Integer planeSize = getPlaneSize();

        return (long) rowSize * y + (long) timepointSize * t
                + (long) stackSize * c + (long) planeSize * z;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getPlaneOffset(Integer, Integer, Integer)
	 */
    public Long getPlaneOffset(Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, null, z, c, t);

        Integer timepointSize = getTimepointSize();
        Integer stackSize = getStackSize();
        Integer planeSize = getPlaneSize();

        return (long) timepointSize * t + (long) stackSize * c
                + (long) planeSize * z;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getStackOffset(Integer, Integer)
	 */
    public Long getStackOffset(Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, null, null, c, t);

        Integer timepointSize = getTimepointSize();
        Integer stackSize = getStackSize();

        return (long) timepointSize * t + (long) stackSize * c;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getTimepointOffset(Integer)
	 */
    public Long getTimepointOffset(Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, null, null, null, t);
        Integer timepointSize = getTimepointSize();
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
        return new PixelData(pixels.getPixelsType(), b);
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
		ByteBuffer b = getRegion(size, offset).getData();
		b.get(buffer);
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
        PixelData column = new PixelData(pixels.getPixelsType(), buf);
        int offset;
        double value;
        for (int i = 0; i < sizeY; i++) {
            offset = (i * sizeX) + x;
            value = plane.getPixelValue(offset);
            column.setPixelValue(i, value);
        }
        
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
		ByteBuffer b = getRow(y, z, c, t).getData();
		b.get(buffer);
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
        PixelData column = new PixelData(pixels.getPixelsType(), buf);
        int offset;
        double value;
        for (int i = 0; i < sizeY; i++) {
            offset = (i * sizeX) + x;
            value = plane.getPixelValue(offset);
            column.setPixelValue(i, value);
        }

        return buffer;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getHypercube(List<Integer>, IList<Integer>, List<Integer>)
	 */
    public PixelData getHypercube(List<Integer> offset, List<Integer> size, 
            List<Integer> step) throws IOException, DimensionsOutOfBoundsException 
    {
        throw new UnsupportedOperationException(
            "Not yet supported.");
	}
                
    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getHypercubeDirect(List<Integer>, IList<Integer>, List<Integer>, byte[])
	 */
    public byte[] getHypercubeDirect(List<Integer> offset, List<Integer> size, 
            List<Integer> step, byte[] buffer) 
            throws IOException, DimensionsOutOfBoundsException 
    {
        throw new UnsupportedOperationException(
            "Not yet supported.");
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
		ByteBuffer b = getPlane(z, c, t).getData();
		b.position(offset * getByteWidth());
		b.get(buffer, 0, count * getByteWidth());
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
        Integer size = getPlaneSize();
        PixelData region = getRegion(size, offset);

        byte[] nullPlane = PixelsService.nullPlane;

        for (int i = 0; i < PixelsService.NULL_PLANE_SIZE; i++) {
            if (region.getData().get(i) != nullPlane[i]) {
                return region;
            }
        }

        return null; // All of the nullPlane bytes match, non-filled plane
    }
    
    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
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
            region = new PixelData(pixels.getPixelsType(), buf);
            for (int i = 0; i < height; i++) {
            	for (int j = 0; j < width; j++) {
            		offset = (i+y)*getSizeX()+x+j;
            		region.setPixelValue(i*width+j, plane.getPixelValue(offset));
            	}
            }
            return region;
    	}
    	stride++;
    	int w = width/stride;
    	size = width*height*getByteWidth()/(stride*stride);
        buf = ByteBuffer.wrap(new byte[size]);
        region = new PixelData(pixels.getPixelsType(), buf);
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
		ByteBuffer b = getPlane(z, c, t).getData();
		b.get(buffer);
		return buffer;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getStack(Integer, Integer)
	 */
    public PixelData getStack(Integer c, Integer t) throws IOException,
            DimensionsOutOfBoundsException {
        Long offset = getStackOffset(c, t);
        Integer size = getStackSize();

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
		ByteBuffer b = getStack(c, t).getData();
		b.get(buffer);
		return buffer;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getTimepoint(Integer)
	 */
    public PixelData getTimepoint(Integer t) throws IOException,
            DimensionsOutOfBoundsException {
        Long offset = getTimepointOffset(t);
        Integer size = getTimepointSize();

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
		ByteBuffer b = getTimepoint(t).getData();
		b.get(buffer);
		return buffer;
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#setRegion(Integer, Long, byte[])
	 */
    public void setRegion(Integer size, Long offset, byte[] buffer)
            throws IOException {
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
        Long offset = getPlaneOffset(z, c, t);
        Integer size = getPlaneSize();
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
    	setPlane(ByteBuffer.wrap(buffer), z, c, t);
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#setStack(ByteBuffer, Integer, Integer, Integer)
	 */
    public void setStack(ByteBuffer buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        Long offset = getStackOffset(c, t);
        Integer size = getStackSize();
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
    	setStack(MappedByteBuffer.wrap(buffer), z, c, t);
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#setTimepoint(ByteBuffer, Integer)
	 */
    public void setTimepoint(ByteBuffer buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException {
        Long offset = getTimepointOffset(t);
        Integer size = getTimepointSize();
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
            try {
                ByteBuffer buffer = getTimepoint(t).getData();
                md.update(buffer);
            } catch (DimensionsOutOfBoundsException e) {
                // This better not happen. :)
                throw new RuntimeException(e);
            }
        }

        return md.digest();
    }

    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getByteWidth()
     */
    public int getByteWidth() {
        return PixelsService.getBitDepth(pixels.getPixelsType()) / 8;
    }
    
    /**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#isSigned()
	 */
	public boolean isSigned()
	{
		PixelData d = new PixelData(pixels.getPixelsType(), null);
		return d.isSigned();
	}
	
	/**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#isFloat()
	 */
	public boolean isFloat()
	{
		PixelData d = new PixelData(pixels.getPixelsType(), null);
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
}
