 /*
 * org.openmicroscopy.shoola.agents.editor.model.params.SingleParam
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

import java.util.HashMap;

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a Parameter that has a single value attribute, PARAM_VALUE.
 * Can be used as the data object for any parameter that has a single
 * value, Eg. Text-Line, Text-Box, Number, Boolean, Enum (drop-down)?
 * and a single default attribute DEFAULT_VALUE.
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class SingleParam 
	extends AbstractParam {

	public static final String PARAM_VALUE = "value";
	
	public static final String DEFAULT_VALUE = "default-value";
	
	/**
	 * A parameter defined as a short text string. 
	 * Equivalent to the "TextField" of Beta 3.0
	 */
	public static final String 		TEXT_LINE_PARAM = "TEXT";
	
	/**
	 * This defines a parameter that is a longer piece of text.
	 * Equivalent to the "TextBox" of Beta 3.0
	 */
	public static final String 		TEXT_BOX_PARAM = "TEXTBOX";
	
	/**
	 * Creates an instance. 
	 * 
	 * @param fieldType		The String defining the field type
	 */
	public SingleParam(String fieldType) 
	{
		super(fieldType);
	}
	
	
	/**
	 * The value attribute is a single value.
	 * If this is filled, then the parameter is filled.
	 * If the field is cleared, this attribute is cleared etc.
	 * 
	 * @see AbstractParam#getParamAttributes()
	 */
	public String[] getParamAttributes() { 
		
		return new String[] {PARAM_VALUE, DEFAULT_VALUE}; 
		}
	
	/**
	 * Returns a single attribute name that identifies the default value
	 * 
	 * @see AbstractParam#getDefaultAttributes()
	 */
	public String[] getDefaultAttributes() 
	{
		return new String [] {DEFAULT_VALUE};
	}

	/**
	 * This field is filled if the value isn't null, and 
	 * is not an empty string. 
	 * 
	 * @see AbstractParam#isParamFilled()
	 */
	public boolean isParamFilled() {
		String textValue = getAttribute(PARAM_VALUE);
		
		return (textValue != null && textValue.length() > 0);
	}
	
	/**
	 * Returns the value of the parameter. 
	 * 
	 * @see Object#toString()
	 */
	public String toString() {
		String text = "";
		
		String value = getAttribute (PARAM_VALUE);
		String def = getAttribute (DEFAULT_VALUE);
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
	 * Implemented as specified by {@link IParam#loadDefaultValues()}
	 * 
	 * Copies the value of the {@link #DEFAULT_VALUE} (if not null)
	 * to the value of the {@link #PARAM_VALUE} attribute. 
	 * 
	 * @see IParam#loadDefaultValues()
	 */
	public HashMap<String, String> loadDefaultValues() 
	{	
		HashMap<String,String> oldValues = new HashMap<String, String>();
		
		String defValue = getAttribute(DEFAULT_VALUE);
		if (defValue != null) {
			oldValues.put(PARAM_VALUE, getAttribute(PARAM_VALUE));
			setAttribute(PARAM_VALUE, defValue);
		}
		return oldValues;
	}
}
