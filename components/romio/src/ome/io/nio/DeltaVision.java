package ome.io.nio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
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

	protected int initExtHdrOffset = 1024;

	protected final String currentOrder = "XYCZT";

	private OriginalFile originalFile;

	public static final int NULL_PLANE_SIZE = 64;

	public boolean little = false;
	
	public DeltaVisionHeader header;

	public static final byte[] nullPlane = new byte[] { -128, 127, -128, 127,
			-128, 127, -128, 127, -128, 127, // 10
			-128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 20
			-128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 30
			-128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 40
			-128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 50
			-128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 60
			-128, 127, -128, 127 };

	/**
	 * Constructor requires OriginalFile object
	 * 
	 * @param originalFile
	 */
	public DeltaVision(OriginalFile originalFile, boolean little) {

		this.little = little;

		try {
			this.originalFile = originalFile;
			initFile();
		} catch (IOException ioex) {
			throw new RuntimeException(
					"Original file not available, Critical Error!");
		} catch (RuntimeException rtex) {
			throw new RuntimeException(
					"The binary file is not a valid DeltaVision file!");

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

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getPlane(java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	public MappedByteBuffer getPlane(Integer z, Integer c, Integer t)
			throws IOException, DimensionsOutOfBoundsException {
		Long offset = getPlaneOffset(z, c, t);
		Integer size = getPlaneSize();
		
		MappedByteBuffer region = getRegion(size, offset);

		byte[] myNullPlane = nullPlane;

		for (int i = 0; i < NULL_PLANE_SIZE; i++) {
			if (region.get(i) != myNullPlane[i]) {
				return region;
			}
		}

		return null; // All of the nullPlane bytes match, non-filled plane

	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getPlaneOffset(java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	public Long getPlaneOffset(Integer z, Integer c, Integer t)
			throws DimensionsOutOfBoundsException {

		checkBounds(null, z, c, t);

		Integer timepointSize = getTimepointSize();
		Integer stackSize = getStackSize();
		Integer planeSize = getPlaneSize();

		return (long) timepointSize * t + (long) stackSize * c
				+ (long) planeSize * z;

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
		buf.order(ByteOrder.BIG_ENDIAN);
		return buf;

	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getRow(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	public MappedByteBuffer getRow(Integer y, Integer z, Integer c, Integer t)
			throws IOException, DimensionsOutOfBoundsException {
		// TODO - affected by stack and timepoint
		Long offset = getRowOffset(y, z, c, t);
		Integer size = getRowSize();

		return getRegion(size, offset);
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getRowOffset(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	public Long getRowOffset(Integer y, Integer z, Integer c, Integer t)
			throws DimensionsOutOfBoundsException {
		// TODO - affected by stack and timepoint
		checkBounds(y, z, c, t);

		Integer rowSize = getRowSize();
		Integer timepointSize = getTimepointSize();
		Integer stackSize = getStackSize();
		Integer planeSize = getPlaneSize();

		return (long) rowSize * y + (long) timepointSize * t + (long) stackSize
				* c + (long) planeSize * z;
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getRowSize()
	 */
	public Integer getRowSize() {
		// row is X-wide
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
		// TODO - affected by ZCT ordering
		Long offset = getStackOffset(c, t);
		Integer size = getStackSize();

		return getRegion(size, offset);
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getStackOffset(java.lang.Integer, java.lang.Integer)
	 */
	public Long getStackOffset(Integer c, Integer t)
			throws DimensionsOutOfBoundsException {
		// TODO - affected by ZCT ordering
		checkBounds(null, null, c, t);

		Integer timepointSize = getTimepointSize();
		Integer stackSize = getStackSize();

		return (long) timepointSize * t + (long) stackSize * c;
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getStackSize()
	 */
	public Integer getStackSize() {
		// stack is X-Y (plane) by Z (focalpoints)
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
		// TODO - affected by ZCT ordering
		Long offset = getTimepointOffset(t);
		Integer size = getTimepointSize();

		return getRegion(size, offset);
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.PixelBuffer#getTimepointOffset(java.lang.Integer)
	 */
	public Long getTimepointOffset(Integer t)
			throws DimensionsOutOfBoundsException {
		// TODO - affected by ZCT ordering
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

	public DeltaVisionHeader getHeader() {
		return header;
	}

	public void setHeader(DeltaVisionHeader header) {
		this.header = header;
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
		return originalFile.getPath();
	}

	// ---------- private methods ----------------------

	/**
	 * utility method returns byte width, 1, 2, 4, etc
	 */
	private int getByteWidth() {
		return header.getBytesPerPixel();
	}
	
	/**
	 * This method is used to establish most of the DeltaVision data that the
	 * Pixels object once held. Key data is obtained from a random access data
	 * structure after the DeltaVision file header is read into memory.
	 * 
	 * @throws IOException
	 */
	private void initFile() throws IOException, RuntimeException {

		File file = new File(originalFile.getPath());
		RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
		
		channel = randomAccessFile.getChannel();
		
		buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, 1024);

		if (header == null) {
			header = new DeltaVisionHeader(buf, true);
		}
	}

	/**
	 * Private access only to read-write file
	 * 
	 * @return
	 */
	private FileChannel getFileChannel() {
		return channel;
	}

}
