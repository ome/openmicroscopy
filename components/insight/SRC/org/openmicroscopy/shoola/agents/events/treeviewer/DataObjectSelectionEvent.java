/*
 * org.openmicroscopy.shoola.agents.events.treeviewer.DataObjectSelectionEvent 
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
package org.openmicroscopy.shoola.agents.events.treeviewer;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Event indicating that one object has been selected.
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
public class DataObjectSelectionEvent 
	extends RequestEvent
{

	/** Identifies the data type. */
	private Class dataType;
	
	/** The identifier of the object to select. */
	private long  id;
	
	/** Flag indicating to select the tab corresponding to the passed type. */
	private boolean selectTab;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dataType The type of data object.
	 * @param id       The identifier of object.
	 */
	public DataObjectSelectionEvent(Class dataType, long id)
	{
		this.dataType = dataType;
		this.id = id;
		selectTab = false;
	}
	
	/**
	 * Returns <code>true</code> to select the tab corresponding to the passed 
	 * type, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isSelectTab() { return selectTab; }
	
	/**
	 * Sets the flag indicating to the select or no the tab.
	 * 
	 * @param selectTab Pass <code>true</code> to select the tab corresponding
	 * 					to the passed type, <code>false</code> otherwise.
	 */
	public void setSelectTab(boolean selectTab) { this.selectTab = selectTab; }
	
	/**
	 * Returns the identifier of the object.
	 * 
	 * @return See above.
	 */
	public long getID() { return id; }
	
	/**
	 * Returns the type of object to handle.
	 * 
	 * @return See above.
	 */
	public Class getDataType() { return dataType; }
	
}
