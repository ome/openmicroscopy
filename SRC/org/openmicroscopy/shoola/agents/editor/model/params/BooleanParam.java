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
		
		return text;
	}
	
	/**
	 * Implemented as specified by the {@link IParam} interface. 
	 * 
	 * @see IParam#getParamAttributes()
	 */
	public String[] getParamAttributes() 
	{
		return new String[] {TextParam.PARAM_VALUE, TextParam.DEFAULT_VALUE};
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
