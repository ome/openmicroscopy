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

import java.util.LinkedHashMap;

public class XMLSchema {

	private static LinkedHashMap<String, String> rootAttributes = new LinkedHashMap<String, String>();
	
	public static LinkedHashMap<String, String> getRootAttributes() {
		rootAttributes.put("xmlns", "http://morstonmud.com/omero/schemas");
		rootAttributes.put("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
		rootAttributes.put("xsi:schemaLocation", "http://morstonmud.com/omero/schemas http://morstonmud.com/omero/schemas/PE.xsd");
		//rootAttributes.put("xmlns:ome", "http://www.openmicroscopy.org/Schemas/OME/2007-06");
		
		return rootAttributes;
	}
	
}


