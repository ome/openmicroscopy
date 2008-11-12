 /*
 * org.openmicroscopy.shoola.agents.editor.model.UPEEditorExport 
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

//Third-party libraries

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.XMLElement;

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.BooleanParam;
import org.openmicroscopy.shoola.agents.editor.model.params.DateTimeParam;
import org.openmicroscopy.shoola.agents.editor.model.params.EnumParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.NumberParam;
import org.openmicroscopy.shoola.agents.editor.model.params.SingleParam;

/** 
 * This class Saves a version of the 'UPE' file format that is suitable for 
 * the OMERO.editor. Ie, it saves parameters in context with description
 * elements within a step. 
 * Also, it does not save a description element to the step.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class UPEEditorExport 
	extends UPEexport {

	protected void addStepDescription(IField field, IXMLElement step) {	}
	
	/**
	 * Overrides this method to save parameters in context with descriptions, 
	 * as required by the OMERO.editor data structure. 
	 * This is not currently supported by the 'UPE' file format, which is 
	 * produced by the superclass. 
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
			} else
			if (content instanceof TextContent) {
				parameter = new XMLElement("description");
				parameter.setContent(content.toString());
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
	 * will be TEXT. No other types supported as yet. 
	 * 
	 * @param param			The parameter object 
	 * @return				A new XML Element that defines the parameter
	 */
	protected IXMLElement createParamElement(IParam param) 
	{
		IXMLElement parameter = new XMLElement("parameter");
		
		// Add name, necessity, value and default-value, (nulls not added)
		String name = param.getAttribute(AbstractParam.PARAM_NAME);
		addChildContent(parameter, "name", name);
		
		addChildContent(parameter, "necessity", "OPTIONAL");
		
		String value = param.getAttribute(SingleParam.PARAM_VALUE);
		addChildContent(parameter, "value", value);
		
		String defaultValue = param.getAttribute(SingleParam.DEFAULT_VALUE);
		addChildContent(parameter, "default-value", defaultValue);
		
		// Depending on the type of parameter, set the param-type, 
		// and add any additional attributes. 
		if (param instanceof NumberParam) {
			addChildContent(parameter, "param-type", "NUMERIC");
			String units = param.getAttribute(NumberParam.PARAM_UNITS);
			if (units != null)
				addChildContent(parameter, "unit", units);
		} else 
		if (param instanceof EnumParam) {
			addChildContent(parameter, "param-type", "ENUMERATION");
			String enumOptions = param.getAttribute(EnumParam.ENUM_OPTIONS);
			if (enumOptions != null) {
				IXMLElement enumList = new XMLElement("enum-list");
				String[] options = enumOptions.split(",");
				for (int i=0; i<options.length; i++) {
					addChildContent(enumList, "enum", options[i].trim());
				}
				parameter.addChild(enumList);
			}
		} else
		if (param instanceof BooleanParam) {
			addChildContent(parameter, "param-type", "BOOLEAN");
		} else
		if (param instanceof DateTimeParam) {
			addChildContent(parameter, "param-type", "DATE-TIME");
			String UTCmillis = param.getAttribute(DateTimeParam.DATE_ATTRIBUTE);
			addChildContent(parameter, DateTimeParam.DATE_ATTRIBUTE, UTCmillis);
		}
		else {
			// default type is TEXT
			addChildContent(parameter, "param-type", "TEXT");
		}
		
		return parameter;
	}
}
