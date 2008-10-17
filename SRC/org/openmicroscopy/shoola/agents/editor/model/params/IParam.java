 /*
 * org.openmicroscopy.shoola.agents.editor.model.params.IParam
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

import java.util.HashMap;

import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.IFieldContent;

/** 
 * This interface specifies the minimum methods needed to 
 * retrieve data from a Parameter (the data object that models 
 * experimental variables within a Field). 
 * A Parameter has one or move experimental values, stored in attributes
 * defined by {@link #getValueAttributes()} and may also have default values
 * and other attributes (e.g. drop-down options).   
 * 
 * Data is saved and retrieved using methods of the {@link IAttributes}
 * interface, which the IParam interface extends. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public interface IParam 
	extends IFieldContent 
{
	
	/**
	 * This method tests to see whether the field has been filled out. 
	 * ie, Has the user entered a "valid" value into the Form. 
	 * For fields that have a single 'value', this method will return true if 
	 * that value is filled (not null). 
	 * For fields with several attributes, it depends on what is considered 'filled'.
	 * This method can be used to check that 'Obligatory Fields' have been completed 
	 * when a file is saved. 
	 * Subclasses should override this method.
	 * 
	 * @return	True if the field has been filled out by user. Required values are not null. 
	 */
	public boolean isParamFilled();
	
	/**
	 * This method returns a list of the names of attributes. 
	 * These attributes represent the experimental "value" of this
	 * parameter (rather than other attributes such as name or default
	 * values that represent the "template" part of the parameter. 
	 * This method is used for eg. clearing the value of a parameter by 
	 * setting all value attributes to null. 
	 */
	public String[] getValueAttributes();
	
	/**
	 * Unless specified by subclasses, parameter has no default values.
	 * Default implementation {@see AbstractParam#getDefaultAttributes()}
	 * returns an empty array. 
	 * 
	 * If this is implemented by subclasses, they should also implement 
	 * {@link #loadDefaultValues()}
	 */
	public String[] getDefaultAttributes();
	
	/**
	 * Loads the default values (if any) into the parameter's value.
	 * If no defaults, or no default values exist, an empty Map is returned.
	 * 
	 * @return		A HashMap of the attributes (with Old Values) affected.
	 */
	public HashMap<String, String> loadDefaultValues();
}
