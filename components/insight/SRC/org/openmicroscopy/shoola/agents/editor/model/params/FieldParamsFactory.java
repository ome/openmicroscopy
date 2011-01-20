 /*
 * org.openmicroscopy.shoola.agents.editor.model.params.FieldParamsFactory 
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
package org.openmicroscopy.shoola.agents.editor.model.params;

//Java imports
import java.util.HashMap;

//Third-party libraries

//Application-internal dependencies

/** 
 * This factory is used to create new Parameter objects (subclasses of
 * AbstractParam), according to the String that describes their data type.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FieldParamsFactory {
	
	public static final String NO_PARAMS = "noParams";
	
	private static final String[] PARAM_TYPES = 
	{	TextParam.TEXT_LINE_PARAM,
		NumberParam.NUMBER_PARAM, 
		BooleanParam.BOOLEAN_PARAM, 
		EnumParam.ENUM_PARAM, 
		DateTimeParam.DATE_TIME_PARAM,
		EditorLinkParam.EDITOR_LINK_PARAM,
		OntologyTermParam.ONTOLOGY_TERM_PARAM
	};
	
	//	 the names used for the UI - MUST be in SAME ORDER as INPUT_TYPES they correspond to 
	// this means you can change the UI names without changing INPUT_TYPES.
	private static final String[] UI_INPUT_TYPES = 	
	{ "Text", 
		"Number", 
		"Check-Box", 
		"Drop-down Menu", 
		"Date & Time",
		"Editor File Link",
		"Ontology Term"
		};
	
	/**
	 * Gets a list of the Strings that ID the parameter types used by Editor.
	 * 
	 * @return		see above
	 */
	public static String[] getParamTypes() { return PARAM_TYPES; }
	
	/**
	 * Gets a list of the Strings to display the parameter types used by Editor.
	 * NB these correspond to the IDs returned by {@link #getParamTypes()}
	 * 
	 * @return		see above
	 */
	public static String[] getUiParamTypes() { return UI_INPUT_TYPES; }
	
	/**
	 * Returns the UI-suitable parameter type name 
	 * (from the {@link #UI_INPUT_TYPES} list, that represents the paramType
	 * from the {@link #PARAM_TYPES} list. 
	 * If paramType is not in this list, paramType is returned.
	 * 
	 * @param paramType		see above
	 * @return				see above
	 */
	public static String getTypeForDisplay(String paramType) 
	{
		if (paramType == null)		return null;
		
		int paramTypesLength = PARAM_TYPES.length;
		
		for (int i = 0; i < paramTypesLength; i++) {
			
			if (PARAM_TYPES[i].equals(paramType)) {
				return UI_INPUT_TYPES[i];
			}
		}
		
		return paramType;
	}

	/**
	 * This create new Parameter objects (subclasses of
	 * AbstractParam), according to the String that describes their data type.
	 * 
	 * @param paramType		A string to describe the data type
	 * @return		A new parameter object
	 */
	public static IParam getFieldParam(String paramType) 
	{	
		IParam fieldValue = null;
		
		if (paramType.equals(TextParam.TEXT_LINE_PARAM)) {
			fieldValue = new TextParam(TextParam.TEXT_LINE_PARAM);
		}
		else if (paramType.equals(TextBoxParam.TEXT_BOX_PARAM)) {
			fieldValue = new TextBoxParam(TextBoxParam.TEXT_BOX_PARAM);
		}
		else if (paramType.equals(NumberParam.NUMBER_PARAM)) {
			fieldValue = new NumberParam();
		}
		else if (paramType.equals(EnumParam.ENUM_PARAM)) {
			fieldValue = new EnumParam();;
		}
		else if (paramType.equals(BooleanParam.BOOLEAN_PARAM)) {
			fieldValue = new BooleanParam();
		}
		else if (paramType.equals(DateTimeParam.DATE_TIME_PARAM)) {
			fieldValue = new DateTimeParam();
		}
		else if (paramType.equals(EditorLinkParam.EDITOR_LINK_PARAM)) {
			fieldValue = new EditorLinkParam();
		}
		else if (paramType.equals(OntologyTermParam.ONTOLOGY_TERM_PARAM)) {
			fieldValue = new OntologyTermParam();
		}
		return fieldValue;
	}
	
	/**
	 * Method for duplicating a parameter 
	 * 
	 * @param cloneThis		The parameter to clone
	 * @return				The new parameter. 
	 */
	public static IParam cloneParam(IParam cloneThis) 
	{
		String paramType = cloneThis.getAttribute(AbstractParam.PARAM_TYPE);
		
		AbstractParam newParam = (AbstractParam)getFieldParam(paramType);
		HashMap<String, String> attr = new HashMap<String, String>(
				((AbstractParam)cloneThis).getAllAttributes());
		
		if(newParam != null)	// just in case...
		newParam.setAllAttributes(attr);
		
		return newParam;
	}

}
