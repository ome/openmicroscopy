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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.conditions.ApiUsageException;
import ome.model.core.Pixels;

/**
 * Class implementation of the PixelBuffer interface for standard "proprietary"
 * ROMIO/OMEIS data data format.
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

    private Pixels pixels;

    private FileChannel channel;

    private Integer rowSize;

    private Integer planeSize;

    private Integer stackSize;

    private Integer timepointSize;

    private Integer totalSize;

    RomioPixelBuffer(String path, Pixels pixels) {
        super(path);
        if (pixels == null) {
            throw new NullPointerException(
                    "Expecting a not-null pixels element.");
        }

        this.pixels = pixels;
    }

    public void checkBounds(Integer y, Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
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
            RandomAccessFile file = new RandomAccessFile(getPath(), "rw");
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
            channel.close();
            channel = null;
        }
    }

    public Integer getPlaneSize() {
        if (planeSize == null) {
            planeSize = getSizeX() * getSizeY() * getByteWidth();
        }

        return planeSize;
    }

    public Integer getRowSize() {
        if (rowSize == null) {
            rowSize = getSizeX() * getByteWidth();
        }

        return rowSize;
    }

    public Integer getStackSize() {
        if (stackSize == null) {
            stackSize = getPlaneSize() * getSizeZ();
        }

        return stackSize;
    }

    public Integer getTimepointSize() {
        if (timepointSize == null) {
            timepointSize = getStackSize() * getSizeC();
        }

        return timepointSize;
    }

    public Integer getTotalSize() {
        if (totalSize == null) {
            totalSize = getTimepointSize() * getSizeT();
        }

        return totalSize;
    }

    public Long getRowOffset(Integer y, Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(y, z, c, t);

        Integer rowSize = getRowSize();
        Integer timepointSize = getTimepointSize();
        Integer stackSize = getStackSize();
        Integer planeSize = getPlaneSize();

        return (long) rowSize * y + (long) timepointSize * t
                + (long) stackSize * c + (long) planeSize * z;
    }

    public Long getPlaneOffset(Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, z, c, t);

        Integer timepointSize = getTimepointSize();
        Integer stackSize = getStackSize();
        Integer planeSize = getPlaneSize();

        return (long) timepointSize * t + (long) stackSize * c
                + (long) planeSize * z;
    }

    public Long getStackOffset(Integer c, Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, null, c, t);

        Integer timepointSize = getTimepointSize();
        Integer stackSize = getStackSize();

        return (long) timepointSize * t + (long) stackSize * c;
    }

    public Long getTimepointOffset(Integer t)
            throws DimensionsOutOfBoundsException {
        checkBounds(null, null, null, t);

        Integer timepointSize = getTimepointSize();

        return (long) timepointSize * t;
    }

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
    
    public byte[] getRegionDirect(Integer size, Long offset, byte[] buffer)
    		throws IOException
    {
		if (buffer.length != size)
			throw new ApiUsageException("Buffer size incorrect.");
		ByteBuffer b = getRegion(size, offset).getData();
		b.get(buffer);
		return buffer;
    }

    public PixelData getRow(Integer y, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        Long offset = getRowOffset(y, z, c, t);
        Integer size = getRowSize();

        return getRegion(size, offset);
    }
    
    public byte[] getRowDirect(Integer y, Integer z, Integer c, Integer t,
    		byte[] buffer) throws IOException, DimensionsOutOfBoundsException
    {
		if (buffer.length != getRowSize())
			throw new ApiUsageException("Buffer size incorrect.");
		ByteBuffer b = getRow(y, z, c, t).getData();
		b.get(buffer);
		return buffer;
    }
    
	public byte[] getPlaneRegionDirect(Integer z, Integer c, Integer t,
			Integer count, Integer offset, byte[] buffer)
		throws IOException, DimensionsOutOfBoundsException
	{
		ByteBuffer b = getPlane(z, c, t).getData();
		b.position(offset);
		b.get(buffer, 0, count * getByteWidth());
		return buffer;
	}

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
    
    public byte[] getPlaneDirect(Integer z, Integer c, Integer t, byte[] buffer)
    		throws IOException, DimensionsOutOfBoundsException
    {
		if (buffer.length != getPlaneSize())
			throw new ApiUsageException("Buffer size incorrect.");
		ByteBuffer b = getPlane(z, c, t).getData();
		b.get(buffer);
		return buffer;
    }

    public PixelData getStack(Integer c, Integer t) throws IOException,
            DimensionsOutOfBoundsException {
        Long offset = getStackOffset(c, t);
        Integer size = getStackSize();

        return getRegion(size, offset);
    }
    
    public byte[] getStackDirect(Integer c, Integer t, byte[] buffer)
    		throws IOException, DimensionsOutOfBoundsException
    {
		if (buffer.length != getStackSize())
			throw new ApiUsageException("Buffer size incorrect.");
		ByteBuffer b = getStack(c, t).getData();
		b.get(buffer);
		return buffer;
    }

    public PixelData getTimepoint(Integer t) throws IOException,
            DimensionsOutOfBoundsException {
        Long offset = getTimepointOffset(t);
        Integer size = getTimepointSize();

        return getRegion(size, offset);
    }
    
    public byte[] getTimepointDirect(Integer t, byte[] buffer)
    		throws IOException, DimensionsOutOfBoundsException
    {
		if (buffer.length != getTimepointSize())
			throw new ApiUsageException("Buffer size incorrect.");
		ByteBuffer b = getTimepoint(t).getData();
		b.get(buffer);
		return buffer;
    }

    public void setRegion(Integer size, Long offset, byte[] buffer)
            throws IOException {
    	setRegion(size, offset, MappedByteBuffer.wrap(buffer));
    }

    public void setRegion(Integer size, Long offset, ByteBuffer buffer)
            throws IOException {
        FileChannel fileChannel = getFileChannel();

        /*
         * fileChannel should not be "null" as it will throw an exception if
         * there happens to be an error.
         */
        fileChannel.write(buffer, offset);
    }

    public void setRow(ByteBuffer buffer, Integer y, Integer z, Integer c,
            Integer t) throws IOException, DimensionsOutOfBoundsException {
        Long offset = getRowOffset(y, z, c, t);
        Integer size = getRowSize();

        setRegion(size, offset, buffer);
    }

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

    public void setPlane(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
    	setPlane(ByteBuffer.wrap(buffer), z, c, t);
    }

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

    public void setStack(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
    	setStack(MappedByteBuffer.wrap(buffer), z, c, t);
    }

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

    public void setTimepoint(byte[] buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException {
    	setTimepoint(MappedByteBuffer.wrap(buffer), t);
    }

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

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getByteWidth()
     */
    public int getByteWidth() {
        return PixelsService.getBitDepth(pixels.getPixelsType()) / 8;
    }
    
	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#isSigned()
	 */
	public boolean isSigned()
	{
		MappedByteBuffer b = null;
		PixelData d = new PixelData(pixels.getPixelsType(), b);
		return d.isSigned();
	}
	
	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#isFloat()
	 */
	public boolean isFloat()
	{
		MappedByteBuffer b = null;
		PixelData d = new PixelData(pixels.getPixelsType(), b);
		return d.isFloat();
	}
    
    //
    // Delegate methods to ease work with pixels
    //

    public int getSizeC() {
        return pixels.getSizeC();
    }

    public int getSizeT() {
        return pixels.getSizeT();
    }

    public int getSizeX() {
        return pixels.getSizeX();
    }

    public int getSizeY() {
        return pixels.getSizeY();
    }

    public int getSizeZ() {
        return pixels.getSizeZ();
    }

    public long getId() {
        return pixels.getId();
    }

    public String getSha1() {
        return pixels.getSha1();
    }
}
