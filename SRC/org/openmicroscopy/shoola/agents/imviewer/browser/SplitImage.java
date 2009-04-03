/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.SplitImage 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies

/** 
 * Helper class used to host the image for a given channel or color band
 * and the string related to the image.
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
class SplitImage
{

	/** The image. */
	private BufferedImage 	image;
	
	/** The text related to the image. */
	private String			name;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param image	The image. Can be <code>null</code>.
	 * @param name	The related text.
	 */
	SplitImage(BufferedImage image, String name)
	{
		this.image = image;
		this.name = name;
	}
	
	/**
	 * Returns the image hosted by this class.
	 * 
	 * @return See above.
	 */
	BufferedImage getImage() { return image; }
	
	/** 
	 * Returns the text related to the image.
	 * 
	 * @return See above.
	 */
	String getName() { return name; }
	
}
