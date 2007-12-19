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


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import tree.DataField;
import ui.components.OntologyTermSelector;
import util.ImageFactory;

public class FormFieldOLS extends FormField {
	
	// variables
	String termId;
	
	boolean metadataPanelVisible = false;
	
	OLSMetadataPanel olsMetadataPanel;
	
	JButton toggleMetadataButton;
	
	public FormFieldOLS(DataField dataField) {
		
		super(dataField);
		
		termId = dataField.getAttribute(DataField.ONTOLOGY_TERM_ID);
		
		
		Icon metadataIcon = ImageFactory.getInstance().getIcon(ImageFactory.ONTOLOGY_METADATA_ICON);
		toggleMetadataButton = new JButton(metadataIcon);
		toggleMetadataButton.setBackground(null);
		toggleMetadataButton.setBorder(null);
		toggleMetadataButton.addActionListener(new ToggleMetadataVisibilityListener());

		OntologyTermSelector ontologyTermSelector = new OntologyTermSelector(dataField, DataField.ONTOLOGY_TERM_ID, "");
		
		horizontalBox.add(toggleMetadataButton);
		horizontalBox.add(ontologyTermSelector);
		
		refreshTermDetails();
	}
	
	
	
	// when a term is loaded (constructor) or selected from the drop-down options 
	// this updates the olsMetadataPanel
	private void refreshTermDetails() {
		if ((metadataPanelVisible) && (olsMetadataPanel != null)) {
			olsMetadataPanel.resetTerm(termId);
			this.validate();
		}
	}
	
	
	private void refreshOlsMetadataPanelVisible() {	
		// this is null until it is set visible.
		// Therefore, no calls need to be made to the OntologyLookupService till that time 
		// (or until auto-complete is required). 

		if (olsMetadataPanel == null) {
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
		termId = dataField.getAttribute(DataField.ONTOLOGY_TERM_ID);
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
