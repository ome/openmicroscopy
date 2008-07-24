 /*
 * fields.FieldValueFactory 
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
package fields;

//Java imports

//Third-party libraries

//Application-internal dependencies

import java.util.HashMap;

import tree.DataFieldConstants;

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
	
	public static IParam cloneParam(IParam cloneThis) {
		
		String paramType = cloneThis.getAttribute(AbstractParam.PARAM_TYPE);
		
		AbstractParam newParam = (AbstractParam)getFieldParam(paramType);
		HashMap<String, String> attr = new HashMap<String, String>(
				((AbstractParam)cloneThis).getAllAttributes());
		
		if(newParam != null)	// just in case...
		newParam.setAllAttributes(attr);
		
		return newParam;
	}
	
	/**
	 * This create new Parameter objects (subclasses of
	 * AbstractParam), according to the String that describes their data type.
	 * 
	 * @param paramType		A string to describe the data type
	 * @return		A new parameter object
	 */
	public static IParam getFieldParam(String paramType) {
		
		IParam fieldValue = null;
		
		if (paramType.equals(SingleParam.TEXT_LINE_PARAM)) {
			fieldValue = new SingleParam(SingleParam.TEXT_LINE_PARAM);
		}
		else if (paramType.equals(SingleParam.TEXT_BOX_PARAM)) {
			fieldValue = new SingleParam(SingleParam.TEXT_BOX_PARAM);
		}
		else if (paramType.equals(DataFieldConstants.DATE_TIME_FIELD)) {
			fieldValue = new DateTimeParam(DataFieldConstants.DATE_TIME_FIELD);
		}
		
		return fieldValue;
		
		/*
		else if (inputType.equals(DataFieldConstants.MEMO_ENTRY_STEP)) {
			fieldEditor = new FieldEditorMemo(dataField);
		}
		else if (inputType.equals(DataFieldConstants.PROTOCOL_TITLE)) {
			fieldEditor = new FieldEditorProtocol(dataField);
		}
		else if (inputType.equals(DataFieldConstants.NUMBER_ENTRY_STEP)) {
			fieldEditor = new FieldEditorNumber(dataField);
		}
		else if (inputType.equals(DataFieldConstants.DATE)) {
			fieldEditor = new FieldEditorDate(dataField);
		}
		else if (inputType.equals(DataFieldConstants.DATE_TIME_FIELD)) {
			fieldEditor = new FieldEditorDateTime(dataField);
		}
		else if (inputType.equals(DataFieldConstants.TIME_FIELD)) {
			fieldEditor = new FieldEditorTime(dataField);
		}
		else if (inputType.equals(DataFieldConstants.DROPDOWN_MENU_STEP)) {
			fieldEditor = new FieldEditorDropDown(dataField);
		}
		else if (inputType.equals(DataFieldConstants.CUSTOM)) {
			fieldEditor = new FieldEditorCustom(dataField);
		} 
		else if (inputType.equals(DataFieldConstants.TABLE)) {
			fieldEditor = new FieldEditorTable(dataField);
		} 
		else if (inputType.equals(DataFieldConstants.CHECKBOX_STEP)) {
			fieldEditor = new FieldEditorCheckBox(dataField);
		}
		else if (inputType.equals(DataFieldConstants.FIXED_PROTOCOL_STEP)) {
			fieldEditor = new FieldEditorFixed(dataField);
		}
		else if (inputType.equals(DataFieldConstants.OLS_FIELD)) {
			fieldEditor = new FieldEditorOLS(dataField);
		}
		else if (inputType.equals(DataFieldConstants.OBSERVATION_DEFINITION)) {
			fieldEditor = new FieldEditorObservation(dataField);
		}
		else if (inputType.equals(DataFieldConstants.IMAGE_FIELD)) {
			fieldEditor = new FieldEditorFixed(dataField);
		}
		else if (inputType.equals(DataFieldConstants.LINK_FIELD)) {
			fieldEditor = new FieldEditorFixed(dataField);
		}
		else {
			fieldEditor = new FieldEditorCustom(dataField);
			dataField.setAttribute(DataFieldConstants.INPUT_TYPE, DataFieldConstants.CUSTOM, false);
		}
		*/
	}

}
