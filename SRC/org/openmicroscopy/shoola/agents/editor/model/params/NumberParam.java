 /*
 * org.openmicroscopy.shoola.agents.editor.model.params.NumberParam 
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
 * A class for modeling a number parameter
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class NumberParam 
extends AbstractParam 
{

	/**
	 * An attribute to describe the units of a NUMBER_PARAM. eg "%" or "grams"
	 */
	public static final String PARAM_UNITS = "paramUnits";

	/**
	 * This defines a parameter that is a number. 
	 * Could be integer or float.
	 * Additional attributes are "units"
	 * Equivalent to the "NumberField" of Beta 3.0
	 */
	public static final String 		NUMBER_PARAM = "NUMERIC";

	/**
	 * Creates an instance. 
	 * 
	 * @param fieldType		The String defining the field type
	 */
	public NumberParam() 
	{
		super(NumberParam.NUMBER_PARAM);
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
			String units = getAttribute(PARAM_UNITS);
			if (units != null) {
				text = text + " " + units;
			}
		}
		
		return text;
	}
}
