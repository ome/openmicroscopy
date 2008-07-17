
/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package fields;

import java.util.HashMap;

import tree.DataFieldConstants;


public class Field 
	implements IField {

	HashMap<String, String> allAttributesMap;
	
	/**
	 * The "Value" of the field. 
	 * This could be a simple string, or a mixture of dates, times etc. 
	 * It represents the experimental parameters that are stored by this
	 * field. 
	 */
	private IFieldValue fieldValue;
	
	public Field() {
		this("untitled", null, DataFieldConstants.FIXED_PROTOCOL_STEP);
	}
	
	public Field(String name, String value, String fieldType) {
		
		allAttributesMap = new HashMap<String, String>();
		
		fieldValue = FieldValueFactory.getFieldValue(fieldType);
		
		setAttribute(DataFieldConstants.ELEMENT_NAME, name);
		setAttribute(DataFieldConstants.VALUE, value);
	}
	
	public String getAttribute(String name) {
		//System.out.println("Field getAttribute()");
		if (fieldValue.isValueAttribute(name)) {
			return fieldValue.getAttribute(name);
		}
		
		return allAttributesMap.get(name);
	}
	
	public void setAttribute(String name, String value) {
		System.out.println("Field setAttribute() " + name + " = " + value);
		if (fieldValue.isValueAttribute(name)) {
			fieldValue.setAttribute(name, value);
		}
		allAttributesMap.put(name, value);
	}
	
	public String toString() {
		return getAttribute(DataFieldConstants.ELEMENT_NAME) + ": " + 
		getAttribute(DataFieldConstants.VALUE);
	}

	public boolean isAttributeTrue(String attributeName) {
		String value = getAttribute(attributeName);
		return DataFieldConstants.TRUE.equals(value);
	}
	
	public IFieldValue getValueObject() {
		return fieldValue;
	}
	
	/**
	 * Gets the names of the attributes where this field stores its "value"s.
	 * This is used eg. (if a single value is returned)
	 * as the destination to copy the default value when defaults are loaded.
	 * Also used by EditClearFields to set all values back to null. 
	 * This method delegates to the Value object for this field.
	 *  
	 * @return	the names of the attributes that holds the "value" of this field
	 */
	public String[] getValueAttributes() {
		return fieldValue.getValueAttributes();
	}
	
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
	public boolean isFieldFilled() {
		return fieldValue.isFieldFilled();
	}
}
