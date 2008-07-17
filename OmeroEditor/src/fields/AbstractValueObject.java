 /*
 * fields.AbstractValueObject 
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
package fields;

import java.util.HashMap;

import tree.DataFieldConstants;

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
public abstract class AbstractValueObject 
	implements
	IFieldValue {
	
	private String fieldType;
	
	private HashMap<String, String> valueAttributesMap;

	
	public AbstractValueObject(String fieldType) {
		this.fieldType = fieldType;
		valueAttributesMap = new HashMap<String, String>();
	}
	
	public abstract String[] getValueAttributes();

	public abstract boolean isFieldFilled();

	/**
	 * A convenience method to test whether the attribute is included in
	 * the list of value attributes. 
	 * 
	 * @param attributeName		The name of the attribute to test. 
	 * @return		True if the attribute is a value attribute. 
	 */
	public boolean isValueAttribute(String attributeName) {
		
		if (attributeName == null) return false;
		
		String[] attributes = getValueAttributes();
		for (int i=0; i<attributes.length; i++) {
			if (attributeName.equals(attributes[i]))
				return true;
		}
		return false;
	}
	
	public String getFieldType() {
		return fieldType;
	}
	
	public String getAttribute(String name) {
		return valueAttributesMap.get(name);
	}

	public boolean isAttributeTrue(String attributeName) {
		return (DataFieldConstants.TRUE.equals(getAttribute(attributeName)));
	}

	public void setAttribute(String name, String value) {
		System.out.println("ValueObject setAttribute() " + name + " = " + value);
		valueAttributesMap.put(name, value);
	}

}
