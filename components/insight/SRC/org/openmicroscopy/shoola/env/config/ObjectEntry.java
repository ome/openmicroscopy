/*
 * org.openmicroscopy.shoola.env.config.ObjectEntry
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.env.config;


//Java imports

//Third-party libraries
import org.w3c.dom.Node;

//Application-internal dependencies

/** 
 * Convenience class to store a name-value pair that is added to the registry
 * by means of the {@link Registry#bind(String, Object) bind} method. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ObjectEntry
	extends Entry
{
	
	/** The lookup name. Overrides the same field in {@link Entry}. */
	//NB: we do this b/c Entry.name has to be kept private to avoid subclasses
	//screwing around and modifying mappings.
	private String		name;
	
	/** The value. */
	private Object		value;
	
	/**
	 * Creates a new entry to be added to the registry.
	 * 
	 * @param name	The lookup name given to this entry.
	 */
	ObjectEntry(String name)
	{
		this.name = name;
	}

	/** 
	 * Returns this entry's value.
	 * @see Entry#getValue()
	 */
	Object getValue() { return value; }

	/**
	 * Does nothing as this entry is created by the 
	 * {@link Registry#bind(String, Object) bind} method.
	 * @see Entry#setContent(org.w3c.dom.Node)
	 */
	protected void setContent(Node node)
	{
		//Do nothing. This is an in-memory entry.	
	}

	/** 
	 * Stores <code>content</code> as this entry's value.
	 * 
	 * @param content	The entry's value.
	 */
	void setContent(Object content) { value = content; }
	
	/** 
     * Overrides the superclass method to return the correct value. 
     * @see Entry#getName()
     */
     @Override
	String getName() { return name; }

}
