 /*
 * org.openmicroscopy.shoola.agents.editor.model.PROimport 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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

import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.BooleanParam;
import org.openmicroscopy.shoola.agents.editor.model.params.DateTimeParam;
import org.openmicroscopy.shoola.agents.editor.model.params.EnumParam;
import org.openmicroscopy.shoola.agents.editor.model.params.FieldParamsFactory;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.ImageParam;
import org.openmicroscopy.shoola.agents.editor.model.params.LinkParam;
import org.openmicroscopy.shoola.agents.editor.model.params.NumberParam;
import org.openmicroscopy.shoola.agents.editor.model.params.OntologyTermParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TimeParam;
import org.openmicroscopy.shoola.agents.editor.model.tables.TableModelFactory;
import org.openmicroscopy.shoola.agents.editor.util.Ontologies;

import net.n3.nanoxml.IXMLElement;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This class is used to read an XML document from Beta-3 Editor (.pro.xml).
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class PROimport {

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
	 * @param inputElement		XML element	
	 * @param treeNode			A treeNode.
	 */
	private static void buildTreeB3(IXMLElement inputElement, 
			DefaultMutableTreeNode treeNode)
	{
		
		List<IXMLElement> children = inputElement.getChildren();
		
		IField newField;
		DefaultMutableTreeNode child;
		for (IXMLElement node : children) {
			
			if (node != null) {
				 newField = createField(node);
				 child = new FieldNode(newField);
				 
				 treeNode.add(child);
				 buildTreeB3(node, child);
			 }
		}
		
	}

	/**
	 * To represent 'foreign' XML element in the tree model, a field is created 
	 * that has a single parameter containing all the field attributes and text
	 * Content of the element. The field name becomes the tag name of the element. 
	 * 
	 * @param element
	 * @return
	 */
	private static IField createXMLField (IXMLElement element) {
		
		 // First, make a Map of the element attributes.
		 Map<String, String> allAttributes = getAttributeMap(element);
		 
		 String fieldName = element.getFullName();
		 
		// Create a new field and set it's attributes.
		 Field field = new Field();
		 field.setAttribute(Field.FIELD_NAME, fieldName);
		 
		 // Create a "Custom XML" content, to hold all other attributes,
		 XMLFieldContent content = new XMLFieldContent(allAttributes);
		 
		 // and text content, if any exists. 
		 String textContent = element.getContent();
		 if ((textContent != null) && (textContent.trim().length() > 0)) {
			 content.setTextContent(textContent);
		 }
		 
		 // add the XML content to the field
		 field.addContent(content);
		 
		 return field;
	}
	
	
	/**
	 * A convenience method for getting the attributes of an XML element in 
	 * a Map, where they can be easily queried etc. 
	 * 
	 * @param element		The XML element
	 * @return				A Map of the name, value attribute pairs. 
	 */
	private static Map<String, String> getAttributeMap (IXMLElement element) {
		
		 Map<String, String> allAttributes = new HashMap<String, String>();
		 
		 String attribute;
		 String attributeValue;
		 
		 Enumeration<Object> atts = element.getAttributes().keys();
		 
		 while (atts.hasMoreElements()) {
			 attribute = atts.nextElement().toString();
			 attributeValue = element.getAttribute(attribute, null);
			 if (attributeValue != null) {
				 allAttributes.put(attribute, attributeValue);
			 }
		 }
		 
		 return allAttributes;
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
	private static IField createField (IXMLElement element) {
		 
		 // First, make a Map of the element attributes. Easier to query
		 Map<String, String> allAttributes = getAttributeMap(element);
		 
		 // Need to determine whether this is "Editor" XML or foreign/custom XML
		 
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
			 String elementName = element.getFullName();
			 if (DataFieldConstants.isInputTypeRecognised(elementName))
				 paramType = elementName;
			 else 
				 // otherwise, this is a custom XML element... 
				 return PROimport.createXMLField(element);
		 }
		 
		 
		 // Get values for the Name, Description and Url...
		 String fieldName = allAttributes.get(DataFieldConstants.ELEMENT_NAME);
		 fieldName = removeHtmlTags(fieldName);
		 String description = allAttributes.get(DataFieldConstants.DESCRIPTION);
		 description = removeHtmlTags(description);
		 String url = allAttributes.get(DataFieldConstants.URL);
		 
		 String colour = allAttributes.get(DataFieldConstants.BACKGROUND_COLOUR);
		 
		 // Create a new field and set it's attributes.
		 Field field = new Field();
		 
		 field.setAttribute(Field.FIELD_NAME, fieldName);
		 field.setAttribute(Field.BACKGROUND_COLOUR, colour);
		 
		 field.addContent(new TextContent(description));
		 
		 
		 IParam param;
		 // if there was a url set, add this as a link parameter. 
		 if (url != null) {
			 param = getFieldParam(LinkParam.LINK_PARAM);
			 param.setAttribute(LinkParam.URL_LINK, url);
			 field.addContent(param);
		 }
		 
		 // parameter that represents the field 
		 // if field contains tabular data, add a parameter for each column...
		 if (paramType.equals(DataFieldConstants.TABLE)) {
			 
			// fill columns
			 String colData = allAttributes.get(
					 DataFieldConstants.TABLE_COLUMN_NAMES);
			 String[] colNames = colData.split(",");
			 String paramName;
			 for (int c=0; c<colNames.length; c++) {
				 paramName = colNames[c].trim();
				 param = getFieldParam(TextParam.TEXT_LINE_PARAM);
				 param.setAttribute(AbstractParam.PARAM_NAME, paramName);
				 field.addContent(param);
			 }
			 field.setTableData(TableModelFactory.getFieldTable(field));
			 TableModel tabularData = field.getTableData();
			 
			// fill row data
			 int row = 0;
			 String[] cellData;
			 String rowDataString = allAttributes.get(
					 DataFieldConstants.ROW_DATA_NUMBER + row);
			 // row data exists for this row.
			 while (rowDataString != null) {
				 cellData = rowDataString.split(",");
				 // fill the cells. Makes more rows as needed. 
				 for (int c=0; c<cellData.length; c++) {
					 tabularData.setValueAt(cellData[c].trim(), row, c);
				 }
				 // get the next row
				 row++;
				 rowDataString = allAttributes.get(
						 DataFieldConstants.ROW_DATA_NUMBER + row);
			 }
			 
		 } else {
			 // all other field types can be converted to a single parameter.
			 param = getParameter(paramType, allAttributes);
			 if (param != null) {
				 field.addContent(param);
			 }
		 }
		 
		 return field;
	}
	
	/**
	 * Creates a single parameter object from an attribute map, according
	 * to the parameter type given.
	 * This method should be used for reading Beta-3.0 version XML, where 
	 * single parameter is defined per XML element / attribute map.
	 *  
	 * @param paramType			The type of parameter to create
	 * @param allAttributes		The attribute map with parameter attributes
	 * @return					A new parameter object 
	 */
	private static IParam getParameter(String paramType,
			Map<String, String> allAttributes) {
		
		// Field will have 0 or 1 "parameters", depending on type
		 IParam param = null;
		 
		 if (paramType.equals(DataFieldConstants.TEXT_ENTRY_STEP)) {
			 param = getFieldParam(TextParam.TEXT_LINE_PARAM);
			 setValueAndDefault(allAttributes, param);
		 } 
		 else if (paramType.equals(DataFieldConstants.MEMO_ENTRY_STEP)) {
			 param = getFieldParam(TextParam.TEXT_BOX_PARAM);
			 setValueAndDefault(allAttributes, param);
		 } 
		 else if (paramType.equals(DataFieldConstants.NUMBER_ENTRY_STEP)) {
			 param = getFieldParam(NumberParam.NUMBER_PARAM);
			 setValueAndDefault(allAttributes, param);
			 String units = allAttributes.get(DataFieldConstants.UNITS);
			 param.setAttribute(NumberParam.PARAM_UNITS, units);
		 } 
		 else if (paramType.equals(DataFieldConstants.DROPDOWN_MENU_STEP)) {
			 param = getFieldParam(EnumParam.ENUM_PARAM);
			 setValueAndDefault(allAttributes, param);
			 String ddOptions = allAttributes.get(
					 DataFieldConstants.DROPDOWN_OPTIONS);
			 param.setAttribute(EnumParam.ENUM_OPTIONS, ddOptions);
		 }
		 else if (paramType.equals(DataFieldConstants.CHECKBOX_STEP)) {
			 param = getFieldParam(BooleanParam.BOOLEAN_PARAM);
			 setValueAndDefault(allAttributes, param);
		 } 
		 else if (paramType.equals(DataFieldConstants.TIME_FIELD)) {
			 param = getFieldParam(TimeParam.TIME_PARAM);
			 // old (pre 7th March 08) use the old value "hh:mm:ss" and default
			 setValueAndDefault(allAttributes, param);
			 // newer XML uses SECONDS attribute for timeInSecs. 
			 String secs = allAttributes.get(DataFieldConstants.SECONDS);
			 param.setAttribute(TimeParam.SECONDS, secs);
			 
		 } 
		 else if (paramType.equals(DataFieldConstants.DATE_TIME_FIELD)) {
			
			// if absolute date, use UTC millisecs of Date-Time parameter...
			 String millisecs = allAttributes.get(DataFieldConstants.UTC_MILLISECS);
			 if (millisecs != null) {
				// create a test calendar (see below).
				Calendar testForAbsoluteDate = new GregorianCalendar();
				testForAbsoluteDate.setTimeInMillis(new Long(millisecs));
				int year = testForAbsoluteDate.get(Calendar.YEAR);
				if (year != 1970) {		// date is not "relative"
					param = getFieldParam(DateTimeParam.DATE_TIME_PARAM);
					long utcMillis = new Long(millisecs);
					
					millisecs = allAttributes.get(DataFieldConstants.SECONDS);
					if (millisecs != null) {
						utcMillis += (new Long(millisecs)* 1000);
					}
					param.setValueAt(0, utcMillis + "");
					
				} else {		// date is relative.
					// store relative date as number parameter (days)
					param = getFieldParam(NumberParam.NUMBER_PARAM);
					int days = testForAbsoluteDate.get(Calendar.DAY_OF_MONTH);
					param.setValueAt(0, days + "");
					param.setAttribute(NumberParam.PARAM_UNITS, "days");
				}
			 }
		 } 
		 else if (paramType.equals(DataFieldConstants.LINK_FIELD)){
			 param = getFieldParam(LinkParam.LINK_PARAM);
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
			 param = getFieldParam(ImageParam.IMAGE_PARAM);
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
		 else if (paramType.equals(DataFieldConstants.OLS_FIELD)){
			 param = getFieldParam(OntologyTermParam.ONTOLOGY_TERM_PARAM);
			 String ontologyTermName = allAttributes.get
			 							(DataFieldConstants.ONTOLOGY_TERM_ID);
			 String ontologyId = Ontologies.getOntologyIdFromB3
			 												(ontologyTermName);
			 String termId = Ontologies.getTermIdFromB3(ontologyTermName);
			 String termName = Ontologies.getTermNameFromB3(ontologyTermName);
			 
			 param.setAttribute(OntologyTermParam.ONTOLOGY_ID, ontologyId);
			 param.setAttribute(OntologyTermParam.TERM_ID, termId);
			 param.setAttribute(OntologyTermParam.TERM_NAME, termName);
		 }
		 
		 return param;
	}
	
	private static IParam getFieldParam(String paramType) 
	{
		return FieldParamsFactory.getFieldParam(paramType);
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
			IParam param) {
		
		String value = attributes.get(DataFieldConstants.VALUE);
		String defaultValue = attributes.get(DataFieldConstants.DEFAULT);
		
		param.setValueAt(0, value);
		param.setAttribute(TextParam.DEFAULT_VALUE, defaultValue);
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
	private static String removeHtmlTags(String withTags) 
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
	
	/**
	 * Creates a TreeModel from a Beta-3.0 OMERO.editor XML document.
	 * The tree model contains one XML element per node. 
	 * Each node/field is created from an XML element using the 
	 * {@link #createField(IXMLElement)} method. 
	 * 
	 * @param xmlFile	The Beta-3.0 XML file to convert.
	 * @return			A TreeModel, containing 
	 */
	public static TreeModel createTreeModel(IXMLElement root) {
		
		IField rootField = createField(root);
		DefaultMutableTreeNode rootNode = new FieldNode(rootField);
		
		/*
		 * This is a recursive method that iterates through the whole tree.
		 */
		PROimport.buildTreeB3(root, rootNode);
		
		return new DefaultTreeModel(rootNode);
	}

}
