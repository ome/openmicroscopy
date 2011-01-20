 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate
 * .DefaultTextField 
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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.AbstractParamEditor;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ITreeEditComp;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.TextFieldEditor;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;

/** 
 * This is a UI component with a {@link TextFieldEditor}
 *  used for editing a named attribute.
 * It uses an instance of TextFieldEditor to provide a text field that 
 * fires propertyChangeEvents when edited.
 * These are forwarded by calling the attributeEdited method of the
 * DefaultTextField's superclass.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class AttributeEditLine 
	extends AbstractParamEditor
	implements PropertyChangeListener 
{
	/**
	 * This is the name of the attribute being edited by this UI
	 */
	protected String 			attributeName;
	
	/**
	 * A string for the label beside the text field. e.g. "Name".
	 * This is also used for the {@link #getEditDisplayName()} to provide 
	 * an undo/redo display name. 
	 */
	private String 				labelText;
	
	/** The text field used in this UI */
	protected JTextField 		textField;
	
	/**
	 * Builds the UI. 
	 */
	protected void buildUI() 
	{
		setLayout(new BorderLayout());
		
		add(new CustomLabel (labelText + ": "), BorderLayout.NORTH);
		
		// Add a text field
		TextFieldEditor textEditor = new TextFieldEditor(getParameter(), 
				attributeName);
		// listen for changes to the Value property, indicating that the 
		// field has been edited.
		textEditor.addPropertyChangeListener(ITreeEditComp.VALUE_CHANGED_PROPERTY, 
				this);
		textField = textEditor.getTextField();	// allows setting fonts etc.
		
		add(textEditor, BorderLayout.CENTER);
	}
	
	/**
	 * Creates an instance, and builds the UI. 
	 * 
	 * @param param		The parameter you're editing. 
	 * @param attributeName 	The name of the attribute to edit
	 * @param label		The display name (label) for the text input. 
	 */
	public AttributeEditLine(IAttributes param, String attributeName, String label) 
	{	
		super(param);
		
		this.attributeName = attributeName;
		this.labelText = label;
		
		buildUI();
	}

	/**
	 * If the value property of the text field changes, 
	 * call attributeEdited(attribute, value) to notify listeners.
	 * 
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
	public void propertyChange(PropertyChangeEvent evt) 
	{	
		if (ITreeEditComp.VALUE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
			attributeEdited(attributeName, evt.getNewValue());
		}
	}

	/**
	 * A display name for undo/redo
	 * 
	 * @return String 	see above.
	 */
	public String getEditDisplayName() {
		return "Edit " + labelText;
	}
	
	/**
	 * Sets the size of the font in the text-field (not the label)
	 * 
	 * @param size		The new font size. 
	 */
	public void setFontSize(int size)
	{
		if (textField != null) {
			Font oldFont = textField.getFont();
			Font newFont =  oldFont.deriveFont((float)size);
			textField.setFont(newFont);
		}
	}

}
