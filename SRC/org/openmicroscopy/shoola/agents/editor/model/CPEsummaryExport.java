 /*
 * org.openmicroscopy.shoola.agents.editor.model.UPEexport 
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
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

//Third-party libraries

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLWriter;

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.DateTimeParam;
import org.openmicroscopy.shoola.agents.editor.model.params.EnumParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.NumberParam;
import org.openmicroscopy.shoola.agents.util.editorpreview.PreviewPanel;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * This class is for exporting OMERO.editor files as Summary XML for display
 * by other Agents in Insight. 
 * This contains only the bare minimum needed for a summary:
 * The protocol name and description, step name and parameter name, value 
 * pairs, and is limited in size (large files will be truncated).
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CPEsummaryExport {
	
	/**
	 * To limit the length of a summary, don't add more than a certain number
	 * of 'lines'. Each step is one line, and each parameter is one line. 
	 */
	private static final int 		MAX_LINES = 15;
	
	/**
	 * The maximum number of characters allowed for the description/abstract.
	 */
	public static final int 		MAX_DESC = 500;
	
	/** A counter to keep track of the number of lines */
	private static int				lines;
	
	/**
	 * A recursive method that traverses the treeModel, adding step
	 * elements to the list
	 * 
	 * @param node			The root node of the tree
	 * @param stepList		The container for list of step elements. 
	 * @param level			The current level we are processing. 
	 */
	private static void buildSteps(TreeNode node, IXMLElement stepList, int level) 
	{
		if (lines > MAX_LINES) return;
		
		IField field = CPEexport.getFieldFromTreeNode(node);
		
		// step element
		IXMLElement step = new XMLElement(PreviewPanel.STEP);
		step.setAttribute(PreviewPanel.LEVEL, level+"");
		// step name
		String name = field.getAttribute(Field.FIELD_NAME);
		if (name == null)
			name = TreeModelMethods.getNodeName((DefaultMutableTreeNode)node);
		step.setAttribute(PreviewPanel.NAME, name);
		
		// params
		int contentCount = field.getContentCount();
		IFieldContent content;
		IXMLElement parameter;
		int paramCount = 0;
		for (int i=0; i<contentCount; i++) {
			content = field.getContentAt(i);
			if (content instanceof IParam) {
				paramCount++;
				parameter = createParamElement((IParam)content);
				if (lines < MAX_LINES) {
					step.addChild(parameter);
					lines++;
				}
			}
		}
		// if it has parameters, add to steps list
		if (paramCount > 0) {
			if (lines < MAX_LINES) {
				stepList.addChild(step);
				lines++;
			}
		}
		
		TreeNode childNode;
		int childLevel = level + 1;
		for(int i=0; i<node.getChildCount(); i++) {
			
			childNode = node.getChildAt(i);
			// recursively process the tree rooted at childNode
			buildSteps(childNode, stepList, childLevel);
		}
	}

	
	/**
	 * Handles the creation of an XML element for a parameter.
	 * 
	 * @param param			The parameter object 
	 * @return				A new XML Element that defines the parameter
	 */
	private static IXMLElement createParamElement(IParam param) 
	{
		IXMLElement parameter = new XMLElement(PreviewPanel.PARAMETER);
		
		// Add name,
		String name = param.getAttribute(AbstractParam.PARAM_NAME);
		if (name == null) 	name = AbstractParam.DEFAULT_PARAM_NAME;
		if (param instanceof NumberParam || param instanceof EnumParam) {
			String units = param.getAttribute(NumberParam.PARAM_UNITS);
			if (units != null) 		name = name + " (" + units + ")";
		}
		CPEexport.addChildContent(parameter, PreviewPanel.NAME, name);
		// Add value
		String value = "";
		String v;
		Object o;
		int valueCount = param.getValueCount();
		for (int i=0; i<valueCount; i++) {
			if (i > 0) value = value + ", ";
			
			o = param.getValueAt(i);
			if (o == null) continue;
			v = o.toString();
			if (param instanceof DateTimeParam) {
				v = DateTimeParam.formatDate(v);
			}
			value = value + v;
		}
		
		CPEexport.addChildContent(parameter, PreviewPanel.VALUE, value);
		
		return parameter;
	}
	
	/**
	 * Exports a short, sumamry XML string created from the {@link TreeModel}.
	 * 
	 * @param treeModel			The OMERO.editor data model. 
	 */
	public static String export(TreeModel treeModel) 
	{
		// add protocol element, which contains name and description
		IXMLElement protocol = new XMLElement(CPEimport.PROTOCOL);
		
		// get the root of the data
		TreeNode root = (TreeNode)treeModel.getRoot();
		IField protocolRoot = CPEexport.getFieldFromTreeNode(root);
		
		// name
		String protName = protocolRoot.getAttribute(Field.FIELD_NAME);
		if (protName == null) 
			protName = TreeModelMethods.getNodeName((DefaultMutableTreeNode)root);
		CPEexport.addChildContent(protocol, PreviewPanel.NAME, protName);
		
		
		// description (truncate if longer than max characters)
		if (protocolRoot.getContentCount() >0) {
			String desc = protocolRoot.getContentAt(0).toString();
			if (desc != null && desc.length() > MAX_DESC) {
				desc = desc.substring(0, MAX_DESC-1) + "...";
			}
			CPEexport.addChildContent(protocol, PreviewPanel.DESCRIPTION, desc);
		}

		// steps element holds top level of step elements
		XMLElement steps = new XMLElement(PreviewPanel.STEPS);
		protocol.addChild(steps);
		
		TreeNode childNode;
		
		lines = 0;
		for(int i=0; i<root.getChildCount(); i++) {
			childNode = root.getChildAt(i);
			// recursively process the tree rooted at childNode
			buildSteps(childNode, steps, 0);
		}
		
		Writer output;
		try {
			// output the XML file with suitable headers...
			output = new StringWriter();
			XMLWriter xmlwriter = new XMLWriter(output);
			xmlwriter.write(protocol, true);
		} catch (IOException e) {
			
			// Register error message...
			Registry reg = EditorAgent.getRegistry();
			reg.getLogger().error(new CPEsummaryExport(), e.toString());
			
			return null;
		}
		if (output != null) {
			String s = output.toString();
			return s;
		}
		return null;
	}
}
