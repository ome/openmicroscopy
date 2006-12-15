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
import ome.annotations.NotNull;

/**
 * metadata gateway for the {@link omeis.providers.re.RenderingEngine}. This
 * service provides all DB access that the rendering engine needs. This allows
 * the rendering engine to also be run external to the server (e.g. client-side)
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/08 15:21:59 $) </small>
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
    public Integer getPlaneSize();

    public Integer getRowSize();

    public Integer getStackSize();

    public Integer getTimepointSize();

    public Integer getTotalSize();

    public Long getRowOffset(@NotNull
    Integer y, @NotNull
    Integer z, @NotNull
    Integer c, @NotNull
    Integer t);

    public Long getPlaneOffset(@NotNull
    Integer z, @NotNull
    Integer c, @NotNull
    Integer t);

    public Long getStackOffset(@NotNull
    Integer c, @NotNull
    Integer t);

    public Long getTimepointOffset(@NotNull
    Integer t);

    public byte[] getRegion(@NotNull
    Integer size, Long offset);

    public byte[] getRow(@NotNull
    Integer y, @NotNull
    Integer z, @NotNull
    Integer c, @NotNull
    Integer t);

    public byte[] getPlane(@NotNull
    Integer z, @NotNull
    Integer c, @NotNull
    Integer t);

    public byte[] getStack(@NotNull
    Integer c, @NotNull
    Integer t);

    public byte[] getTimepoint(@NotNull
    Integer t);

    public void setRegion(@NotNull
    Integer size, Long offset, @NotNull
    byte[] buffer);

    public void setRow(@NotNull
    byte[] buffer, @NotNull
    Integer y, @NotNull
    Integer z, @NotNull
    Integer c, @NotNull
    Integer t);

    public void setPlane(@NotNull
    byte[] buffer, @NotNull
    Integer z, @NotNull
    Integer c, @NotNull
    Integer t);

    public void setStack(@NotNull
    byte[] buffer, @NotNull
    Integer z, @NotNull
    Integer c, @NotNull
    Integer t);

    public void setTimepoint(@NotNull
    byte[] buffer, @NotNull
    Integer t);

    public byte[] calculateMessageDigest();

}
