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

import tree.DataFieldConstants;
import ui.fieldEditors.FieldEditorCheckBox;
import ui.fieldEditors.FieldEditorCustom;
import ui.fieldEditors.FieldEditorDate;
import ui.fieldEditors.FieldEditorDateTime;
import ui.fieldEditors.FieldEditorDropDown;
import ui.fieldEditors.FieldEditorFixed;
import ui.fieldEditors.FieldEditorMemo;
import ui.fieldEditors.FieldEditorNumber;
import ui.fieldEditors.FieldEditorOLS;
import ui.fieldEditors.FieldEditorObservation;
import ui.fieldEditors.FieldEditorProtocol;
import ui.fieldEditors.FieldEditorTable;
import ui.fieldEditors.FieldEditorText;
import ui.fieldEditors.FieldEditorTime;

//Java imports

//Third-party libraries

//Application-internal dependencies

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
public class FieldValueFactory {
	
	public static IFieldValue getFieldValue(String inputType) {
		
		IFieldValue fieldValue = null;
		
		if (inputType == null) {
			fieldValue = new NoValue(DataFieldConstants.FIXED_PROTOCOL_STEP);
		}
		else if (inputType.equals(DataFieldConstants.TEXT_ENTRY_STEP)) {
			fieldValue = new TextValueObject(DataFieldConstants.TEXT_ENTRY_STEP);
		}
		else if (inputType.equals(DataFieldConstants.FIXED_PROTOCOL_STEP)) {
			fieldValue = new NoValue(DataFieldConstants.FIXED_PROTOCOL_STEP);
		}
		else if (inputType.equals(DataFieldConstants.DATE_TIME_FIELD)) {
			fieldValue = new DateTimeValueObject(DataFieldConstants.DATE_TIME_FIELD);
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
