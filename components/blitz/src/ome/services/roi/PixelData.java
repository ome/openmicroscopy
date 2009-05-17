/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.roi;

import java.io.IOException;

import ome.api.IPixels;
import ome.conditions.ApiUsageException;
import ome.conditions.ResourceError;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Strategy for loading and optionally caching pixel data.
 * 
 * @since Beta4.1
 */
public class PixelData {

    protected Log log = LogFactory.getLog(PixelData.class);

    protected final PixelsService data;

    protected final IPixels meta;

    public PixelData(PixelsService data, IPixels meta) {
        this.data = data;
        this.meta = meta;
    }

    public double get(long pix, int x, int y, int z, int c, int t) {
        PixelBuffer buf = data.getPixelBuffer(meta.retrievePixDescription(pix),
                null, true);
        try {
            ome.io.nio.PixelData pd = buf.getRow(y, z, c, t);
            return pd.getData().asDoubleBuffer().get(x);
        } catch (IOException e) {
            throw new ResourceError("IOException: " + e);
        } catch (DimensionsOutOfBoundsException e) {
            throw new ApiUsageException("DimensionsOutOfBounds: " + e);
        }
    }

}
