/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.WikiDataObject 
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
package org.openmicroscopy.shoola.util.ui.omeeditpane;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Object hosting details about an OMERO object to launch.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class WikiDataObject 
{

	/** Identifies a <code>Project</code>. */
	public static final int PROJECT = 0;
	
	/** Identifies a <code>Dataset</code>. */
	public static final int DATASET = 1;
	
	/** Identifies a <code>Image</code>. */
	public static final int IMAGE = 2;
	
	/** Identifies a <code>Thumbnail</code>. */
	public static final int THUMBNAIL = 3;
	
	/** Identifies a <code>URL</code>. */
	public static final int URL = 5;
	
	/** Identifies an unrecognized data type */
	public static final int OTHER = 6;
	
	/** One of the constants defined by this class. */
	private int 	index;

	/** The id of the object. */
	private long 	id;
	
	/** The name of the object. */
	private String 	name;
	
	/** The first regex group captured by the regex */
	private String group;
	
	/** The regex that was recognized */
	private String regex;
	
	/** The text that was recognized by the regex. i.e. regex group 0 */
	private String matchedText;

	/**
	 * Controls if the index is supported.
	 * 
	 * @param value The value to check.
	 */
	private void checkIndex(int value)
	{
		switch (value) {
			case PROJECT:
			case DATASET:
			case IMAGE:
			case THUMBNAIL:
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param regex 	The regular that was recognized.
	 * @param group		The text captured by the first group of the regular 
	 * 					expression.
	 * @param matchedText The text found.
	 */
	WikiDataObject(String regex, String group, String matchedText)
	{
		this.regex = regex;
		this.group = group;
		this.matchedText = matchedText;
		this.index = OTHER;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index One of the constants defined by this class.
	 * @param id	The id of the object.
	 */
	WikiDataObject(int index, long id)
	{
		checkIndex(index);
		this.index = index;
		this.id = id;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index One of the constants defined by this class.
	 * @param name	The name of the object.
	 */
	WikiDataObject(int index, String name)
	{
		checkIndex(index);
		this.index = index;
		this.name = name;
		id = -1;
	}
	
	/**
	 * Returns the index.
	 * 
	 * @return See above.
	 */
	public int getIndex()
	{ 
		// if we have a recognized index, return it.
		if (index != OTHER)  return index;
		// otherwise, try to match the regex to a type
		if (regex != null) {
			if (regex.contains("Image")) {
				index = IMAGE;
			} else if (regex.contains("Project")) {
				index = PROJECT;
			} else if (regex.contains("Dataset")) {
				index = DATASET;
			} else if (regex.contains("http") || regex.contains("www"))
				index = URL;
		}
		
		return index;
	}
	
	/**
	 * Returns the id of the object.
	 * 
	 * @return See above.
	 */
	public long getId()
	{ 
		if (id > 0) return id; 
		else { 	// often the 'group' attribute will hold an ID
			try {
				id = new Long(group);
			}
			catch (Exception e) {
				id = -1;
			}
		}
		return id;
	}
	
	/**
	 * Returns the name of the object.
	 * 
	 * @return See above.
	 */
	public String getName() { return name; }
	
	/**
	 * Returns the group of the object.
	 * 
	 * @return See above.
	 */
	public String getGroup() { return group; }
	
	/**
	 * Returns the regex of the object.
	 * 
	 * @return See above.
	 */
	public String getRegex() { return regex; }
	
	/**
	 * Returns the matched Text of the object.
	 * 
	 * @return See above.
	 */
	public String getMatchedText() { return matchedText; }
	
}
