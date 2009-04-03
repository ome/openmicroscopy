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
package treeIO;


//Java imports

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//Third-party libraries

//Application-internal dependencies


import tree.DataField;
import tree.DataFieldConstants;
import treeModel.fields.DateTimeParam;
import treeModel.fields.Field;
import treeModel.fields.IAttributes;
import treeModel.fields.IField;
import treeModel.fields.IParam;
import treeModel.fields.ImageParam;
import treeModel.fields.LinkParam;
import treeModel.fields.MutableTableModel;
import treeModel.fields.SingleParam;
import treeModel.fields.TableParam;
import treeModel.fields.TimeParam;
import util.ExceptionHandler;
import util.XMLMethods;


/** 
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TreeModelFactory {
	
	public static TreeModel getTree(File xmlFile) {
		
		Document document = null;
		
		try {
			document = XMLMethods.readXMLtoDOM(xmlFile); // overwrites document
		} catch (SAXException e) {
			
			// show error and give user a chance to submit error
			ExceptionHandler.showErrorDialog("File failed to open.",
					"XML was not read correctly. XML may be 'badly-formed'", e);
			
			e.printStackTrace();
			return null;
		}	
		
		Element rootElement = document.getDocumentElement();
		
		IField rootField = createField(rootElement);
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootField);
		
		buildTreeFromDOM(rootElement, rootNode);
		
		return new DefaultTreeModel(rootNode);
	}
	
	
	private static void buildTreeFromDOM(Element inputElement, 
			DefaultMutableTreeNode treeNode) {
		
		NodeList children = inputElement.getChildNodes();
		
		for (int i=0; i < children.getLength(); i++) {
			
			// skip any empty (text) nodes
			Node node = children.item(i);
	
			 
			 if (node != null && (node.getNodeType() == Node.ELEMENT_NODE)) {
				 Element element = (Element)node; 
				 IField newField = createField(element);
				 
				 DefaultMutableTreeNode child = 
					 new DefaultMutableTreeNode(newField);
				 
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
	 * 
	 * @param element
	 * @return
	 */
	private static IField createField (Element element) {
		 
		 /*
		  * First, make a Map of the element attributes.
		  * Makes it easier to query (see below) without worrying about nulls.
		  */
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
		 
		 /*
		  * Get values for the Name, Description and Url...
		  */
		 String fieldName = allAttributes.get(DataFieldConstants.ELEMENT_NAME);
		 
		 /*
		  * if the xml file's elements don't have "elementName" attribute, 
		  * use the <tagName>
		  */
		 if (fieldName == null) {
			 fieldName = element.getNodeName();
		 }
		 
		 String description = allAttributes.get(DataFieldConstants.DESCRIPTION);
		 String url = allAttributes.get(DataFieldConstants.URL);
		 
		
		
		 /*
		  * Need to create a parameter object according to the type of 
		  * element 
		  */
		 
		// the 'old' version-1 xml used "inputType" attribute.
		 // if this attribute exists, need to convert it to the new type 
		 // eg. "Fixed Step" becomes "FixedStep"
		 // otherwise, need to use the NodeName as the inputType (as in the new version)
		 String paramType = allAttributes.get(DataFieldConstants.INPUT_TYPE);
		 
		 if (paramType != null) {
			 paramType = DataField.getNewInputTypeFromOldInputType(paramType);
		 } else {
			 /* 
			  * InputType is null: Therefore this is the newer xml version: 
			  * (used up until Beta 3.0)
			  * Use <NodeName> for inputType IF the inputType is recognised.
			  */
			 String elementName = element.getNodeName();
			 if (DataFieldConstants.isInputTypeRecognised(elementName))
				 paramType = elementName;
			 else 
				 paramType = DataFieldConstants.CUSTOM;
		 }
		 
		 /*
		  * Create a new field and set it's attributes.
		  */
		 IField field = new Field();
		 
		 field.setAttribute(Field.FIELD_NAME, fieldName);
		 field.setAttribute(Field.FIELD_DESCRIPTION, description);
		 field.setAttribute(Field.FIELD_URL, url);
		 
		 /*
		  * Field will have 0 or 1 "parameters", depending on type
		  */
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
	
	
	public static void setValueAndDefault(Map<String,String> attributes, 
			IAttributes param) {
		
		String value = attributes.get(DataFieldConstants.VALUE);
		String defaultValue = attributes.get(DataFieldConstants.DEFAULT);
		
		param.setAttribute(SingleParam.PARAM_VALUE, value);
		param.setAttribute(SingleParam.DEFAULT_VALUE, defaultValue);
	}

}
