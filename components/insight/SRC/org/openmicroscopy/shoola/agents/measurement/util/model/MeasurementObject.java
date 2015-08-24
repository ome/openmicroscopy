/*
 * org.openmicroscopy.shoola.agents.measurement.util.MeasurementObject 
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
package org.openmicroscopy.shoola.agents.measurement.util.model;


import java.util.ArrayList;
import java.util.List;

import org.openmicroscopy.shoola.util.roi.model.ROIShape;

/** 
 * Helper class used to store various object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class MeasurementObject
{

	/** Store the passed objects. */
	protected List<Object>	elements;
	
	/** The object of reference.*/
	private ROIShape reference;
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param reference The object of reference.
	 */
	public MeasurementObject(ROIShape reference)
	{
		this.reference = reference;
		elements = new ArrayList<Object>();
	}
	
	/**
	 * Returns the object of reference.
	 * 
	 * @return See above.
	 */
	public ROIShape getReference() { return reference; }
	
	/**
	 * Adds the passed element to the collection if not <code>null</code>.
	 * 
	 * @param element The element to add.
	 */
	public void addElement(Object element)
	{
		if (element != null) elements.add(element);
		else elements.add("");
	}
	
	/**
	 * Returns the element at the specified position in this list.
	 * 
	 * @param index The index of the element to return.
	 * @return See above.
	 */
	public Object getElement(int index)
	{
		if (index >= 0 &&  index < elements.size()) 
			return elements.get(index);
		return null;
	}
	
	/**
	 * Replaces the element at the specified position in this list with the
     * specified element.
     * 
	 * @param value	The value to set.
	 * @param index	The index of the element to set.
	 */
	public void setElement(Object value, int index)
	{
		if (index >= 0 || index < elements.size()) 
			elements.set(index, value);
	}
	
	/**
	 * Returns the size of the list.
	 * 
	 * @return See above.
	 */
	public int getSize() { return elements.size(); }
	
}
