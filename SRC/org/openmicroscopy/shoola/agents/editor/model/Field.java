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
	private List<IFieldContent> 			fieldParams;
	
	/**
	 * A reference to a lock that may be applied to this field to prevent
	 * editing of some attributes or parameters. 
	 */
	private Lock 					fieldLock;

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
		fieldParams = new ArrayList<IFieldContent>();
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
		
		for (int i=0; i<getContentCount(); i++) {
			
			IFieldContent content = getContentAt(i);
			if (content instanceof IParam) {
				IParam param = (IParam)content;
					IParam newP = FieldParamsFactory.cloneParam(param);
				newField.addContent(newP);
			} else if (content instanceof TextContent) {
				TextContent text = (TextContent)content;
				TextContent newText = new TextContent(text);	// clone content
				newField.addContent(newText);
			}
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
	 * Implemented as specified by the {@link IField} interface.
	 * Convenience method for querying the attributes map for 
	 * boolean attributes.
	 */
	public boolean isAttributeTrue(String attributeName) {
		String value = getAttribute(attributeName);
		return DataFieldConstants.TRUE.equals(value);
	}
	

	
	/**
	 * Implemented as specified by the {@link IField} interface.
	 * This method tests to see whether the field has been filled out. 
	 * ie, Has the user entered a "valid" value into the Form. 
	 * This will return false if any of the parameters for this field are
	 * not filled. 
	 * 
	 * @return	True if the all the parameters have been filled out by user.  
	 */
	public boolean isFieldFilled() {
		
		for (IFieldContent content : fieldParams) {
			if (content instanceof IParam) {
				IParam param = (IParam)content;
				if (! param.isParamFilled()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Implemented as specified by the {@link IField} interface.
	 * Returns the number of IParam parameters for this field.
	 */
	public int getContentCount() {
		return fieldParams.size();
	}

	/**
	 * Implemented as specified by the {@link IField} interface.
	 * Returns the content of this field at the given index.
	 */
	public IFieldContent getContentAt(int index) {
		return fieldParams.get(index);
	}

	/**
	 * Implemented as specified by the {@link IField} interface.
	 * Adds a parameter to the list for this field
	 */
	public void addContent(IFieldContent param) {
		if (param != null)
			fieldParams.add(param);
	}
	
	/**
	 * Implemented as specified by the {@link IField} interface.
	 * Adds a parameter to the list for this field
	 */
	public void addContent(int index, IFieldContent param) 
	{
		if (param != null)
			fieldParams.add(index, param);
	}

	/**
	 * Implemented as specified by the {@link IField} interface.
	 * Removes the specified content from the list. 
	 */
	public int removeContent(IFieldContent param) 
	{
		int index = fieldParams.indexOf(param);
		
		fieldParams.remove(param);
		return index;
	}
	
	/**
	 * Implemented as specified by the {@link IField} interface.
	 * Removes the specified content from the list. 
	 */
	public void removeContent(int index) 
	{
		fieldParams.remove(index);
	}
	
	/**
	 * Implemented as specified by the {@link IField} interface.
	 * 
	 * @see IField#getParams()
	 */
	public List<IParam> getParams() 
	{
		List<IParam> params = new ArrayList<IParam>();
		
		for (IFieldContent content : fieldParams) {
			if (content instanceof IParam) {
				params.add((IParam)content);
			}
		}
		
		return params;
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
	public void setDisplayAttribute(String name, String value) 
	{
		displayAttributesMap.put(name, value);
	}
	
	/**
	 * Returns a String containing the field description, plus the 
	 * tool-tip-text from it's parameters. 
	 * 
	 * @return		see above.
	 */
	public String getToolTipText() 
	{
		String toolTipText = "";
		
		String paramText;
		for (int i=0; i<getContentCount(); i++) {
			paramText = getContentAt(i).toString();
			if ((paramText != null) && (paramText.length() > 0))
			{
				if (toolTipText.length() > 0) 
					toolTipText = toolTipText + ", ";
				toolTipText = toolTipText + paramText;
			}
		}
		
		return toolTipText;
	}
	
	/**
	 * Sets a lock on this field to prevent editing. 
	 * @see #fieldLock
	 * 
	 * @param lock
	 */
	public void setLock(Lock lock)
	{
		fieldLock = lock;
	}
	
	/**
	 * Returns this field's lock (or null if no lock).
	 * 
	 * @return		see above
	 */
	public Lock getLock() { return fieldLock; }

	/**
	 * Implemented as specified by the {@link IField} interface.
	 * 
	 * @see IField#isFieldLocked()
	 */
	public boolean isFieldLocked() 
	{
		if (fieldLock == null)	return false;
	
		return (! (fieldLock.getLockLevel() == Lock.NOT_LOCKED));
	}
	
	/**
	 * Returns an html representation of the field. 
	 * 
	 * @return
	 */
	public String toHtmlString() 
	{
		
		String fieldName = getAttribute(Field.FIELD_NAME);
		
		String text = fieldName + ": ";
		String contentText;
		IFieldContent content;
		
		for (int index=0; index<fieldParams.size(); index++) {
			content = fieldParams.get(index);
			if (content instanceof IParam) {
				contentText = "<a href='" + index + "'>" + 
				content.toString() + "</a>";
			} else {
				contentText = content.toString();
			}
			
			text = text + " " + contentText;
		}
		return text;
		
	}
}
