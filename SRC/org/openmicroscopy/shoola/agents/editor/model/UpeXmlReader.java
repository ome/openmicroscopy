 /*
 * org.openmicroscopy.shoola.agents.editor.model.UpeXmlReader 
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

//Third-party libraries

import net.n3.nanoxml.IXMLElement;

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.params.BooleanParam;
import org.openmicroscopy.shoola.agents.editor.model.params.DateTimeParam;
import org.openmicroscopy.shoola.agents.editor.model.params.EnumParam;
import org.openmicroscopy.shoola.agents.editor.model.params.FieldParamsFactory;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.NumberParam;
import org.openmicroscopy.shoola.agents.editor.model.params.SingleParam;

/** 
 * This class is used for reading 'UPE' Universal Protocol Exchange XML files,
 * and building a treeModel of 'Fields' and 'Parameters'.
 * It reads most details that iLAP saves to 'UPE' 
 * (except parameter description, parameter necessity, parameter deleteable).
 * Also ignores 'STEP_GROUP' vv 'SPLIT_STEP' differences.
 * It also reads some OMERO.editor specific info: putting the parameters
 * in context with descriptions. 
 * 
 * @see #getTreeUPE(File)
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class UpeXmlReader {

	/**
	 * A handy method for getting the content of a child XML element. 
	 * This is used for reading data from UPE XML elements, where most of the
	 * attributes of a 'step' or 'parameter' element are stored as text
	 * content of children. 
	 * 		
	 * @param parent			The parent element
	 * @param childName		The name of the child you want the text content of. 
	 * @return
	 */
	private static String getChildContent(IXMLElement parent, String childName) 
	{
		IXMLElement child = parent.getFirstChildNamed(childName);
		if (child == null) return null;
		return child.getContent();
	}

	/**
	 * This copies the name, value and default value of a 'parameter' element 
	 * (from UPE XML file) to a {@link IAttributes} parameter
	 * This is a convenience method, used after the creation of a parameter,
	 * since these attributes are common to several parameter types. 
	 * 
	 * @param upeParam		The 'parameter' XML element, source of data
	 * @param param			The new parameter object. Copies values to here. 
	 */
	private static void setNameValueDefault(IXMLElement upeParam, 
														IAttributes param) 
	{
		String attributeValue;
		attributeValue = getChildContent(upeParam, "name");
		param.setAttribute(SingleParam.PARAM_NAME, attributeValue);
		attributeValue = getChildContent(upeParam, "value");
		param.setAttribute(SingleParam.PARAM_VALUE, attributeValue);
		attributeValue = getChildContent(upeParam, "default-value");
		param.setAttribute(SingleParam.DEFAULT_VALUE, attributeValue);
	}

	/**
	 * This creates a {@link IParam} instance from a 'parameter' element of 
	 * the UPE XML file. 
	 * 
	 * @param upeParam		The 'parameter' element of the UPE XML file
	 * @return				A new {@link IParam} parameter.
	 */
	private static IParam getParameter(IXMLElement upeParam) 
	{
		String attributeValue;
		
		// need to have a param-type
		attributeValue = getChildContent(upeParam, "param-type");
		if (attributeValue == null) 	return null;
		
		IParam param;
		if ("ENUMERATION".equals(attributeValue)) {
			param = FieldParamsFactory.getFieldParam(EnumParam.ENUM_PARAM);
			setNameValueDefault(upeParam, param);
			// enumerations
			String enumOptions = "";
			IXMLElement enumList = upeParam.getFirstChildNamed("enum-list");
			List<IXMLElement> enums = enumList.getChildrenNamed("enum");
			for (IXMLElement e : enums) {
				if (enumOptions.length() > 0)  enumOptions = enumOptions + ", ";
				enumOptions = enumOptions + e.getContent();
			}
			if (enums.size() > 0)
				param.setAttribute(EnumParam.ENUM_OPTIONS, enumOptions);
			
		} else  
		if ("NUMERIC".equals(attributeValue)) {
			param = FieldParamsFactory.getFieldParam(NumberParam.NUMBER_PARAM);
			setNameValueDefault(upeParam, param);
			// units
			attributeValue = getChildContent(upeParam, "unit");
			param.setAttribute(NumberParam.PARAM_UNITS, attributeValue);
			
		} else 
		if ("BOOLEAN".equals(attributeValue)) {
			param = FieldParamsFactory.getFieldParam(BooleanParam.BOOLEAN_PARAM);
			setNameValueDefault(upeParam, param);
		}
		else
		if ("DATE-TIME".equals(attributeValue)) {
			param = FieldParamsFactory.getFieldParam(DateTimeParam.DATE_TIME_PARAM);
			attributeValue = getChildContent(upeParam, "name");
			param.setAttribute(SingleParam.PARAM_NAME, attributeValue);
			attributeValue = getChildContent(upeParam, DateTimeParam.DATE_ATTRIBUTE);
			param.setAttribute(DateTimeParam.DATE_ATTRIBUTE, attributeValue);
		}
		else
		 //if ("TEXT".equals(attributeValue)) {
		// at least make a text Param. 
		{
			param = FieldParamsFactory.getFieldParam(SingleParam.TEXT_LINE_PARAM);
			setNameValueDefault(upeParam, param);
		}
		
		return param;
		
	}

	/**
	 * This creates a {@link IField} from a 'step' element of the UPE 
	 * (Universal Protocol Exchange) XML file. 
	 * Processes name, description and parameters.
	 * 
	 * @param upeStep
	 * @return
	 */
	private static IField upeCreateField(IXMLElement upeStep) {
		
		// Create a new field...
		Field field = new Field();
		
		String attributeValue;
		
		// ...and set it's attributes (could be null, but shouldn't)
		attributeValue = getChildContent(upeStep, "name");
		field.setAttribute(Field.FIELD_NAME, attributeValue);
		
		// if the step has a 'description' add this to field content
		// NB if UPE was created by Editor, step description not used. 
		attributeValue = getChildContent(upeStep, "description");
		if (attributeValue != null)
			field.addContent(new TextContent(attributeValue));
		
		IXMLElement stepAttribute = upeStep.getFirstChildNamed("parameters");
		// if no parameters, return field.
		if (stepAttribute == null) return field;
		
		// add parameters
		List<IXMLElement> params = stepAttribute.getChildren();
		for (IXMLElement param : params) {
			if ("parameter".equals(param.getName())) {
				field.addContent(getParameter(param));
			} else 
				
			// there won't be any 'description' elements in strict UPE, but
				// the OMERO.editor uses them to put parameters in context
			if ("description".equals(param.getName())) {
				String description = param.getContent();
				field.addContent(new TextContent(description));
			}
		}
		
		return field;
	}

	/**
	 * This method gets the value of the "path" attribute of a step element
	 * in the 'UPE' file format. This is held in a child "path" element. 
	 * Null is returned if this element is not found.
	 * 
	 * @param upeStep		The 'step' element
	 * @return				The text content of the 'path' child element
	 */
	private static String upeGetPath(IXMLElement upeStep) {
		IXMLElement stepAttribute = upeStep.getFirstChildNamed("path");
		if (stepAttribute != null) {
			return stepAttribute.getContent();
		} 
		return null;
	}

	/**
	 * A handy method for processing the path as a string, to return the 
	 * parent:
	 * This is used for processing the 'UPE' paths:
	 * Eg If path = "001/002/002" then parent path = "001/002"
	 * 
	 * @param path		The path, as a string. eg. "001/002/002"
	 * @return			The 'parent', ie, without the last /etc
	 */
	private static String getParentPath(String path) 
	{
		String[] stepPath = path.split("/");
		if (stepPath.length <2 ) return "root";
		
		String parentPath = "";
		for (int i=0; i<stepPath.length-1; i++) {
			if (i >0)  parentPath = parentPath + "/";
			parentPath = parentPath + stepPath[i];
		}
		return parentPath;
	}

	/**
	 * Builds an OMERO.editor treeModel, of Fields, Parameters etc, based on
	 * a UPE (Universal Protocol Exchange) format XML file, rooted at
	 * the <code>root</code> element. 
	 * 
	 * @param xHtmlFile
	 * @return
	 */
	static TreeModel getTreeUPE(IXMLElement root) {
		
		// parse the top elements...
		IXMLElement protocol = root.getFirstChildNamed("protocol");
		IXMLElement protocolInfo = protocol.
									getFirstChildNamed("protocol-information");
		
		// create a protocol root field and add name, description
		IField rootField = new Field();
		String protName = getChildContent(protocolInfo, "name");
		rootField.setAttribute(Field.FIELD_NAME, protName);
		protName = getChildContent(protocolInfo, "description");
		rootField.addContent(new TextContent(protName));
		
		// A map to hold new fields, according to their path, so new children
		// can be added to their previously created parents, id's by path
		Map<String, DefaultMutableTreeNode> fieldMap = new HashMap
											<String, DefaultMutableTreeNode>();
		// place new Field in a node, and add it to the map
		DefaultMutableTreeNode rootNode = new FieldNode(rootField);
		fieldMap.put("root", rootNode);
		
		// process the steps of this protocol, creating a field for each
		IXMLElement steps = protocol.getFirstChildNamed("steps");
		List<IXMLElement> stepList = steps.getChildren();
		
		IField field;
		DefaultMutableTreeNode treeNode;
		DefaultMutableTreeNode parentNode;
		String path;
		String parentPath;
		for (IXMLElement step : stepList) {
			field = upeCreateField(step);
			treeNode = new FieldNode(field);
			path = upeGetPath(step);
			fieldMap.put(path, treeNode);		// add new node to the map. 
			
			// identify the node's parent, using path. Get it from the map...
			parentPath = getParentPath(path);
			parentNode = fieldMap.get(parentPath);
			// add the node as child. 
			// assumes that child nodes are processed in order (first -> last)
			parentNode.add(treeNode);		 
		}
		
		return new DefaultTreeModel(rootNode);
	}

}
