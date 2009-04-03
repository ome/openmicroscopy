package util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tree.DataFieldNode;

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

/**
 * Defines the export methods, for converting a document into eg. Web-page or text doc.
 * Need to pass a file as destination for exporting (write to this file). 
 */

public interface IExport {

	/**
	 * Simplest export. Export the given nodes to the file.
	 *  
	 * @param file		Where the file will be written to. 
	 * @param rootNodes	The list of nodes to export (could be a single 'root' node)
	 */
	public void export(File file, List<DataFieldNode> rootNodes);
	
	
	/**
	 * Export according to a preference Map. Export the given nodes to the file.
	 *  
	 * @param file		Where the file will be written to. 
	 * @param rootNodes	The list of nodes to export (could be a single 'root' node)
	 * @param exportPreferences		A Map of boolean values. eg printDefaultValue
	 */
	public void export(File file, List<DataFieldNode> rootNodes, Map<String, Boolean> exportPreferences);
	
	
	/**
	 * Exports the given dataField nodes as a String. 
	 * This method works by creating a temporary file, delegating the
	 * export to that file, using {@link #export(File, List)} and then 
	 * converting the file to a String. The temp file is deleted before 
	 * returning the String.
	 * 
	 * @param rootNodes		A list of the nodes to export
	 * @return String		A string representation of the export. 
	 */
	public String exportToString(List<DataFieldNode> rootNodes);
}
