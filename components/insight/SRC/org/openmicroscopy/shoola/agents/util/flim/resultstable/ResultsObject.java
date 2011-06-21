/*
 * org.openmicroscopy.shoola.env.ui.resultstable.ResultsObject 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.flim.resultstable;

//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/** 
 * Displays the results stored in the passed file.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ResultsObject
{

	/** Store the passed objects. */
	protected Map<String, Object>	elements;
		
	
	/** Creates a new instance. */
	public ResultsObject()
	{
		elements = new HashMap<String, Object>(); 
	}
	
	/**
	 * Create the results object from two lists, the first the keys, and the second the matching values.
	 * @param keys See above.
	 * @param values See above.
	 */
	public ResultsObject(List<String> keys, List<String> values)
	{
		if(keys.size()!=values.size() || keys.size()==0)
			throw new IllegalArgumentException("Keys and Values must be same length and non-zero length.");
		elements = new HashMap<String, Object>();
		for(int i = 0 ; i < keys.size() ; i++)
			elements.put(keys.get(i), values.get(i));
	}
	/**
	 * Adds the passed element to the collection if not <code>null</code>.
	 * 
	 * @param element The element to add.
	 */
	public void addElement(String key, Object element)
	{
		if (element != null) elements.put(key, element);
	}
	
	/**
	 * Returns the element at the specified position in this list.
	 * 
	 * @param index The index of the element to return.
	 * @return See above.
	 */
	public Object getElement(String key)
	{
		if(elements.containsKey(key))
		{
			return elements.get(key);
		}
		return null;
	}
	
	/**
	 * Replaces the element at the specified position in this list with the
     * specified element.
     * 
	 * @param value	The value to set.
	 * @param index	The index of the element to set.
	 */
	public void setElement(String key, Object value)
	{
		elements.put(key, value);
	}
	
	/**
	 * Returns the size of the list.
	 * 
	 * @return See above.
	 */
	public int getSize() { return elements.size(); }
	
	/**
	 * Get all the keys in the results object.
	 * @return
	 */
	public List<String> getKeys()
	{
		List<String> keyList = new ArrayList<String>();
		Iterator<String> keyIterator = elements.keySet().iterator();
		while(keyIterator.hasNext())
		{
			keyList.add(keyIterator.next());
		}
		return keyList;
	}
}

