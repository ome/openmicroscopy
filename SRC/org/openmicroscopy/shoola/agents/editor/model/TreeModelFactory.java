 /*
 * treeIO.TreeModelFactory 
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

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

//Third-party libraries

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.model.Field;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * A Factory for creating a TreeModel from an XML editor file. 
 * Opens the file and parses the XML to make a 'DOM'.
 * This class delegates to others to create the TreeModel from the DOM, 
 * depending on what the root element of the DOM is.  
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TreeModelFactory
{

	
	/**
	 * Stub for creating a tree from XML file of several types. 
	 * 
	 * 
	 * @param xHtmlFile
	 * @return
	 */
	public static TreeModel getTree(File xHtmlFile) {
		// TODO return an Object (TreeModel if file was read OK, or String if not)
		IXMLElement root = null;
		
		String errMsg = null;
		String absPath = null;
		
		try {
			IXMLParser parser = XMLParserFactory.createDefaultXMLParser();

			absPath = xHtmlFile.getAbsolutePath();
			IXMLReader reader = StdXMLReader.fileReader(absPath);

			parser.setReader(reader);
			
			root = (IXMLElement) parser.parse();
		} catch (Exception ex) {
			
			ex.printStackTrace();
			errMsg = "Error reading XML file at " + absPath + "/n" 
			+ ex.toString();
		} 
		
		String rootName = null;
		
		if (root != null) {
		
			rootName = root.getFullName();
		
			if ("protocol-archive".equals(rootName))
				return CPEimport.createTreeModel(root);
			
			if ("ProtocolTitle".equals(rootName))
				return PROimport.createTreeModel(root);
			
			errMsg = "File format not recognised: " +
			" XML root element named '" + rootName + 
			"' is not an OMERO.Editor File"; 
		}
		
		// if you reach here, something is wrong. Register error message...
		Registry reg = EditorAgent.getRegistry();
		reg.getLogger().error(new TreeModelFactory(), errMsg);
		
		// ...and notify the user. 
		UserNotifier un = reg.getUserNotifier();
	    un.notifyInfo("File Failed to Open", 
				"The file could not be read, or was not recognised as " +
				"an OMERO.editor file.");
	      
		return null;
	}
	
	
	/**
	 * Creates a new 'blank file' TreeModel, for users to start editing. 
	 * Contains only a root field, with no attributes set, no parameters etc. 
	 * 
	 * @return			A new TreeModel 
	 */
	public static TreeModel getTree() {
		
		IField rootField = new Field();
		 
		DefaultMutableTreeNode rootNode = new FieldNode(rootField);
		
		return new DefaultTreeModel(rootNode);
	}

}
