 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.AttributeEditNoLabel 
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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate;

//Java imports

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ITreeEditComp;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.TextFieldEditor;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.uiComponents.UIUtilities;

/** 
 * This class extends {@link AttributeEditLine} with a different UI look. 
 * There is no label beside the text input. Instead, the label is used to
 * fill the text box if no text is entered. 
 * Also, text area has no border. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class AttributeEditNoLabel 
	extends AttributeEditLine 
	implements FocusListener {
	
	/**
	 * The text to display if no text is set. 
	 */
	private String 					labelText;

	/** A border to illustrate when this field is selected */
	private Border 					selectedBorder;
	
	/** A border to illustrate when this field is unselected */
	private Border 					unselectedBorder;

	/**
	 * Called by constructor. 
	 * If no text is entered, the {@link #labelText} is displayed in grey. 
	 */
	private void checkFieldEmpty() {
		String displayedText = textField.getText().trim();
		
		if (displayedText.length() == 0) {
			textField.setText(labelText);
			textField.setForeground(Color.GRAY);
		}
	}

	/**
	 * Creates an instance. 
	 * Delegates to the superclass constructor, but also sets the value
	 * of {@link #labelText} and calls {@link #checkFieldEmpty()}
	 * 
	 * @param param				The parameter we're editing	
	 * @param attributeName		The name of the attribute to edit
	 * @param label				The text to display in the field if empty
	 */
	public AttributeEditNoLabel(IAttributes param, String attributeName,
			String label) {
		super(param, attributeName, label);
		labelText = label;
		
		checkFieldEmpty();
	}
	
	
	/**
	 * Builds the UI. 
	 */
	protected void buildUI() 
	{
		setLayout(new BorderLayout());
		
		selectedBorder = BorderFactory.createLineBorder(org.openmicroscopy.shoola.util.ui.UIUtilities.LIGHT_GREY);
		unselectedBorder = BorderFactory.createLineBorder(Color.white);
		
		// Add a text field
		TextFieldEditor editor = new TextFieldEditor(getParameter(), 
				attributeName);
		textField = editor.getTextField();
		textField.setBorder(unselectedBorder);
		
		textField.addFocusListener(this);
		
		// listen for changes to the Value property, indicating that the 
		// field has been edited.
		editor.addPropertyChangeListener(ITreeEditComp.VALUE_CHANGED_PROPERTY, 
				this);
		
		add(editor, BorderLayout.CENTER);
	}
	
	/**
	 * Implemented as specified by the {@link FocusListener} interface.
	 * Sets the border to selected. 
	 */
	public void focusGained(FocusEvent e) {
		textField.setBorder(selectedBorder);
		
		// if the text has not been edited, select all the text
		if (labelText != null && labelText.equals(textField.getText())) {
			textField.setSelectionStart(0);
			textField.setSelectionEnd(labelText.length());
		}
	}

	/**
	 * Implemented as specified by the {@link FocusListener} interface.
	 * Sets the border to unselected. 
	 */
	public void focusLost(FocusEvent e) {
		textField.setBorder(unselectedBorder);
	}

}
