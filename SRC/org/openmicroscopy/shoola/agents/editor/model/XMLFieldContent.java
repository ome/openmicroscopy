 /*
 * org.openmicroscopy.shoola.agents.editor.model.XMLFieldContent 
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
package org.openmicroscopy.shoola.agents.editor.model;

//Java imports

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/** 
 * This class models the data in a 'foreign' or 'custom' XML element, so that
 * OMERO.editor can model and display other XML documents.
 * The element Name is not stored here, but in the {@link Field} that 
 * contains this 'content'.
 * This class keeps the element attributes in an attribute Map, and the
 * text content of the element in a separate variable. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class XMLFieldContent 
	implements IFieldContent {

	/**
	 * A map to contain the XML attributes.
	 */
	private HashMap<String, String> 		valueAttributesMap;

	/**
	 * A String that represents the text content of the XML element
	 */
	private String 							textContent;
	
	/**
	 * Name of the attribute that stores the text content of the XML node. 
	 * This allows the text content to be edited using a UI component that 
	 * uses {@link #setAttribute(TEXT_CONTENT, String)} method. 
	 */
	public static final String 				TEXT_CONTENT = "textContent";
	
	/**
	 * Creates an instance of this class, using a Map of the attributes in
	 * the XML element. 
	 * 
	 * @param attributes	The attribute map that is copied to the new class.		
	 */
	public XMLFieldContent(Map<String, String> attributes)
	{
		valueAttributesMap = new HashMap<String, String>(attributes);
	}
	
	/**
	 * Creates an instance by cloning an existing {@link XMLFieldContent}.
	 * 
	 * @param cloneThis		The TextContent to copy.
	 */
	public XMLFieldContent(XMLFieldContent cloneThis) 
	{
		this(cloneThis.valueAttributesMap);
		setTextContent(cloneThis.getTextContent());
	}
	
	/**
	 * Returns a String array of the attribute names.
	 * Needed when displaying or exporting all the attributes. 
	 * 
	 * @return		see above. 
	 */
	public String[] getAttributeNames() 
	{
		int nameCount = valueAttributesMap.size();
		String[] names = new String[nameCount];
		
		Iterator<String> i = valueAttributesMap.keySet().iterator();
		int c = 0;
		while ((i.hasNext()) && (c < nameCount)) {
			names[c] = i.next();
			c++;
		}
		return names;
	}
	
	/**
	 * Returns all the attributes in name:value pairs as a single string. 
	 * 
	 * @see Object#toString()
	 */
	public String toString() 
	{
		String text = "";
		
		Iterator<String> i = valueAttributesMap.keySet().iterator();
		String name;
		String value;
		while (i.hasNext()) {
			name = i.next();
			value = valueAttributesMap.get(name);
			if (value != null) {
				if (text.length() > 0) text = text + ", ";
				text = text + name + ":" + value;
			}
		}
		return text;
	}
	
	/**
	 * Sets the text content of the XML element that this field represents 
	 * 
	 * @param text		new text value
	 */
	public void setTextContent(String text) 
	{
		textContent = text;
	}
	
	/**
	 * Gets the text content of the XML element that this field represents 
	 * 
	 * @return see above
	 */
	public String getTextContent() 
	{
		return textContent;
	}
	
	/**
	 * @see		IAttributes.getAttribute(String name)
	 */
	public String getAttribute(String name) 
	{
		if (TEXT_CONTENT.equals(name)) {
			return getTextContent();
		}
		return valueAttributesMap.get(name);
	}

	/**
	 * @see		IAttributes.isAttributeTrue(String attributeName)
	 */
	public boolean isAttributeTrue(String attributeName) 
	{
		return (Boolean.valueOf(getAttribute(attributeName)));
	}

	/**
	 * @see		IAttributes.setAttribute(String name, String value)
	 */
	public void setAttribute(String name, String value) 
	{
		if (TEXT_CONTENT.equals(name)) {
			setTextContent(value);
			return;
		}
		valueAttributesMap.put(name, value);
	}

}
