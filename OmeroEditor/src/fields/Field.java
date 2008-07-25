
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tree.DataFieldConstants;

/**
 * This is the data object that occupies a node of the tree hierarchy. 
 * It has name, description, url, stored in an AttributeMap, and may
 * have 0, 1 or more Parameter objects {link# IParam} to store 
 * experimental variables, or parameters. 
 * 
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class Field 
	implements IField,
	Cloneable {
	
	/**
	 * A property of this field. The attribute for an (optional) Name.
	 */
	public static final String FIELD_NAME = "fieldName";
	
	/**
	 * A property of this field. The attribute for an optional Description.
	 */
	public static final String FIELD_DESCRIPTION = "fieldDescription";
	
	/**
	 * A property of this field. The attribute for an optional Url.
	 */
	public static final String FIELD_URL = "fieldUrl";
	
	/**
	 * The list of Parameters, representing experimental variables for this 
	 * field.
	 */
	private List<IParam> fieldParams;

	/**
	 * A map of attributes for this Field. eg Name, Description etc. 
	 */
	HashMap<String, String> allAttributesMap;
	
	/**
	 * Default constructor. Sets the name of the field to "untitled"
	 */
	public Field() {
		this("untitled");
	}
	
	/**
	 * Returns a copy of this object.
	 * This is implemented manually, rather than calling super.clone()
	 * Therefore, any subclasses should also manually override this method to 
	 * copy any additional attributes they have.  
	 */
	public Object clone() {
		
		//Field newField = (Field)super.clone();
		
		Field newField = new Field();
		
		HashMap<String,String> newAtt = new HashMap<String,String>(getAllAttributes());
		
		newField.setAllAttributes(newAtt);
		
		for (int i=0; i<getParamCount(); i++) {
			IParam param = getParamAt(i);
			IParam newP = FieldParamsFactory.cloneParam(param);
			newField.addParam(newP);
		}
		
		return newField;
	}
	
	/**
	 * Duplicates a field by making a copy of the given field. 
	 * 
	 * @param cloneField	The field to be copied. 
	 
	public Field(Field cloneField) {
		this();
		
		/*
		 * Clone all attributes
		 
		allAttributesMap = new HashMap<String,String>
				(cloneField.getAllAttributes());
		
		/*
		 * Clone the parameter objects...
		 
		for (int i=0; i<cloneField.getParamCount(); i++) {
			IParam param = cloneField.getParamAt(i);
			IParam newP = FieldParamsFactory.cloneParam(param);
			addParam(newP);
		}
	}
*/
	
	/**
	 * A constructor used to set the name of the field.
	 * This constructor is called by the others, in order to initialise
	 * the attributesMap and parameters list. 
	 * 
	 * @param name		A name given to this field. 
	 */
	public Field(String name) {
		
		allAttributesMap = new HashMap<String, String>();
		fieldParams = new ArrayList<IParam>();
		
		setAttribute(FIELD_NAME, name);
		
	}
	
	/**
	 * gets an attribute in the attributesMap
	 */
	public String getAttribute(String name) {
		//System.out.println("Field getAttribute()");
		
		return allAttributesMap.get(name);
	}
	
	/**
	 * gets all attributes in the attributesMap
	 */
	public Map getAllAttributes() {
		return allAttributesMap;
	}
	
	/**
	 * sets the attribute map.
	 * 
	 * @param newAtt	The new attribute map
	 */
	public void setAllAttributes(HashMap<String,String> newAtt) {
		allAttributesMap = newAtt;
	}
	
	/**
	 * sets an attribute in the attributesMap
	 */
	public void setAttribute(String name, String value) {
		
		allAttributesMap.put(name, value);
	}
	
	/**
	 * For display etc. Simply returns the name...
	 */
	public String toString() {
		return getAttribute(DataFieldConstants.ELEMENT_NAME);
	}

	/**
	 * Convenience method for querying the attributes map for 
	 * boolean attributes.
	 */
	public boolean isAttributeTrue(String attributeName) {
		String value = getAttribute(attributeName);
		return DataFieldConstants.TRUE.equals(value);
	}
	

	
	/**
	 * This method tests to see whether the field has been filled out. 
	 * ie, Has the user entered a "valid" value into the Form. 
	 * This will return false if any of the parameters for this field are
	 * not filled. 
	 * 
	 * @return	True if the all the parameters have been filled out by user.  
	 */
	public boolean isFieldFilled() {
		
		for (IParam param : fieldParams) {
			if (! param.isParamFilled()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the number of IParam parameters for this field.
	 */
	public int getParamCount() {
		return fieldParams.size();
	}

	/**
	 * Returns the parameter of this field at the given index.
	 */
	public IParam getParamAt(int index) {
		return fieldParams.get(index);
	}

	/**
	 * Adds a parameter to the list for this field
	 */
	public void addParam(IParam param) {
		if (param != null)
			fieldParams.add(param);
	}

	/**
	 * Removes the specified parameter from the list. 
	 */
	public boolean removeParam(IParam param) {
		return fieldParams.remove(param);
	}
}
