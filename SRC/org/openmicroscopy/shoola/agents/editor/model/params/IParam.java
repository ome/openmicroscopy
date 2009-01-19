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
 * A Parameter may have one or more experimental values, stored in a list and
 * accessed by the {@link #getValueAt(int)} method.
 * The parameter may also have default values
 * and other attributes (e.g. drop-down options), which are stored in a 
 * Map and accessed using {@link IAttributes#getAttribute(String)};
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
	 * Returns the number of values that have been set for this parameter. 
	 * A parameter may have a list of values when several instances of a 
	 * field are saved in a table. 
	 * Each parameter in the field will be a column, with the list of values
	 * populating the rows of that column. 
	 * 
	 * @return		The number of values saved for this parameter. 
	 */
	public int getValueCount();
	
	/**
	 * Gets the specified value in the list of values for this parameter.
	 * Returns null if index is greater than length of list.  
	 * 
	 * @param index		The index of the parameter. 
	 * @return			The value
	 */
	public Object getValueAt(int index);
	
	/**
	 * Gets a string representation of the value of the parameter.
	 * This can be used e.g. for exporting the value of a field as text
	 * to UPE file format, or for display.
	 * May return null if the parameter has no value. 
	 * 
	 * @return		see above. 
	 */
	public String getParamValue();
	
	/**
	 * Sets the value at the specified index in the list of values for this
	 * parameter. If the index is greater than the length of the list, the
	 * list should automatically be extended to this length. 
	 * 
	 * @param index		The index to add this value. 
	 * @param value		The new value
	 */
	public void setValueAt(int index, Object value);
	
	/**
	 * Inserts a value to the list of values for this parameter
	 * 
	 * @param index		The index to insert this value. 
	 * @param value			The value to insert into the list
	 */
	public void insertValue(int index, Object value);
	
	/**
	 * Removes the value at the specified index of the list of values for this
	 * parameter. 
	 * 
	 * @param index		The index to remove a value. 
	 */
	public void removeValueAt(int index);
	
	/**
	 * Method to clone a parameter object. 
	 * Subclasses should override this to copy any additional attributes 
	 * that they have.
	 * 
	 * @return 		A clone of this parameter. 
	 */
	public IParam cloneParam();
}
