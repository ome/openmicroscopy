/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import ome.io.nio.PixelBuffer;
import ome.io.nio.PyramidPixelBufferProvider;
import ome.io.nio.PyramidPixelBufferException;
import ome.model.core.Pixels;
import ome.services.blitz.repo.BfPixelBuffer;

/**
 * Denotes an object which can provide a pixel buffer for image pyramids via
 * the Bio-Formats pixel buffer.
 * @author <br>
 *         Chris Allan&nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @since OMERO-Beta4.3
 */
public class BfPyramidPixelBufferProvider
    implements PyramidPixelBufferProvider
{

    /* (non-Javadoc)
     * @see ome.io.nio.PyramidPixelBufferProvider#getPyramidPixelBuffer(ome.model.core.Pixels, java.lang.String)
     */
    public PixelBuffer getPyramidPixelBuffer(Pixels pixels, String path)
        throws PyramidPixelBufferException
    {
        try
        {
            return new BfPixelBuffer(path);
        }
        catch (Exception e)
        {
            throw new PyramidPixelBufferException(
                    "Error instantiating Bio-Formats pyramid pixel buffer.", e);
        }
    }
}
