/*
 * org.openmicroscopy.shoola.agents.imviewer.view.ImViewerRecentObject 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.view;



//Java imports
import javax.swing.ImageIcon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import omero.gateway.SecurityContext;

/** 
 * Utility class where details about a recenlty viewed image are stored.
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
public class ImViewerRecentObject
{

	/** Reference to an image recently viewed. */
	private long 		imageID;
	
	/** Reference to an image recently viewed. */
	private String		imageName;
	
	/** A thumbnail representation of the image. */
	private ImageIcon	icon;
	
	/** The security context.*/
	private SecurityContext ctx;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param ctx The security context.
	 * @param imageID The id of image viewed.
	 * @param imageName The name of image viewed
	 * @param icon A icon of the last image.
	 */
	ImViewerRecentObject(SecurityContext ctx, long imageID, String imageName,
			ImageIcon icon)
	{
		this.imageID = imageID;
		this.imageName = imageName;
		this.ctx = ctx;
		if (icon == null) {
			IconManager im = IconManager.getInstance();
			icon = im.getImageIcon(IconManager.VIEWER);
		}
		this.icon = icon;
	}
	
	/**
	 * Returns the id of image recently viewed.
	 * 
	 * @return See above.
	 */
	public long getImageID() { return imageID; }
	
	/**
	 * Returns the name of image recently viewed.
	 * 
	 * @return See above.
	 */
	public String getImageName() { return imageName; }
	
	/**
	 * Returns a thumbnail representation of the image. 
	 * 
	 * @return See above.
	 */
	public ImageIcon getImageIcon() { return icon; }
	
	/**
     * Returns the security context.
     * 
     * @return See above.
     */
    public SecurityContext getSecurityContext() { return ctx; }
}
