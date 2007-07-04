package ome.io.nio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ome.io.nio.DimensionsOutOfBoundsException;
import ome.model.core.OriginalFile;

/**
 * Class implementation of the PixelBuffer interface for a DeltaVision specific
 * image file.
 * <p>
 * Copyright 2007 Glencoe Software Inc. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt 
 * <p/>
 *
 * @author David L. Whitehurst &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:david@glencoesoftware.com">david@glencoesoftware.com</a>
 * @version $Revision$
 * @since 3.0
 * @see PixelBuffer
 */
public class DeltaVision implements PixelBuffer {

	private FileChannel channel;

	private Integer rowSize;

	private Integer planeSize;

	private Integer stackSize;

	private Integer timepointSize;

	private Integer totalSize;

	protected MappedByteBuffer buf;

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
				MappedByteBuffer buffer = getTimepoint(t);
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
	 * @see ome.io.nio.PixelBuffer#getPlane(java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	public MappedByteBuffer getPlane(Integer z, Integer c, Integer t)
			throws IOException, DimensionsOutOfBoundsException {
		Long offset = getPlaneOffset(z, c, t);
		Integer size = getPlaneSize();
		
		return getRegion(size, offset);
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getPlaneOffset(java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	public Long getPlaneOffset(Integer z, Integer c, Integer t)
			throws DimensionsOutOfBoundsException {
		checkBounds(null, z, c, t);

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
	public MappedByteBuffer getRegion(Integer size, Long offset)
			throws IOException {
		FileChannel fileChannel = getFileChannel();
		MappedByteBuffer buf = fileChannel.map(MapMode.READ_ONLY, offset, size);
		return swapIfRequired(buf);
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getRow(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	public MappedByteBuffer getRow(Integer y, Integer z, Integer c, Integer t)
			throws IOException, DimensionsOutOfBoundsException {
		Long offset = getRowOffset(y, z, c, t);
		Integer size = getRowSize();

		return getRegion(size, offset);
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getRowOffset(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	public Long getRowOffset(Integer y, Integer z, Integer c, Integer t)
			throws DimensionsOutOfBoundsException {
		checkBounds(y, z, c, t);
		Long planeOffset = getPlaneOffset(z, c, t);
		Integer rowSize = getRowSize();

		return planeOffset + rowSize * y;
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
	public MappedByteBuffer getStack(Integer c, Integer t) throws IOException,
			DimensionsOutOfBoundsException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getStackOffset(java.lang.Integer, java.lang.Integer)
	 */
	public Long getStackOffset(Integer c, Integer t)
			throws DimensionsOutOfBoundsException {
		checkBounds(null, null, c, t);

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
	public MappedByteBuffer getTimepoint(Integer t) throws IOException,
			DimensionsOutOfBoundsException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getTimepointOffset(java.lang.Integer)
	 */
	public Long getTimepointOffset(Integer t)
			throws DimensionsOutOfBoundsException {
		checkBounds(null, null, null, t);
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
	public void setSequence(int sequence)
	{
		header.setSequence(sequence);
	}
	
	// ---------- set or write methods ----------
	/**
	 * not implemented
	 */
	public void setRegion(Integer size, Long offset, byte[] buffer)
			throws IOException, BufferOverflowException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/**
	 * not implemented
	 */
	public void setRegion(Integer size, Long offset, ByteBuffer buffer)
			throws IOException, BufferOverflowException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/**
	 * not implemented
	 */
	public void setPlane(ByteBuffer buffer, Integer z, Integer c, Integer t)
			throws IOException, DimensionsOutOfBoundsException,
			BufferOverflowException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/**
	 * not implemented
	 */
	public void setPlane(byte[] buffer, Integer z, Integer c, Integer t)
			throws IOException, DimensionsOutOfBoundsException,
			BufferOverflowException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/**
	 * not implemented
	 */
	public void setRow(ByteBuffer buffer, Integer y, Integer z, Integer c,
			Integer t) throws IOException, DimensionsOutOfBoundsException,
			BufferOverflowException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/**
	 * not implemented
	 */
	public void setStack(ByteBuffer buffer, Integer z, Integer c, Integer t)
			throws IOException, DimensionsOutOfBoundsException,
			BufferOverflowException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/**
	 * not implemented
	 */
	public void setStack(byte[] buffer, Integer z, Integer c, Integer t)
			throws IOException, DimensionsOutOfBoundsException,
			BufferOverflowException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/**
	 * not implemented
	 */
	public void setTimepoint(ByteBuffer buffer, Integer t) throws IOException,
			DimensionsOutOfBoundsException, BufferOverflowException {
		throw new UnsupportedOperationException("This method is not supported");
	}

	/**
	 * not implemented
	 */
	public void setTimepoint(byte[] buffer, Integer t) throws IOException,
			DimensionsOutOfBoundsException, BufferOverflowException {
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
	
	/**
	 * Returns the file's byte width.
	 * @return See above.
	 */
	private int getByteWidth()
	{
		return header.getBytesPerPixel();
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
     * Examines a byte array to see if it needs to be byte swapped. If byte
     * swapping is deemed necessary a new swap buffer is created and returned.
     * @param buffer The byte buffer to check and modify if required.
     * @return <i>buffer</i> directly if swapping is required, otherwise a newly
     * allocated buffer with byte swapped pixel values.
     * @throws IOException if there is an error read from the file.
     * @throws FormatException if there is an error during metadata parsing.
     */
	private MappedByteBuffer swapIfRequired(MappedByteBuffer buffer)
		throws IOException
	{
		int pixelType = header.getPixelType();
		int bytesPerPixel = header.getBytesPerPixel();
		
		// We've got nothing to do if the samples are only 8-bits wide, if they
		// are floating point or complex.
		switch (pixelType)
		{
			case DeltaVisionHeader.PIXEL_TYPE_FLOAT:
			case DeltaVisionHeader.PIXEL_TYPE_2BYTE_COMPLEX:
			case DeltaVisionHeader.PIXEL_TYPE_4BYTE_COMPLEX:
				return buffer;
		}
		if (bytesPerPixel == 1)
			return buffer;
		
		if (!header.isNative())  // Is little endian.
		{
			if (bytesPerPixel == 2)  // Short.
			{
				MappedByteBuffer swapBuffer = (MappedByteBuffer)
					MappedByteBuffer.allocateDirect(buffer.capacity());
				ShortBuffer buf = buffer.asShortBuffer();
				ShortBuffer swapBuf = swapBuffer.asShortBuffer();
				for (int i = 0; i < (buffer.capacity() / 2); i++) {
					swapBuf.put(i, swap(buf.get(i)));
				}
				return swapBuffer;
			} else if (bytesPerPixel == 4) {  // Integer or unsigned integer.
				MappedByteBuffer swapBuffer = (MappedByteBuffer)
					MappedByteBuffer.allocateDirect(buffer.capacity());
				IntBuffer buf = buffer.asIntBuffer();
				IntBuffer swapBuf = swapBuffer.asIntBuffer();
				for (int i = 0; i < (buffer.capacity() / 4); i++) {
					swapBuf.put(i, swap(buf.get(i)));
				}
				return swapBuffer;
			} else {
				throw new RuntimeException(
					"Unsupported sample bit width: '" + bytesPerPixel + "'");
			}
		}
		return buffer;
	}
	
	/** Byte swaps one short value. */
	public static short swap(short x) {
		return (short) ((x << 8) | ((x >> 8) & 0xFF));
	}

	/** Byte swaps one integer value. */
	public static int swap(int x) {
		return (int) ((swap((short) x) << 16) | (swap((short) (x >> 16)) & 0xFFFF));
	}
}
