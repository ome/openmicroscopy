/*
 * ome.logic.QueryImpl
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

// Java imports
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Third-party libraries

// Application-internal dependencies
import ome.api.IScale;

/**
 * Provides methods for scaling buffered images.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 * 
 */
public class AWTScaleService implements IScale {
    /** The logger for this class. */
    private static Logger log = LoggerFactory.getLogger(AWTScaleService.class);

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.IScale#scaleBufferedImage(java.awt.image.BufferedImage,
     *      float, float)
     */
    public BufferedImage scaleBufferedImage(BufferedImage image, float xScale,
            float yScale) {
        int thumbHeight = (int) (image.getHeight() * yScale);
        int thumbWidth = (int) (image.getWidth() * xScale);
        log.info("Scaling to: " + thumbHeight + "x" + thumbWidth);

        // Create the required compatible (thumbnail) buffered image to avoid
        // potential errors from Java's ImagingLib.
        ColorModel cm = image.getColorModel();
        WritableRaster r = cm.createCompatibleWritableRaster(thumbWidth,
                thumbHeight);
        BufferedImage thumbImage = new BufferedImage(cm, r, false, null);

        // Do the actual scaling and return the result
        Graphics2D graphics2D = thumbImage.createGraphics();
        // graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        // RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
        return thumbImage;
    }
}
