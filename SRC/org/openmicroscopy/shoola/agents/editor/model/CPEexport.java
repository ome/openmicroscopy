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
import org.openmicroscopy.shoola.agents.editor.model.params.EditorLinkParam;
import org.openmicroscopy.shoola.agents.editor.model.params.EnumParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.NumberParam;
import org.openmicroscopy.shoola.agents.editor.model.params.OntologyTermParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextBoxParam;
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
	
	private String 		cpeDtd;

	/**
	 * Reference to a style-sheet, so that when the XML is viewed in a 
	 * browser (NOT FireFox!) the XML is transformed with remote stylesheet. 
	 */
	private String 		cpeStyles;
	
	private int 					paramID = 0;
	
	public CPEexport() {
		String dtd = (String)EditorAgent.getRegistry().lookup("/xml/cpe.dtd");
		String xsl = (String)EditorAgent.getRegistry().lookup("/xml/editor.xsl");
		
		cpeDtd = "<!DOCTYPE protocol-archive " +
			"PUBLIC \"-//Common Protocol Exchange Format//DTD upe 1.0//EN\" " +
			"\"" + dtd + "\">";
		
		if ((xsl != null) && (xsl.length() > 0)) {
			cpeStyles ="<?xml-stylesheet href=\"" + xsl + "\""
				+ " type=\"text/xsl\"?>";
		}
	}

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
		IField field = getFieldFromTreeNode(node);
		
		IXMLElement rootStep = createStepElement(field);
		IXMLElement childSteps = new XMLElement("step-children");
		
		TreeNode childNode;
		IXMLElement step;
		for(int i=0; i<node.getChildCount(); i++) {
			
			childNode = node.getChildAt(i);
			// recursively process the tree rooted at childNode
			step = buildSteps(childNode);
			childSteps.addChild(step);
		}
		
		String stepType = field.getAttribute(Field.STEP_TYPE);
		
		// set step-type depending on whether children exist.
		if (childSteps.getChildrenCount() > 0) {
			// add the step_type attribute before the child steps
			// If step-type is not a split step, must be STEP_GROUP
			if (! CPEimport.SPLIT_STEP.equals(stepType)) {
				addChildContent(rootStep, CPEimport.STEP_TYPE, 
													CPEimport.STEP_GROUP);
			}
			else {
				addChildContent(rootStep, CPEimport.STEP_TYPE, 
													CPEimport.SPLIT_STEP);
			}
			// only add child-steps element if not empty.
			rootStep.addChild(childSteps);
		} else {
			// no children. 
			// Step-type is SINGLE_STEP, unless otherwise defined
			if (stepType == null) {
				addChildContent(rootStep, CPEimport.STEP_TYPE, 
													CPEimport.SINGLE_STEP);
			}
			else {
				addChildContent(rootStep, CPEimport.STEP_TYPE, stepType);
			}
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
	private IXMLElement createStepElement(IField field) 
	{
		// create element, add essential attributes
		IXMLElement step = new XMLElement(CPEimport.STEP);
		
		if (field == null) return step;
	
		// name
		String name = field.getAttribute(Field.FIELD_NAME);
		if (name != null) {
			addChildContent(step, CPEimport.NAME, name);
		}
		
		// add parameters (and step description)
		addParameters(field, step);
		
		// add step notes
		addNotes(field, step);
		
		// add data references
		addDataRefs(field, step);
		
		return step;
	}
	
	/**
	 * Copies the notes from a {@link IField} data object into an 
	 * {@link IXMLElement} XML step element;
	 *  
	 * @param field			The field data object
	 * @param step			The step XML element
	 */
	private void addNotes(IField field, IXMLElement step)
	{
		int noteCount = field.getNoteCount();
		if (noteCount == 0)		return;
		
		IXMLElement notes = new XMLElement(CPEimport.NOTES);
		IXMLElement note;
		Note noteData;
		String name, content;
		for (int i = 0; i < noteCount; i++) {
			note = new XMLElement(CPEimport.NOTE);
			noteData = field.getNoteAt(i);
			name = noteData.getName();
			content = noteData.getContent();
			addChildContent(note, CPEimport.NAME, name);
			addChildContent(note, CPEimport.CONTENT, content);
			notes.addChild(note);
		}
		step.addChild(notes);
	}
	
	/**
	 * Copies the data references from a {@link IField} data object into an 
	 * {@link IXMLElement} XML step element;
	 *  
	 * @param field			The field data object
	 * @param step			The step XML element
	 */
	private void addDataRefs(IField field, IXMLElement step)
	{
		int dRefCount = field.getDataRefCount();
		if (dRefCount == 0)		return;
		
		IXMLElement drefs = new XMLElement(CPEimport.DATA_REFS);
		IXMLElement ref;
		IAttributes noteData;
		String attribute;
		for (int i = 0; i < dRefCount; i++) {
			ref = new XMLElement(CPEimport.DATA_REF);
			noteData = field.getDataRefAt(i);
			attribute = noteData.getAttribute(DataReference.NAME);
			addChildContent(ref, DataReference.NAME, attribute);
			attribute = noteData.getAttribute(DataReference.REFERENCE);
			addChildContent(ref, DataReference.REFERENCE, attribute);
			attribute = noteData.getAttribute(DataReference.DESCRIPTION);
			addChildContent(ref, DataReference.DESCRIPTION, attribute);
			attribute = noteData.getAttribute(DataReference.MIME_TYPE);
			addChildContent(ref, DataReference.MIME_TYPE, attribute);
			attribute = noteData.getAttribute(DataReference.SIZE);
			addChildContent(ref, DataReference.SIZE, attribute);
			attribute = noteData.getAttribute(DataReference.CREATION_TIME);
			addChildContent(ref, DataReference.CREATION_TIME, attribute);
			attribute = noteData.getAttribute(DataReference.MODIFICATION_TIME);
			addChildContent(ref, DataReference.MODIFICATION_TIME, attribute);
			drefs.addChild(ref);
		}
		step.addChild(drefs);
	}
	
	/**
	 * This method uses the parameters from the field (of the editor data model)
	 * to build parameter XML elements, which are added to the <code>step</code>
	 * element. 
	 * 
	 * @param field			see above
	 * @param step			see above
	 */
	private void addParameters(IField field, IXMLElement step) 
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
	private IXMLElement createParamElement(IParam param) 
	{
		IXMLElement parameter = new XMLElement(CPEimport.PARAMETER);
		
		// Add name, necessity, value and default-value, if not null
		String name = param.getAttribute(AbstractParam.PARAM_NAME);
		if (name == null) {
			// must have a name
			name = AbstractParam.DEFAULT_PARAM_NAME;
		}
		addChildContent(parameter, CPEimport.NAME, name);
		addChildContent(parameter, CPEimport.ID, paramID++ +"");
		
		// parameter description. Added below
		String paramDesc = param.getAttribute(AbstractParam.PARAM_DESC);
		
		// parameter necessity
		boolean required = param.isAttributeTrue(AbstractParam.PARAM_REQUIRED);
		String necessity = required ? CPEimport.REQUIRED : CPEimport.OPTIONAL;
		addChildContent(parameter, CPEimport.NECESSITY, necessity);
		
		// Depending on the type of parameter, set the param-type, 
		// and add any additional attributes. 
		if (param instanceof NumberParam) {
			addChildContent(parameter, CPEimport.PARAM_TYPE, "NUMERIC");
			setValueAndDefault(parameter, param);
			String units = param.getAttribute(NumberParam.PARAM_UNITS);
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
			String units = param.getAttribute(NumberParam.PARAM_UNITS);
			addChildContent(parameter, CPEimport.UNITS, units);
		} 
		else 
		if (param instanceof TextParam) {
			addChildContent(parameter, CPEimport.PARAM_TYPE, "TEXT");
			setValueAndDefault(parameter, param);
		} 
		else
		if (param instanceof TextBoxParam) {
			addChildContent(parameter, CPEimport.PARAM_TYPE, "TEXT");
			setValueAndDefault(parameter, param);
			// if text should be a text-box, add flag to parameter description
			// since text-box parameter type is not supported by cpe.xml
			paramDesc = CPEimport.TEXT_BOX_FLAG + 
									(paramDesc == null ? "" : paramDesc);
		}
		else 
		if (param instanceof EditorLinkParam) {
			addChildContent(parameter, CPEimport.PARAM_TYPE, "TEXT");
			setValueAndDefault(parameter, param);
			// if text should be a link to Editor file, add flag to 
			// parameter description
			paramDesc = CPEimport.PROTOCOL_LINK_FLAG + 
									(paramDesc == null ? "" : paramDesc);
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
				// store boolean as an enumeration (No boolean in cpe.xml)
				addChildContent(parameter, CPEimport.PARAM_TYPE, "ENUMERATION");
				setValueAndDefault(parameter, param);
				IXMLElement enumList = new XMLElement(CPEimport.ENUM_LIST);
				addChildContent(enumList, CPEimport.ENUM, "true");
				addChildContent(enumList, CPEimport.ENUM, "false");
				parameter.addChild(enumList);
			}
		
		else 
			if (param instanceof OntologyTermParam) {
				// store ontology term as a text field
				addChildContent(parameter, CPEimport.PARAM_TYPE, "TEXT");
				String value = param.getParamValue();
				if (value != null) {
					IXMLElement data = new XMLElement(CPEimport.DATA);
					addChildContent(data, CPEimport.VALUE, value);
					parameter.addChild(data);
				}
				// if text should be an ontology, add flag to parameter
				// description. Look for this on import. 
				paramDesc = CPEimport.ONTOLOGY_FLAG + 
										(paramDesc == null ? "" : paramDesc);
			}
		
		else {
			// use a TEXT parameter to store any parameter type not 
			// supported by CPE, eg. Ontology Term. 
			addChildContent(parameter, CPEimport.PARAM_TYPE, "TEXT");
			String value = param.getParamValue();
			IXMLElement data = new XMLElement(CPEimport.DATA);
			if (value != null) {
				addChildContent(data, CPEimport.VALUE, value);
				parameter.addChild(data);
			}
		}
		
		addChildContent(parameter, CPEimport.DESCRIPTION, paramDesc);
		
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
	static void addChildContent(IXMLElement parent, 
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
	static IField getFieldFromTreeNode(TreeNode treeNode) 
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
	 * {@link File} specified. Returns <code>true</code> if the export
	 * was successful, <code>false</code> otherwise.
	 * 
	 * @param treeModel The OMERO.editor data model. 
	 * @param file		The XML file to export to. 
	 * @return See above.
	 */
	public boolean export(TreeModel treeModel, File file) 
	{
		// start with the root of the XHTML document
		IXMLElement protocolArchive = new XMLElement("protocol-archive");
		
		// add archive info element
		IXMLElement archiveInfo = new XMLElement(CPEimport.ARCHIVE_INFO);
		addChildContent(archiveInfo, "archive-version", "1.0");
		Date now = new Date();
		addChildContent(archiveInfo, CPEimport.ARCHIVE_DATE, now.getTime() + "");
		addChildContent(archiveInfo, "archive-creator", "OMERO.editor");
		addChildContent(archiveInfo, "archive-type", "PROTOCOL_ARCHIVE");
		protocolArchive.addChild(archiveInfo);
		
		// add protocol element, which contains info and steps 
		IXMLElement protocol = new XMLElement(CPEimport.PROTOCOL);
		protocolArchive.addChild(protocol);
		IXMLElement protocolInfo = new XMLElement(CPEimport.PROTOCOL_INFO);
		protocol.addChild(protocolInfo);
		
		// get the root of the data
		TreeNode root = (TreeNode)treeModel.getRoot();
		IField protocolRoot = getFieldFromTreeNode(root);
		// first, update the last-saved date to 'now'
		protocolRoot.setAttribute(CPEimport.ARCHIVE_DATE, now.getTime() + "");
		
		// name
		String protName = protocolRoot.getAttribute(Field.FIELD_NAME);
		if ((protName != null) && (protName.length() >0)) {
			addChildContent(protocolInfo, CPEimport.NAME, protName);
		}
		
		// description
		if (protocolRoot.getContentCount() >0) {
			String desc = protocolRoot.getContentAt(0).toString();
			addChildContent(protocolInfo, CPEimport.DESCRIPTION, desc);
		}
		
		// revision. Optional. Not used by Editor. Preserve cpe.xml data only. 
		String revision = protocolRoot.getAttribute(CPEimport.REVISION);
		addChildContent(protocolInfo, CPEimport.REVISION, revision);
		
		// experiment-info.
		IAttributes eInfo = null;
		if (protocolRoot instanceof ProtocolRootField) {
			eInfo = ((ProtocolRootField)protocolRoot).getExpInfo();
		}

		if (eInfo != null) {
			String expDate = eInfo.getAttribute(ExperimentInfo.EXP_DATE);
			String investigName = eInfo.getAttribute(ExperimentInfo.INVESTIG_NAME);
			IXMLElement expInfo = new XMLElement(CPEimport.EXP_INFO);
			// make sure neither are null
			if (expDate == null)	expDate = now.getTime() + "";
			if (investigName == null)	{
				investigName = System.getProperty("user.name");
			}
			addChildContent(expInfo, ExperimentInfo.EXP_DATE, expDate);
			addChildContent(expInfo, ExperimentInfo.INVESTIG_NAME, investigName);
			protocolInfo.addChild(expInfo);
		}

		// steps element holds top level of step elements
		XMLElement steps = new XMLElement(CPEimport.STEPS);
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
			if (cpeStyles != null) 	output.write(cpeStyles + "\n");
			output.write(cpeDtd + "\n");
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
