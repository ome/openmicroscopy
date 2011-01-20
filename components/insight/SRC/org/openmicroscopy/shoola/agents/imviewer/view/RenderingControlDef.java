/*
 * org.openmicroscopy.shoola.agents.imviewer.view.RenderingControlDef 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.RenderingControl;

/** 
 * 
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
class RenderingControlDef
{

	/** The id of the pixels set. */
	private long 				pixelsID;
	
	/** The image's id. */
	private long 				imageID;
	
	/** The name of the image. */
	private String 				name;
	
	/** The rendering control. */
	private RenderingControl	rndControl;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param pixelsID	The id of the pixels set.
	 * @param imageID	The id of the image.
	 * @param name		The name of the image.
	 */
	RenderingControlDef(long pixelsID, long imageID, String name)
	{
		this.pixelsID = pixelsID;
		this.imageID = imageID;
		this.name = name;
	}
	
	/**
	 * Sets the rendering control.
	 * 
	 * @param rndControl The value to set.
	 */
	void setRenderingControl(RenderingControl rndControl)
	{
		this.rndControl = rndControl;
	}
	
	/**
	 * Returns the name of the image.
	 * 
	 * @return See above.
	 */
	String getName() { return name; }
	
	/**
	 * Returns the pixels set id.
	 * 
	 * @return See above.
	 */
	long getPixelsID() { return pixelsID; }
	
	/**
	 * Returns the image's id.
	 * 
	 * @return See above.
	 */
	long getImageID() { return imageID; }
	
	/**
	 * Returns the rendering control associated to the pixels set.
	 * 
	 * @return See above.
	 */
	RenderingControl getRndControl() { return rndControl; }

}
