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
import ome.conditions.ValidationException;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;

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
        try {
            ome.util.PixelData pd = buf.getRow(y, z, c, t);
            return pd.getPixelValue(x);
        } catch (IOException e) {
            throw new ResourceError("IOException: " + e);
        } catch (DimensionsOutOfBoundsException e) {
            throw new ApiUsageException("DimensionsOutOfBounds: " + e);
        } catch (IndexOutOfBoundsException iobe) {
            throw new ValidationException("IndexOutOfBounds: " + iobe);
        }
    }

}
