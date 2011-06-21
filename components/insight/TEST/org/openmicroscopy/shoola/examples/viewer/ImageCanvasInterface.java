/*
 * org.openmicroscopy.shoola.examples.viewer.ImageCanvasInterface
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *----------------------------------------------------------------------------*/

package org.openmicroscopy.shoola.examples.viewer;

//Java imports
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies

/** 
 * All canvas's that work in the demo implement this interface. 
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
public interface ImageCanvasInterface
{
	
	/**
	 * Sets the image.
	 * 
	 * @param image The image to paint.
	 */
	void setImage(BufferedImage image);
	
	/**
	 * Get the UI for the canvas.
	 * @return See above.
	 */
	Component getCanvas();
	
	/**
	 * Set the size of the canvas.
	 * @param d The size of the canvas.
	 */
	void setCanvasSize(Dimension d);
	
}
