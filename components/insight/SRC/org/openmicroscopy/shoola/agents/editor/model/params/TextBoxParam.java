 /*
 * org.openmicroscopy.shoola.agents.editor.model.params.TextBoxParam 
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
package org.openmicroscopy.shoola.agents.editor.model.params;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a text parameter that is used to store a large/multi-line bit 
 * of text. E.g. Comments, Conclusions or Abstract for a Protocol. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class TextBoxParam 
	extends AbstractParam{

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
	public TextBoxParam(String fieldType) 
	{
		super(fieldType);
	}
	
	
	/**
	 * Returns the value of the parameter. 
	 * 
	 * @see Object#toString()
	 */
	public String toString() {
		
		String name = getAttribute(PARAM_NAME);
		
		return (name == null ? "Text-Box" : name);
	}
	
	/**
	 * Implemented as specified by the {@link IParam} interface. 
	 */
	public String getParamValue() 
	{
		if (getValueAt(0) == null) 		return null;
		else return getValueAt(0) + "";
	}
}
