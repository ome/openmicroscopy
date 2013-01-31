/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.nio;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ome.model.core.Pixels;
import ome.util.PixelData;

/**
 * Class implementation of the PixelBuffer interface for in memory planar pixel
 * data. It does not support indexing the pixel data as one large array as the
 * data is underlying modeled as a 5-dimensional array. It is also 
 * <b>read-only.</b>
 *
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:chris@glencoesoftware.com">chris@glencoesoftware.com</a>
 * @version $Revision$
 * @since 3.0
 * @see PixelBuffer
 */
public class InMemoryPlanarPixelBuffer implements PixelBuffer
{
    /** ZCT ordered planar data. */
    private byte[][][][] planes;
    
    /** Pixels object describing the planar data's dimensionality. */
    private Pixels pixels;
    
    /**
     * Constructs an in memory pixel buffer based on a defined dimensionality.
     * @param pixels Dimensionality and pixels type of the planar data.
     * @param planes The planar data.
     */
    public InMemoryPlanarPixelBuffer(Pixels pixels, byte[][][][] planes)
    {
        this.pixels = pixels;
        this.planes = planes;
    }
    
    public byte[] calculateMessageDigest() throws IOException
    {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "Required SHA-1 message digest algorithm unavailable.");
        }

        for (int z = 0; z < getSizeZ(); z++) {
            for (int c = 0; c < getSizeC(); c++) {
                for (int t = 0; t < getSizeT(); t++) {
                    try {
                        ByteBuffer buffer = getPlane(z, c, t).getData();
                        md.update(buffer);
                    } catch (DimensionsOutOfBoundsException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return md.digest();
    }

    public void checkBounds(Integer x, Integer y, Integer z, Integer c, 
    		Integer t)
            throws DimensionsOutOfBoundsException
    {
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

    public void close() throws IOException
    {
    }

    public int getByteWidth()
    {
        return PixelData.getBitDepth(pixels.getPixelsType().getValue()) / 8;
    }

    public long getId()
    {
        throw new NullPointerException("In memory planar buffers have no Id.");
    }

    public String getPath()
    {
        throw new NullPointerException("In memory planar buffers have no path.");
    }

    public Long getHypercubeSize(List<Integer> offset, List<Integer> size,
            List<Integer> step) throws DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }
                
    public PixelData getHypercube(List<Integer> offset, List<Integer> size,
            List<Integer> step) throws IOException, DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public byte[] getHypercubeDirect(List<Integer> offset, List<Integer> size,
            List<Integer> step, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public PixelData getPlane(Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException
    {
        ByteBuffer buf = ByteBuffer.wrap(planes[z][c][t]);
        return new PixelData(pixels.getPixelsType().getValue(), buf);
    }

    public byte[] getPlaneDirect(Integer z, Integer c, Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException
    {
        return planes[z][c][t];
    }

    public Long getPlaneOffset(Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException(
                "Not supported with in memory planar buffers.");
    }

    public PixelData getPlaneRegion(Integer x, Integer y, Integer width, 
    		Integer height, Integer z, Integer c, Integer t, Integer stride)
    throws IOException, DimensionsOutOfBoundsException
	{
    	return null;
	}
    
    public byte[] getPlaneRegionDirect(Integer z, Integer c, Integer t,
            Integer count, Integer offset, byte[] buffer) throws IOException,
            DimensionsOutOfBoundsException
    {
        byte[] plane = planes[z][c][t];
        int sourceOffset = offset * getByteWidth();
        int length = count * getByteWidth();
        System.arraycopy(plane, sourceOffset, buffer, 0, length);
        return buffer;
    }

    public Long getPlaneSize()
    {
        return (long) pixels.getSizeX() * (long) pixels.getSizeY() * getByteWidth();
    }

    public PixelData getRegion(Integer size, Long offset) throws IOException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public byte[] getRegionDirect(Integer size, Long offset, byte[] buffer)
            throws IOException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public PixelData getRow(Integer y, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException
    {
        // TODO: This could be supported, we're just not going to right now.
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }
    
    public PixelData getCol(Integer x, Integer z, Integer c, Integer t)
    throws IOException, DimensionsOutOfBoundsException
    {
        // TODO: This could be supported, we're just not going to right now.
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public byte[] getRowDirect(Integer y, Integer z, Integer c, Integer t,
            byte[] buffer) throws IOException, DimensionsOutOfBoundsException
    {
        // TODO: This could be supported, we're just not going to right now.
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }
    
    public byte[] getColDirect(Integer x, Integer z, Integer c, Integer t,
            byte[] buffer) throws IOException, DimensionsOutOfBoundsException
    {
        // TODO: This could be supported, we're just not going to right now.
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public Long getRowOffset(Integer y, Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public Integer getRowSize()
    {
        return getSizeX() * getByteWidth();
    }
    
    public Integer getColSize()
    {
        return getSizeY() * getByteWidth();
    }

    public int getSizeC()
    {
        return pixels.getSizeC();
    }

    public int getSizeT()
    {
        return pixels.getSizeT();
    }

    public int getSizeX()
    {
        return pixels.getSizeX();
    }

    public int getSizeY()
    {
        return pixels.getSizeY();
    }

    public int getSizeZ()
    {
        return pixels.getSizeZ();
    }

    public PixelData getStack(Integer c, Integer t) throws IOException,
            DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public byte[] getStackDirect(Integer c, Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public Long getStackOffset(Integer c, Integer t)
            throws DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public Long getStackSize()
    {
        return getPlaneSize() * pixels.getSizeZ();
    }

    public PixelData getTimepoint(Integer t) throws IOException,
            DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public byte[] getTimepointDirect(Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public Long getTimepointOffset(Integer t)
            throws DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public Long getTimepointSize()
    {
        return getStackSize() * pixels.getSizeC();
    }

    public Long getTotalSize()
    {
        return getTimepointSize() * pixels.getSizeT();
    }

    public boolean isFloat()
    {
        MappedByteBuffer b = null;
        PixelData d = new PixelData(pixels.getPixelsType().getValue(), b);
        return d.isFloat();
    }

    public boolean isSigned()
    {
        MappedByteBuffer b = null;
        PixelData d = new PixelData(pixels.getPixelsType().getValue(), b);
        return d.isSigned();
    }

    public void setPlane(ByteBuffer buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public void setPlane(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public void setRegion(Integer size, Long offset, byte[] buffer)
            throws IOException, BufferOverflowException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public void setRegion(Integer size, Long offset, ByteBuffer buffer)
            throws IOException, BufferOverflowException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public void setRow(ByteBuffer buffer, Integer y, Integer z, Integer c,
            Integer t) throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public void setStack(ByteBuffer buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public void setStack(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public void setTimepoint(ByteBuffer buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException, BufferOverflowException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    public void setTimepoint(byte[] buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException, BufferOverflowException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTile(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public PixelData getTile(Integer z, Integer c, Integer t, Integer x,
            Integer y, Integer w, Integer h) throws IOException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTileDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
     */
    public byte[] getTileDirect(Integer z, Integer c, Integer t, Integer x,
            Integer y, Integer w, Integer h, byte[] buffer) throws IOException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setTile(byte[], java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public void setTile(byte[] buffer, Integer z, Integer c, Integer t, Integer x, Integer y,
            Integer w, Integer h) throws IOException,
            BufferOverflowException
    {
        throw new UnsupportedOperationException(
            "Not supported with in memory planar buffers.");
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
        return null;
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setResolutionLevel(int)
     */
    public void setResolutionLevel(int resolutionLevel)
    {
        throw new UnsupportedOperationException(
                "Cannot set resolution levels on an in memory pixel buffer.");
    }
}
