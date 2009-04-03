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

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a Parameter for storing a single line of text.
 * 
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TextParam 
	extends AbstractParam {

	public static final String PARAM_VALUE = "value";
	
	public static final String DEFAULT_VALUE = "default-value";
	
	/**
	 * A parameter defined as a short text string. 
	 * Equivalent to the "TextField" of Beta 3.0
	 */
	public static final String 		TEXT_LINE_PARAM = "TEXT";
	
	/**
	 * Creates an instance. 
	 * 
	 * @param fieldType		The String defining the field type
	 */
	public TextParam(String fieldType) 
	{
		super(fieldType);
	}

	
	/**
	 * Returns the value of the parameter. 
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
}
