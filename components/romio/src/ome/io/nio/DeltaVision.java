/*
 * ome.io.nio.DeltaVision
 *
 *   Copyright 2007 Glencoe Software Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.nio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import ome.conditions.ApiUsageException;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.model.core.OriginalFile;
import ome.util.PixelData;

/**
 * Class implementation of the PixelBuffer interface for a DeltaVision specific
 * image file.
 *
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:chris@glencoesoftware.com">chris@glencoesoftware.com</a>
 * @author David L. Whitehurst &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:david@glencoesoftware.com">david@glencoesoftware.com</a>
 * @version $Revision$
 * @since 3.0
 * @see PixelBuffer
 */
public class DeltaVision implements PixelBuffer {

	private FileChannel channel;

	private Integer rowSize;
	
	private Integer colSize;

	private Integer planeSize;

	private Integer stackSize;

	private Integer timepointSize;

	private Integer totalSize;
	
	protected ByteBuffer buf;

	private OriginalFile originalFile;
	
	private String originalFilePath;

	public DeltaVisionHeader header;

	/**
	 * Constructor.
	 * @param originalFilePath the path to the original file in the ROMIO
	 * repository.
	 * @param originalFile the original file object that corresponds to the 
	 * <code>originalFilePath</code>.
	 */
	public DeltaVision(String originalFilePath, OriginalFile originalFile)
	{
		try {
			this.originalFile = originalFile;
			this.originalFilePath = originalFilePath;
			initFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#calculateMessageDigest()
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
				throw new RuntimeException(e);
			}
		}

		return md.digest();
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#close()
	 */
	public void close() throws IOException {
		if (channel != null) {
			channel.close();
			channel = null;
		}
	}
	
	/**
	 * Returns the offset of the first plane in the file.
	 * @return See above.
	 */
	public long getFirstPlaneOffset()
	{
		return header.getExtendedHeaderSize() + 1024;
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getHypercube
	 */
    public PixelData getHypercube(List<Integer> offset, List<Integer> size, 
            List<Integer> step) throws IOException, DimensionsOutOfBoundsException 
    {
        throw new UnsupportedOperationException(
            "Not supported with DeltaVision pixel buffers.");
	}
                
	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getHypercubeDirect
	 */
    public byte[] getHypercubeDirect(List<Integer> offset, List<Integer> size, 
            List<Integer> step, byte[] buffer) 
            throws IOException, DimensionsOutOfBoundsException 
    {
        throw new UnsupportedOperationException(
            "Not supported with DeltaVision pixel buffers.");
	}
                
	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getPlaneRegionDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
	 */
	public byte[] getPlaneRegionDirect(Integer z, Integer c, Integer t,
			Integer count, Integer offset, byte[] buffer)
		throws IOException, DimensionsOutOfBoundsException
	{
		if (buffer.length != count * header.getBytesPerPixel())
			throw new ApiUsageException("Buffer size incorrect.");
		ByteBuffer plane = getPlane(z, c, t).getData();
		swapAndReorderIfRequired(plane, ByteBuffer.wrap(buffer), offset, count);
		return buffer;
	}
	
	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getPlane(java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	public PixelData getPlane(Integer z, Integer c, Integer t)
			throws IOException, DimensionsOutOfBoundsException
	{
		Long offset = getPlaneOffset(z, c, t);
		Integer size = getPlaneSize();
		PixelData d = getRegion(size, offset);
		return d;
	}
	
	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getPlaneDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
	 */
	public byte[] getPlaneDirect(Integer z, Integer c, Integer t, byte[] buffer)
			throws IOException, DimensionsOutOfBoundsException
	{
		if (buffer.length != getPlaneSize())
			throw new ApiUsageException("Buffer size incorrect.");
		ByteBuffer b = getPlane(z, c, t).getData();
		swapAndReorderIfRequired(b, ByteBuffer.wrap(buffer), 
		                         0, getSizeX() * getSizeY());
		return buffer;
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getPlaneOffset(java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	public Long getPlaneOffset(Integer z, Integer c, Integer t)
			throws DimensionsOutOfBoundsException {
		checkBounds(null, null, z, c, t);

		long firstPlaneOffset = getFirstPlaneOffset();
		int planeNumber = getPlaneNumber(z, c, t);
		long off = firstPlaneOffset + planeNumber * getPlaneSize(); 
		return off;
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getPlaneSize()
	 */
	public Integer getPlaneSize() {
		if (planeSize == null) {
			planeSize = getSizeX() * getSizeY() * getByteWidth();
		}
		return planeSize;
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getRegion(java.lang.Integer, java.lang.Long)
	 */
	public PixelData getRegion(Integer size, Long offset)
			throws IOException {
		FileChannel fileChannel = getFileChannel();
		ByteBuffer buf = fileChannel.map(MapMode.READ_ONLY, offset, size);
		if (!header.isNative())
			buf.order(ByteOrder.LITTLE_ENDIAN);
		return new PixelData(header.getOmeroPixelType().getValue(), buf);
	}
	
	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getRegionDirect(java.lang.Integer, java.lang.Long, byte[])
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

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getRow(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	public PixelData getRow(Integer y, Integer z, Integer c, Integer t)
			throws IOException, DimensionsOutOfBoundsException {
		Long offset = getRowOffset(y, z, c, t);
		Integer size = getRowSize();

		return getRegion(size, offset);
	}
	
    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getCol(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public PixelData getCol(Integer x, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException {
        // TODO: This could be supported, we're just not going to right now.
        throw new UnsupportedOperationException(
            "Not supported with DeltaVision pixel buffers.");
    }
	
	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getRowDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
	 */
	public byte[] getRowDirect(Integer y, Integer z, Integer c, Integer t,
			byte[] buffer) throws IOException, DimensionsOutOfBoundsException
	{
		if (buffer.length != getRowSize())
			throw new ApiUsageException("Buffer size incorrect.");
		ByteBuffer b = getRow(y, z, c, t).getData();
		swapAndReorderIfRequired(b, ByteBuffer.wrap(buffer), 0, getSizeX());
		return buffer;
	}

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getColDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
     */
    public byte[] getColDirect(Integer y, Integer z, Integer c, Integer t,
            byte[] buffer) throws IOException, DimensionsOutOfBoundsException
    {
        if (buffer.length != getRowSize())
            throw new ApiUsageException("Buffer size incorrect.");
        ByteBuffer b = getRow(y, z, c, t).getData();
        swapAndReorderIfRequired(b, ByteBuffer.wrap(buffer), 0, getSizeX());
        return buffer;
    }
    
	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getRowOffset(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	public Long getRowOffset(Integer y, Integer z, Integer c, Integer t)
			throws DimensionsOutOfBoundsException {
		checkBounds(null, y, z, c, t);
		Long planeOffset = getPlaneOffset(z, c, t);
		int sizeY = getSizeX();
		Integer rowSize = getRowSize();

		return planeOffset + rowSize * (sizeY - y);
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getRowSize()
	 */
	public Integer getRowSize() {
		if (rowSize == null) {
			rowSize = getSizeX() * getByteWidth();
		}

		return rowSize;
	}
	
    public Integer getColSize() {
        if (colSize == null) {
            colSize = getSizeY() * getByteWidth();
        }

        return colSize;
    }

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getSizeC()
	 */
	public int getSizeC() {
		return header.getSizeC();
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getSizeT()
	 */
	public int getSizeT() {
		return header.getSizeT();
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getSizeX()
	 */
	public int getSizeX() {
		return header.getSizeX();
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getSizeY()
	 */
	public int getSizeY() {
		return header.getSizeY();
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getSizeZ()
	 */
	public int getSizeZ() {
		return header.getSizeZ();
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getStack(java.lang.Integer, java.lang.Integer)
	 */
	public PixelData getStack(Integer c, Integer t) throws IOException,
			DimensionsOutOfBoundsException {
		throw new UnsupportedOperationException("This method is not supported");
	}
	
	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getStackDirect(java.lang.Integer, java.lang.Integer, byte[])
	 */
	public byte[] getStackDirect(Integer c, Integer t, byte[] buffer)
			throws IOException, DimensionsOutOfBoundsException
	{
		throw new UnsupportedOperationException("This method is not supported");
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getStackOffset(java.lang.Integer, java.lang.Integer)
	 */
	public Long getStackOffset(Integer c, Integer t)
			throws DimensionsOutOfBoundsException {
		checkBounds(null, null, null, c, t);

		Integer timepointSize = getTimepointSize();
		Integer stackSize = getStackSize();

		return (long) timepointSize * t + stackSize * c;
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getStackSize()
	 */
	public Integer getStackSize() {
		if (stackSize == null) {
			stackSize = getPlaneSize() * getSizeZ();
		}
		return stackSize;
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getTimepoint(java.lang.Integer)
	 */
	public PixelData getTimepoint(Integer t) throws IOException,
			DimensionsOutOfBoundsException {
		throw new UnsupportedOperationException("This method is not supported");
	}
	
	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getTimepointDirect(java.lang.Integer, byte[])
	 */
	public byte[] getTimepointDirect(Integer t, byte[] buffer)
			throws IOException, DimensionsOutOfBoundsException
	{
		throw new UnsupportedOperationException("This method is not supported");
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getTimepointOffset(java.lang.Integer)
	 */
	public Long getTimepointOffset(Integer t)
			throws DimensionsOutOfBoundsException {
		checkBounds(null, null, null, null, t);
		Integer timepointSize = getTimepointSize();

		return (long) timepointSize * t;
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getTimepointSize()
	 */
	public Integer getTimepointSize() {
		if (timepointSize == null) {
			timepointSize = getStackSize() * getSizeC();
		}
		return timepointSize;
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getTotalSize()
	 */
	public Integer getTotalSize() {
		if (totalSize == null) {
			totalSize = getTimepointSize() * getSizeT();
		}

		return totalSize;
	}


	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#checkBounds(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
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
	
	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getId()
	 */
	public long getId() {
		return 0;
	}

	/**
	 * Returns an object representing the header of this DeltaVision file.
	 * @return See above.
	 */
	public DeltaVisionHeader getHeader() {
		return header;
	}
	
	/**
	 * Sets the sequence of the file. <b>Should be used for testing ONLY.</b>
	 * @param sequence
	 */
	public void setSequence(short sequence)
	{
		header.setSequence(sequence);
	}
	
	// ---------- set or write methods ----------
	/**
	 * not implemented
	 */
	public void setRegion(Integer size, Long offset, byte[] buffer)
			throws IOException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/**
	 * not implemented
	 */
	public void setRegion(Integer size, Long offset, ByteBuffer buffer)
			throws IOException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/**
	 * not implemented
	 */
	public void setPlane(ByteBuffer buffer, Integer z, Integer c, Integer t)
			throws IOException, DimensionsOutOfBoundsException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/**
	 * not implemented
	 */
	public void setPlane(byte[] buffer, Integer z, Integer c, Integer t)
			throws IOException, DimensionsOutOfBoundsException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/**
	 * not implemented
	 */
	public void setRow(ByteBuffer buffer, Integer y, Integer z, Integer c,
			Integer t) throws IOException, DimensionsOutOfBoundsException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/**
	 * not implemented
	 */
	public void setStack(ByteBuffer buffer, Integer z, Integer c, Integer t)
			throws IOException, DimensionsOutOfBoundsException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/**
	 * not implemented
	 */
	public void setStack(byte[] buffer, Integer z, Integer c, Integer t)
			throws IOException, DimensionsOutOfBoundsException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/**
	 * not implemented
	 */
	public void setTimepoint(ByteBuffer buffer, Integer t) throws IOException,
			DimensionsOutOfBoundsException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/**
	 * not implemented
	 */
	public void setTimepoint(byte[] buffer, Integer t) throws IOException,
			DimensionsOutOfBoundsException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getPath()
	 */
	public String getPath() {
		return originalFilePath;
	}

	/**
	 * Returns the plane number (starting from <code>zero</code>) in the file.
	 * @param z the Z-section offset.
	 * @param c the channel.
	 * @param t the timepoint.
	 * @return See above.
	 */
	private int getPlaneNumber(int z, int c, int t)
	{
		int sequence = header.getSequence();
		switch (sequence)
		{
			case DeltaVisionHeader.ZTW_SEQUENCE:
				return getPlaneNumberZTW(z, c, t);
			case DeltaVisionHeader.WZT_SEQUENCE:
				return getPlaneNumberWZT(z, c, t);
			case DeltaVisionHeader.ZWT_SEQUENCE:
				return getPlaneNumberZWT(z, c, t);
			default:
				throw new RuntimeException("Unknown sequence: " + sequence);
		}
	}
	
	/**
	 * Returns the plane number (starting from <code>zero</code>) for a file
	 * with a "ZTW" plan sequence.
	 * @param z the Z-section offset.
	 * @param c the channel.
	 * @param t the timepoint.
	 * @return See above.
	 */
	private int getPlaneNumberZTW(int z, int c, int t)
	{
		int a = t * getSizeZ();
		int b = c * getSizeZ() * getSizeT();
		return z + a + b;
	}

	/**
	 * Returns the plane number (starting from <code>zero</code>) for a file
	 * with a "WZT" plan sequence.
	 * @param z the Z-section offset.
	 * @param c the channel.
	 * @param t the timepoint.
	 * @return See above.
	 */
	private int getPlaneNumberWZT(int z, int c, int t)
	{
		int a = z * getSizeC();
		int b = getSizeC() * getSizeZ() * t;
		return c + a + b;
	}
	
	/**
	 * Returns the plane number (starting from <code>zero</code>) for a file
	 * with a "ZWT" plan sequence.
	 * @param z the Z-section offset.
	 * @param c the channel.
	 * @param t the timepoint.
	 * @return See above.
	 */
	private int getPlaneNumberZWT(int z, int c, int t)
	{
		int a = c * getSizeZ();
		int b = getSizeZ() * getSizeC() * t;
		return z + a + b;
	}
	
	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getByteWidth()
	 */
	public int getByteWidth()
	{
		return header.getBytesPerPixel();
	}
	
	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#isSigned()
	 */
	public boolean isSigned()
	{
		switch(header.getPixelType())
		{
			case DeltaVisionHeader.PIXEL_TYPE_BYTE:
			case DeltaVisionHeader.PIXEL_TYPE_SIGNED_SHORT:
				return true;
			default:
				return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#isFloat()
	 */
	public boolean isFloat()
	{
		switch(header.getPixelType())
		{
			case DeltaVisionHeader.PIXEL_TYPE_2BYTE_COMPLEX:
			case DeltaVisionHeader.PIXEL_TYPE_4BYTE_COMPLEX:
			case DeltaVisionHeader.PIXEL_TYPE_FLOAT:
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * This method is used to establish most of the DeltaVision data that the
	 * Pixels object once held. Key data is obtained from a random access data
	 * structure after the DeltaVision file header is read into memory.
	 * 
	 * @throws IOException if there is an error reading from the file.
	 */
	private void initFile() throws IOException
	{
		channel = getFileChannel();
		buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, 1024);
		if (header == null) {
			header = new DeltaVisionHeader(buf, true);
		}
		//swapBuffer = (MappedByteBuffer)
		//	MappedByteBuffer.allocateDirect(getPlaneSize());
	}

	/**
	 * Private access only to read-write file
	 * 
	 * @return opened file channel.
	 */
	private FileChannel getFileChannel() throws FileNotFoundException
	{
		if (channel == null)
		{
			RandomAccessFile f = new RandomAccessFile(originalFilePath, "r");
			channel = f.getChannel();
		}
		return channel;
	}

    /**
     * Examines a byte array to see if it needs to be byte swapped. It also 
	 * handles the re-ordering of the origin from top-left to bottom-left.
     * @param from The byte buffer to swap and re-order from.
     * @param to The byte buffer to swap and re-order into.
     * @param offset The pixel offset to start from.
     * @param count The number of pixels to process.
     * @throws IOException if there is an error read from the file.
     */
	private void swapAndReorderIfRequired(ByteBuffer from, ByteBuffer to, 
	                                      int offset, int count)
		throws IOException
	{
		int pixelType = header.getPixelType();
		int rowSize = getSizeX();
		int reorderedOffset;
		int size;
		
		// NOTE: The various byte buffers that are created and used for
		// re-ordering have already had the correct byte order set through the
		// order() method so we do not have to swap pixel values by hand.
		switch (pixelType)
		{
			case DeltaVisionHeader.PIXEL_TYPE_BYTE:
			{
				size = from.capacity();
				for (int i = 0; i < count; i++)
				{
					reorderedOffset = 
						ReorderedPixelData.getReorderedPixelOffset(
								size, i + offset, rowSize);
					to.put(i, from.get(reorderedOffset));
				}
				break;
			}
			case DeltaVisionHeader.PIXEL_TYPE_UNSIGNED_SHORT:
			case DeltaVisionHeader.PIXEL_TYPE_SIGNED_SHORT:
			{
				ShortBuffer swapBuf = from.asShortBuffer();
				ShortBuffer copyBuf = to.asShortBuffer();
				size = from.capacity() / 2;
				for (int i = 0; i < count; i++)
				{
					reorderedOffset =
						ReorderedPixelData.getReorderedPixelOffset(
								size, i + offset, rowSize);
					copyBuf.put(i, swapBuf.get(reorderedOffset));
				}
				break;
			}
			case DeltaVisionHeader.PIXEL_TYPE_FLOAT:
			{
				IntBuffer swapBuf = from.asIntBuffer();
				IntBuffer copyBuf = to.asIntBuffer();
				size = from.capacity() / 4;
				for (int i = 0; i < count; i++)
				{
					reorderedOffset = 
						ReorderedPixelData.getReorderedPixelOffset(
								size, i + offset, rowSize);
					copyBuf.put(i, swapBuf.get(reorderedOffset));
				}
				break;
			}
			// This particular pixel type is basically just double the number
			// of shorts, so we'll just handle that.
			case DeltaVisionHeader.PIXEL_TYPE_2BYTE_COMPLEX:
			{
				ShortBuffer swapBuf = from.asShortBuffer();
				ShortBuffer copyBuf = to.asShortBuffer();
				size = from.capacity() / 2;
				for (int i = 0; i < count * 2; i++)
				{
					reorderedOffset =
						ReorderedPixelData.getReorderedPixelOffset(
								size, i + offset, rowSize * 2);
					copyBuf.put(i, swapBuf.get(reorderedOffset));
				}
				break;
			}
			// This particular pixel type is basically just double the number
			// of 4-byte floating point values, so we'll just handle that.
			case DeltaVisionHeader.PIXEL_TYPE_4BYTE_COMPLEX:
			{
				IntBuffer swapBuf = from.asIntBuffer();
				IntBuffer copyBuf = to.asIntBuffer();
				size = from.capacity() / 4;
				for (int i = 0; i < count * 2; i++)
				{
					reorderedOffset = 
						ReorderedPixelData.getReorderedPixelOffset(
								size, i + offset, rowSize * 2);
					copyBuf.put(i, swapBuf.get(reorderedOffset));
				}
				break;
			}
			default:
				throw new RuntimeException(
					"Unsupported DeltaVision pixel type: "+ pixelType);
		}
	}

	/**
     * Implemented as specified by {@link PixelBuffer} I/F.
     * @see PixelBuffer#getPlaneRegion(Integer, Integer, Integer, Integer, 
     * Integer, Integer, Integer, Integer)
	 */
    public PixelData getPlaneRegion(Integer x, Integer y, Integer width, 
    		Integer height, Integer z, Integer c, Integer t, Integer stride)
            throws IOException, DimensionsOutOfBoundsException {
    	return null;
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTile(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public PixelData getTile(Integer z, Integer c, Integer t, Integer x,
            Integer y, Integer w, Integer h) throws IOException
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTileDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
     */
    public byte[] getTileDirect(Integer z, Integer c, Integer t, Integer x,
            Integer y, Integer w, Integer h, byte[] buffer) throws IOException
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setTile(byte[], java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public void setTile(byte[] buffer, Integer z, Integer c, Integer t, Integer x, Integer y,
            Integer w, Integer h) throws IOException,
            BufferOverflowException
    {
        throw new UnsupportedOperationException("Not implemented.");
    }
	
}
