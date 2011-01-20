/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.WikiDataObject 
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
package org.openmicroscopy.shoola.util.ui.omeeditpane;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Object hosting details about an OMERO object to launch.
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
public class WikiDataObject 
{

	/** Indentifies a <code>Project</code>. */
	public static final int PROJECT = 0;
	
	/** Indentifies a <code>Dataset</code>. */
	public static final int DATASET = 1;
	
	/** Indentifies a <code>Image</code>. */
	public static final int IMAGE = 2;
	
	/** Indentifies a <code>Thumbnail</code>. */
	public static final int THUMBNAIL = 3;
	
	/** Indentifies a <code>Protocol</code>. */
	public static final int PROTOCOL = 4;
	
	/** One of the constants defined by this class. */
	private int 	index;

	/** The id of the object. */
	private long 	id;
	
	/** The name of the object. */
	private String 	name;
	
	/**
	 * Controls if the index is supported.
	 * 
	 * @param value The value to check.
	 */
	private void checkIndex(int value)
	{
		switch (value) {
			case PROJECT:
			case DATASET:
			case IMAGE:
			case THUMBNAIL:
			case PROTOCOL:
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index One of the constants defined by this class.
	 * @param id	The id of the object.
	 */
	WikiDataObject(int index, long id)
	{
		checkIndex(index);
		this.index = index;
		this.id = id;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index One of the constants defined by this class.
	 * @param name	The name of the object.
	 */
	WikiDataObject(int index, String name)
	{
		checkIndex(index);
		this.index = index;
		this.name = name;
		id = -1;
	}
	
	/**
	 * Returns the index.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
	/**
	 * Returns the id of the object.
	 * 
	 * @return See above.
	 */
	public long getId() { return id; }
	
	/**
	 * Returns the name of the object.
	 * 
	 * @return See above.
	 */
	public String getName() { return name; }
	
}
