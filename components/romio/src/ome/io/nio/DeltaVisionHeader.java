package ome.io.nio;

import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;

/**
 * Temporary class implementation of an image file header for the server-side
 * import of a DeltaVision file.
 * <p>
 * Copyright 2007 Glencoe Software Inc. All rights reserved. Use is subject to
 * license terms supplied in LICENSE.txt <p/>
 * 
 * @author David L. Whitehurst &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:david@glencoesoftware.com">david@glencoesoftware.com</a>
 * @version $Revision$
 * @since 3.0
 * @see DeltaVision
 */
public class DeltaVisionHeader {

	private MappedByteBuffer data;
	
	/**
	 * A common sequence of processed images. Sometimes referred
 	 * to as "interleaved".
 	 */
	public static final int ZTW_SEQUENCE = 0;
	
	/**
	 * The most common sequence of images aquired from a microscope. Sometimes 
	 * referred to as "non-interleaved" since the wavelengths are interleaved 
	 * with the Z sections.
	 */
	public static final int WZT_SEQUENCE = 1;
	
	/**
	 * A new image sequence, as of DeltaVision version 2.10. Although not 
	 * widely used, ZWT will find uses with certain processing algorithms 
	 * and data collection schemes.
	 */
	public static final int ZWT_SEQUENCE = 2;
	
	/** The sequence to allow a caller to modify it for testing purposes. */
	private Integer sequence;
	
	/** Whether or not the file is of native endianness. */
	private Boolean isNative;
	
	/** Pixels are of type 1 byte, unsigned integer. */
	public static final int PIXEL_TYPE_BYTE = 0;
	
	/** Pixels are of type 2 byte, unsigned integer. */
	public static final int PIXEL_TYPE_SIGNED_SHORT = 1;
	
	/** Pixels are of type 4 byte, floating-point. */
	public static final int PIXEL_TYPE_FLOAT = 2;
	
	/**
	 * Pixels are of type 4 byte, complex, composed of two 2 byte signed 
	 * integers.
	 */
	public static final int PIXEL_TYPE_2BYTE_COMPLEX = 3;
	
	/** 
	 * Pixels are of type 8 byte, complex, composed of two 4 byte 
	 * floating-point numbers. 
	 */
	public static final int PIXEL_TYPE_4BYTE_COMPLEX = 4;
	
	/** Pixels are of type 2 byte, signed integer. */
	public static final int PIXEL_TYPE_UNSIGNED_SHORT = 6;

	private static final int SIZE_X_OFFSET = 0;

	private static final int SIZE_Y_OFFSET = 4;

	private static final int IMAGE_COUNT_OFFSET = 8;

	private static final int PIXEL_TYPE_OFFSET = 12;

	private static final int EXT_HEADER_SIZE_OFFSET = 92;

	private static final int IMAGE_TYPE_OFFSET = 160;

	private static final int SIZE_T_OFFSET = 180;

	private static final int IMAGE_SEQUENCE_OFFSET = 182;

	private static final int SIZE_C_OFFSET = 196;

	public static final int BYTE_WIDTH = 1;

	private static final short NATIVE_DVID = -16224;

	private static final short BYTE_SWAPPED_DVID = -24384;

	private static final int IMAGE_HEADER_SIZE = 1024;

	private static final int DVID_OFFSET = 96;

	/**
	 * Constructor
	 * 
	 * @param data
	 * @param endian
	 */
	public DeltaVisionHeader(MappedByteBuffer data, boolean endian) {
		this.data = data;
		
		// make sure this is a header
		if (data.capacity() != IMAGE_HEADER_SIZE) {
			throw new IllegalArgumentException("Buffer size " + data.capacity()
					+ " larger than expected size: " + IMAGE_HEADER_SIZE);
		}

		// endianness
		if (isNative()) {
			data.order(ByteOrder.BIG_ENDIAN);
		} else {
			data.order(ByteOrder.LITTLE_ENDIAN);
		}

		// set our data
		this.data = data;
	}

	/**
	 * Returns the number of channels
	 * @return
	 */
	public int getSizeC() {
		return data.getShort(SIZE_C_OFFSET);
	}

	/**
	 * Returns the number of plane images
	 * @return int
	 */
	public int getImageCount() {
		return data.getInt(IMAGE_COUNT_OFFSET);
	}

	/**
	 * Returns the numeric image type
	 * @return
	 */
	public int getImageTypeCode() {
		return data.getShort(IMAGE_TYPE_OFFSET);
	}

	/**
	 * Returns the number of bytes to the beginning of the Pixels
	 * @return
	 */
	public int getPixelBeginOffset() {
		return IMAGE_HEADER_SIZE + getExtendedHeaderSize();
	}

	/**
	 * Returns the size in bytes of the extended header
	 * @return
	 */
	public int getExtendedHeaderSize() {
		return data.getInt(EXT_HEADER_SIZE_OFFSET);
	}

	/**
	 * Returns a numeric value for the pixel type
	 * @return
	 */
	public int getPixelType() {
		return data.getInt(PIXEL_TYPE_OFFSET);
	}

	/**
	 * Sets the sequence of the file. <b>Should be used for testing ONLY.</b>
	 * @param sequence
	 */
	public void setSequence(int sequence)
	{
		this.sequence = sequence;
	}
	
	/**
	 * Returns a numeric value that represents the Z,C, and T ordering
	 * @return
	 */
	public int getSequence() {
		if (sequence == null)
			sequence = data.getInt(IMAGE_SEQUENCE_OFFSET);
		return sequence;
	}

	/**
	 * Returns the number of timepoints taken
	 * @return
	 */
	public int getSizeT() {
		return data.getShort(SIZE_T_OFFSET);
	}

	/**
	 * Returns the width (no. of X pixels) of a single plane image
	 * @return
	 */
	public int getSizeX() {
		return data.getInt(SIZE_X_OFFSET);
	}

	/**
	 * Returns the height (no. of Y pixels) of a single plane image
	 * @return
	 */
	public int getSizeY() {
		return data.getInt(SIZE_Y_OFFSET);
	}

	/**
	 * Returns the number of focal points (Z-dimension) taken
	 * @return
	 */
	public int getSizeZ() {
		return getImageCount() / (getSizeC() * getSizeT());
	}

	/**
	 * Returns the number of bytes per pixel.
	 * @return See above.
	 */
	public int getBytesPerPixel()
	{
		int pixelType = getPixelType(); 
		switch (pixelType) {
		case PIXEL_TYPE_BYTE:
			return 1;
		case PIXEL_TYPE_UNSIGNED_SHORT:
		case PIXEL_TYPE_SIGNED_SHORT:
			return 2;
		case PIXEL_TYPE_FLOAT:
		case PIXEL_TYPE_2BYTE_COMPLEX:
			return 4;
		case PIXEL_TYPE_4BYTE_COMPLEX:
			return 8;
		default:
			throw new RuntimeException("Unknown pixel type: " + pixelType);
		}
	}

	/**
	 * Returns true if the DeltaVision file is of native endianness.
	 * @return See above.
	 * @throws RuntimeException if there is an error with the DeltaVision file's
	 * DVID.
	 */
	public boolean isNative()
	{
		if (isNative == null)
		{
			short dvid = data.getShort(DVID_OFFSET);

			if (dvid == NATIVE_DVID)
			{
				isNative = true;
			}
			else if (dvid == BYTE_SWAPPED_DVID)
			{
				isNative = false;
			}
			else
			{
				throw new RuntimeException("Unexpected DVID: " + dvid);
			}
		}
		return isNative;
	}
}
