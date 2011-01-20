/*
 * org.openmicroscopy.shoola.agents.treeviewer.browser.TreeFileSet 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.browser;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Nodes hosting <code>FileAnnotationData</code> objects.
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
public class TreeFileSet 
	extends TreeImageSet
{

	/** Indicates that the node should host editor file for protocol. */
	public static final int PROTOCOL = 0;
	
	/** Indicates that the node should host editor file for experiments. */
	public static final int EXPERIMENT = 1;
	
	/** Indicates that the node should host movie files. */
	public static final int MOVIE = 2;
	
	/** Indicates that the node should host all the other types of files. */
	public static final int OTHER = 3;

	/**
	 * Returns the value corresponding to the passed index.
	 * 
	 * @param type The type to handle;
	 * @return See above.
	 */
	private static String getTypeName(int type)
	{
		switch (type) {
			case PROTOCOL: 
				return "Protocols";
			case EXPERIMENT: return "Experiments";
			case MOVIE: return "Movies";
			case OTHER:
			default:
				return "Other files";
		}
	}
	
	/** One of the constants defined by this class. */
	private int type;
	
	/**
	 * Creates a new intance.
	 * 
	 * @param type One of the constants defined by this class.
	 */
	public TreeFileSet(int type)
	{
		super(getTypeName(type));
		switch (type) {
			case PROTOCOL: 
			case EXPERIMENT: 
			case MOVIE:
				this.type = type;
				break;
			case OTHER:
			default:
				this.type = OTHER;
		}
	}
	
	/**
	 * Returns the type. One of the constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getType() { return type; }
	
}
