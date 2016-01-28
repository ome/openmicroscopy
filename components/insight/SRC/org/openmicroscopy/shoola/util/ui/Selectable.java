/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola.util.ui;


/** 
 * Provides a wrapper class for a selectable option
 *
 * @author Scott Littlewood, <a href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since 4.4
 */
public class Selectable<T> {

	/** The wrapped object */
	private T obj;
	
	/** The state of selection */
	private boolean selectable;
	
	/**
	 * Creates an instance of the selectable class
	 * @param obj The object being wrapped
	 * @param selectable Whether the item is selectable or not.
	 */
	public Selectable(T obj, boolean selectable)
	{
		this.obj = obj;
		this.selectable = selectable;
	}
	
	/**
	 * Return the wrapped object
	 * @return See above
	 */
	public T getObject()
	{
		return obj;
	}
	
	/**
	 * Returns whether the item should be selectable or not.
	 * 
	 * @return See above.
	 */
	public boolean isSelectable()
	{
		return selectable;
	}
	
	/**
	 * Sets to <code>true</code> if the object can be selected,
	 * <code>false</code> otherwise.
	 * 
	 * @param isSelectable The value to set.
	 */
	public void setSelectable(boolean isSelectable) {
		this.selectable = isSelectable;
	}
	
	/**
	 * Returns the String representation of the wrapped object.
	 * @see #toString()
	 */
	public String toString()
	{
		return obj.toString();
	}


}
