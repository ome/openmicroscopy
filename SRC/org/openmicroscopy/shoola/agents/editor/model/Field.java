/*
 * org.openmicroscopy.shoola.agents.editor.model.Field
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package org.openmicroscopy.shoola.agents.editor.model;

// Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.params.FieldParamsFactory;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;

/**
 * This is the data object that occupies a node of the tree hierarchy. 
 * It has name, description, url etc, stored in an AttributeMap, and may
 * have 0, 1 or more Parameter objects {@link IParam} to store 
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
	public static final String 		FIELD_NAME = "fieldName";
	
	/**
	 * A property of this field. The attribute for an optional Description.
	 */
	public static final String 		FIELD_DESCRIPTION = "fieldDescription";
	
	/**
	 * A property of this field. The attribute for an optional Url.
	 */
	public static final String 		FIELD_URL = "fieldUrl";
	
	/**
	 * A property of this field. 
	 * Stores a color as a string in the form "r:g:b";
	 */
	public static final String 		BACKGROUND_COLOUR = "backgroundColour";
	
	/**
	 * A display property of this field.
	 * getDisplayAttribute(TOOL_TIP_TEXT) should return a string composed
	 * of field description and parameter values etc. 
	 */
	public static final String 		TOOL_TIP_TEXT = "toolTipText";
	
	/**
	 * The list of Parameters, representing experimental variables for this 
	 * field.
	 */
	private List<IParam> 			fieldParams;

	/**
	 * A map of the template attributes for this Field. 
	 * eg Name, Description etc. 
	 */
	private HashMap<String, String> templateAttributesMap;
	
	/**
	 * A map of the display attributes for this Field. 
	 * eg Description visible, 
	 * Not saved.  
	 */
	private HashMap<String, String> displayAttributesMap;
	
	/**
	 * Default constructor.
	 */
	public Field() 
	{
		templateAttributesMap = new HashMap<String, String>();
		displayAttributesMap = new HashMap<String, String>();
		fieldParams = new ArrayList<IParam>();
	}
	
	/**
	 * Returns a copy of this object.
	 * This is implemented manually, rather than calling super.clone()
	 * Therefore, any subclasses should also manually override this method to 
	 * copy any additional attributes they have.  
	 */
	public Object clone() 
	{
		
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
	 * A constructor used to set the name of the field.
	 * This constructor is called by the others, in order to initialise
	 * the attributesMap and parameters list. 
	 * 
	 * @param name		A name given to this field. 
	 */
	public Field(String name) 
	{	
		this();
		
		setAttribute(FIELD_NAME, name);
	}
	
	/**
	 * gets an attribute in the templateAttributesMap
	 * 
	 * Implemented as specified by the {@link IAttributes} interface
	 * 
	 * @see IAttributes#getAttribute(String)
	 */
	public String getAttribute(String name) 
	{
		return templateAttributesMap.get(name);
	}
	
	/**
	 * gets all attributes in the templateAttributesMap
	 */
	public Map getAllAttributes() {
		return templateAttributesMap;
	}
	
	/**
	 * sets the attribute map.
	 * 
	 * @param newAtt	The new attribute map
	 */
	public void setAllAttributes(HashMap<String,String> newAtt) {
		templateAttributesMap = newAtt;
	}
	
	/**
	 * sets an attribute in the attributesMap
	 * Implemented as specified by the {@link IAttributes} interface
	 * 
	 * @see IAttributes#setAttribute(String, String)
	 */
	public void setAttribute(String name, String value) {
		
		templateAttributesMap.put(name, value);
	}
	
	/**
	 * For display etc. Simply returns the name...
	 */
	public String toString() {
		return getAttribute(FIELD_NAME);
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
	 * Adds a parameter to the list for this field
	 */
	public void addParam(int index, IParam param) {
		if (param != null)
			fieldParams.add(index, param);
	}

	/**
	 * Removes the specified parameter from the list. 
	 */
	public int removeParam(IParam param) {
		int index = fieldParams.indexOf(param);
		
		fieldParams.remove(param);
		return index;
	}

	/**
	 * Gets a display attribute (eg description visible)
	 */
	public String getDisplayAttribute(String name) 
	{
		if (TOOL_TIP_TEXT.equals(name)) return getToolTipText();
		
		return displayAttributesMap.get(name);
	}

	/**
	 * Sets a display attribute. 
	 * This will not be saved in the file, 
	 */
	public void setDisplayAttribute(String name, String value) {
		displayAttributesMap.put(name, value);
	}
	
	/**
	 * Returns a String containing the field description, plus the 
	 * tool-tip-text from it's parameters. 
	 * 
	 * @return		see above.
	 */
	private String getToolTipText() 
	{
		String desc = getAttribute(FIELD_DESCRIPTION);
		String toolTipText = "";
		if (desc != null)
		{
			toolTipText = toolTipText + desc;
		}
		String paramText;
		for (int i=0; i<getParamCount(); i++) {
			paramText = getParamAt(i).toString();
			if (paramText.length() > 0)
			{
				if (toolTipText.length() > 0) 
					toolTipText = toolTipText + ", ";
				toolTipText = toolTipText + paramText;
			}
		}
		
		return toolTipText;
	}
}
