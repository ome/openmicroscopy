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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

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
import org.openmicroscopy.shoola.agents.editor.model.params.EnumParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.NumberParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * This class is for exporting OMERO.editor files as "UPE" Universal Protocol 
 * Exchange XML files. It writes "strict" UPE files, according to our 
 * currently agreed format. 
 * A subclass {@link UPEEditorExport} adapts this export to support additional
 * features required by OMERO.editor files (eg parameters in context with 
 * descriptions). 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class UPEexport {
	
	public static final String  	XML_HEADER = 
							"<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	
	public static final String 		UPE_DTD = "<!DOCTYPE protocol-archive " +
		"PUBLIC \"-//Universal Protocol Exchange Format//DTD upe 1.0//EN\" " +
		"\"http://genome.tugraz.at/iLAP/upe/upe.dtd\">";

	/**
	 * Reference to a style-sheet, so that when the XML is viewed in a 
	 * browser (NOT FireFox!) the XML is transformed with remote stylesheet. 
	 */
	public static final String 		UPE_STYLESHEET ="<?xml-stylesheet " +
	"href=\"http://users.openmicroscopy.org.uk/~will/schemas/upeEditor2html.xsl\""
	+ " type=\"text/xsl\"?>";

	/**
	 * A recursive method that traverses the treeModel, building an 
	 * equivalent structure of <code>step</code> elements.
	 * Step element creation is handled by the {@link #createStepElement(TreeNode)} 
	 * method.
	 * 
	 * @param node			The root node of the tree
	 * @param element		The element corresponding to the node. 
	 */
	private IXMLElement buildSteps(TreeNode node) 
	{
		IXMLElement rootStep = createStepElement(node);
		IXMLElement childSteps = new XMLElement("step-children");
		
		TreeNode childNode;
		IXMLElement step;
		for(int i=0; i<node.getChildCount(); i++) {
			
			childNode = node.getChildAt(i);
			// recursively process the tree rooted at childNode
			step = buildSteps(childNode);
			childSteps.addChild(step);
		}
		
		// only add child-steps element if not empty.
		if (childSteps.getChildrenCount() > 0) {
			// add the step_type attribute before the child steps
			// TODO  Discuss whether this is useful, since I don't allow the 
			// option of "SPLIT_STEP" to contain concurrent steps
			addChildContent(rootStep, "step_type", "STEP_GROUP");
			rootStep.addChild(childSteps);
		} else {
			addChildContent(rootStep, "step_type","SINGLE_STEP");
		}
		
		return rootStep;
	}
	
	/**
	 * Creates an {@link IXMLElement} from a {@link TreeNode}.
	 * Gets the {@link IField} object from the node, and uses it to add
	 * name and content elements to a new {@link IXMLElement};
	 * 
	 * @param treeNode		The node used to generate a new Element
	 * @return				A new Element, based on the node. 
	 */
	private IXMLElement createStepElement(TreeNode treeNode) 
	{
		// create element, add essential attributes
		IXMLElement step = new XMLElement("step");
		
		IField field = getFieldFromTreeNode(treeNode);
		if (field == null) return step;
	
		// name
		String name = field.getAttribute(Field.FIELD_NAME);
		if (name != null)
			addChildContent(step, "name", name);
		// description
		addStepDescription(field, step);
		
		// deleteable = true
		addChildContent(step, "deletable", "true");
		
		// add parameters
		addParameters(field, step);
		
		return step;
	}
	
	/**
	 * In order to write XML files that adhere to the current UPE format,
	 * a step should have a description. If the file has been created by 
	 * reading a UPE file, the description will be the first item in the 
	 * field's content. 
	 * This method can be overridden by subclasses that write a UPE file 
	 * with the step description in context with parameters. 
	 * 
	 * @param field			The tree-model field/node which equates to a step
	 * @param step			The XML step element to add the description to. 
	 */
	protected void addStepDescription(IField field, IXMLElement step) 
	{
		// description
		if (field.getContentCount() >0) {
			IFieldContent content = field.getContentAt(0);
			if (content instanceof TextContent) {
				String desc = content.toString();
				addChildContent(step, "description", desc);
			}
		}
	}
	
	/**
	 * This method uses the parameters from the field (of the editor data model)
	 * to build parameter XML elements, which are added to the <code>step</code>
	 * element. 
	 * 
	 * @param field			see above
	 * @param step			see above
	 */
	protected void addParameters(IField field, IXMLElement step) 
	{
		// add parameters
		int contentCount = field.getContentCount();
		
		IXMLElement params = new XMLElement("parameters");
		
		IFieldContent content;
		IXMLElement parameter;
		for (int i=0; i<contentCount; i++) {
			content = field.getContentAt(i);
			if (content instanceof IParam) {
				parameter = createParamElement((IParam)content);
				params.addChild(parameter);
			}
		}
		// if any parameters, add parameters element to step. 
		if (params.getChildrenCount() > 0) {
			step.addChild(params);
		}
	}
	
	/**
	 * Handles the creation of an XML element for a parameter.
	 * If appropriate, the type of parameter will be NUMERIC or 
	 * ENUMERATION, with the associated additional attributes. Otherwise
	 * will be TEXT. No other types supported as yet by 'UPE'.
	 * Need to use {@link UPEEditorExport#createParamElement(IParam)} which
	 * will write parameter types not supported by 'UPE'.
	 * 
	 * @param param			The parameter object 
	 * @return				A new XML Element that defines the parameter
	 */
	protected IXMLElement createParamElement(IParam param) 
	{
		IXMLElement parameter = new XMLElement("parameter");
		
		// Add name, necessity, value and default-value, if not null
		String name = param.getAttribute(AbstractParam.PARAM_NAME);
		if (name != null)
			addChildContent(parameter, "name", name);
		
		addChildContent(parameter, "necessity", "OPTIONAL");
		
		// Depending on the type of parameter, set the param-type, 
		// and add any additional attributes. 
		if (param instanceof NumberParam) {
			addChildContent(parameter, "param-type", "NUMERIC");
			setValueAndDefault(parameter, param);
			String units = param.getAttribute(NumberParam.PARAM_UNITS);
			if (units != null)
				addChildContent(parameter, "unit", units);
		} else 
		if (param instanceof EnumParam) {
			addChildContent(parameter, "param-type", "ENUMERATION");
			setValueAndDefault(parameter, param);
			String enumOptions = param.getAttribute(EnumParam.ENUM_OPTIONS);
			if (enumOptions != null) {
				IXMLElement enumList = new XMLElement("enum-list");
				String[] options = enumOptions.split(",");
				for (int i=0; i<options.length; i++) {
					addChildContent(enumList, "enum", options[i].trim());
				}
				parameter.addChild(enumList);
			}
		} 
		else 
		if (param instanceof TextParam) {
			addChildContent(parameter, "param-type", "TEXT");
			setValueAndDefault(parameter, param);
		}
		
		else {
			// use a TEXT parameter to store any parameter type not 
			// supported by UPE. 
			addChildContent(parameter, "param-type", "TEXT");
			String value = param.getParamValue();
			if (value != null) {
				addChildContent(parameter, "value", value);
			}
		}
		
		return parameter;
	}
	
	/**
	 * Convenience method to map the "value" and "default" attributes 
	 * from an {@link IParam} to an {@link IXMLElement} XML element. 
	 * 
	 * @param parameter
	 * @param param
	 */
	private void setValueAndDefault(IXMLElement parameter, IParam param) 
	{
		String value = param.getAttribute(TextParam.PARAM_VALUE);
		if (value != null) {
			addChildContent(parameter, "value", value);
		}
		
		String defaultValue = param.getAttribute(TextParam.DEFAULT_VALUE);
		if (defaultValue != null)
			addChildContent(parameter, "default-value", defaultValue);
	}
	
	/**
	 * Convenience method for adding child content to an XML element.
	 * String is added as text content of a new child element.
	 * 
	 * @param parent			The parent XML element
	 * @param childName			The name of the new Child element
	 * @param childContent		The text content of the new child element
	 */
	protected static void addChildContent(IXMLElement parent, 
									String childName, String childContent) 
	{
		// check not null
		if (parent == null)		return;
		if ((childName == null) || (childName.contains(" ")) || 
				(childName.length() == 0))	return;
		if (childContent == null)	return;
		
		// create child, with content, and add child to parent
		IXMLElement child = new XMLElement(childName);
		child.setContent(childContent);
		parent.addChild(child);
	}
	
	/**
	 * Handles the retrieval of the {@link IField} from a node of treeModel. 
	 * Returns null if the node is not a {@link DefaultMutableTreeNode} or
	 * the userObject in the node is not a {@link IField}.
	 * 
	 * @param treeNode		The node that contains the field.
	 * @return				see above 
	 */
	private static IField getFieldFromTreeNode(TreeNode treeNode) 
	{
		// if treeNode isn't a DefaultMutableTreeNode, return null
		if (treeNode == null) return null;
		if (! (treeNode instanceof DefaultMutableTreeNode)) return null;
		
		// get the userObject from the node. If it's a Field...
		DefaultMutableTreeNode dmNode = (DefaultMutableTreeNode)treeNode;
		Object userOb = dmNode.getUserObject();
		if (userOb instanceof IField) {
			IField field = (IField)userOb;
			return field;
		}
		return null;
	}

	/**
	 * Exports a UPE XML document created from the {@link TreeModel} to the
	 * {@link File} specified. 
	 * 
	 * @param treeModel			The OMERO.editor data model. 
	 * @param file				The XML file to export to. 
	 */
	public boolean export(TreeModel treeModel, File file) 
	{
		// start with the root of the XHTML document
		IXMLElement protocolArchive = new XMLElement("protocol-archive");
		
		// add archive info element
		IXMLElement archiveInfo = new XMLElement("archive-info");
		addChildContent(archiveInfo, "archive-version", "1.0");
		Date now = new Date();
		addChildContent(archiveInfo, "archive-date", now.getTime() + "");
		addChildContent(archiveInfo, "archive-creator", "OMERO.editor");
		addChildContent(archiveInfo, "archive-type", "PROTOCOL_ARCHIVE");
		protocolArchive.addChild(archiveInfo);
		
		// add protocol element, which contains info and steps 
		IXMLElement protocol = new XMLElement("protocol");
		protocolArchive.addChild(protocol);
		IXMLElement protocolInfo = new XMLElement("protocol-information");
		protocol.addChild(protocolInfo);
		
		// get the root of the data
		TreeNode root = (TreeNode)treeModel.getRoot();
		IField protocolRoot = getFieldFromTreeNode(root);
		
		// name
		String protName = protocolRoot.getAttribute(Field.FIELD_NAME);
		if ((protName != null) && (protName.length() >0)) {
			addChildContent(protocolInfo, "name", protName);
		}

		// description
		if (protocolRoot.getContentCount() >0) {
			String desc = protocolRoot.getContentAt(0).toString();
			addChildContent(protocolInfo, "description", desc);
		}
		// could add revision info if available
		// addChildContent(protocolInfo, "revision", "1");
		
		XMLElement steps = new XMLElement("steps");
		protocol.addChild(steps);
		
		TreeNode childNode;
		IXMLElement step;
		for(int i=0; i<root.getChildCount(); i++) {
			childNode = root.getChildAt(i);
			// recursively process the tree rooted at childNode
			step = buildSteps(childNode);
			steps.addChild(step);
		}
		
		Writer output;
		try {
			// output the XML file with suitable headers...
			output = new FileWriter(file);
			output.write(XML_HEADER + "\n");
			output.write(UPE_STYLESHEET + "\n");
			output.write(UPE_DTD + "\n");
			XMLWriter xmlwriter = new XMLWriter(output);
			xmlwriter.write(protocolArchive, true);
		} catch (IOException e) {
			
			// Register error message...
			Registry reg = EditorAgent.getRegistry();
			reg.getLogger().error(this, e.toString());
			
			// ...and notify the user. Maybe output file doesn't exist? SaveAs?
			UserNotifier un = reg.getUserNotifier();
		    un.notifyInfo("File Failed to Save", 
				"The file could not be saved for some reason. Try 'Save As...'");
			return false;
		}
		return true;
	}
}
