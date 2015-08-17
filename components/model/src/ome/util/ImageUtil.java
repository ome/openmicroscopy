/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

import sun.awt.image.IntegerInterleavedRaster;

/**
 * Provides helper methods for performing various things on image data.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0
 * @since 3.0
 */
public class ImageUtil
{
    /**
     * Creates a buffered image from a rendering engine RGB buffer without data
     * copying.
     * 
     * @param buf
     *            the rendering engine packed integer buffer.
     * @param sizeX
     *            the X-width of the image rendered.
     * @param sizeY
     *            the Y-width of the image rendered.
     * @return a buffered image wrapping <i>buf</i> with the X-Y dimensions
     *         provided.
     */
    public static BufferedImage createBufferedImage(int[] buf, int sizeX, int sizeY)
    {
        // First wrap the packed integer array with a Java2D buffer
        DataBuffer j2DBuf = new DataBufferInt(buf, sizeX * sizeY, 0);

        // Create a sample model which supplies the bit masks for each colour
        // component.
        SinglePixelPackedSampleModel sampleModel = new SinglePixelPackedSampleModel(
                DataBuffer.TYPE_INT, sizeX, sizeY, sizeX, new int[] {
                        0x00ff0000, // Red
                        0x0000ff00, // Green
                        0x000000ff, // Blue
                // 0xff000000 // Alpha
                });

        // Now create a compatible raster which wraps the Java2D buffer and is
        // told how to get to the pixel data by the sample model.
        WritableRaster raster = new IntegerInterleavedRaster(sampleModel,
                j2DBuf, new Point(0, 0));

        // Finally create a screen accessible colour model and wrap the raster
        // in a buffered image.
        ColorModel colorModel = new DirectColorModel(24, 0x00ff0000, // Red
                0x0000ff00, // Green
                0x000000ff // Blue
        // 0xff000000 // Alpha
        );
        BufferedImage image = new BufferedImage(colorModel, raster, false, null);

        return image;
    }
}
