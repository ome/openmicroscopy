/*
 * org.openmicroscopy.shoola.agents.events.treeviewer.DisplayModeEvent
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.events.treeviewer;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/**
 * Posts an event when the display mode is modified.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class DisplayModeEvent
	extends RequestEvent
{

	/** The display mode.*/
	private int displayMode;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param displayMode The value to set.
	 */
	public DisplayModeEvent(int displayMode)
	{
		this.displayMode = displayMode;
	}
	
	/**
	 * Returns the display mode.
	 * 
	 * @return See above.
	 */
	public int getDisplayMode() { return displayMode; }

}
