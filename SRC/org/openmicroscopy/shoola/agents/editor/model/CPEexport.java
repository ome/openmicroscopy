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
import org.openmicroscopy.shoola.agents.editor.model.params.BooleanParam;
import org.openmicroscopy.shoola.agents.editor.model.params.DateTimeParam;
import org.openmicroscopy.shoola.agents.editor.model.params.EnumParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.NumberParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * This class is for exporting OMERO.editor files as "CPE" Common Protocol 
 * Exchange XML files. It writes "strict" CPE files, according to our 
 * currently agreed format. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CPEexport {
	
	public static final String  	XML_HEADER = 
							"<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	
	public static final String 		CPE_DTD = "<!DOCTYPE protocol-archive " +
		"PUBLIC \"-//Universal Protocol Exchange Format//DTD upe 1.0//EN\" " +
		"\"http://genome.tugraz.at/iLAP/upe/upe.dtd\">";

	/**
	 * Reference to a style-sheet, so that when the XML is viewed in a 
	 * browser (NOT FireFox!) the XML is transformed with remote stylesheet. 
	 */
	public static final String 		CPE_STYLESHEET ="<?xml-stylesheet " +
	"href=\"http://users.openmicroscopy.org.uk/~will/schemas/cpeEditor2html.xsl\""
	+ " type=\"text/xsl\"?>";
	
	private int 					paramID = 0;

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
			// TODO should be "SPLIT_STEP" if this has been set somehow
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
		IXMLElement step = new XMLElement(CPEimport.STEP);
		
		IField field = getFieldFromTreeNode(treeNode);
		if (field == null) return step;
	
		// name
		String name = field.getAttribute(Field.FIELD_NAME);
		if (name != null) {
			addChildContent(step, CPEimport.NAME, name);
		}
		
		// add parameters (and step description)
		addParameters(field, step);
		
		return step;
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
		
		String elementName = CPEimport.PARAM_LIST;
		if (field.getTableData() != null) {
			elementName = CPEimport.PARAM_TABLE;
		}
		IXMLElement params = new XMLElement(elementName);
		
		String stepDescription = "";
		
		IFieldContent content;
		IXMLElement parameter;
		String paramID;
		for (int i=0; i<contentCount; i++) {
			content = field.getContentAt(i);
			if (content instanceof IParam) {
				parameter = createParamElement((IParam)content);
				params.addChild(parameter);
				paramID = parameter.getFirstChildNamed(CPEimport.ID)
																.getContent();
				stepDescription = stepDescription + CPEimport.ID_START 
					+ paramID + CPEimport.ID_END;
			} else {
				stepDescription = stepDescription + content.toString();
			}
		}
		addChildContent(step, CPEimport.DESCRIPTION, stepDescription);
		
		// if any parameters, add parameters element to step. 
		if (params.getChildrenCount() > 0) {
			step.addChild(params);
		}
	}
	
	/**
	 * Handles the creation of an XML element for a parameter.
	 * If appropriate, the type of parameter will be NUMERIC or 
	 * ENUMERATION or DATE_TIME, with the associated additional attributes. 
	 * Otherwise will be TEXT. No other types supported by 'CPE'
	 * 
	 * @param param			The parameter object 
	 * @return				A new XML Element that defines the parameter
	 */
	protected IXMLElement createParamElement(IParam param) 
	{
		IXMLElement parameter = new XMLElement(CPEimport.PARAMETER);
		
		// Add name, necessity, value and default-value, if not null
		String name = param.getAttribute(AbstractParam.PARAM_NAME);
		if (name == null) {
			// must have a name
			name = param.getAttribute(AbstractParam.PARAM_TYPE);
		}
		addChildContent(parameter, CPEimport.NAME, name);
		addChildContent(parameter, CPEimport.ID, paramID++ +"");
		addChildContent(parameter, CPEimport.NECESSITY, "OPTIONAL");
		
		// Depending on the type of parameter, set the param-type, 
		// and add any additional attributes. 
		if (param instanceof NumberParam) {
			addChildContent(parameter, CPEimport.PARAM_TYPE, "NUMERIC");
			setValueAndDefault(parameter, param);
			String units = param.getAttribute(NumberParam.PARAM_UNITS);
			if (units != null)
				addChildContent(parameter, CPEimport.UNITS, units);
		} else 
		if (param instanceof EnumParam) {
			addChildContent(parameter, CPEimport.PARAM_TYPE, "ENUMERATION");
			setValueAndDefault(parameter, param);
			String enumOptions = param.getAttribute(EnumParam.ENUM_OPTIONS);
			if (enumOptions != null) {
				IXMLElement enumList = new XMLElement(CPEimport.ENUM_LIST);
				String[] options = enumOptions.split(",");
				for (int i=0; i<options.length; i++) {
					addChildContent(enumList, CPEimport.ENUM, options[i].trim());
				}
				parameter.addChild(enumList);
			}
		} 
		else 
		if (param instanceof TextParam) {
			addChildContent(parameter, CPEimport.PARAM_TYPE, "TEXT");
			setValueAndDefault(parameter, param);
		}
		else 
			if (param instanceof DateTimeParam) {
				addChildContent(parameter, CPEimport.PARAM_TYPE, "DATE_TIME");
				
				String ms = param.getAttribute(TextParam.PARAM_VALUE);
				if (ms != null) {
					IXMLElement data = new XMLElement(CPEimport.DATA);
					addChildContent(data, CPEimport.VALUE, ms);
					parameter.addChild(data);
				}
			}
		else 
			if (param instanceof BooleanParam) {
				addChildContent(parameter, CPEimport.PARAM_TYPE, "ENUMERATION");
				setValueAndDefault(parameter, param);
				IXMLElement enumList = new XMLElement(CPEimport.ENUM_LIST);
				addChildContent(enumList, CPEimport.ENUM, "true");
				addChildContent(enumList, CPEimport.ENUM, "false");
				parameter.addChild(enumList);
			}
		
		else {
			// use a TEXT parameter to store any parameter type not 
			// supported by CPE, eg. Ontology Term. 
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
		int valCount = param.getValueCount();
		IXMLElement data = new XMLElement(CPEimport.DATA);
		Object v;
		String value;
		for (int i = 0; i < valCount; i++) {
			v = param.getValueAt(i);
			if (v == null)		value = "";
			else 	value = v + "";
			addChildContent(data, CPEimport.VALUE, value);
		}
		if (valCount > 0) {
			parameter.addChild(data);
		}
		
		String defaultValue = param.getAttribute(TextParam.DEFAULT_VALUE);
		if (defaultValue != null)
			addChildContent(parameter, CPEimport.DEFAULT, defaultValue);
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
	 * Exports a CPE XML document created from the {@link TreeModel} to the
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
			output.write(CPE_STYLESHEET + "\n");
			output.write(CPE_DTD + "\n");
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
