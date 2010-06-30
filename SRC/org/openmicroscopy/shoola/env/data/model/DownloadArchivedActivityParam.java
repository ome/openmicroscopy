/*
 * org.openmicroscopy.shoola.env.data.model.DownloadArchivedActivityParam 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.model;



//Java imports
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies
import pojos.ImageData;

/** 
 * Hosts the parameters required to download the archived image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class DownloadArchivedActivityParam 
{

	/** The icon associated to the parameters. */
    private Icon			icon;
    
    /** The location where to download the archived files. */
    private String			location;
    
    /** The collection of archived images to download. */
    private ImageData 		image;
    
    /**
     * Creates a new instance.
     * 
     * @param location  The location where to download the archived files.
     * @param image     The archived images to download.
     * @param icon	    The icon associated to the parameters.
     */
    public DownloadArchivedActivityParam(String location, ImageData image, 
    		Icon icon)
    {
    	this.location = location;
    	this.image = image;
    	this.icon = icon;
    }
    
    /**
     * Returns the icon.
     * 
     * @return See above.
     */
    public Icon getIcon() { return icon; }
    
    /**
     * Returns the path to the folder where to download the archived files.
     * 
     * @return See above.
     */
    public String getLocation() { return location; }
    
    /**
     * Returns the archived image to download.
     * 
     * @return See above.
     */
    public ImageData getImage() { return image; }
    
}
