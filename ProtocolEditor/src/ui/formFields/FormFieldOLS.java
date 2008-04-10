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


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.OLSMetadataPanel;
import ui.components.OntologyTermSelector;
import ui.formFields.FormField.FocusGainedPropertyChangedListener;
import util.ImageFactory;

public class FormFieldOLS extends FormField {
	
	// variables
	String termIdAndName;
	
	boolean metadataPanelVisible = false;
	
	OLSMetadataPanel olsMetadataPanel;
	
	OntologyTermSelector ontologyTermSelector;
	
	JButton toggleMetadataButton;
	
	public FormFieldOLS(IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		termIdAndName = dataField.getAttribute(DataFieldConstants.ONTOLOGY_TERM_ID);
		
		Icon metadataIcon = ImageFactory.getInstance().getIcon(ImageFactory.ONTOLOGY_METADATA_ICON);
		toggleMetadataButton = new JButton(metadataIcon);
		toggleMetadataButton.addFocusListener(componentFocusListener);
		toggleMetadataButton.setBackground(null);
		toggleMetadataButton.setBorder(null);
		toggleMetadataButton.addActionListener(new ToggleMetadataVisibilityListener());

		ontologyTermSelector = new OntologyTermSelector(dataField, DataFieldConstants.ONTOLOGY_TERM_ID, "");
		// requests focus
		ontologyTermSelector.addPropertyChangeListener(new FocusGainedPropertyChangedListener());
		
		horizontalBox.add(toggleMetadataButton);
		horizontalBox.add(ontologyTermSelector);
		
		refreshTermDetails();
	
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

		if (ontologyTermSelector != null)	// just in case!
			ontologyTermSelector.setEnabled(enabled);
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
		return new String[] {DataFieldConstants.ONTOLOGY_TERM_ID};
	}
	
	/**
	 * This method tests to see whether the field has been filled out. 
	 * 
	 * @see FormField.isFieldFilled()
	 * @return	True if the field has been filled out by user (Required values are not null)
	 */
	public boolean isFieldFilled() {
		return (dataField.getAttribute(DataFieldConstants.ONTOLOGY_TERM_ID) != null);
	}
	
	// when a term is loaded (constructor) or selected from the drop-down options 
	// this updates the olsMetadataPanel
	private void refreshTermDetails() {
		if ((metadataPanelVisible) && (olsMetadataPanel != null)) {
			if (termIdAndName == null) {
				olsMetadataPanel.resetTerm(null);
				return;
			}
			String termId = OntologyTermSelector.getOntologyIdFromIdAndName(termIdAndName);
			olsMetadataPanel.resetTerm(termId);
			this.validate();
		}
	}
	
	
	private void refreshOlsMetadataPanelVisible() {	
		// this is null until it is set visible.
		// Therefore, no calls need to be made to the OntologyLookupService till that time 
		// (or until auto-complete is required). 

		if ((olsMetadataPanel == null) && (termIdAndName != null)) {
			String termId = OntologyTermSelector.getOntologyIdFromIdAndName(termIdAndName);
			olsMetadataPanel = new OLSMetadataPanel(termId);
			this.add(olsMetadataPanel, BorderLayout.SOUTH);
		}
		
		// if olsMetadataPanel hasn't just been instantiated, and is visible, it may need updating
		else if (metadataPanelVisible) {
				refreshTermDetails();
		}
		olsMetadataPanel.setVisible(metadataPanelVisible);
		
		this.validate();
	}
	
//	 overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		termIdAndName = dataField.getAttribute(DataFieldConstants.ONTOLOGY_TERM_ID);
		
		// update the ontology term selector
		ontologyTermSelector.dataFieldUpdated();
		
		// update metadata panel
		refreshTermDetails();
	}

	/*
	 * shows/hides the metadataPanel.
	 */
	public class ToggleMetadataVisibilityListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			metadataPanelVisible = !metadataPanelVisible;
			refreshOlsMetadataPanelVisible();
		}
	}
}
