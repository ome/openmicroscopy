/*
 *   Copyright (C) 2009-2013 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omeis.providers.re.utests;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.model.core.Pixels;
import ome.util.PixelData;

public class TestPixelBuffer implements PixelBuffer {

    private final ByteBuffer dummyPlane;

    private final String pixelsType;
    private final int bytesPerPixel;

    private final int x, y, z, c, t;

    /* cache the latest buffer returned by getByteBuffer */
    private ByteBuffer buffer;

    public TestPixelBuffer(Pixels pixels, byte[] dummyPlane)
    {
        this.dummyPlane = ByteBuffer.wrap(dummyPlane).asReadOnlyBuffer();
        this.pixelsType = pixels.getPixelsType().getValue();
        this.bytesPerPixel = new PixelData(this.pixelsType, null).bytesPerPixel();
        this.x = pixels.getSizeX();
        this.y = pixels.getSizeY();
        this.z = pixels.getSizeZ();
        this.c = pixels.getSizeC();
        this.t = pixels.getSizeT();
    }

	public byte[] calculateMessageDigest() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	public int getByteWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	public PixelData getCol(Integer arg0, Integer arg1, Integer arg2,
			Integer arg3) throws IOException, DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getColDirect(Integer arg0, Integer arg1, Integer arg2,
			Integer arg3, byte[] arg4) throws IOException,
			DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getColSize() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public PixelData getPlane(Integer arg0, Integer arg1, Integer arg2)
			throws IOException, DimensionsOutOfBoundsException
	{
		return new PixelData(pixelsType, dummyPlane);
	}

	public byte[] getPlaneDirect(Integer arg0, Integer arg1, Integer arg2,
			byte[] arg3) throws IOException, DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Long getPlaneOffset(Integer arg0, Integer arg1, Integer arg2)
			throws DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getPlaneRegionDirect(Integer arg0, Integer arg1,
			Integer arg2, Integer arg3, Integer arg4, byte[] arg5)
			throws IOException, DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Long getPlaneSize() {
		// TODO Auto-generated method stub
		return null;
	}

	public PixelData getRegion(Integer arg0, Long arg1) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getRegionDirect(Integer arg0, Long arg1, byte[] arg2)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public PixelData getRow(Integer arg0, Integer arg1, Integer arg2,
			Integer arg3) throws IOException, DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getRowDirect(Integer arg0, Integer arg1, Integer arg2,
			Integer arg3, byte[] arg4) throws IOException,
			DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Long getRowOffset(Integer arg0, Integer arg1, Integer arg2,
			Integer arg3) throws DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getRowSize() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSizeC() {
        return c;
	}

	public int getSizeT() {
        return t;
	}

	public int getSizeX() {
        return x;
	}

	public int getSizeY() {
        return y;
	}

	public int getSizeZ() {
        return z;
	}

	public PixelData getStack(Integer arg0, Integer arg1) throws IOException,
			DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getStackDirect(Integer arg0, Integer arg1, byte[] arg2)
			throws IOException, DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Long getStackOffset(Integer arg0, Integer arg1)
			throws DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Long getStackSize() {
		// TODO Auto-generated method stub
		return null;
	}

	public PixelData getTimepoint(Integer arg0) throws IOException,
			DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getTimepointDirect(Integer arg0, byte[] arg1)
			throws IOException, DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Long getTimepointOffset(Integer arg0)
			throws DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Long getTimepointSize() {
		// TODO Auto-generated method stub
		return null;
	}

	public Long getTotalSize() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isFloat() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSigned() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setPlane(ByteBuffer arg0, Integer arg1, Integer arg2,
			Integer arg3) throws IOException, DimensionsOutOfBoundsException,
			BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void setPlane(byte[] arg0, Integer arg1, Integer arg2, Integer arg3)
			throws IOException, DimensionsOutOfBoundsException,
			BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void setRegion(Integer arg0, Long arg1, byte[] arg2)
			throws IOException, BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void setRegion(Integer arg0, Long arg1, ByteBuffer arg2)
			throws IOException, BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void setRow(ByteBuffer arg0, Integer arg1, Integer arg2,
			Integer arg3, Integer arg4) throws IOException,
			DimensionsOutOfBoundsException, BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void setStack(ByteBuffer arg0, Integer arg1, Integer arg2,
			Integer arg3) throws IOException, DimensionsOutOfBoundsException,
			BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void setStack(byte[] arg0, Integer arg1, Integer arg2, Integer arg3)
			throws IOException, DimensionsOutOfBoundsException,
			BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void setTimepoint(ByteBuffer arg0, Integer arg1) throws IOException,
			DimensionsOutOfBoundsException, BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void setTimepoint(byte[] arg0, Integer arg1) throws IOException,
			DimensionsOutOfBoundsException, BufferOverflowException {
		// TODO Auto-generated method stub

	}

	public void checkBounds(Integer arg0, Integer arg1, Integer arg2,
			Integer arg3, Integer arg4) throws DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		
	}

	public PixelData getPlaneRegion(Integer arg0, Integer arg1, Integer arg2,
			Integer arg3, Integer arg4, Integer arg5, Integer arg6, Integer arg7)
			throws IOException, DimensionsOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

    public Long getHypercubeSize(List<Integer> arg0, List<Integer> arg1,
            List<Integer> arg2) throws DimensionsOutOfBoundsException
    {
		// TODO Auto-generated method stub
		return null;
	}

    public PixelData getHypercube(List<Integer> arg0, List<Integer> arg1, 
            List<Integer> arg2) throws IOException, DimensionsOutOfBoundsException 
    {
		// TODO Auto-generated method stub
		return null;
	}
                
    public byte[] getHypercubeDirect(List<Integer> arg0, List<Integer> arg1, 
            List<Integer> arg2, byte[] arg3) 
            throws IOException, DimensionsOutOfBoundsException 
    {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * Get a ByteBuffer filled to at least the requested limit.
     * If possible, ByteBuffers are reused.
     * @param limit the required limit
     * @return a filled ByteBuffer
     */
    private ByteBuffer getByteBuffer(int limit) {
        if (this.buffer == null || this.buffer.capacity() < limit) {
            this.buffer = ByteBuffer.wrap(new byte[limit]).asReadOnlyBuffer();
        }
        return this.buffer;
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTile(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public PixelData getTile(Integer z, Integer c, Integer t, Integer x,
            Integer y, Integer w, Integer h) throws IOException
    {
        return new PixelData(this.pixelsType, getByteBuffer(w * h * this.bytesPerPixel));
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTileDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
     */
    public byte[] getTileDirect(Integer z, Integer c, Integer t, Integer x,
            Integer y, Integer w, Integer h, byte[] buffer) throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setTile(byte[], java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public void setTile(byte[] buffer, Integer z, Integer c, Integer t, Integer x, Integer y,
            Integer w, Integer h) throws IOException,
            BufferOverflowException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getResolutionLevel()
     */
    public int getResolutionLevel()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getResolutionLevels()
     */
    public int getResolutionLevels()
    {
        // TODO Auto-generated method stub
        return 0;
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
        return new Dimension(256, 256);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setResolutionLevel(int)
     */
    public void setResolutionLevel(int resolutionLevel)
    {
        // TODO Auto-generated method stub
        
    }

}
