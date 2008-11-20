 /*
 * org.openmicroscopy.shoola.agents.editor.model.params.EnumParam 
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
 *  A class for modeling an enumeration parameter. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class EnumParam 
	extends AbstractParam 
{

	/**
	 * This defines a string parameter that is an enumeration of
	 * a fixed number of values. 
	 * Additional attributes define the possible values.
	 * Equivalent to the "DropDownMenu" of Beta 3.0
	 */
	public static final String 		ENUM_PARAM = "ENUMERATION";
	
	/**
	 * An attribute used by the ENUM_PARAM to store a comma-delimited
	 * set of options. 
	 * Equivalent to the "dropdownOptions" of Beta 3.0
	 */
	public static final String 		ENUM_OPTIONS = "enumOptions";

	/**
	 * Creates an instance. 
	 * 
	 * @param fieldType		The String defining the field type
	 */
	public EnumParam() 
	{
		super(EnumParam.ENUM_PARAM);
	}
	
	/**
	 * Returns the value of the parameter. 
	 * 
	 * @see Object#toString()
	 */
	public String toString() {
		String text = "";
		
		String value = getAttribute (TextParam.PARAM_VALUE);
		String def = getAttribute (TextParam.DEFAULT_VALUE);
		if (value != null) { 
			text = value; 
		} else if (def != null) {
			text = "Default: " + def;
		} else {
			text = "Value not set";
		}
		
		String units = getAttribute(NumberParam.PARAM_UNITS);
		if (units != null) { text = text + " " + units; }
		
		return text;
	}
	
	/**
	 * Implemented as specified by the {@link IParam} interface. 
	 * 
	 * @see IParam#getParamAttributes()
	 */
	public String[] getParamAttributes() 
	{
		return new String[] {TextParam.PARAM_VALUE, 
				TextParam.DEFAULT_VALUE,
				EnumParam.ENUM_OPTIONS};
	}

	/**
	 * Implemented as specified by the {@link IParam} interface. 
	 * 
	 * @see IParam#isParamFilled()
	 */
	public boolean isParamFilled() 
	{
		return (getAttribute(TextParam.PARAM_VALUE) != null);
	}

}
