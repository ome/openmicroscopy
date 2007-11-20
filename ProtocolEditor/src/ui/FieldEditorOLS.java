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

import javax.swing.JComboBox;

import ols.Ontologies;

import tree.DataField;

public class FieldEditorOLS extends FieldEditor {
	
	String[] ontologyIds;
	String[] ontologyNames;
	JComboBox ontologySelector;
	
	public FieldEditorOLS (DataField dataField) {
		
		super(dataField);
		
		LinkedHashMap<String, String> allOntologies = Ontologies.getInstance().getSupportedOntologies();
		
		ontologyIds = new String[allOntologies.size()];
		ontologyNames = new String[allOntologies.size()];
		
		int index=0;
		for (Iterator i = allOntologies.keySet().iterator(); i.hasNext();){
			String key = (String) i.next();
			String name = allOntologies.get(key);
			ontologyIds[index] = key;
			ontologyNames[index] = key + "\t" + name;
			index++;
		}
		
		ontologySelector = new JComboBox(ontologyNames);
		ontologySelector.addActionListener(new OntologySelectionListener());
		ontologySelector.setMaximumRowCount(25);
		attributeFieldsPanel.add(ontologySelector);
		
	}
	
	public class OntologySelectionListener implements ActionListener {
		public void actionPerformed (ActionEvent event) {
			String ontologyID = ontologyIds[ontologySelector.getSelectedIndex()];
			dataField.setAttribute(DataField.ONTOLOGY_ID, ontologyID, true);
		}
	}

}
