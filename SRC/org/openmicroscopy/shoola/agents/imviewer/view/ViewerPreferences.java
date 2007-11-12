/*
 * org.openmicroscopy.shoola.agents.imviewer.view.ViewerPreferences 
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
package org.openmicroscopy.shoola.agents.imviewer.view;


//Java imports
import java.awt.Rectangle;

//Third-party libraries

//Application-internal dependencies

/** 
 * Utility class used to store the user preferences for the viewer.
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
public class ViewerPreferences
{

	private boolean 	renderer;
	
	private Rectangle 	viewerBounds;
	
	private int			zoomIndex;

	/**
	 * Returns <code>true</code> if the renderer is visible,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isRenderer() { return renderer; }

	/**
	 * Sets the passed flag.
	 * 
	 * @param renderer Pass <code>true</code> to have the renderer visible,
	 * 				   <code>false</code> otherwise.
	 */
	public void setRenderer(boolean renderer) { this.renderer = renderer; }

	/**
	 * 
	 * @return
	 */
	public Rectangle getViewerBounds() { return viewerBounds; }

	public void setViewerBounds(Rectangle viewerBounds) {
		this.viewerBounds = viewerBounds;
	}

	/** 
	 * Returns the selected zoom index.
	 * 
	 * @return See above.
	 */
	public int getZoomIndex() {
		return zoomIndex;
	}

	/**
	 * Sets the preferred zooming index.
	 * 
	 * @param zoomIndex The value to set.
	 */
	public void setZoomIndex(int zoomIndex) { this.zoomIndex = zoomIndex; }
	
}
