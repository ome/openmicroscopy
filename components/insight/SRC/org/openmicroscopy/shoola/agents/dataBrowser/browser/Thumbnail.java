/*
 * org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.dataBrowser.browser;


//Java imports
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.Icon;

import org.openmicroscopy.shoola.util.image.geom.Factory;

//Third-party libraries

//Application-internal dependencies

/** 
 * Defines the functionality required of a thumbnail provider.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public interface Thumbnail
{

    /** The maximum width of the thumbnail. */
    public static final int		THUMB_MAX_WIDTH = Factory.THUMB_DEFAULT_WIDTH; 
    
    /** The maximum height of the thumbnail. */
    public static final int     THUMB_MAX_HEIGHT = Factory.THUMB_DEFAULT_HEIGHT;
    
    /** The maximum magnification factor. */
    public static final double  MAX_SCALING_FACTOR = 2.5;//1;
    
    /** The minimum magnification factor. */
    public static final double  MIN_SCALING_FACTOR = 0.25;
    
    /** The magnification factor for the icon.*/
    public static final double ICON_ZOOM = 0.16;
    
    /**
     * Returns the width, in pixels, of the thumbnail.
     * 
     * @return See above.
     */
    public int getWidth();
    
    /**
     * Returns the height, in pixels, of the thumbnail.
     * 
     * @return See above.
     */
    public int getHeight();
    
    /**
     * Retrieves the thumbnail image.
     * This method may return <code>null</code> if the image is not readily
     * available.  In this case, an asynchronous data retrieval should be
     * fired and then the <code>repaint</code> method of the related <code>node
     * </code> should be called when the image is available.  This will cause
     * the node to call this method again to retrieve the image.
     * 
     * @return The thumbnail image.
     */
    public BufferedImage getDisplayedImage();
   
    /** 
     * Scales the thumbnail.
     * 
     * @param f scaling factor. Must be a value strictly positive and <=1.
     */
    public void scale(double f);
    
    /** 
     * Returns the current magnification factor. 
     * 
     * @return The magnification factor.
     */
    public double getScalingFactor();
    
    /**
     * Sets the original thumbnail retrieved from the server.
     * 
     * @param thumb The value to set.
     */
    public void setFullScaleThumb(BufferedImage thumb);
    
    /** 
     * Returns the original thumbnail.
     * 
     * @return See above.
     */
    public BufferedImage getFullScaleThumb();
    
    /**
     * Returns a magnified version of the original thumbnail.
     * 
     * @return See above.
     */
    public BufferedImage getZoomedFullScaleThumb();
    
    /** 
     * Returns the icon representing the thumbnail.
     * The magnification factor uses is {@link #ICON_ZOOM}.
     * 
     * @return See above.
     */
    public Icon getIcon();
    
    /** 
     * Returns the icon representing the thumbnail.
     * The magnification factor uses is {@link #ICON_ZOOM} if the specified
     * value is negative or equals to <code>0</code>.
     * 
     * @param magnification The factor to use to create the icon.
     * @return See above.
     */
    public Icon getIcon(double magnification);
    
    /**
     * Sets the node hosting the display.
     * 
     * @param node The node to set.
     */
    public void setImageNode(ImageNode node);
    
    /**
     * Returns <code>true</code> if the thumbnail is loaded, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isThumbnailLoaded();
    
    /**
     * Returns the full size image associated to the <code>ImageNode</code>.
     * This method should only be used for the slide show.
     * 
     * @return See above.
     */
    public BufferedImage getFullSizeImage();
    
    /**
     * Sets the full size image associated to the <code>ImageNode</code>.
     * This method should only be used for the slide show.
     * 
     * @param image The value to set.
     */
    public void setFullSizeImage(BufferedImage image);
    
    /**
     * Returns the original size of the thumbnail.
     * 
     * @return See above.
     */
    public Dimension getOriginalSize();
    
    /** 
     * Sets the flag indicating if the thumbnail is a dummy one or a real one.
     * 
     * @param valid Pass <code>true</code> if it is a valid thumbnail,
     * 				<code>false</code> otherwise.
     */
    public void setValid(boolean valid);
    
    /** Flushes the images when closing.*/
    public void flush();
    
}
