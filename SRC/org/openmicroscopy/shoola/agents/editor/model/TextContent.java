 /*
 * org.openmicroscopy.shoola.agents.editor.model.TextContent 
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

//Third-party libraries

//Application-internal dependencies

/** 
 * A simple text holder. 
 * Used for including a field description in context with parameters, since 
 * parameters also implement the {@link IFieldContent} interface. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TextContent 
	implements IFieldContent {

	/**
	 * A map to contain the attributes. 
	 * Only a single {@link #TEXT_CONTENT} attribute currently, but may 
	 * be more in future.
	 */
	private HashMap<String, String> 		valueAttributesMap;

	/**
	 * Name of the text attribute. 
	 */
	public static final String 				TEXT_CONTENT = "textContent";
	
	/**
	 * Creates an instance. 
	 * 
	 * @param textContent		The initial value of the {@link #TEXT_CONTENT} 
	 * 						attribute
	 */
	public TextContent(String textContent)
	{
		valueAttributesMap = new HashMap<String, String>();
		
		setTextContent(textContent);
	}
	
	/**
	 * Creates a clone.
	 * 
	 * @param cloneThis		The TextContent to copy.
	 */
	public TextContent(TextContent cloneThis) 
	{
		this(cloneThis.getAttribute(TEXT_CONTENT));
	}
	
	/**
	 * Returns the {@link #TEXT_CONTENT} attribute.
	 * 
	 * @see Object#toString()
	 */
	public String toString() 
	{
		String text = getAttribute(TEXT_CONTENT);
		if (text == null) return "";
		return text;
	}
	
	/**
	 * Convenience method for setting the {@link #TEXT_CONTENT} attribute.
	 * 
	 * @param text		new text value
	 */
	public void setTextContent(String text) 
	{
		setAttribute(TEXT_CONTENT, text);
	}
	
	/**
	 * @see		IAttributes.getAttribute(String name)
	 */
	public String getAttribute(String name) 
	{
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
		valueAttributesMap.put(name, value);
	}
}
