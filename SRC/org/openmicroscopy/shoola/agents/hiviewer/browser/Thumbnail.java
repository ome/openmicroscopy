/*
 * org.openmicroscopy.shoola.agents.hiviewer.browser.Thumbnail
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package org.openmicroscopy.shoola.agents.hiviewer.browser;


//Java imports
import java.awt.image.BufferedImage;
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies

/** 
 * Defines the functionality required of a thumbnail provider.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public interface Thumbnail
{
    
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
    
    /** Returns the current scaling factor. */
    public double getScalingFactor();
    
    /** Returns the original thumbnail. */
    public BufferedImage getFullScaleThumb();
    
    /** Returns the icon representing the thumbnail. */
    public Icon getIcon();
    
    public void setImageNode(ImageNode node);
    
}
