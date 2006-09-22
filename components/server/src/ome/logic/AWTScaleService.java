/*
 * ome.logic.QueryImpl
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.logic;

//Java imports
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Third-party libraries

//Application-internal dependencies
import ome.api.IScale;

/** 
 * Provides methods for scaling buffered images.
 * 
 * @author  Chris Allan &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 3.0
 * 
 */
public class AWTScaleService implements IScale
{
	/** The logger for this class. */
	private static Log log = LogFactory.getLog(ThumbImpl.class);
	
    /* (non-Javadoc)
     * @see ome.api.IScale#scaleBufferedImage(java.awt.image.BufferedImage, float, float)
     */
    public BufferedImage scaleBufferedImage(BufferedImage image,
                                            float xScale, float yScale)
    {
    	int thumbHeight = (int) (image.getHeight() * yScale);
    	int thumbWidth  = (int) (image.getWidth() * xScale);
    	log.info("Scaling to: " + thumbHeight + "x" + thumbWidth);
    	
    	// Create the required compatible (thumbnail) buffered image to avoid
    	// potential errors from Java's ImagingLib.
    	ColorModel cm = image.getColorModel();
    	WritableRaster r =
    		cm.createCompatibleWritableRaster(thumbWidth, thumbHeight);
    	BufferedImage thumbImage = new BufferedImage(cm, r, false, null);
    	
    	// Do the actual scaling and return the result
        Graphics2D graphics2D = thumbImage.createGraphics();
        //graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        //  RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
        return thumbImage;
    }
}
				