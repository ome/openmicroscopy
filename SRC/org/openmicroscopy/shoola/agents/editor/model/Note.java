 /*
 * org.openmicroscopy.shoola.agents.editor.model.Note 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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

import java.util.HashMap;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * A note is a simple annotation (name and content), added to a "Step" / Field 
 * when the protocol is performed, to create an 'Experiment' instance. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class Note 
implements IAttributes {
	
	/** The name of the note */
	public static final String 		NAME = "name";
	
	/** The content of the note */
	public static final String 		CONTENT = "content";
	
	/**
	 * A map of the attributes that define this note
	 * Can be used to store any name, value pair. 
	 */
	private HashMap<String, String> valueAttributesMap;

	/**
	 * Creates an instance. 
	 */
	public Note() 
	{
		valueAttributesMap = new HashMap<String, String>();
	}
	
	/**
	 * Returns the name of the note. 
	 * @return
	 */
	public String getName()
	{
		return getAttribute(NAME);
	}
	
	/**
	 * Returns the content of the note. 
	 * @return
	 */
	public String getContent()
	{
		return getAttribute(CONTENT);
	}
	
	/**
	 * Creates an instance.
	 * 
	 * @param name			name of note
	 * @param content		content of note
	 */
	public Note(String name, String content) {
		this();
		setAttribute(NAME, name);
		setAttribute(CONTENT, content);
	}
	
	/**
	 * Gets an attribute of this data-reference.
	 * 
	 * @param name		Name of the attribute. 
	 * @return			The value of this attribute, or null if not set. 
	 */
	public String getAttribute(String name) 
	{
		return valueAttributesMap.get(name);
	}
	
	/**
	 * Implemented as specified by the {@link IAttributes} interface. 
	 * 
	 * @see IAttributes#setAttribute(String, String)
	 */
	public void setAttribute(String name, String value) 
	{
		valueAttributesMap.put(name, value);
	}

	/**
	 * Implemented as specified by the {@link IAttributes} interface. 
	 * 
	 * @see IAttributes#isAttributeTrue(String)
	 */
	public boolean isAttributeTrue(String attributeName) {
		return "true".equals(valueAttributesMap.get(attributeName));
	}
}

	
