/*
 *   ome.api.RawPixelsStore
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.List;
import java.util.Set;

import ome.annotations.Validate;
import ome.model.core.Pixels;
import ome.util.PixelData;

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
    /**
     * Initializes the stateful service for a given Pixels set.
     * @param pixelsId Pixels set identifier.
     * @param bypassOriginalFile Whether or not to bypass checking for an
     * original file to back the pixel buffer used by this service. If requests
     * are predominantly <code>write-only</code> or involve the population of
     * a brand new pixel buffer using <code>true</code> here is a safe
     * optimization otherwise <code>false</code> is expected.
     */
    public void setPixelsId(long pixelsId, boolean bypassOriginalFile);
    
    /**
     * Returns the current Pixels path.
     * @return See above.
     */
    public String getPixelsPath();

    /**
     * Returns the current Pixels set identifier.
     * @return See above.
     */
    public long getPixelsId();

    /**
     * Prepares the stateful service with a cache of loaded Pixels objects.
     * This method is designed to combat query overhead, where many sets of
     * Pixels are to be read from or written to, by loading all the Pixels
     * sets at once. Multiple calls will result in the existing cache being
     * overwritten. 
     * @param pixelsIds Pixels IDs to cache.
     */
    public void prepare(@Validate(Long.class) Set<Long> pixelsIds);

    /**
     * Returns the plane size
     * @return the plane size
     */
    public long getPlaneSize();

    public int getRowSize();

    public long getStackSize();

    public long getTimepointSize();

    public long getTotalSize();

    public long getRowOffset(int y, int z, int c, int t);

    public long getPlaneOffset(int z, int c, int t);

    public long getStackOffset(int c, int t);

    public long getTimepointOffset(int t);

    public byte[] getTile(int z, int c, int t, int x, int y, int w, int h);

    public byte[] getRegion(int size, long offset);
    
    public byte[] getRow(int y, int z, int c, int t);
    
    public byte[] getCol(int x, int z, int c, int t);

    public byte[] getHypercube(@Validate(Integer.class) List<Integer> offset, @Validate(Integer.class) List<Integer> size, @Validate(Integer.class) List<Integer> step);

    public byte[] getPlaneRegion(int z, int c, int t, int count, int offset);

    public byte[] getPlane(int z, int c, int t);

    public byte[] getStack(int c, int t);

    public byte[] getTimepoint(int t);

    public void setTile(byte[] buffer, int z, int c, int t, int x, int y, int w, int h);

    public void setRegion(int size, long offset, byte[] buffer);

    public void setRow(byte[] buffer, int y, int z, int c, int t);

    public void setPlane(byte[] buffer, int z, int c, int t);

    public void setStack(byte[] buffer, int z, int c, int t);

    public void setTimepoint(byte[] buffer, int t);

    public int getByteWidth();

    public boolean isSigned();

    public boolean isFloat();

    public byte[] calculateMessageDigest();

    public Pixels save();

    public boolean requiresPixelsPyramid();

    public Object getResolutionDescriptions();

    public int getResolutionLevels();

    public int getResolutionLevel();

    public void setResolutionLevel(int resolutionLevel);

    public int[] getTileSize();

}
