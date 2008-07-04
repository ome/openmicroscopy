
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

package treeModel;

import java.util.HashMap;


public class Field {

	HashMap<String, String> allAttributesMap;
	
	public Field() {
		this("untitled", "no value");
	}
	
	public Field(String name, String value) {
		
		allAttributesMap = new HashMap<String, String>();
		
		setAttribute("name", name);
		setAttribute("value", value);
	}
	
	public String getAttribute(String name) {
		return allAttributesMap.get(name);
	}
	
	public void setAttribute(String name, String value) {
		allAttributesMap.put(name, value);
	}
	
	public String toString() {
		return getAttribute("name") + ": " + getAttribute("value");
	}
}
