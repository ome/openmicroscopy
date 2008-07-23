
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

import tree.DataFieldConstants;

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
public class Field 
	implements IField {
	
	public static final String FIELD_NAME = "fieldName";
	
	public static final String FIELD_DESCRIPTION = "fieldDescription";
	
	public static final String FIELD_URL = "fieldUrl";
	
	
	private List<IParam> fieldParams;

	HashMap<String, String> allAttributesMap;
	
	/**
	 * The "Value" of the field. 
	 * This could be a simple string, or a mixture of dates, times etc. 
	 * It represents the experimental parameters that are stored by this
	 * field. 
	 */
	//private IFieldValue fieldValue;
	
	public Field() {
		this("untitled");
	}
	
	public Field(String name) {
		
		allAttributesMap = new HashMap<String, String>();
		fieldParams = new ArrayList<IParam>();
		
		setAttribute(FIELD_NAME, name);
		
	}
	
	public String getAttribute(String name) {
		//System.out.println("Field getAttribute()");
		
		return allAttributesMap.get(name);
	}
	
	public void setAttribute(String name, String value) {
		
		allAttributesMap.put(name, value);
	}
	
	public String toString() {
		return getAttribute(DataFieldConstants.ELEMENT_NAME);
	}

	
	public boolean isAttributeTrue(String attributeName) {
		String value = getAttribute(attributeName);
		return DataFieldConstants.TRUE.equals(value);
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
		
		for (IParam param : fieldParams) {
			if (! param.isParamFilled()) {
				return false;
			}
		}
		return true;
	}

	public int getParamCount() {
		return fieldParams.size();
	}

	public IParam getParamAt(int index) {
		return fieldParams.get(index);
	}

	public void addParam(IParam param) {
		fieldParams.add(param);
	}

	public boolean removeParam(IParam param) {
		return fieldParams.remove(param);
	}
}
