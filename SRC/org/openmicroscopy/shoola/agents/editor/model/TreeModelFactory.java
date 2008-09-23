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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

//Third-party libraries

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.model.params.DateTimeParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.ImageParam;
import org.openmicroscopy.shoola.agents.editor.model.params.LinkParam;
import org.openmicroscopy.shoola.agents.editor.model.params.MutableTableModel;
import org.openmicroscopy.shoola.agents.editor.model.params.SingleParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TableParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TimeParam;
import org.openmicroscopy.shoola.agents.editor.model.DataFieldConstants;
import org.openmicroscopy.shoola.agents.editor.model.Field;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.util.XMLMethods;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;

/** 
 * A Factory for creating a TreeModel from an XML editor file. 
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
	 * Takes a (root) XML element and a treeNode and converts the 
	 * XML rooted at the element into a treeModel, rooted at the treeNode.
	 * This method operates on the Beta-3.0 XML version, where each XML 
	 * element represents one Field, and all the nodes
	 * contained within an Element are child Fields (no child nodes are used to 
	 * describe attributes of the field).
	 * 
	 * New treeNodes are instances of FieldNode, which extends
	 * DefaultMutableTreeNode. 
	 * 
	 * @param inputElement		XML element	in a DOM document
	 * @param treeNode			A treeNode.
	 */
	private static void buildTreeFromDOM(Element inputElement, 
			DefaultMutableTreeNode treeNode)
	{
		
		NodeList children = inputElement.getChildNodes();
		
		Node node;
		
		for (int i=0; i < children.getLength(); i++) {
			
			// skip any empty (text) nodes
			node = children.item(i);
	
			 
			 if (node != null && (node.getNodeType() == Node.ELEMENT_NODE)) {
				 Element element = (Element) node; 
				 IField newField = createField(element);
				 
				 DefaultMutableTreeNode child = 
					 new FieldNode(newField);
				 
				 treeNode.add(child);
				 buildTreeFromDOM(element, child);
			 }
			 
			 // if there is a text node (a string of text between element tags), 
			 // apply is to the PARENT node
			 
			 if (node != null && (node.getNodeType() == Node.TEXT_NODE)) {
				 String textValue = node.getTextContent().trim();
				 if (textValue.length() > 0){
					 // set this attribute of the parent node
					 IField parentField = (IField)treeNode.getUserObject();
					 parentField.setAttribute(DataFieldConstants.TEXT_NODE_VALUE, 
							 node.getTextContent());
				 }
			 } 
		}
		
	}
	
	
	/**
	 * Used to convert XML Elements (Beta 3 and before) into 
	 * IField instances.
	 * Fields will contain 1 parameter (or 0 if "FixedStep"), which is
	 * created from the appropriate attributes in the XML Element. 
	 * 
	 * @param 	element		the Beta-3 XML element, which maps to a Field, and
	 * 					all attributes are in the element attribute map.
	 * @return			A Field that represents the data in a single node
	 * 					of the Beta-4 JTree.
	 */
	private static IField createField (Element element) {
		 
		 // First, make a Map of the element attributes.
		 // Makes it easier to query (see below) without worrying about nulls.
		 NamedNodeMap attributes = element.getAttributes();
		 Map<String, String> allAttributes = new HashMap<String, String>();
		 
		 String attribute;
		 String attributeValue;
		 
		 for (int i=0; i<attributes.getLength(); i++) {
			 attribute = attributes.item(i).getNodeName();
			 attributeValue = attributes.item(i).getNodeValue();
	
			 if (attributeValue != null) {
				allAttributes.put(attribute, attributeValue);
			 }
		 }
		 
		 // Get values for the Name, Description and Url...
		 String fieldName = allAttributes.get(DataFieldConstants.ELEMENT_NAME);
		 fieldName = removeHtmlTags(fieldName);
		 
		 // if the xml file's elements don't have "elementName" attribute, 
		  // use the <tagName>
		 if (fieldName == null) {
			 fieldName = element.getNodeName();
		 }
		 
		 String description = allAttributes.get(DataFieldConstants.DESCRIPTION);
		 description = removeHtmlTags(description);
		 String url = allAttributes.get(DataFieldConstants.URL);
		 
		 String colour = allAttributes.get(DataFieldConstants.BACKGROUND_COLOUR);
		
		 // Need to create a parameter object according to the type of element 
		 
		 // The 'old' version-1 xml used the "inputType" attribute to 
		 // define the type of field (one field per XML element).
		  
		  // If this attribute exists, need to convert it to the new type 
		  // eg. "Fixed Step" becomes "FixedStep", so that it can be
		  // used as the element tag name (no spaces allowed).
		   
		  // If it doesn't exist, need to use the NodeName as the inputType
		  //  (as in the new version)
		  
		  String paramType = allAttributes.get(DataFieldConstants.INPUT_TYPE);
		 
		 if (paramType != null) {
			 paramType = DataFieldConstants.getNewInputTypeFromOldInputType
			 							(paramType);
		 } else {
			 // InputType is null: Therefore this is the newer xml version: 
			 // (used up until Beta 3.0)
			 // Use <NodeName> for inputType IF the inputType is recognised.
			 String elementName = element.getNodeName();
			 if (DataFieldConstants.isInputTypeRecognised(elementName))
				 paramType = elementName;
			 else 
				 paramType = DataFieldConstants.CUSTOM;
		 }
		 
		 // Create a new field and set it's attributes.
		 IField field = new Field();
		 
		 field.setAttribute(Field.FIELD_NAME, fieldName);
		 field.setAttribute(Field.FIELD_DESCRIPTION, description);
		 field.setAttribute(Field.FIELD_URL, url);
		 field.setAttribute(Field.BACKGROUND_COLOUR, colour);
		 
		 // Field will have 0 or 1 "parameters", depending on type
		 IParam param = null;
		 
		 if (paramType.equals(DataFieldConstants.TEXT_ENTRY_STEP)) {
			 param = new SingleParam(SingleParam.TEXT_LINE_PARAM);
			 setValueAndDefault(allAttributes, param);
		 } 
		 else if (paramType.equals(DataFieldConstants.MEMO_ENTRY_STEP)) {
			 param = new SingleParam(SingleParam.TEXT_BOX_PARAM);
			 setValueAndDefault(allAttributes, param);
		 } 
		 else if (paramType.equals(DataFieldConstants.NUMBER_ENTRY_STEP)) {
			 param = new SingleParam(SingleParam.NUMBER_PARAM);
			 setValueAndDefault(allAttributes, param);
			 String units = allAttributes.get(DataFieldConstants.UNITS);
			 param.setAttribute(SingleParam.PARAM_UNITS, units);
		 } 
		 else if (paramType.equals(DataFieldConstants.DROPDOWN_MENU_STEP)) {
			 param = new SingleParam(SingleParam.ENUM_PARAM);
			 setValueAndDefault(allAttributes, param);
			 String ddOptions = allAttributes.get(
					 DataFieldConstants.DROPDOWN_OPTIONS);
			 param.setAttribute(SingleParam.ENUM_OPTIONS, ddOptions);
		 }
		 else if (paramType.equals(DataFieldConstants.CHECKBOX_STEP)) {
			 param = new SingleParam(SingleParam.BOOLEAN_PARAM);
			 setValueAndDefault(allAttributes, param);
		 } 
		 else if (paramType.equals(DataFieldConstants.TIME_FIELD)) {
			 param = new TimeParam(TimeParam.TIME_PARAM);
			 // old (pre 7th March 08) use the old value "hh:mm:ss" and default
			 setValueAndDefault(allAttributes, param);
			 // newer XML uses SECONDS attribute for timeInSecs. 
			 String secs = allAttributes.get(DataFieldConstants.SECONDS);
			 param.setAttribute(TimeParam.SECONDS, secs);
			 
		 } 
		 else if (paramType.equals(DataFieldConstants.DATE_TIME_FIELD)) {
			 param = new DateTimeParam(DateTimeParam.DATE_TIME_PARAM);
			 String millisecs = allAttributes.get(DataFieldConstants.UTC_MILLISECS);
			 if (millisecs != null) {
				// create a test calendar (see below).
				Calendar testForAbsoluteDate = new GregorianCalendar();
				testForAbsoluteDate.setTimeInMillis(new Long(millisecs));
				int year = testForAbsoluteDate.get(Calendar.YEAR);
				if (year != 1970) {		// date is not "relative"
					param.setAttribute(DateTimeParam.DATE_ATTRIBUTE, millisecs);
				} else {		// date is relative. 
					param.setAttribute(DateTimeParam.REL_DATE_ATTRIBUTE, millisecs);
					param.setAttribute(DateTimeParam.IS_RELATIVE_DATE, "true");
				}
			 }
			 millisecs = allAttributes.get(DataFieldConstants.SECONDS);
			 param.setAttribute(DateTimeParam.TIME_ATTRIBUTE, millisecs);
			 millisecs = allAttributes.get(DataFieldConstants.ALARM_SECONDS);
			 param.setAttribute(DateTimeParam.ALARM_SECONDS, millisecs);
		 } 
		 else if (paramType.equals(DataFieldConstants.TABLE)) {
			 param = new TableParam(TableParam.TABLE_PARAM);
			 Object tM = ((TableParam)param).getTableModel();
			 MutableTableModel tableModel = (MutableTableModel)tM;
			 
			 // fill columns
			 String colData = allAttributes.get(
					 DataFieldConstants.TABLE_COLUMN_NAMES);
			 String[] colNames = colData.split(",");
			 for (int c=0; c<colNames.length; c++) {
				 tableModel.addEmptyColumn(colNames[c].trim());
			 }
			 
			 // fill row data
			 int row = 0;
			 String[] cellData;
			 String rowDataString = allAttributes.get(
					 DataFieldConstants.ROW_DATA_NUMBER + row);
			 // row data exists for this row.
			 while (rowDataString != null) {
				 tableModel.addEmptyRow();	// create the row
				 cellData = rowDataString.split(",");
				 // fill the cells
				 for (int c=0; c<cellData.length; c++) {
					 tableModel.setValueAt(cellData[c].trim(), row, c);
				 }
				 // get the next row
				 row++;
				 rowDataString = allAttributes.get(
						 DataFieldConstants.ROW_DATA_NUMBER + row);
			 }
		 } else if (paramType.equals(DataFieldConstants.LINK_FIELD)){
			 param = new LinkParam(LinkParam.LINK_PARAM);
			 String link = allAttributes.get(
					 DataFieldConstants.ABSOLUTE_FILE_LINK);
			 param.setAttribute(LinkParam.ABSOLUTE_FILE_LINK, link);
			 link = allAttributes.get(
					 DataFieldConstants.RELATIVE_FILE_LINK);
			 param.setAttribute(LinkParam.RELATIVE_FILE_LINK, link);
			 link = allAttributes.get(
					 DataFieldConstants.URL_LINK);
			 param.setAttribute(LinkParam.URL_LINK, link);
		 }
		 else if (paramType.equals(DataFieldConstants.IMAGE_FIELD)){
			 param = new ImageParam(ImageParam.IMAGE_PARAM);
			 String link = allAttributes.get(
					 DataFieldConstants.ABSOLUTE_IMAGE_PATH);
			 param.setAttribute(ImageParam.ABSOLUTE_IMAGE_PATH, link);
			 link = allAttributes.get(
					 DataFieldConstants.RELATIVE_IMAGE_PATH);
			 param.setAttribute(ImageParam.RELATIVE_IMAGE_PATH, link);
			 String zoom = allAttributes.get(
					 DataFieldConstants.IMAGE_ZOOM);
			 param.setAttribute(ImageParam.IMAGE_ZOOM, zoom);
		 }
		 
		 //TODO ADD conversion from other element types to IParam
		 
		 if (param != null) {
			 field.addParam(param);
		 }
		 
		 return field;
	}
	
	/**
	 * Convenience method for copying the value and default value from
	 * an attribute Map to a Parameter.
	 * Used to convert from old (Beta 3.0) XML elements to Beta 4 parameter
	 * objects. 
	 * 
	 * @param attributes	The attribute map
	 * @param param			The parameter object. 
	 */
	private static void setValueAndDefault(Map<String,String> attributes, 
			IAttributes param) {
		
		String value = attributes.get(DataFieldConstants.VALUE);
		String defaultValue = attributes.get(DataFieldConstants.DEFAULT);
		
		param.setAttribute(SingleParam.PARAM_VALUE, value);
		param.setAttribute(SingleParam.DEFAULT_VALUE, defaultValue);
	}

	public static void saveTreeToFile(File file, TreeModel treeModel) {
		
	}

	/**
	 * Creates a TreeModel from a Beta-3.0 OMERO.editor XML document.
	 * The tree model contains one XML element per node. 
	 * Each node/field is created from an XML element using the 
	 * createField(Element) method. 
	 * 
	 * @param xmlFile	The XML file to convert.
	 * @return			A TreeModel, containing 
	 */
	public static TreeModel getTree(File xmlFile) {
		
		Document document = null;
		
		try {
			document = XMLMethods.readXMLtoDOM(xmlFile); // overwrites document
		} catch (ParsingException e) {
			
			// show error and give user a chance to submit error
			EditorAgent.getRegistry().getUserNotifier().notifyError(
					"File failed to open.",
					"XML was not read correctly. XML may be 'badly-formed'", e);
					
			
			e.printStackTrace();
			return null;
		}	
		
		Element rootElement = document.getDocumentElement();
		
		IField rootField = createField(rootElement);
		DefaultMutableTreeNode rootNode = new FieldNode(rootField);
		
		/*
		 * This is a recursive method that iterates through the whole tree.
		 */
		buildTreeFromDOM(rootElement, rootNode);
		
		return new DefaultTreeModel(rootNode);
	}
	
	/**
	 * Convenience method for converting html-formatted strings to tag-free
	 * strings.
	 * Beta-3.0 used HTML for formatting the text of Field name, and
	 * description.
	 * Beta-4.0 does not support HTML formatting of these (or any other)
	 * attributes. So, these tags must be removed when importing 
	 * Beta-3.0 XML documents. 
	 * 
	 * @param withTags		A string containing br, u and b tags. 
	 * @return			The same string, without the br, u and b tags. 
	 */
	public static String removeHtmlTags(String withTags) 
	{
		if (withTags == null) return null;
		
		String noTags = withTags.replace("<br>", "\n");
		noTags = noTags.replace("<br />", "");
		noTags = noTags.replace("<u>", "");
		noTags = noTags.replace("</u>", "");
		noTags = noTags.replace("<b>", "");
		noTags = noTags.replace("</b>", "");
		
		return noTags;
	}

}
