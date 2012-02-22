/*
 * org.openmicroscopy.shoola.agents.events.treeviewer.ExperimenterLoadedDataEvent 
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
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Event posted when data are loaded, tree refreshed.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class ExperimenterLoadedDataEvent 
	extends RequestEvent
{

	/** Map hosting the data objects owned by users displayed.*/
	private Map<Long, List<TreeImageDisplay>> data;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param data The data to store.
	 */
	public ExperimenterLoadedDataEvent(Map<Long, List<TreeImageDisplay>> data)
	{
		this.data = data;
	}
	
	/**
	 * Returns the data.
	 * 
	 * @return See above.
	 */
	public Map<Long, List<TreeImageDisplay>> getData() { return data; }
	
}
