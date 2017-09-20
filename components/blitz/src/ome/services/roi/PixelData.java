/*
 *   $Id$
 *
 *   Copyright 2009-2013 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.roi;

import java.io.IOException;

import ome.api.IPixels;
import ome.conditions.ApiUsageException;
import ome.conditions.ResourceError;
import ome.conditions.ValidationException;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strategy for loading and optionally caching pixel data.
 * 
 * @since Beta4.1
 */
public class PixelData {

    protected Logger log = LoggerFactory.getLogger(PixelData.class);

    protected final PixelsService data;

    protected final IPixels meta;

    public PixelData(PixelsService data, IPixels meta) {
        this.data = data;
        this.meta = meta;
    }

    public PixelBuffer getBuffer(long pix) {
        return data.getPixelBuffer(meta.retrievePixDescription(pix), false);
    }

    public double get(PixelBuffer buf, int x, int y, int z, int c, int t) {
        ome.util.PixelData pd = null;
        try {
            pd = buf.getRow(y, z, c, t);
            return pd.getPixelValue(x);
        } catch (IOException e) {
            throw new ResourceError("IOException: " + e);
        } catch (DimensionsOutOfBoundsException e) {
            throw new ApiUsageException("DimensionsOutOfBounds: " + e);
        } catch (IndexOutOfBoundsException iobe) {
            throw new ValidationException("IndexOutOfBounds: " + iobe);
        } finally {
            if (pd != null) {
                pd.dispose();
            }
        }
    }

    /**
     * Returns the {@link ome.util.PixelData} for plane given its z, c and t
     * as well as a {@link PixelBuffer}
     *
     * @param buf the {@link PixelBuffer}
     * @param z the Z
     * @param c the C
     * @param t the T
     * @return the ome.util.PixelData for the plane
     */
    public ome.util.PixelData getPlane(PixelBuffer buf, int z, int c, int t) {
        try {
            return buf.getPlane(z, c, t);
        } catch (IOException e) {
            throw new ResourceError("IOException: " + e);
        } catch (DimensionsOutOfBoundsException e) {
            throw new ApiUsageException("DimensionsOutOfBounds: " + e);
        } catch (IndexOutOfBoundsException iobe) {
            throw new ValidationException("IndexOutOfBounds: " + iobe);
        }
    }

    /**
     * Returns whether a pyramid should be used for the given {@link Pixels}.
     * This usually implies that this is a "Big image" and therefore will
     * need tiling.
     *
     * @see PixelsService#requiresPixelsPyramid(Pixels)
     * @param pix the pixels
     * @return {@code true} if a pyramid should be used, {@code false}
     *         otherwise
     */
    public boolean requiresPixelsPyramid(Pixels pix) {
        return data.requiresPixelsPyramid(pix);
    }

}
