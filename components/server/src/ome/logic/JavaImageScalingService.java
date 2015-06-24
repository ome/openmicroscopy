/*
 * ome.logic.JavaImageScalingService
 *
 *   Copyright 2006-2015 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

// Java imports
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

//Third-party libraries
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import com.mortennobel.imagescaling.ResampleOp;

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
    private static Logger log = LoggerFactory.getLogger(JavaImageScalingService.class);

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.IScale#scaleBufferedImage(java.awt.image.BufferedImage,
     * float, float)
     */
    public BufferedImage scaleBufferedImage(BufferedImage image, float xScale,
            float yScale) {
        int thumbHeight = (int) (image.getHeight() * yScale);
        int thumbWidth = (int) (image.getWidth() * xScale);
        if (thumbHeight < 3)
            thumbHeight = 3;
        if (thumbWidth < 3)
            thumbWidth = 3;
        
        log.info("Scaling to: " + thumbHeight + "x" + thumbWidth);
        
        StopWatch s1 = new Slf4JStopWatch("java-image-scaling.resampleOp");
        BufferedImage toReturn;
        if (image.getHeight() >= 3 && image.getWidth() >= 3) {
            ResampleOp resampleOp = new ResampleOp(thumbWidth, thumbHeight);
            toReturn = resampleOp.filter(image, null);
        } else {
            toReturn = new BufferedImage(thumbWidth, thumbHeight,
                    image.getType());
            Graphics2D g = toReturn.createGraphics();
            g.getRenderingHints().add(
                    new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_OFF));
            g.drawImage(image, 0, 0, thumbWidth, thumbHeight, 0, 0,
                    image.getWidth(), image.getHeight(), null);
        }
        s1.stop();
        return toReturn;
    }
}
