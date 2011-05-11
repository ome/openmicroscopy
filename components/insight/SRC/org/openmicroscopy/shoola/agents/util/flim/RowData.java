/*
 * org.openmicroscopy.shoola.agents.util.flim.RowData
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
package org.openmicroscopy.shoola.agents.util.flim;

//Java imports
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies

/**
 * Component displaying the histogram.
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
public class RowData 
{
	/** The data of the row.*/
	List<Object> data;
	
	/** The names of the data.*/
	List<String> names;
	
	/**
	 * Instantiate the row.
	 */
	RowData()
	{
		data = new ArrayList<Object>();
		names = new ArrayList<String>();
	}
	
	/**
	 * Add an element to the row.
	 * @param element The element to add.
	 */
	public void addElement(Object element)
	{
		data.add(element);
	}
	
	/**
	 * Get the element.
	 * @param index The index of the element.
	 * @return See above.
	 */
	public Object getElement(int index)
	{
		return data.get(index);
	}
	
	/**
	 * Get the number of elements in the row.
	 * @return See above.
	 */
	public int size()
	{
		return data.size();
	}
	
}
