/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.api;

import java.awt.image.BufferedImage;

/**
 * Provides methods for performing scaling (change of the image size through
 * interpolation or other means) on BufferedImages.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0
 * @since 3.0
 */
public interface IScale extends ServiceInterface {

    /**
     * Scales a buffered image using defined X and Y axis scale factors. For
     * example:
     * <p>
     * If you wanted to take a 512x512 image and scale it to 256x256 you would
     * use an X and Y scale factor of 0.5.
     * </p>
     * NOTE: The X and Y scale factors <b>do not</b> have to be equal.
     * 
     * @param image
     *            the buffered image to scale.
     * @param xScale
     *            X-axis scale factor.
     * @param yScale
     *            Y-axis scale factor.
     * @return a scaled buffered image.
     */
    public BufferedImage scaleBufferedImage(BufferedImage image, float xScale,
            float yScale);
}
