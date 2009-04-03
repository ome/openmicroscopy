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

package ui.formFields;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.border.Border;


import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import treeModel.fields.FieldPanel;
import ui.components.AttributeTextAreaEditor;

public class FormFieldMemo extends FieldPanel {
	
	//AttributeMemoFormatEditor inputEditor;
	
	JTextArea textInput;
	
	public FormFieldMemo(IDataFieldObservable dataFieldObs) {
		super(dataFieldObs);
		
		textInput = new AttributeTextAreaEditor(dataField, 
				DataFieldConstants.VALUE);
		
		textInput.addMouseListener(new FormPanelMouseListener());
		
		horizontalBox.add(textInput);
		

		// enable or disable components based on the locked status of this field
		refreshLockedStatus();
	}
	
	/**
	 * This simply enables or disables all the editable components of the 
	 * FormField.
	 * Gets called (via refreshLockedStatus() ) from dataFieldUpdated()
	 * 
	 * @param enabled
	 */
	public void enableEditing(boolean enabled) {
		
		if (textInput != null)	// just in case!
			textInput.setEnabled(enabled);
	}
	
	
	public void setSelected(boolean highlight) {
		super.setSelected(highlight);
		// if the user highlighted this field by clicking the field (not the textArea itself) 
		// need to get focus, otherwise focus will remain elsewhere. 
		if (highlight && (!textInput.hasFocus()))
			textInput.requestFocusInWindow();
	}

	/**
	 * Gets the names of the attributes where this field stores its "value"s.
	 * This is used eg. (if a single value is returned)
	 * as the destination to copy the default value when defaults are loaded.
	 * Also used by EditClearFields to set all values back to null. 
	 * Mostly this is DataFieldConstants.VALUE, but this method should be over-ridden by 
	 * subclasses if they want to store their values under a different attributes (eg "seconds" for TimeField)
	 * 
	 * @return	the name of the attribute that holds the "value" of this field
	 */
	public String[] getValueAttributes() {
		return new String[] {DataFieldConstants.VALUE};
	}
	
	/**
	 * This method tests to see whether the field has been filled out. 
	 * 
	 * @see FormField.isFieldFilled()
	 * @return	True if the field has been filled out by user (Required values are not null)
	 */
	public boolean isFieldFilled() {
		String value = dataField.getAttribute(DataFieldConstants.VALUE);
		return ((value != null) && (value.length() > 0));
	}
	
}
