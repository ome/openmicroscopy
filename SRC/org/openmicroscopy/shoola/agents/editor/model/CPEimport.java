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
import java.util.ArrayList;
import java.util.List;

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
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
import org.openmicroscopy.shoola.agents.editor.model.tables.TableModelFactory;
import org.openmicroscopy.shoola.util.ui.omeeditpane.OMEWikiConstants;
import org.openmicroscopy.shoola.util.ui.omeeditpane.Position;
import org.openmicroscopy.shoola.util.ui.omeeditpane.WikiView;

/** 
 * This class is used for reading 'CPE' Common Protocol Exchange XML files,
 * and building a treeModel of 'Fields'/'Steps' and 'Parameters'.
 * It should read all details that iLAP saves to 'CPE'.
 * 
 * @see #createTreeModel(File)
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CPEimport {
	
	/**  The name of the element used to hold the top level of steps */
	public static final String 			STEPS = "steps";
	
	/**  The name of the element used to hold nested level of steps */
	public static final String 			STEP_CHILDREN = "step-children";
	
	/**  The name of the element used to define a protocol step */
	public static final String 			STEP = "step";
	
	/**
	 * The name of the element used to store the name of parameter and
	 * step elements in cpe.xml files. 
	 */
	public static final String 			NAME = "name";
	
	/**  The name of the element used to hold the description of step 
	 * or parameter */
	public static final String 			DESCRIPTION = "description";
	
	/**  The name of the element used to hold the values of parameters */
	public static final String 			DATA = "data";
	
	/**  The name of the element used to store the value of parameters */
	public static final String 			VALUE = "value";
	
	/** The name of the element used to store the default value of parameters */
	public static final String 			DEFAULT = "default-value";
	
	/**  The name of the element used to store the 'type' of a parameter */
	public static final String 			PARAM_TYPE = "param-type";
	
	/** 
	 * The opening 'tag' used to identify a parameter ID in the exported 
	 * description for a step. 
	 */
	public static final String 			ID_START =	"[[";
	
	/** 
	 * The closing 'tag' used to identify a parameter ID in the exported 
	 * description for a step. 
	 */
	public static final String 			ID_END =	"]]";
	
	/**  The name of the element used to store the id of a parameter */
	public static final String 			ID =	"id";
	
	/**  The name of the element used to contain the list of parameter elements */
	public static final String 			PARAM_LIST = "parameter-list";
	
	/**  
	 * The name of the element used contain a list of parameter elements 
	 * when they contain data that can be represented in a table, where
	 * each parameter is used to define a column. 
	 */
	public static final String 			PARAM_TABLE = "parameter-table";
	
	/**  The name of the element used to define a parameter */
	public static final String 			PARAMETER = "parameter";
	
	/**  
	 * The name of the element used to store the list of enumerations for 
	 * enumeration parameters
	 */
	public static final String 			ENUM_LIST =	"enum-list";
	
	/**  The name of the element used to store each enumeration in the 
	 * enumerations list */
	public static final String 			ENUM =	"enum";
	
	/**  The name of the element used to store the units of a parameter */
	public static final String 			UNITS =	"units";
	
	/**  The name of the element used to store the 'necessity' of a parameter */
	public static final String 			NECESSITY =	"necessity";

	/**
	 * A handy method for getting the content of a child XML element. 
	 * This is used for reading data from CPE XML elements, where most of the
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
	 * (from CPE XML file) to a {@link IAttributes} parameter
	 * This is a convenience method, used after the creation of a parameter,
	 * since these attributes are common to several parameter types. 
	 * 
	 * @param cpeParam		The 'parameter' XML element, source of data
	 * @param param			The new parameter object. Copies values to here. 
	 */
	private static void setNameValueDefault(IXMLElement cpeParam, 
														IParam param) 
	{
		setName(cpeParam, param);
		String attributeValue;
		
		IXMLElement data = cpeParam.getFirstChildNamed(DATA);
		if (data != null) {
			List<IXMLElement> values = data.getChildrenNamed(VALUE);
			String value;
			int v = 0;
			for (IXMLElement element : values) {
				value = element.getContent();
				param.setValueAt(v++, value);
			}
		}
		attributeValue = getChildContent(cpeParam, DEFAULT);
		param.setAttribute(TextParam.DEFAULT_VALUE, attributeValue);
	}
	
	/**
	 * This copies the name of a 'parameter' element 
	 * (from CPE XML file) to a {@link IAttributes} parameter
	 * This is a convenience method, used after the creation of a parameter,
	 * since these attributes are common to several parameter types. 
	 * 
	 * @param cpeParam		The 'parameter' XML element, source of data
	 * @param param			The new parameter object. Copies values to here. 
	 */
	private static void setName(IXMLElement cpeParam, IAttributes param) 
	{
		String attributeValue;
		attributeValue = getChildContent(cpeParam, NAME);
		param.setAttribute(TextParam.PARAM_NAME, attributeValue);
	}

	/**
	 * This creates a {@link IParam} instance from a 'parameter' element of 
	 * the CPE XML file. 
	 * It reads elements that are standard 'CPE' (enumeration, number, text,
	 * date). 
	 * 
	 * @param cpeParam		The 'parameter' element of the CPE XML file
	 * @return				A new {@link IParam} parameter.
	 */
	private static IParam getParameter(IXMLElement cpeParam) 
	{
		String attributeValue;
		
		// need to have a param-type
		attributeValue = getChildContent(cpeParam, PARAM_TYPE);
		if (attributeValue == null) 	return null;
		
		IParam param;
		if (EnumParam.ENUM_PARAM.equals(attributeValue)) {
			IXMLElement enumList = cpeParam.getFirstChildNamed(ENUM_LIST);
			List<IXMLElement> enums = enumList.getChildrenNamed(ENUM);
			// if enumeration options are "true" and "false", need a boolean...
			if (enumsAreBoolean(enums)) {
				param = FieldParamsFactory.getFieldParam(BooleanParam.BOOLEAN_PARAM);
				setNameValueDefault(cpeParam, param);
			} else {
				param = FieldParamsFactory.getFieldParam(EnumParam.ENUM_PARAM);
				setNameValueDefault(cpeParam, param);
				// enumerations
				String enumOptions = "";
				for (IXMLElement e : enums) {
					if (enumOptions.length() > 0)  enumOptions = enumOptions + ", ";
					enumOptions = enumOptions + e.getContent();
				}
				if (enums.size() > 0) {
					param.setAttribute(EnumParam.ENUM_OPTIONS, enumOptions);
				}
				// units
				attributeValue = getChildContent(cpeParam, UNITS);
				param.setAttribute(NumberParam.PARAM_UNITS, attributeValue);
			}
		} else  
		if (NumberParam.NUMBER_PARAM.equals(attributeValue)) {
			param = FieldParamsFactory.getFieldParam(NumberParam.NUMBER_PARAM);
			setNameValueDefault(cpeParam, param);
			// units
			attributeValue = getChildContent(cpeParam, UNITS);
			param.setAttribute(NumberParam.PARAM_UNITS, attributeValue);
		} 
		else
		if (TextParam.TEXT_LINE_PARAM.equals(attributeValue)) {
			param = FieldParamsFactory.getFieldParam(TextParam.TEXT_LINE_PARAM);
			setNameValueDefault(cpeParam, param);
		}
		else
		if (DateTimeParam.DATE_TIME_PARAM.equals(attributeValue)) {
			param = FieldParamsFactory.getFieldParam(DateTimeParam.DATE_TIME_PARAM);
			setNameValueDefault(cpeParam, param);
		}
		
		else {
			System.err.println("UpeXmlReader getParameter() param-type not recognised.");
			param = FieldParamsFactory.getFieldParam(attributeValue);
			if (param == null) {	
				// if paramType not recognised, return text text parameter
				param = FieldParamsFactory.getFieldParam(TextParam.TEXT_LINE_PARAM);
				setNameValueDefault(cpeParam, param);
				return param;
			}
			setName(cpeParam, param);
			/*
			// all parameter attributes are saved as attributes
			String[] paramAts = param.getParamAttributes();
			String attValue;
			for (int a=0; a<paramAts.length; a++){
				attValue = getChildContent(upeParam, paramAts[a]);
				param.setAttribute(paramAts[a], attValue);
			}
			*/
		}
		
		
		IXMLElement data = cpeParam.getFirstChildNamed("data");
		if (data != null) {
			int index = 0;
			List <IXMLElement> values = data.getChildrenNamed(VALUE);
			for (IXMLElement element : values) {
				param.setValueAt(index++, element.getContent());
			}
		}
		
		return param;
	}
	
	/**
	 * Convenience method for checking whether a list of {@link IXMLElement}
	 * elements has element content of "true" and "false" only. 
	 * Used to check whether an Enumeration parameter (in cpe.xml file) is 
	 * being used to store boolean data.  
	 * 
	 * @param enums		List of elements.
	 * @return			True is the list is 2, and contains "true" and "false"
	 */
	private static boolean enumsAreBoolean(List<IXMLElement> enums)
	{
		if (enums == null)		return false;
		if (enums.size() != 2)	return false;
		
		String option1 = enums.get(0).getContent();
		String option2 = enums.get(1).getContent();
		
		if ("true".equals(option1) && "false".equals(option2)) return true;
		if ("false".equals(option1) && "true".equals(option2)) return true;
		
		return false;
		
	}
	
	/**
	 * This is a recursive method that creates a Tree from the 
	 * XML element <code>cpeStep</code>. This step should contain any 
	 * child steps within a "step-children" element. 
	 * This method returns a {@link DefaultMutableTreeNode} that is the root
	 * of the new tree.
	 * 
	 * @param cpeStep	The root of the XML tree structure. 
	 * @return			see above. 
	 */
	private static DefaultMutableTreeNode buildStepTree(IXMLElement cpeStep) {
		
		IField field = createField(cpeStep);
		DefaultMutableTreeNode fieldNode = new FieldNode(field);
		
		IXMLElement childSteps = cpeStep.getFirstChildNamed("step-children");
		if (childSteps != null) {
			List<IXMLElement> children = childSteps.getChildrenNamed("step");
			DefaultMutableTreeNode node;
			for (IXMLElement child : children) {
				node = buildStepTree(child);	// recursively build tree
				fieldNode.add(node);
			}
		}
		return fieldNode;
	}

	/**
	 * This creates a {@link IField} from a 'step' element of the CPE 
	 * (Universal Protocol Exchange) XML file. 
	 * Processes name, description and parameters.
	 * 
	 * @param cpeStep
	 * @return
	 */
	private static IField createField(IXMLElement cpeStep) {
		
		// Create a new field...
		Field field = new Field();
		
		String description;
		
		// ...and set it's attributes (could be null, but shouldn't)
		description = getChildContent(cpeStep, NAME);
		field.setAttribute(Field.FIELD_NAME, description);
		
		// description may contain references to parameters, using 
		// paramId in context.
		// These will be parameters of the current step. 
		description = getChildContent(cpeStep, DESCRIPTION);
		
		List<IXMLElement> allParams = new ArrayList<IXMLElement>();
		
		IXMLElement params = cpeStep.getFirstChildNamed(PARAM_LIST);
		if (params != null) {
			allParams.addAll(params.getChildrenNamed(PARAMETER));
		}
		
		params = cpeStep.getFirstChildNamed(PARAM_TABLE);
		if (params != null) {
			allParams.addAll(params.getChildrenNamed(PARAMETER));
			field.setTableData(TableModelFactory.getFieldTable(field));
		}
		
		if (allParams.isEmpty()) {
			// if no parameters, description can simply be added as text content
			if (description != null) {
				field.addContent(new TextContent(description));
			}
		}
		else 
		if (description != null) {
			// need regex to identify [[paramId]] within description
			String regex = OMEWikiConstants.WIKILINKREGEX;
			List<Position> positionList = new ArrayList<Position>();
			WikiView.findExpressions(description, regex, positionList);
			
			int currentPosition = 0;
			int start, end;
			String content;
			String paramId;
			IXMLElement param;
			IParam parameter;
			// process description...
			for (Position position : positionList) {
				start = position.getStart();
				end = position.getEnd(); 
				// take any text before the parameter, add as text content
				if (start > currentPosition) {
					content = description.substring(currentPosition, start);
					field.addContent(new TextContent(content));
				}
				// get the id of the parameter, get the param element,
				// create a new parameter and add it to the field/step
				paramId = description.substring(start+2, end-2);
				param = getElementById(allParams, paramId);
				if (param != null) {
					parameter = getParameter(param);
					field.addContent(parameter);
					allParams.remove(param);	// remember processed parameters
				} else {
					// id was not recognised. Simply add text so it's not lost
					content = description.substring(start, end);
					field.addContent(new TextContent(content));
				}
				currentPosition = end;
			}
			// process any remaining text. 
			if (currentPosition < description.length()) {
				content = description.substring(currentPosition, 
													description.length()-1);
				field.addContent(new TextContent(content));
			}
			
			// process any remaining parameters
			for (IXMLElement element : allParams) {
				parameter = getParameter(element);
				field.addContent(parameter);
			}
		}
		
		return field;
	}
	
	/**
	 * Convenience method for finding an {@link IXMLElement} element 
	 * by Id.  Elements in the list must have their Id stored in a child
	 * {@link #ID} element.
	 * 
	 * @param parent		Parent of children to search
	 * @param id			The id of a child element to return
	 * @return				First child element that has id
	 */
	private static IXMLElement getElementById(List<IXMLElement> elems, String id)
	{
		if(elems == null)		return null;
		
		String childId;
		for (IXMLElement element : elems) {
			childId = getChildContent(element, ID);
			if (id.equals(childId)) 
				return element;
		}
		return null;
	}

	/**
	 * Builds an OMERO.editor treeModel, of Fields, Parameters etc, based on
	 * a CPE (Common Protocol Exchange) format XML file, rooted at
	 * the <code>root</code> element. 
	 * 
	 * @param xHtmlFile
	 * @return
	 */
	static TreeModel createTreeModel(IXMLElement root) {
		
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
		
		// place new Field in a node
		DefaultMutableTreeNode rootNode = new FieldNode(rootField);
		
		
		// process the steps of this protocol, creating a field for each
		IXMLElement steps = protocol.getFirstChildNamed("steps");
		List<IXMLElement> stepList = steps.getChildren();
		
		DefaultMutableTreeNode treeNode;
		for (IXMLElement step : stepList) {
			treeNode = buildStepTree(step);
			rootNode.add(treeNode);
		}
		
		return new DefaultTreeModel(rootNode);
	}

}
