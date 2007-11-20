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
