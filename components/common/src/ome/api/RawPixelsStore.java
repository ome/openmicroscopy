/*
 *   ome.api.RawPixelsStore
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

/**
 * Binary data provider. Initialized with the id of a
 * {@link ome.model.core.Pixels} instance, this interface can provide various
 * slices, stacks, regions of the 5-dimensional (X-Y planes with multiple
 * Z-sections and Channels over Time). The byte array returned by the getter
 * methods and passed to the setter methods can and will be interpreted
 * according to results of {@link #getByteWidth()}, {@link #isFloat()}, and
 * {@link #isSigned()}.
 */
public interface RawPixelsStore extends StatefulServiceInterface {

    // State management.
    public void setPixelsId(long pixelsId);

    /**
     * delegates to {@link ome.io.nio.PixelBuffer}
     * 
     * @param pixelsId
     * @return
     * @see ome.io.nio.PixelBuffer#getPlaneSize()
     */
    public int getPlaneSize();

    public int getRowSize();

    public int getStackSize();

    public int getTimepointSize();

    public int getTotalSize();

    public long getRowOffset(int y, int z, int c, int t);

    public long getPlaneOffset(int z, int c, int t);

    public long getStackOffset(int c, int t);

    public long getTimepointOffset(int t);

    public byte[] getRegion(int size, long offset);
    
    public byte[] getRow(int y, int z, int c, int t);
    
    public byte[] getCol(int x, int z, int c, int t);

    public byte[] getPlaneRegion(int z, int c, int t, int count, int offset);

    public byte[] getPlane(int z, int c, int t);

    public byte[] getStack(int c, int t);

    public byte[] getTimepoint(int t);

    public void setRegion(int size, long offset, byte[] buffer);

    public void setRow(byte[] buffer, int y, int z, int c, int t);

    public void setPlane(byte[] buffer, int z, int c, int t);

    public void setStack(byte[] buffer, int z, int c, int t);

    public void setTimepoint(byte[] buffer, int t);

    public int getByteWidth();

    public boolean isSigned();

    public boolean isFloat();

    public byte[] calculateMessageDigest();

}
