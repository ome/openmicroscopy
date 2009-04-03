/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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

package validation;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is the source of attributes required in the root element of an OMERO.editor XML file,
 * so that the file can be validated against it's on-line schema. 
 * The static method getRootAttributes() returns a Map of these attributes.
 * 
 * @author will
 *
 */
public class XMLSchema {
	
	public static final String SCHEMA_LOCATION = "xsi:schemaLocation";
	
	public static final String XML_NAMESPACE = "xmlns";

	private static Map<String, String> rootAttributes = new HashMap<String, String>();
	
	/**
	 * This method returns a Map of the attributes required in the root Element of an
	 * OMERO.editor file, such that the file can be validated against it's online schema. 
	 * 
	 * These attributes include the XML namespace and the schema location. 
	 * 
	 * @return
	 */
	public static Map<String, String> getRootAttributes() {
		rootAttributes.put(XML_NAMESPACE, "http://www.openmicroscopy.org/Schemas/Editor/2008-04");
		rootAttributes.put("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
		rootAttributes.put(SCHEMA_LOCATION, "http://www.openmicroscopy.org/Schemas/Editor/2008-04 http://www.openmicroscopy.org/Schemas/Editor/2008-04/Editor.xsd");
		//rootAttributes.put("xmlns:ome", "http://www.openmicroscopy.org/Schemas/OME/2007-06");
		
		return rootAttributes;
	}
	
}


