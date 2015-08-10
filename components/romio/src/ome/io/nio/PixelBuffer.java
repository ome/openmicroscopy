/*
 * ome.io.nio.PixelBuffer
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.nio;

import java.awt.Dimension;
import java.io.Closeable;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.List;

import ome.util.PixelData;

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
public interface PixelBuffer extends Closeable
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
     * 
     * @param x offset across the X-axis of the pixel buffer to check.
     * @param y offset across the Y-axis of the pixel buffer to check.
     * @param z offset across the Z-axis of the pixel buffer to check.
     * @param c offset across the C-axis of the pixel buffer to check.
     * @param t offset across the T-axis of the pixel buffer to check.
     * @throws DimensionsOutOfBoundsException if <code>y</code>,
     * <code>z</code>, <code>c</code> or <code>t</code> is out of bounds.
     */
    public void checkBounds(Integer x, Integer y, Integer z, Integer c, 
    		Integer t)
    	throws DimensionsOutOfBoundsException;
    
    /**
     * Retrieves the in memory size of a 2D image plane in this pixel buffer.
     * @return 2D image plane size in bytes (sizeX*sizeY*ByteWidth).
     */
    public Long getPlaneSize();

    /**
     * Retrieves the in memory size of a row or scanline of pixels in this
     * pixel buffer.
     * @return row or scanline size in bytes (sizeX*ByteWidth)
     */
    public Integer getRowSize();
    
    /**
     * Retrieves the in memory size of a column of pixels in this pixel buffer.
     * @return column size in bytes (sizeY*ByteWidth)
     */
    public Integer getColSize();

    /**
     * Retrieves the in memory size of the entire number of optical sections
     * for a <b>single</b> wavelength or channel at a particular timepoint in
     * this pixel buffer.
     * @return stack size in bytes (sizeX*sizeY*sizeZ*ByteWidth).
     */
    public Long getStackSize();

    /**
     * Retrieves the in memory size of the entire number of optical sections for
     * <b>all</b> wavelengths or channels at a particular timepoint in this
     * pixel buffer.
     * @return timepoint size in bytes (sizeX*sizeY*sizeZ*sizeC*ByteWidth).
     */
    public Long getTimepointSize();

    /**
     * Retrieves the in memory size of the entire pixel buffer.
     * @return total size of the pixel size in bytes
     * (sizeX*sizeY*sizeZ*sizeC*sizeT*ByteWidth).
     */
    public Long getTotalSize();

    /**
     * Retrieves a the size of a hypercube from this pixel buffer.
     * @param offset The offset of each dimension of the pixel buffer.
     * @param size The number of pixels to retrieve along each dimension .
     * @param step The step size across each dimension .
     * @return the size. 
     * @throws IOException if there is a problem reading from the pixel buffer.
     */
    public Long getHypercubeSize(List<Integer> offset, List<Integer> size,
            List<Integer> step) throws DimensionsOutOfBoundsException;

    /**
     * Retrieves the offset for a particular row or scanline in this pixel 
     * buffer.
     * @param y offset across the Y-axis of the pixel buffer.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @return offset of the row or scaline.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
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
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     */
    public Long getPlaneOffset(Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException;

    /**
     * Retrieves the offset for the entire number of optical sections
     * for a <b>single</b> wavelength or channel at a particular timepoint in
     * this pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @return offset of the stack.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
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
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     */
    public Long getTimepointOffset(Integer t)
            throws DimensionsOutOfBoundsException;
    
    /**
     * Retrieves a hypercube from this pixel buffer.
     * @param offset The offset of each dimension of the pixel buffer.
     * @param size The number of pixels to retrieve along each dimension .
     * @param step The step size across each dimension .
     * @return buffer containing the data. 
     * @throws IOException if there is a problem reading from the pixel buffer.
     */
    public PixelData getHypercube(List<Integer> offset, List<Integer> size, 
            List<Integer> step) throws IOException, DimensionsOutOfBoundsException; 
                
    /**
     * Retrieves a hypercube from the given pixels directly.
     * @param offset The offset of each dimension of the pixel buffer.
     * @param size The number of pixels to retrieve along each dimension .
     * @param step The step size across each dimension .
     * @param buffer pre-allocated buffer, <code>count</code> in size.
     * @return buffer containing the data. 
     * @throws IOException if there is a problem reading from the pixel buffer.
     */
    public byte[] getHypercubeDirect(List<Integer> offset, List<Integer> size, 
            List<Integer> step, byte[] buffer) 
            throws IOException, DimensionsOutOfBoundsException; 
                
    /**
     * Retrieves a region from a given plane directly.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @param count the number of pixels to retrieve.
     * @param offset the offset at which to retrieve <code>count</code> pixels.
     * @param buffer pre-allocated buffer, <code>count</code> in size.
     * @return buffer containing the data which comprises the region of the
     * given 2D image plane. It is guaranteed that this buffer will have been 
     * byte swapped.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @see #getRegionDirect(Integer, Long, byte[])
     */
    public byte[] getPlaneRegionDirect(Integer z, Integer c, Integer t, 
    		Integer count, Integer offset, byte[] buffer)
    	throws IOException, DimensionsOutOfBoundsException;

    /**
     * Retrieves a tile from this pixel buffer.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @param x Top left corner of the tile, X offset.
     * @param y Top left corner of the tile, Y offset.
     * @param w Width of the tile.
     * @param h Height of the tile.
     * @return buffer containing the data which comprises the region of the
     * given 2D image plane. It is guaranteed that this buffer will have been
     * byte swapped.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @see #getTileDirect(Integer, Integer, Integer, Integer, Integer, Integer, Integer, byte[])
     */
    public PixelData getTile(Integer z, Integer c, Integer t, Integer x,
                             Integer y, Integer w, Integer h)
            throws IOException;

    /**
     * Retrieves a tile from this pixel buffer.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @param x Top left corner of the tile, X offset.
     * @param y Top left corner of the tile, Y offset.
     * @param w Width of the tile.
     * @param h Height of the tile.
     * @param buffer Pre-allocated buffer of the tile's size.
     * @return <code>buffer</code> containing the data which comprises this
     * region. It is guaranteed that this buffer will have been byte
     * swapped. <b>The buffer is essentially directly from disk.</b>
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @see #getTile(Integer, Integer, Integer, Integer, Integer, Integer, Integer)
     */
    public byte[] getTileDirect(Integer z, Integer c, Integer t, Integer x,
                                Integer y, Integer w, Integer h, byte[] buffer)
            throws IOException;

    /**
     * Retrieves a region from this pixel buffer.
     * @param size byte width of the region to retrieve.
     * @param offset offset within the pixel buffer.
     * @return buffer containing the data. It is guaranteed that this buffer 
     * will have its <code>order</code> set correctly but <b>not</b> that the
     * backing buffer will have been byte swapped. <b>The buffer is essentially 
     * directly from disk.</b>
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @see #getRegionDirect(Integer, Long, byte[])
     */
    public PixelData getRegion(Integer size, Long offset)
            throws IOException;
    
    /**
     * Retrieves a region from this pixel buffer directly.
     * @param size byte width of the region to retrieve.
     * @param offset offset within the pixel buffer.
     * @param buffer pre-allocated buffer of the row's size.
     * @return <code>buffer</code> containing the data which comprises this 
     * region. It is guaranteed that this buffer will have been byte 
     * swapped. <b>The buffer is essentially directly from disk.</b>
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @see #getRegion(Integer, Long)
     */
    public byte[] getRegionDirect(Integer size, Long offset, byte[] buffer)
    	throws IOException;

    /**
     * Retrieves a particular row or scanline from this pixel buffer.
     * @param y offset across the Y-axis of the pixel buffer.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @return buffer containing the data which comprises this row or scanline.
     * It is guaranteed that this buffer will have its <code>order</code> set 
     * correctly but <b>not</b> that the backing buffer will have been byte 
     * swapped.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     * @see #getRowDirect(Integer, Integer, Integer, Integer, byte[])
     */
    public PixelData getRow(Integer y, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException;
    
    /**
     * Retrieves a particular column from this pixel buffer.
     * @param x offset across the X-axis of the pixel buffer.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @return buffer containing the data which comprises this column. It is 
     * guaranteed that this buffer will have its <code>order</code> set 
     * correctly but <b>not</b> that the backing buffer will have been byte 
     * swapped.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     * @see #getColDirect(Integer, Integer, Integer, Integer, byte[])
     */
    public PixelData getCol(Integer x, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException;
    
    /**
     * Retrieves a particular row or scanline from this pixel buffer.
     * @param y offset across the Y-axis of the pixel buffer.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @param buffer pre-allocated buffer of the row's size.
     * @return <code>buffer</code> containing the data which comprises this row 
     * or scanline. It is guaranteed that this buffer will have been byte 
     * swapped.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     * @see #getRow(Integer, Integer, Integer, Integer)
     */
    public byte[] getRowDirect(Integer y, Integer z, Integer c, 
    		                   Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException;
    
    /**
     * Retrieves a particular column from this pixel buffer.
     * @param x offset across the X-axis of the pixel buffer.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @param buffer pre-allocated buffer of the row's size.
     * @return <code>buffer</code> containing the data which comprises this 
     * column. It is guaranteed that this buffer will have been byte swapped.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     * @see #getCol(Integer, Integer, Integer, Integer)
     */
    public byte[] getColDirect(Integer x, Integer z, Integer c, 
                               Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException;
    
    /**
     * Retrieves a particular 2D image plane from this pixel buffer.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @return buffer containing the data which comprises this 2D image plane.
     * It is guaranteed that this buffer will have its <code>order</code> set 
     * correctly but <b>not</b> that the backing buffer will have been byte 
     * swapped.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     */
    public PixelData getPlane(Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException;
    
    /**
     * Retrieves a particular region of a 2D image plane from this pixel buffer.
     * 
     * @param x offset across the X-axis of the pixel buffer.
     * @param y offset across the Y-axis of the pixel buffer.
     * @param width The number of pixels to retrieve along the X-axis.
     * @param height The number of pixels to retrieve along the Y-axis.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @param stride The step size.
     * @return buffer containing the data which comprises this 2D image plane.
     * It is guaranteed that this buffer will have its <code>order</code> set 
     * correctly but <b>not</b> that the backing buffer will have been byte 
     * swapped.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     */
    public PixelData getPlaneRegion(Integer x, Integer y, Integer width, Integer
    		height, Integer z, Integer c, Integer t, Integer stride)
            throws IOException, DimensionsOutOfBoundsException;
    
    /**
     * Retrieves a particular 2D image plane from this pixel buffer.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @param buffer pre-allocated buffer of the plane's size.
     * @return <code>buffer</code> containing the data which comprises this 2D 
     * image plane. It is guaranteed that this buffer will have been byte 
     * swapped.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     */
    public byte[] getPlaneDirect(Integer z, Integer c, Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException;

    /**
     * Retrieves the the entire number of optical sections for a <b>single</b>
     * wavelength or channel at a particular timepoint in this pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @return buffer containing the data which comprises this stack. It is 
     * guaranteed that this buffer will have its <code>order</code> set 
     * correctly but <b>not</b> that the backing buffer will have been byte 
     * swapped.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     */
    public PixelData getStack(Integer c, Integer t)
    	throws IOException, DimensionsOutOfBoundsException;
    
    /**
     * Retrieves the the entire number of optical sections for a <b>single</b>
     * wavelength or channel at a particular timepoint in this pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @param buffer pre-allocated buffer of the stack's size.
     * @return <code>buffer</code> containing the data which comprises this 
     * stack. It is guaranteed that this buffer will have been byte swapped.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     */
    public byte[] getStackDirect(Integer c, Integer t, byte[] buffer) 
    	throws IOException, DimensionsOutOfBoundsException;

    /**
     * Retrieves the entire number of optical sections for <b>all</b>
     * wavelengths or channels at a particular timepoint in this pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @return buffer containing the data which comprises this timepoint. It is 
     * guaranteed that this buffer will have its <code>order</code> set 
     * correctly but <b>not</b> that the backing buffer will have been byte 
     * swapped.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     */
    public PixelData getTimepoint(Integer t) 
    	throws IOException, DimensionsOutOfBoundsException;
    /**
     * Retrieves the entire number of optical sections for <b>all</b>
     * wavelengths or channels at a particular timepoint in this pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @param buffer pre-allocated buffer of the timepoint's size.
     * @return <code>buffer</code> containing the data which comprises this 
     * timepoint. It is guaranteed that this buffer will have been byte swapped.
     * @throws IOException if there is a problem reading from the pixel buffer.
     * @throws DimensionsOutOfBoundsException if offsets are out of bounds
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     */
    public byte[] getTimepointDirect(Integer t, byte[] buffer) 
    	throws IOException, DimensionsOutOfBoundsException;

    /**
     * Sets a tile in this pixel buffer.
     * @param buffer A byte array of the data.
     * @param z offset across the Z-axis of the pixel buffer.
     * @param c offset across the C-axis of the pixel buffer.
     * @param t offset across the T-axis of the pixel buffer.
     * @param x Top left corner of the tile, X offset.
     * @param y Top left corner of the tile, Y offset.
     * @param w Width of the tile.
     * @param h Height of the tile.
     * @throws IOException if there is a problem writing to the pixel buffer.
     * @throws BufferOverflowException if an attempt is made to write off the
     * end of the file.
     */
    public void setTile(byte[] buffer, Integer z, Integer c, Integer t,
                        Integer x, Integer y, Integer w, Integer h)
            throws IOException, BufferOverflowException;

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
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     * @throws BufferOverflowException if
     * <code>buffer.length > {@link #getRowSize()}</code>.
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
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     * @throws BufferOverflowException if
     * <code>buffer.length > {@link #getPlaneSize()}</code>.
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
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     * @throws BufferOverflowException if
     * <code>buffer.length > {@link #getPlaneSize()}</code>.
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
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     * @throws BufferOverflowException if
     * <code>buffer.length > {@link #getStackSize()}</code>.
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
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     * @throws BufferOverflowException if
     * <code>buffer.length > {@link #getStackSize()()}</code>.
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
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     * @throws BufferOverflowException if
     * <code>buffer.length > {@link #getTimepointSize()}</code>.
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
     * after checking with {@link #checkBounds(Integer, Integer, Integer, Integer, Integer)}.
     * @throws BufferOverflowException if
     * <code>buffer.length > {@link #getTimepointSize()}</code>.
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
     * Returns the byte width for the pixel buffer.
     * @return See above.
     */
    public int getByteWidth();
    
    /**
     * Returns whether or not the pixel buffer has signed pixels.
     * @return See above.
     */
    public boolean isSigned();
    
    /**
     * Returns whether or not the pixel buffer has floating point pixels. 
     * @return {@code true} if the pixel buffer as floating point,
     *         {@code false} otherwise
     */
    public boolean isFloat();
    
    /**
     * Retrieves the full path to this pixel buffer on disk
     * @return fully qualified path.
     */
    public String getPath();

	/**
	 * Retrieves the identifier of this pixel buffer
	 */
	public long getId();
	
	/**
	 * Retrieves the size in X of this pixel buffer
	 */
	public int getSizeX();

	/**
	 * Retrieves the size in Y of this pixel buffer
	 */
	public int getSizeY();

	/**
	 * Retrieves the size in Z of this pixel buffer
	 */
	public int getSizeZ();

	/**
	 * Retrieves the size in C of this pixel buffer
	 */
	public int getSizeC();

	/**
	 * Retrieves the size in T of this pixel buffer
	 */
	public int getSizeT();

    /**
     * Retrieves the number of resolution levels that the backing
     * pixels pyramid contains.
     * @return The number of resolution levels. This value does not
     * necessarily indicate either the presence or absence of a
     * pixels pyramid.
     **/
    public int getResolutionLevels();

    /**
     * Retrieves the active resolution level.
     * @return The active resolution level.
     **/
    public int getResolutionLevel();

    /**
     * Sets the active resolution level.
     * @param resolutionLevel The resolution level to be used by
     * the pixel buffer.
     **/
    public void setResolutionLevel(int resolutionLevel);

    /**
     * Retrieves the tile size for the pixel store.
     * @return The dimension of the tile or <code>null</code> if the pixel
     * buffer is not tiled.
     **/
    public Dimension getTileSize();

    /**
     * Return a list of lists each of which has sizeX, sizeY for the resolution
     * level matching the index of the outer index. For example, if an image
     * has 2 resolution levels of size 2048x1024 and 1024x512 then this
     * returns:
     * [[2048,1024],[1024,512]]
     * @return a list of lists containing sizeX, sizeY for each resolution
     *         level
     *
     */
    public List<List<Integer>> getResolutionDescriptions();
}
