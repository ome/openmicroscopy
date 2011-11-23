/*
 * org.openmicroscopy.shoola.agents.events.iviewer.ViewImage
 * 
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.events.iviewer;




//Java imports
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Event to retrieve and view a given image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald McDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ViewImage
    extends RequestEvent
{

    /** The images to view. */
    private List<ViewImageObject>	images;

    /** The bounds of the component posting the event. */
    private Rectangle   requesterBounds;
    
    /**  
     * Flag indicating if the viewer should be opened as a separate window
     * or not. The default value is <code>true</code>.
     */
    private boolean		separateWindow;
    
    /**
     * Creates a new instance.
     * 
     * @param image  	The id of the image to view.
     * @param bounds    The bounds of the component posting the event.
     */
    public ViewImage(ViewImageObject image, Rectangle bounds)
    {
    	if (image == null) 
    		throw new IllegalArgumentException("Image not null.");
    	images = new ArrayList<ViewImageObject>();
    	images.add(image);
    	requesterBounds = bounds;
    	separateWindow = true;
    }

    /**
     * Adds a new image to the list of images to view.
     * 
     * @param image The image to view.
     */
    public void addImage(ViewImageObject image)
    {
    	if (image != null)
    		images.add(image);
    }
    
    /**
     * Returns the images to view.
     * 
     * @return See above.
     */
    public List<ViewImageObject> getImages() { return images; }
    
    /**
     * Returns the bounds of the component posting the event. 
     * Returns <code>null</code> if not available.
     * 
     * @return See above.
     */
    public Rectangle getRequesterBounds() { return requesterBounds; }
    
    /**
     * Returns <code>true</code> if the viewer should be opened in a 
     * separate window, <code>false</code> otherwise.
     * The default value is <code>true</code>.
     * 
     * @return See above.
     */
    public boolean isSeparateWindow() { return separateWindow; }
    
    /**
     * Sets to <code>true</code> if the viewer should be opened in a separate
     * window, <code>false</code> otherwise.
     * 
     * @param separateWindow The value to set.
     */
    public void setSeparateWindow(boolean separateWindow)
    {
    	this.separateWindow = separateWindow;
    }

}
