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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import ols.Ontologies;

import tree.DataField;
import util.BareBonesBrowserLaunch;
import util.ImageFactory;

public class FieldEditorOLS extends FieldEditor {
	
	String ontologyId;
	
	String[] ontologyIds;
	String[] ontologyNames;
	JComboBox ontologySelector;
	ActionListener ontologySelectionListener = new OntologySelectionListener();
	
	public FieldEditorOLS (DataField dataField) {
		
		super(dataField);
		
		/* need a String[] of ontology Id-Name pairs
		 * get a map of these from my Ontologies class
		 * then convert to String array. 
		 */
		LinkedHashMap<String, String> allOntologies = Ontologies.getInstance().getSupportedOntologies();
		
		ontologyIds = new String[allOntologies.size()];
		ontologyNames = new String[allOntologies.size()];
		
		// copy map to array
		int index=0;
		for (Iterator i = allOntologies.keySet().iterator(); i.hasNext();){
			String key = (String) i.next();
			String name = allOntologies.get(key);
			ontologyIds[index] = key;
			ontologyNames[index] = key + "\t" + name;
			index++;
		}
		
		// make a new comboBox with the ontology Names
		ontologySelector = new JComboBox(ontologyNames);
		ontologySelector.addActionListener(ontologySelectionListener);
		ontologySelector.setMaximumRowCount(25);
		attributeFieldsPanel.add(ontologySelector);
		
		// a link to the Ontology-Lookup-Service website
		Icon olsIcon = ImageFactory.getInstance().getIcon(ImageFactory.OLS_LOGO_SMALL);
		JLabel olsLabel = new JLabel("Uses the EBI OLS");
		JButton olsButton = new JButton(olsIcon);
		olsButton.addActionListener(new OlsLinkListener());
		attributeFieldsPanel.add(olsLabel);
		attributeFieldsPanel.add(olsButton);
		
		// if ontology ID has been set already, set the right selector
		ontologyId = dataField.getAttribute(DataField.ONTOLOGY_ID);
		refreshOntologySelector();
	}
	
	public void refreshOntologySelector() {
		ontologySelector.removeActionListener(ontologySelectionListener);
		
		if (ontologyId == null) {
			ontologySelector.setSelectedIndex(0);
		} else {
			
			for (int i=0; i<ontologyIds.length; i++)
				if (ontologyId.equals(ontologyIds[i]))
					ontologySelector.setSelectedIndex(i);
			
		}
		ontologySelector.addActionListener(ontologySelectionListener);
	}
	
	public class OlsLinkListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			BareBonesBrowserLaunch.openURL("http://www.ebi.ac.uk/ontology-lookup");
		}
		
	}
	
	public class OntologySelectionListener implements ActionListener {
		public void actionPerformed (ActionEvent event) {
			String ontologyID = ontologyIds[ontologySelector.getSelectedIndex()];
			dataField.setAttribute(DataField.ONTOLOGY_ID, ontologyID, true);
		}
	}

}
