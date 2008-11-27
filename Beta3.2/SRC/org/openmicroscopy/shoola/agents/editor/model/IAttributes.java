 /*
 * org.openmicroscopy.shoola.agents.editor.model.IAttributes 
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
package org.openmicroscopy.shoola.agents.editor.model;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Methods for editing a collection of attributes.
 * The attributes are identified by name (String) and the values are Strings.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public interface IAttributes {
	
	/**
	 * Returns the string value of the named attribute.
	 * 
	 * @param name	Name of the attribute
	 * @return		The value of the attribute
	 */
	public String getAttribute(String name);
	
	/**
	 * Sets the value of the named attribute.
	 * 
	 * @param name	The name of the attribute	
	 * @param value	The value of the attribute
	 */
	public void setAttribute(String name, String value);
	
	/**
	 * A method for querying the value of a boolean attribute
	 * 
	 * @param attributeName		The name of the attribute
	 * @return		True if the attribute value is "true". 
	 */
	public boolean isAttributeTrue(String attributeName);


}
