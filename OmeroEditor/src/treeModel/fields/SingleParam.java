 /*
 * fields.TextValue 
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
package treeModel.fields;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
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

	public static final String PARAM_VALUE = "paramValue";
	
	public static final String DEFAULT_VALUE = "defaultValue";
	
	
	/*
	 * The parameter types below use this single parameter 
	 * because they only have a single "value" and "default"
	 */
	
	/**
	 * A parameter defined as a short text string. 
	 * Equivalent to the "TextField" of Beta 3.0
	 */
	public static final String TEXT_LINE_PARAM = "textLineParam";
	
	/**
	 * This defines a parameter that is a longer piece of text.
	 * Equivalent to the "TextBox" of Beta 3.0
	 */
	public static final String TEXT_BOX_PARAM = "textBoxParam";
	
	/**
	 * This defines a parameter that is a number. 
	 * Could be integer or float.
	 * Additional attributes are "units"
	 * Equivalent to the "NumberField" of Beta 3.0
	 */
	public static final String NUMBER_PARAM = "numberParam";
	
	/**
	 * This defines a string parameter that is an enumeration of
	 * a fixed number of values. 
	 * Additional attributes define the possible values.
	 * Equivalent to the "DropDownMenu" of Beta 3.0
	 */
	public static final String ENUM_PARAM = "enumParam";
	
	/**
	 * This defines a parameter that is a boolean.
	 * Equivalent to the "CheckBoxField" of Beta 3.0
	 */
	public static final String BOOLEAN_PARAM = "booleanParam";
	
	/**
	 * This defines a time parameter, in seconds. 
	 * Equivalent to the "TimeField" of Beta 3.0
	 */
	public static final String TIME_PARAM = "timeParam";
	
	/**
	 * An attribute used by the ENUM_PARAM to store a comma-delimited
	 * set of options. 
	 * Equivalent to the "dropdownOptions" of Beta 3.0
	 */
	public static final String ENUM_OPTIONS = "enumOptions";
	
	/**
	 * Creates an instance. 
	 * 
	 * @param fieldType		The String defining the field type
	 */
	public SingleParam(String fieldType) {
		super(fieldType);
	}
	
	
	/**
	 * The value attribute is a single value
	 */
	public String[] getValueAttributes() {
		
		return new String[] {PARAM_VALUE};
	}
	
	/**
	 * Returns a single attribute name that identifies the default value
	 */
	public String[] getDefaultAttributes() {
		return new String [] {DEFAULT_VALUE};
	}

	/**
	 * This field is filled if the value isn't null, and 
	 * is not an empty string. 
	 */
	public boolean isParamFilled() {
		String textValue = getAttribute(PARAM_VALUE);
		
		return (textValue != null && textValue.length() > 0);
	}

}
