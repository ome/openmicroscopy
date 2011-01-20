/*
 * org.openmicroscopy.shoola.env.data.util.SearchResult 
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
package org.openmicroscopy.shoola.env.data.util;


//Java imports
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies

/** 
 * Utility class hosting the various parameters used to display the 
 * result of a search.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class SearchResult
{

	/** Collection of image's identifiers. */
	private Set<Long> 			nodeIDs;
	
	/** 
	 * Context of the search: key the string to search for, value is a map
	 * whose key is the scope and the value the number of items found.
	 */
	private Map<String, Map> 	context;
	
	/** Creates a new instance, */
	public SearchResult()
	{
		nodeIDs = new HashSet<Long>();
		context = null;
	}
	
	/**
	 * Sets the collection of image's identifiers.
	 * 
	 * @param nodeIDs The value to set.
	 */
	public void setNodeIDs(Set<Long> nodeIDs) { this.nodeIDs = nodeIDs; }
	
	/**
	 * Sets the context of the search.
	 * 
	 * @param context The value to set.
	 */
	public void setContext(Map<String, Map> context) { this.context = context; }
	
	/**
	 * Returns the collection of image's identifiers.
	 * 
	 * @return See above.
	 */
	public Set<Long> getNodeIDs()
	{ 
		return Collections.unmodifiableSet(nodeIDs);
	}
	
	/**
	 * Returns the context of the search.
	 * 
	 * @return See above.
	 */
	public Map<String, Map> getContext() { return context; }
	
}
