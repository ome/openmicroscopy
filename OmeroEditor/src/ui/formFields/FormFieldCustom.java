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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JTextField;


import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import treeModel.fields.FieldPanel;
import ui.XMLView;
import ui.components.AttributeTextEditor;
import ui.components.AttributesDialog;
import util.ImageFactory;

public class FormFieldCustom extends FieldPanel {
	
	
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
		
		textInput = new AttributeTextEditor(dataField, 
				DataFieldConstants.TEXT_NODE_VALUE);
		textInput.addMouseListener(new FormPanelMouseListener());
		
		textInput.setVisible(false);	// not visible unless text node
		
		horizontalBox.add(textInput);
		
		checkForTextNodeValue();
		
	}

	public void checkForTextNodeValue() {
		String textNodeValue = dataField.getAttribute(DataFieldConstants.TEXT_NODE_VALUE);
		
		textInput.setVisible(textNodeValue != null);
		
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
	public void setSelected(boolean highlight) {
		super.setSelected(highlight);
		
		// if the user highlighted this field by clicking the field  
		// need to get focus, otherwise focus will remain elsewhere. 
		if (highlight && (showAttributesButton != null) && (!showAttributesButton.hasFocus()))
				showAttributesButton.requestFocusInWindow();
		
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
		Iterator keyIterator = dataField.getAllAttributes().keySet().iterator();
		ArrayList<String> tempList = new ArrayList<String>();
		
		while (keyIterator.hasNext()) {
			String name = (String)keyIterator.next();
			// don't count these attributes
			if ((name.equals(DataFieldConstants.ELEMENT_NAME )) || (name.equals(DataFieldConstants.INPUT_TYPE))
					|| (name.equals(DataFieldConstants.SUBSTEPS_COLLAPSED))) continue;
			tempList.add(name);
		}
		String[] valueAttributes = new String[tempList.size()];
		for (int i=0; i<valueAttributes.length; i++) {
			valueAttributes[i] = tempList.get(i);
		}
		
		return valueAttributes;
	}

	/**
	 * This simply enables or disables all the editable components of the 
	 * FormField.
	 * This FormField superclass has no editable components, but subclasses
	 * should override this method for their additional components. 
	 */
	@Override
	public void enableEditing(boolean enabled) {
		/*
		 * TODO: Need to disable editing within attributes popUp.
		 */  
		if (textInput != null) {
			textInput.setEnabled(enabled);
		}
	}

	/**
	 * This method tests to see whether the field has been filled out. 
	 * ie, Has the user entered a "valid" value into the Form. 
	 * For CustomFields, all the Value attributes should be filled. 
	 * Subclasses should override this method.
	 * 
	 * @return	True if the field has been filled out by user. Required values are not null. 
	 */
	@Override
	public boolean isFieldFilled() {
		String[] valueAttributes = getValueAttributes();
		for (int i=0; i<valueAttributes.length; i++) {
			// if any value attributes are null, field is not filled.
			if (dataField.getAttribute(valueAttributes[i]) == null)
				return false;
		}
		return true;
	}
}
