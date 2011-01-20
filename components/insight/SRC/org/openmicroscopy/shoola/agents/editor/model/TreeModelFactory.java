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
import java.io.FileInputStream;
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
import org.openmicroscopy.shoola.agents.editor.model.Field;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;

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
	 * Creates a tree model fromt the passed file.
	 * 
	 * @param file The file to handle.
	 * @return See above.
	 * @throws ParsingException If an error occured while parsing the file.
	 */
	public static TreeModel getTree(File file) 
		throws ParsingException
	{
		// TODO return an Object (TreeModel if file was read OK, or String if not)
		IXMLElement root = null;
		
		String errMsg = null;
		String absPath = file.getAbsolutePath();
		
		try {
			IXMLParser parser = XMLParserFactory.createDefaultXMLParser();

			FileInputStream input = new FileInputStream(file);
			IXMLReader reader = new StdXMLReader(input);

			parser.setReader(reader);
			
			root = (IXMLElement) parser.parse();
			
			input.close();
		} catch (Throwable ex) {
			errMsg = "Error reading XML file at " + absPath 
					+ "  File " + (file.exists() ? 
							"may be corrupted or incomplete." : "not found.");
		} 
		
		String rootName = null;
		
		if (root != null) {
		
			rootName = root.getFullName();
		
			if ("protocol-archive".equals(rootName)) {
				return CPEimport.createTreeModel(root);
			}
				
			else if ("ProtocolTitle".equals(rootName)) {
				return PROimport.createTreeModel(root);
			}
			errMsg = "File format not recognised: " +
			" XML root element named '" + rootName + 
			"' is not an OMERO.Editor File";
		}
		// if you reach here, something is wrong. Throw exception.
		throw new ParsingException(errMsg);
	}

	/**
	 * Creates a tree model. This method will handle ANY XML file, converting
	 * it to the .cpe.xml format! 
	 * If the file was not already a .cpe.xml file, DON'T overwrite, or you
	 * will lose the original file! 
	 * 
	 * @param file The file to handle.
	 * @return See above.
	 * @throws ParsingException If an error occured while parsing the file.
	 */
	public static TreeModel getTreeXml(File file) 
		throws ParsingException
	{
		// TODO return an Object (TreeModel if file was read OK, or String if not)
		IXMLElement root = null;
		
		String errMsg = null;
		String absPath = file.getAbsolutePath();
		
		try {
			IXMLParser parser = XMLParserFactory.createDefaultXMLParser();

			FileInputStream input = new FileInputStream(file);
			IXMLReader reader = new StdXMLReader(input);

			parser.setReader(reader);
			
			root = (IXMLElement) parser.parse();
			
			input.close();
		} catch (Throwable ex) {
			errMsg = "Error reading XML file at " + absPath 
					+ "  File " + (file.exists() ? 
							"may be corrupted or incomplete." : "not found.");
		} 
		
		if (root != null) {
			return PROimport.createTreeModel(root);
		}
		// if you reach here, something is wrong. Throw exception.
		throw new ParsingException(errMsg);
	}

	/**
	 * Creates a new 'blank file' TreeModel, for users to start editing. 
	 * Contains a root field and one step (no attributes set, no parameters etc) 
	 * 
	 * @return A new TreeModel 
	 */
	public static TreeModel getTree()
	{
		IField rootField = new ProtocolRootField();
		DefaultMutableTreeNode rootNode = new FieldNode(rootField);
		DefaultMutableTreeNode firstNode = new FieldNode(new Field());
		rootNode.add(firstNode);
		return new DefaultTreeModel(rootNode);
	}

	/**
	 * Creates a new 'blank file' TreeModel, for users to start editing. 
	 * Contains a root field and one step (no attributes set, no parameters etc) 
	 * 
	 * @param name The name of the expereiment
	 * @return A new TreeModel 
	 */
	public static TreeModel getExperimentTree(String name)
	{
		ProtocolRootField rootField = new ProtocolRootField();
		rootField.setAttribute(Field.FIELD_NAME, name);
		IAttributes experimentInfo = new ExperimentInfo();
		rootField.setExpInfo(experimentInfo);
		DefaultMutableTreeNode rootNode = new FieldNode(rootField);
		DefaultMutableTreeNode firstNode = new FieldNode(new Field());
		rootNode.add(firstNode);
		return new DefaultTreeModel(rootNode);
	}
	
}
