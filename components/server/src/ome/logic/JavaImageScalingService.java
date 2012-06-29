/*
 * ome.logic.JavaImageScalingService
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

// Java imports
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;

import com.mortennobel.imagescaling.ResampleOp;

// Third-party libraries

// Application-internal dependencies
import ome.api.IScale;

/**
 * Provides methods for scaling buffered images using
 * <a href="http://code.google.com/p/java-image-scaling/">Java Image Scaling</a>.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * 
 */
public class JavaImageScalingService implements IScale {
    /** The logger for this class. */
    private static Log log = LogFactory.getLog(JavaImageScalingService.class);

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

        StopWatch s1 = new CommonsLogStopWatch("java-image-scaling.resampleOp");
        ResampleOp  resampleOp = new ResampleOp(thumbWidth, thumbHeight);
        //resampleOp.setNumberOfThreads(4);
        BufferedImage toReturn = resampleOp.filter(image, null);
        s1.stop();
        return toReturn;
    }
}
