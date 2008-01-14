/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package ui;

import tree.DataField;
import tree.DataFieldConstants;


// this class handles instantiation of FieldEditor and FormField subclasses
// depends on the inputType attribute of dataField.

public class FieldEditorFormFieldFactory {
	
	// singleton
	private static FieldEditorFormFieldFactory uniqueInstance = new FieldEditorFormFieldFactory();
	
	// constructor
	private FieldEditorFormFieldFactory() {
	}
	
	public static FieldEditorFormFieldFactory getInstance() {
		return uniqueInstance;
	}
	
	// FieldEditor Factory
	public FieldEditor getFieldEditor(DataField dataField) {
		
		FieldEditor fieldEditor = null;
		
		String inputType = dataField.getInputType();
		
		if (inputType == null) {
			fieldEditor = new FieldEditorCustom(dataField);
		}
		else if (inputType.equals(DataFieldConstants.TEXT_ENTRY_STEP)) {
			fieldEditor = new FieldEditorText(dataField);
		}
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
		else {
			fieldEditor = new FieldEditorCustom(dataField);
			dataField.setAttribute(DataFieldConstants.INPUT_TYPE, DataFieldConstants.CUSTOM, false);
		}
		
		return fieldEditor;
	}
	
	
	// FormField Factory
	public FormField getFormField(DataField dataField) {
		
		FormField formField = null;
		
		String inputType = dataField.getInputType();
		
		if (inputType == null) {
			formField = new FormFieldCustom(dataField);
		}
		else if (inputType.equals(DataFieldConstants.TEXT_ENTRY_STEP)) {
			formField = new FormFieldText(dataField);
		}
		else if (inputType.equals(DataFieldConstants.MEMO_ENTRY_STEP)) {
			formField = new FormFieldMemo(dataField);
		}
		else if (inputType.equals(DataFieldConstants.PROTOCOL_TITLE)) {
			formField = new FormFieldProtocol(dataField);
		}
		else if (inputType.equals(DataFieldConstants.NUMBER_ENTRY_STEP)) {
			formField = new FormFieldNumber(dataField);
		}
		else if (inputType.equals(DataFieldConstants.DATE)) {
			formField = new FormFieldDate(dataField);
		}
		else if (inputType.equals(DataFieldConstants.TIME_FIELD)) {
			formField = new FormFieldTime(dataField);
		}
		else if (inputType.equals(DataFieldConstants.DROPDOWN_MENU_STEP)) {
			formField = new FormFieldDropDown(dataField);
		}
		else if (inputType.equals(DataFieldConstants.CUSTOM)) {
			formField = new FormFieldCustom(dataField);
		}
		else if (inputType.equals(DataFieldConstants.TABLE)) {
			formField = new FormFieldTable(dataField);
		}
		else if (inputType.equals(DataFieldConstants.FIXED_PROTOCOL_STEP)) {
			formField = new FormFieldFixed(dataField);
		} 
		else if (inputType.equals(DataFieldConstants.CHECKBOX_STEP)) {
			formField = new FormFieldCheckBox(dataField);
		} 
		else if (inputType.equals(DataFieldConstants.OLS_FIELD)) {
			formField = new FormFieldOLS(dataField);
		}
		else if (inputType.equals(DataFieldConstants.OBSERVATION_DEFINITION)) {
			formField = new FormFieldObservation(dataField);
		}
		else {
			formField = new FormFieldCustom(dataField);
			dataField.setAttribute(DataFieldConstants.INPUT_TYPE, DataFieldConstants.CUSTOM);
		}
		
		return formField;
	}

}
