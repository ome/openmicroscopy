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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JTextField;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.components.AttributesDialog;
import util.ImageFactory;

public class FormFieldCustom extends FormField {
	
	
	AttributesDialog attDialog;
	JButton showAttributesButton;
	JTextField textInput;
	boolean attributesDialogVisible = false;

	public FormFieldCustom(IDataFieldObservable dataFieldObs) {
		super(dataFieldObs);
		
		if (areAnyCustomAttributes()) {
			showAttributesButton = new JButton(ImageFactory.getInstance().getIcon(ImageFactory.NOTE_PAD));
			//showAttributesButton.addMouseListener(new FormPanelMouseListener());
			showAttributesButton.addActionListener(new ShowAttributesListener());
			showAttributesButton.setToolTipText("Display attributes");
			showAttributesButton.setBorder(null);
			horizontalBox.add(showAttributesButton);
		}
		
		textInput = new JTextField();
		textInput.addMouseListener(new FormPanelMouseListener());
		textInput.setName(DataFieldConstants.TEXT_NODE_VALUE);
		textInput.addFocusListener(focusChangedListener);
		textInput.addKeyListener(textChangedListener);
		checkForTextNodeValue();
		
	}

	public void checkForTextNodeValue() {
		String textNodeValue = dataField.getAttribute(DataFieldConstants.TEXT_NODE_VALUE);
		//System.out.println("FormFieldCustom constructor: textNodeValue = " + textNodeValue);
		if (textNodeValue != null) {
			textInput.setText(textNodeValue);
			horizontalBox.add(textInput);
		}
	}
	
	// called when dataField changes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		checkForTextNodeValue();
	}
	
	// toggle visibility of attributes dialog
	public class ShowAttributesListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			
			// first select the field (clears ALL fields - all dialogs hidden) then highlights this field
			int clickType = event.getModifiers();
			if (clickType == XMLView.SHIFT_CLICK) {
				panelClicked(false);
			} else
				panelClicked(true);
			
			// ...NOW show this dialog
			attributesDialogVisible = !attributesDialogVisible;
			showAttributes(attributesDialogVisible);
		}
	}
	
	public void showAttributes(boolean visible) {
		
		if (attDialog == null) attDialog = new AttributesDialog(this, dataFieldObs);
		
		attributesDialogVisible = visible;
		
		if (visible) attDialog.showAttributesDialog();	// also repositions dialog
		else attDialog.dispose();
	}
	
	// called when user clicks on panel
	public void setHighlighted(boolean highlight) {
		super.setHighlighted(highlight);
		
		// always hide attributes dialog when de-selecting a field
		if (!highlight) showAttributes(false);
	}
	
	// check to see if there are any custom attributes to display / edit
	public boolean areAnyCustomAttributes() {
		Iterator keyIterator = dataField.getAllAttributes().keySet().iterator();
		
		while (keyIterator.hasNext()) {
			String name = (String)keyIterator.next();
			// don't count these attributes
			if ((name.equals(DataFieldConstants.ELEMENT_NAME )) || (name.equals(DataFieldConstants.INPUT_TYPE))
					|| (name.equals(DataFieldConstants.SUBSTEPS_COLLAPSED)) || (name.equals(DataFieldConstants.TEXT_NODE_VALUE))) continue;
			return true;
		}
		return false;
	}
}
