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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;


import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import treeModel.fields.Field;
import treeModel.fields.FieldPanel;
import treeModel.fields.IField;
import ui.components.AttributeTextEditor;

public class FormFieldText extends FieldPanel {
	
	JTextField textInput;
	
	/**
	 * bound property
	 */
	public static final String TEXT_VALUE = "textValue";

	
	public FormFieldText() {
		this(new Field());
	}
	
	public FormFieldText(IField field) {
		super(field);
		
		String value = dataField.getAttribute(DataFieldConstants.VALUE);
		
		textInput = new JTextField("test this");
		
		textInput.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				valueChanged();
			}
			
		});
		
		/*
		textInput = new AttributeTextEditor(dataField, 
				DataFieldConstants.VALUE);
				*/
		//textInput.addFocusListener(componentFocusListener);		// to highlight field when textBox gets focus

		horizontalBox.add(textInput);
		
		// enable or disable components based on the locked status of this field
		refreshLockedStatus();
	}
	
	public void valueChanged() {
		this.firePropertyChange(TEXT_VALUE, "", textInput.getText());
	}
	
	public void setValue(Object value) {
		String text = value.toString();
		textInput.setText(text);
	}
	
	public String getText() {
		return textInput.getText();
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

	
//	 overridden by subclasses that have input components
	public void setExperimentalEditing(boolean enabled) {
		
		if (enabled) textInput.setForeground(Color.BLACK);
		else textInput.setForeground(Color.WHITE);
		
		textInput.setEditable(enabled);
	}
	
	public void setSelected(boolean highlight) {
		//boolean previouslyHighlighted = highlighted;
		
		super.setSelected(highlight);
		// if the user highlighted this field by clicking the field (not the textBox itself) 
		// need to get focus, otherwise focus will remain elsewhere. 
		if (highlight && !textInput.hasFocus()) {
		//	textInput.removeFocusListener(componentFocusListener);
			textInput.requestFocusInWindow();
		//	textInput.addFocusListener(componentFocusListener);
		}
	}

}
