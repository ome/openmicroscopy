 /*
 * org.openmicroscopy.shoola.agents.editor.model.DataReference 
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

//Java imports

import java.io.File;
import java.util.HashMap;

import javax.swing.filechooser.FileFilter;

import org.openmicroscopy.shoola.util.filter.file.CustomizedFileFilter;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;

//Third-party libraries

//Application-internal dependencies

/** 
 * A data reference is attached to a 'Field'/Step in order to link that 
 * step to some data that it produces. 
 * This can be a reference to some data on the local file-system, a url or
 * some data on a server (eg OMERO). 
 * Property names correspond to elements within .cpe.xml files that store
 * properties of the data reference. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class DataReference 
	implements IAttributes {
	
	/** The name of the data reference */
	public static final String 		NAME = "name";
	
	/** The location, path, lsid or url of the data reference */
	public static final String 		REFERENCE = "reference";
	
	/** The description of the data reference */
	public static final String 		DESCRIPTION = "description";
	
	/** The mime-type of the data reference */
	public static final String 		MIME_TYPE = "mime-type";
	
	/** The size of the data reference */
	public static final String 		SIZE = "size";
	
	/** The creation-time of the data reference, in UTC millisecs */
	public static final String 		CREATION_TIME = "creation-time";
	
	/** The modification-time of the data reference, in UTC millisecs */
	public static final String 		MODIFICATION_TIME = "modification-time";
		
	/**
	 * A map of the attributes that define this data-reference
	 * Can be used to store any name, value pair. 
	 */
	private HashMap<String, String> valueAttributesMap;

	/**
	 * Creates an instance. 
	 */
	public DataReference() 
	{
		valueAttributesMap = new HashMap<String, String>();
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
	
	/**
	 * Static method for testing whether a file path links to an image that
	 * we can display. E.g jpg or png.
	 * 
	 * @param imagePath
	 * @return
	 */
	public static boolean showImage(String imagePath) 
	{
		if (imagePath == null)		return false;
		File file = new File(imagePath);
		
		FileFilter f = new JPEGFilter();
		if (f.accept(file))
			return true;
		f = new PNGFilter();
		if (f.accept(file))
			return true;
		
		return false;
	}
}
