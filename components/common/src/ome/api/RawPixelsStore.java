/*
 * ome.api.IPixels
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * metadata gateway for the {@link omeis.providers.re.RenderingEngine}. This
 * service provides all DB access that the rendering engine needs. This allows
 * the rendering engine to also be run external to the server (e.g. client-side)
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date: 2005/06/08
 *          15:21:59 $) </small>
 * @since OME2.2
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
