 /*
 * org.openmicroscopy.shoola.agents.editor.model.params.BooleanParam 
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
package org.openmicroscopy.shoola.agents.editor.model.params;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * A class for modeling a boolean parameter
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class BooleanParam 
	extends AbstractParam 
{

	/**
	 * This defines a parameter that is a boolean.
	 * Equivalent to the "CheckBoxField" of Beta 3.0
	 */
	public static final String 		BOOLEAN_PARAM = "BOOLEAN";

	/**
	 * Creates an instance. 
	 * 
	 * @param fieldType		The String defining the field type
	 */
	public BooleanParam() 
	{
		super(BOOLEAN_PARAM);
	}
	
	/**
	 * Returns the value of the parameter (and units, if any). 
	 * 
	 * @see Object#toString()
	 */
	public String toString() {
		
		String text = super.toString();
		
		String value = getParamValue();
		if (value != null) {
			text = value;
		}
		
		return text;
	}
	
	/** 
 	 * Overridden to set the value as a boolean. 
 	 * @see AbstractParam#setValueAt(int, Object) 
 	 */ 
	public void setValueAt(int index, Object value) 
	{
		if (value == null) { 
		 	super.setValueAt(index, null); 
		 	return; 
		} 
		Boolean b = Boolean.valueOf(value.toString());
		super.setValueAt(index, b);
	}

	/**
	 * Gets the specified value in the list of values for this parameter.
	 * Returns null if index is greater than length of list.  
	 * 
	 * @param index		The index of the parameter. 
	 * @return			The value
	 */
	public Object getValueAt(int index) 
	{
		Object obj = super.getValueAt(index);
		
		if (obj == null)
			return null;
		return Boolean.valueOf(obj.toString());
	}
}
