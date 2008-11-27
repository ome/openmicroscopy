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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;

import pojos.DataObject;
import pojos.ImageData;

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

    /** The image to view. */
    private ImageData	image;

    /** The bounds of the component posting the event. */
    private Rectangle   requesterBounds;
    
    /** Rendering settings to set if any. */
    private RndProxyDef	settings;
    
    /** The id of the user who set the rendering settings. */
    private long		selectedUserID;
    
    /** The parent object. */
    private DataObject	parentContext;
    
    /**
     * Creates a new instance.
     * 
     * @param image   	The image to view.
     * @param bounds    The bounds of the component posting the event.
     */
    public ViewImage(ImageData	image, Rectangle bounds)
    {
        if (image == null) 
            throw new IllegalArgumentException("Image not null.");
        this.image = image;
        requesterBounds = bounds;
        selectedUserID = -1;
	}
    
    /**
     * Sets the parent of the image.
     * 
     * @param parentContext The parent to set.
     */
    public void setParentContext(DataObject parentContext)
    {
    	this.parentContext = parentContext;	
    }
    
    /**
     * Returns the parent.
     * 
     * @return See above.
     */
    public DataObject getParentObject() { return parentContext; }
    
    /**
     * Sets the rendering settings set by the selected user.
     * 
     * @param settings			The settings to set.
     * @param selectedUserID	The id of the user who set the
     * 							the rendering settings.
     */
    public void setSettings(RndProxyDef	settings, long selectedUserID)
    {
    	this.settings = settings;
    	this.selectedUserID = selectedUserID;
    }
    
    /**
     * Returns the rendering settings set by the specified user.
     * 
     * @return See above.
     */
    public RndProxyDef getSettings() { return settings; }
    
    /**
     * Returns the ID of the user the settings are related to.
     * 
     * @return See above. 
     */
    public long getSelectedUserID() { return selectedUserID; }

    /**
     * Returns the image.
     * 
     * @return See above. 
     */
    public ImageData getImage() { return image; }

    /**
     * Returns the bounds of the component posting the event. 
     * Returns <code>null</code> if not available.
     * 
     * @return See above.
     */
    public Rectangle getRequesterBounds() { return requesterBounds; }
    
}
