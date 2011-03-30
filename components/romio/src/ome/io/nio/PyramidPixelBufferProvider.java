/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

import ome.model.core.Pixels;


/**
 * Denotes an object which can provide a pixel buffer for image pyramids.
 * @author <br>
 *         Chris Allan&nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @since OMERO-Beta4.3
 */
public interface PyramidPixelBufferProvider
{
    /**
     * Creates a new pyramid pixel buffer for a given set of pixels.
     * @param pixels Pixels set we are creating the pixel buffer for.
     * @param path Path to the pyramid pixel buffer file.
     * starts with.
     * @return <b>Read-only</b> pixel buffer instance.
     * @throws PyramidPixelBufferException If there is an error during the
     * instantiation of the pixel buffer.
     */
    PixelBuffer getPyramidPixelBuffer(Pixels pixels, String path)
        throws PyramidPixelBufferException;
}