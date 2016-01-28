/*
 * org.openmicroscopy.shoola.env.ui.SaveEventBox 
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
package org.openmicroscopy.shoola.env.ui;

import javax.swing.JCheckBox;

import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Utility class used to save the data before closing the application.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class SaveEventBox
	extends JCheckBox
{

	/** The event hosted by this node. */
	private RequestEvent evt;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param evt 	The event hosted by this node. Mustn't be <code>null</code>.
	 */
	public SaveEventBox(RequestEvent evt)
	{
		if (evt == null)
			throw new IllegalArgumentException("Event cannot be null.");
		this.evt = evt;
		setSelected(true);
		setText(evt.toString());
	}
	
	/**
	 * Returns the event hosted by this node.
	 * 
	 * @return See above.
	 */
	public RequestEvent getEvent() { return evt; }
	
}
