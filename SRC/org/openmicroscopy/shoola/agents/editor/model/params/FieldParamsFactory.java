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
	
	public static final String[] PARAM_TYPES = 
	{NO_PARAMS, SingleParam.TEXT_LINE_PARAM,
		SingleParam.TEXT_BOX_PARAM, 
		EnumParam.ENUM_PARAM, 
		BooleanParam.BOOLEAN_PARAM, 
		NumberParam.NUMBER_PARAM, 
		DateTimeParam.DATE_TIME_PARAM,
		TimeParam.TIME_PARAM, 
		LinkParam.LINK_PARAM, 
		TableParam.TABLE_PARAM, 
		ImageParam.IMAGE_PARAM, 
		// OLS_FIELD, 
		// OBSERVATION_DEFINITION
	};
	
	//	 the names used for the UI - MUST be in SAME ORDER as INPUT_TYPES they correspond to 
	// this means you can change the UI names without changing INPUT_TYPES.
	public static final String[] UI_INPUT_TYPES = 	
	{ "DELETE Parameter", 
		"Text  (single line)", 
		"Text Box  (multi-line)", 
		"Drop-down Menu", 
		"Check-Box", 
		"Number", 
		"Date & Time", 
		"Time", 
		"Link", 
		"Table", 
		"Image", 
		// "Ontology Term", 
		// "Phenote Observation"
		};

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
		
		if (paramType.equals(SingleParam.TEXT_LINE_PARAM)) {
			fieldValue = new SingleParam(SingleParam.TEXT_LINE_PARAM);
		}
		else if (paramType.equals(SingleParam.TEXT_BOX_PARAM)) {
			fieldValue = new SingleParam(SingleParam.TEXT_BOX_PARAM);
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
		else if (paramType.equals(TimeParam.TIME_PARAM)) {
			fieldValue = new TimeParam();
		}
		else if (paramType.equals(DateTimeParam.DATE_TIME_PARAM)) {
			fieldValue = new DateTimeParam();
		}
		else if (paramType.equals(ImageParam.IMAGE_PARAM)) {
			fieldValue = new ImageParam(ImageParam.IMAGE_PARAM);
		}
		else if (paramType.equals(LinkParam.LINK_PARAM)) {
			fieldValue = new LinkParam(LinkParam.LINK_PARAM);
		}
		else if (paramType.equals(TableParam.TABLE_PARAM)) {
			fieldValue = new TableParam(TableParam.TABLE_PARAM);
		}
		
		else 
			System.err.println("FieldParamsFactory: PARAM_TYPE " + 
					paramType + " " +
					"NOT RECOGNIZED. Return NULL IParam");
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
