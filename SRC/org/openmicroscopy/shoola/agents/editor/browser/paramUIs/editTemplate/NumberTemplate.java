 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.NumberTemplate 
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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.AbstractParamEditor;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ITreeEditComp;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.NumberFilter;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.NumberParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
import org.openmicroscopy.shoola.util.ui.HistoryDialog;

/** 
 * This is the UI component for editing the "Template" of a number 
 * parameter. ie. Editing the default Number, and the units.
 * 
 * This class displays two {@link AttributeEditLine} panels to handle this
 * editing. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class NumberTemplate 
	extends AbstractParamEditor
	implements PropertyChangeListener, 
		DocumentListener,
		ActionListener
{
	/**
	 * Keep a reference of the units text field for auto-complete.
	 */
	private JTextField			unitsField;
	
	/**
	 * Auto-complete pop-up for units. 
	 */
	private HistoryDialog 		popup;
	
	/**
	 * Builds the UI
	 */
	private void buildUI()
	{
		// NumberDefault: Label and text box
		AttributeEditLine defaultEditor = new AttributeEditLine
			(getParameter(), TextParam.DEFAULT_VALUE, "Number Default");
		defaultEditor.addPropertyChangeListener
				(ITreeEditComp.VALUE_CHANGED_PROPERTY, this);
		Document d = defaultEditor.textField.getDocument();
		AbstractDocument doc;
		if (d instanceof AbstractDocument) {
            doc = (AbstractDocument)d;
            doc.setDocumentFilter(new NumberFilter(doc));
        }
		add(defaultEditor);
		add(Box.createHorizontalStrut(6));
		
		// Units: Label and text box
		AttributeEditLine unitsEditor = new AttributeEditLine
			(getParameter(), NumberParam.PARAM_UNITS, "Units");
		unitsEditor.addPropertyChangeListener
				(ITreeEditComp.VALUE_CHANGED_PROPERTY, this);
		unitsField = unitsEditor.textField;
		unitsField.getDocument().addDocumentListener(this);
		unitsField.addActionListener(this);
		add(unitsEditor);
	}
	
	/**
	 * Shows the pop-up for users to choose an existing unit if they wish. 
	 */
	private void showUnitsPopup() {
		Rectangle rect = unitsField.getBounds();

		String[] unitsOptions = AbstractParam.getCommonUnits();
		
		popup = new HistoryDialog(unitsOptions, rect.width);
		popup.addPropertyChangeListener(
				HistoryDialog.SELECTION_PROPERTY, this);
		popup.show(unitsField, 0, rect.height);
		
		// allows the user to keep typing
		unitsField.requestFocusInWindow();
	}
	
	/**
	 * Creates an instance
	 * 
	 * @param param		The parameter edited by this UI. 
	 */
	public NumberTemplate(IAttributes param) 
	{
		super (param);
		
		buildUI();
	}


	/**
	 * This method is implemented as specified by the 
	 * {@link PropertyChangeListener} interface.
	 * 
	 * This class listens for changes in the
	 * {@link AbstractParamEditor#VALUE_CHANGED_PROPERTY} in the 
	 * {@link AttributeEditLine} classes that make up the UI. 
	 * Any change events are passed on by calling 
	 * {@link #attributeEdited(String, Object)}.
	 * 
	 * @see AbstractParamEditor#attributeEdited(String, Object)
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) 
	{	
		String propName = evt.getPropertyName();
		if (AbstractParamEditor.VALUE_CHANGED_PROPERTY.equals(propName)) {
			
			if (evt.getSource() instanceof ITreeEditComp) {
				ITreeEditComp source = (ITreeEditComp)evt.getSource();
				String attributeName = source.getAttributeName();
				
				// if editing Units, the pop-up causes loss of focus, and the 
				// firing of this propertyChange. Don't want to save the edit
				// at this point, because focus will be permanently lost from 
				// textField when the panel is re-built. 
				if (NumberParam.PARAM_UNITS.equals(attributeName)) {
					// So, if the popup is visible (user not stopped editing units)
					if (popup != null && popup.isVisible()) {
						// ...don't do anything
						return;
					}
				}
				Object newValue = evt.getNewValue();
				attributeEdited(attributeName, newValue);
			}
			
			// If an item has been selected from pop-up, save change.
			// This will also update the UI when the panel is rebuilt
		} else if (HistoryDialog.SELECTION_PROPERTY.equals(propName)) {
			String newUnits = popup.getSelectedTextValue() + "";
			popup.setVisible(false);
			attributeEdited(NumberParam.PARAM_UNITS, newUnits);
		}
	}
	
	/**
	 * Implemented as specified by the {@link ITreeEditComp} interface. 
	 * 
	 * @see {@link ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() {
		return "Edit Number";
	}

	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Listens to typing in the Units field, and calls {@link #showUnitsPopup()}
	 * 
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {
		showUnitsPopup();
	}

	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Listens to typing in the Units field, and calls {@link #showUnitsPopup()}
	 * 
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e) {
		showUnitsPopup();
	}

	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Listens to typing in the Units field, and calls {@link #showUnitsPopup()}
	 * 
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e) {
		showUnitsPopup();
	}

	/**
	 * Implemented as specified by the {@link ActionListener} interface.
	 * Listens for 'Enter' from Units field, hides pop-up and saves text.
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(unitsField)) {
			String newUnits = unitsField.getText();
			if (popup != null && popup.isVisible()) {
				popup.setVisible(false);
			}
			attributeEdited(NumberParam.PARAM_UNITS, newUnits);
		}
	}

}
