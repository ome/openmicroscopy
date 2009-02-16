 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs
 * .EditingComponentFactory 
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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs;

//Java imports

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.ParamTemplateUIFactory;
import org.openmicroscopy.shoola.agents.editor.model.DataFieldConstants;
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

/** 
 * This is a factory that creates UI editing components for IParam objects.
 * Each field in the OMERO.editor may contain one (or more) "parameter" objects,
 * that model the experimental variables in that field. 
 * These variables, are made editable within appropriate UI components created
 * by this Factory, depending on the String getFieldType().
 * Eg, a text value will be edited by a TextField, a boolean parameter will be
 * edited by a checkBox etc. 
 * 
 * These components do not observe changes to the data in the Value objects, as
 * they have done in earlier versions of OMERO.editor. 
 * This is because the JTree in which they are displayed is refreshed on
 * each update, and these editing components are re-built with the new
 * data values. 
 * This occurs very frequently, even when changing the selection path of the 
 * tree. Adding these components as observers of Value objects each time they
 * are created would results in a large number of observers, with no easy 
 * way of de-referencing them? 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ParamUIFactory 
{
	/**
	 * Returns a UI component for editing the values of the parameter data 
	 * object. 
	 * NB: This UI component will not edit the "Template" (e.g. default values)
	 * of the parameter. To get a component for this, see
	 * {@link ParamTemplateUIFactory#getEditDefaultComponent(IParam)}
	 * 
	 * @param paramObject	The parameter data
	 * @return				A JComponent for editing the value of the data.
	 */
	public static AbstractParamEditor getEditingComponent(IParam paramObject) 
	{
		
		if (paramObject == null) {
			throw new NullPointerException("No parameter");
		}
		String inputType = paramObject.getAttribute(AbstractParam.PARAM_TYPE);
		
		if (inputType == null) {
			return null;
		}
		
		if (inputType.equals(TextParam.TEXT_LINE_PARAM)) {
			return new TextFieldEditor(paramObject);
		} 
		
		if (inputType.equals(DateTimeParam.DATE_TIME_PARAM)) {
			return new DateTimeField(paramObject);
		} 
		
		if (inputType.equals(TextBoxParam.TEXT_BOX_PARAM)) {
			return new TextBoxEditor(paramObject);
		}
		
		if (inputType.equals(NumberParam.NUMBER_PARAM)) {
			return new NumberEditor(paramObject);
		}
		
		if (inputType.equals(EnumParam.ENUM_PARAM)) {
			return new EnumEditor(paramObject);
		}
		
		if (inputType.equals(BooleanParam.BOOLEAN_PARAM)) {
			return new BooleanEditor(paramObject);
		}
		
		if (inputType.equals(EditorLinkParam.EDITOR_LINK_PARAM)) {
			return new ProtocolLinkEditor(paramObject);
		}
		
		if (inputType.equals(OntologyTermParam.ONTOLOGY_TERM_PARAM)) {
			return new OntologyTermEditor(paramObject);
		}
		
		if (inputType.equals(DataFieldConstants.FIXED_PROTOCOL_STEP)) {
			return null;
		}
		
		return null;
	}
}
