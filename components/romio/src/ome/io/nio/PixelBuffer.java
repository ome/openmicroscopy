/*
 * ome.io.nio.PixelBuffer
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.nio;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

/**
 * 
 * This interface declares the I/O responsibilities of a buffer, file or
 * otherwise, that contains a 5-dimensional Pixel array (XYZCT).
 * 
 * @author Chris Allan
 *         &nbsp;<a href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version $Revision$
 * @since 3.0
 * 
 */
public interface PixelBuffer
{
    /**
     * Closes the buffer, cleaning up file state.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public void close() throws IOException;

    /**
     * Checks to ensure that no one particular axis has an offset out of bounds.
     * <code>null</code> may be passed as the argument to any one of the offsets
     * to ignore it for the purposes of bounds checking.
     * @param y offset across the Y-axis of the pixel buffer to check.
     * @param z offset across the Z-axis of the pixel buffer to check.
     * @param c offset across the C-axis of the pixel buffer to check.
     * @param t offset across the T-axis of the pixel buffer to check.
     * @throws DimensionsOutOfBoundsException if <code>y</code>,
     * <code>z</code>, <code>c</code> or <code>t</code> is out of bounds.
     */
    public void checkBounds(Integer y, Integer z, Integer c, Integer t)
    	throws DimensionsOutOfBoundsException;
    
    /**
     * Retrieves the in memory size of a 2D image plane in this pixel buffer.
     * @return 2D image plane size in bytes (sizeX*sizeY*ByteWidth).
     */
    public Integer getPlaneSize();

    /**
     * Retreives the in memory size of a row or scanline of pixels in this
     * pixel buffer.
     * @return row or scanline size in bytes (sizeX*ByteWidth)
     */
    public Integer getRowSize();

    /**
     * Retreives the in memory size of the entire number of optical sections
     * for a <b>single</b> wavelength or channel at a particular timepoint in
     * this pixel buffer.
     * @return stack size in bytes (sizeX*sizeY*sizeZ*ByteWidth).
     */
    public Integer getStackSize();

    /**
     * Retrieves the in memory size of the entire number of optical sections for
     * <b>all</b> wavelengths or channels at a particular timepoint in this
     * pixel buffer.
     * @return timepoint size in bytes (sizeX*sizeY*sizeZ*sizeC*ByteWidth).
     */
    public Integer getTimepointSize();

    /**
     * Retrieves the in memory size of the entire pixel buffer.
     * @return total size of the pixel size in bytes
     * (sizeX*sizeY*sizeZ*sizeC*sizeT*ByteWidth).
     */
    public Integer getTotalSize();

    /**
     * Retrieves the offset for a particular row or scanline in this pixel 
     * buffer.
     * @param y offset across the Y-axis of the pixel buffer.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @return offset of the row or scaline.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link checkBounds()}.
     */
    public Long getRowOffset(Integer y, Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException;

    /**
     * Retrieves the offset for a particular 2D image plane in this pixel
     * buffer.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @return offset of the 2D image plane.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link checkBounds()}.
     */
    public Long getPlaneOffset(Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException;

    /**
     * Retreives the offset for the entire number of optical sections
     * for a <b>single</b> wavelength or channel at a particular timepoint in
     * this pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @return offset of the stack.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link checkBounds()}.
     */
    public Long getStackOffset(Integer c, Integer t)
            throws DimensionsOutOfBoundsException;
    

    /**
     * Retrieves the in memory size of the entire number of optical sections for
     * <b>all</b> wavelengths or channels at a particular timepoint in this
     * pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @return offset of the timepoint.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link checkBounds()}.
     */
    public Long getTimepointOffset(Integer t)
            throws DimensionsOutOfBoundsException;

    /**
     * Retrieves a region from this pixel buffer.
     * @param size byte width of the region to retrieve.
     * @param offset offset within the pixel buffer.
     * @return buffer containing the data.
     * @throws IOException if there is a problem reading from the pixel buffer.
     */
    public MappedByteBuffer getRegion(Integer size, Long offset)
            throws IOException;

    /**
     * Retrieves a particular row or scanline from this pixel buffer.
     * @param y offset across the Y-axis of the pixel buffer.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @return buffer containing the data which comprises this row or scanline.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link checkBounds()}.
     */
    public MappedByteBuffer getRow(Integer y, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException;

    /**
     * Retrieves a particular 2D image plane from this pixel buffer.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @return buffer containing the data which comprises this 2D image plane.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link checkBounds()}.
     */
    public MappedByteBuffer getPlane(Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException;

    /**
     * Retreives the the entire number of optical sections for a <b>single</b>
     * wavelength or channel at a particular timepoint in this pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @return buffer containing the data which comprises this stack.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link checkBounds()}.
     */
    public MappedByteBuffer getStack(Integer c, Integer t) throws IOException,
            DimensionsOutOfBoundsException;

    /**
     * Retrieves the entire number of optical sections for <b>all</b>
     * wavelengths or channels at a particular timepoint in this pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @return buffer containing the data which comprises this timepoint.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link checkBounds()}.
     */
    public MappedByteBuffer getTimepoint(Integer t) throws IOException,
            DimensionsOutOfBoundsException;

    /**
     * Sets a region in this pixel buffer.
     * @param size byte width of the region to set.
     * @param offset offset within the pixel buffer.
     * @param buffer a byte array of the data.
     * @throws IOException if there is a problem writing to the pixel buffer.
     * @throws BufferOverflowException if <code>buffer.length > size</code>.
     */
    public void setRegion(Integer size, Long offset, byte[] buffer)
            throws IOException, BufferOverflowException;

    /**
     * Sets a region in this pixel buffer.
     * @param size byte width of the region to set.
     * @param offset offset within the pixel buffer.
     * @param buffer a byte buffer of the data.
     * @throws IOException if there is a problem writing to the pixel buffer.
     * @throws BufferOverflowException if <code>buffer.length > size</code>.
     */
    public void setRegion(Integer size, Long offset, ByteBuffer buffer)
            throws IOException, BufferOverflowException;

    /**
     * Sets a particular row or scanline in this pixel buffer.
     * @param buffer a byte buffer of the data comprising this row or scanline.
     * @param y offset across the Y-axis of the pixel buffer.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link checkBounds()}.
     * @throws BufferOverflowException if
     * <code>buffer.length > {@link getRowSize()}</code>.
     */
    public void setRow(ByteBuffer buffer, Integer y, Integer z, Integer c,
            Integer t) throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException;

    /**
     * Sets a particular 2D image plane in this pixel buffer.
     * @param buffer a byte array of the data comprising this 2D image plane.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @throws IOException if there is a problem writing to the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link checkBounds()}.
     * @throws BufferOverflowException if
     * <code>buffer.length > {@link getPlaneSize()}</code>.
     */
    public void setPlane(ByteBuffer buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException;

    /**
     * Sets a particular 2D image plane in this pixel buffer.
     * @param buffer a byte buffer of the data comprising this 2D image plane.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @throws IOException if there is a problem writing to the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link checkBounds()}.
     * @throws BufferOverflowException if
     * <code>buffer.length > {@link getPlaneSize()}</code>.
     */
    public void setPlane(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException;

    /**
     * Sets the entire number of optical sections for a <b>single</b>
     * wavelength or channel at a particular timepoint in this pixel buffer.
     * @param buffer a byte buffer of the data comprising this stack.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @throws IOException if there is a problem writing to the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link checkBounds()}.
     * @throws BufferOverflowException if
     * <code>buffer.length > {@link getStackSize()}</code>.
     */
    public void setStack(ByteBuffer buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException;

    /**
     * Sets the entire number of optical sections for a <b>single</b>
     * wavelength or channel at a particular timepoint in this pixel buffer.
     * @param buffer a byte array of the data comprising this stack.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @throws IOException if there is a problem writing to the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link checkBounds()}.
     * @throws BufferOverflowException if
     * <code>buffer.length > {@link getStackSize()()}</code>.
     */
    public void setStack(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException;

    /**
     * Sets the entire number of optical sections for <b>all</b>
     * wavelengths or channels at a particular timepoint in this pixel buffer.
     * @param buffer a byte buffer of the data comprising this timepoint.
     * @param t offset across the T-axis of the pixel buffer.
     * @throws IOException if there is a problem writing to the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link checkBounds()}.
     * @throws BufferOverflowException if
     * <code>buffer.length > {@link getTimepointSize()}</code>.
     */
    public void setTimepoint(ByteBuffer buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException, BufferOverflowException;

    /**
     * Sets the entire number of optical sections for <b>all</b>
     * wavelengths or channels at a particular timepoint in this pixel buffer.
     * @param buffer a byte array of the data comprising this timepoint.
     * @param t offset across the T-axis of the pixel buffer.
     * @throws IOException if there is a problem writing to the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link checkBounds()}.
     * @throws BufferOverflowException if
     * <code>buffer.length > {@link getTimepointSize()}</code>.
     */
    public void setTimepoint(byte[] buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException, BufferOverflowException;

    /**
     * Calculates a SHA-1 message digest for the entire pixel buffer.
     * @return byte array containing the message digest.
     * @throws IOException if there is a problem reading from the pixel buffer.
     */
    public byte[] calculateMessageDigest() throws IOException;

	/**
	 * Delegates to {@link Pixels.getId()}.
	 */
	public long getId();
	
	/**
	 * Delegates to {@link Pixels.getSizeX()}.
	 */
	public int getSizeX();

	/**
	 * Delegates to {@link Pixels.getSizeY()}.
	 */
	public int getSizeY();

	/**
	 * Delegates to {@link Pixels.getSizeZ()}.
	 */
	public int getSizeZ();

	/**
	 * Delegates to {@link Pixels.getSizeC()}.
	 */
	public int getSizeC();

	/**
	 * Delegates to {@link Pixels.getSizeT()}.
	 */
	public int getSizeT();
}
