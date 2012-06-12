/*
 * org.openmicroscopy.shoola.agents.events.treeviewer.BrowserSelectionEvent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
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
 * Event posted when a new browser is selected.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class BrowserSelectionEvent
	extends RequestEvent
{

	/** Indicates that the type is for project. */
	public static final int		PROJECT_TYPE = 0;
	
	/** Indicates that the type is for Screening data. */
	public static final int		SCREEN_TYPE = 1;
	
	/** Indicates that the type is for Tag data. */
	public static final int		TAG_TYPE = 2;
	
	/** Indicates that the type is for File Annotation data. */
	public static final int		FILE_TYPE = 3;
	
	/** Indicates that the type is for File System data. */
	public static final int		FILE_SYSTEM_TYPE = 4;
	
	/** Indicates that the type is for Image data. */
	public static final int		IMAGE_TYPE = 5;
	
	/** Indicates that the type is for Administration data. */
	public static final int		ADMIN_TYPE = 6;
	
	/** The type of browser. One of the constants defined by this class.*/
	private int type;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param type The type of browser.
	 */
	public BrowserSelectionEvent(int type)
	{
		this.type = type;
	}
	
	/**
	 * Returns the type of browser.
	 * 
	 * @return See above.
	 */
	public int getType() { return type; }
	
}
