package ui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import ols.Ontologies;
import ols.OntologyLookUp;

import tree.DataField;

public class OntologyTermSelector extends JPanel {
	
	DataField dataField;
	String attributeId;
	
	JLabel nameLabel;
	
	CustomComboBox ontologySelector;
	JComboBox ontologyTermSelector;
	OntologyTermKeyListener ontologyTermListener;
	TermSelectionListener termSelectionListener;
	
	public static final String ONTOLOGY_ID_NAME_SEPARATOR = "   ";

	public OntologyTermSelector(DataField dataField, String attributeId, String termLabel) {
		
		
		/* need a String[] of ontology Id-Name pairs
		 * get a map of these from my Ontologies class
		 * then convert to String array. 
		 */
		
		LinkedHashMap<String, String> allOntologies = Ontologies.getInstance().getSupportedOntologies();
		
		String[] ontologyIds = new String[allOntologies.size()];
		String[] ontologyNames = new String[allOntologies.size()];
		
		// copy map to array
		int index=0;
		for (Iterator i = allOntologies.keySet().iterator(); i.hasNext();){
			String key = (String) i.next();
			String name = allOntologies.get(key);
			ontologyIds[index] = key;
			ontologyNames[index] = key + ONTOLOGY_ID_NAME_SEPARATOR + name;
			index++;
		}
		
		buildPanel(dataField, attributeId, termLabel, ontologyNames);
		
	}
	
	public OntologyTermSelector(DataField dataField, String attributeId, String termLabel, String[] ontologies) {
		buildPanel(dataField, attributeId, termLabel, ontologies);
	}
	
	
	
	protected void buildPanel(DataField dataField, String attributeId, String termLabel, String[] ontologies) {
		
		this.setLayout(new BorderLayout());
		this.setBackground(null);
		
		this.dataField = dataField;
		this.attributeId = attributeId;
		String termId = dataField.getAttribute(attributeId);
		String termName = "";
		if(termId != null) {
			termName = OntologyLookUp.getTermName(termId);
		}
		
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.setBackground(null);
	
		nameLabel = new JLabel(termLabel);
		
	
		// make a new comboBox with the ontology Names
		ontologySelector = new CustomComboBox(ontologies);
		ontologySelector.setMaximumRowCount(25);
		ontologySelector.setMaximumWidth(200);
		//ontologySelector.setMaximumSize(new Dimension(200, 50));
		setCurrentOntology(Ontologies.getOntologyIdFromTermId(termId));
		
		// make an editable comboBox (auto-complete) for ontology Terms
		ontologyTermSelector = new CustomComboBox();
		ontologyTermSelector.setEditable(true);
		if (termId != null) {
			String idAndName = termId + ONTOLOGY_ID_NAME_SEPARATOR + termName;
			ontologyTermSelector.addItem(idAndName);
		}
		ontologyTermListener = new OntologyTermKeyListener();
		ontologyTermSelector.getEditor().getEditorComponent().addKeyListener(ontologyTermListener);
		termSelectionListener = new TermSelectionListener();
		ontologyTermSelector.addActionListener(termSelectionListener);
		JPanel ontologyTermSelectorPanel = new JPanel(new BorderLayout());
		ontologyTermSelectorPanel.setBackground(null);
		ontologyTermSelectorPanel.add(ontologyTermSelector, BorderLayout.WEST);

		
		horizontalBox.add(nameLabel);
		horizontalBox.add(ontologySelector);
		horizontalBox.add(ontologyTermSelectorPanel);
		
		this.add(horizontalBox, BorderLayout.WEST);
	}
	
	
	/*
	 * KeyListener for auto-complete typing into ontologyTermSelector
	 * if > 3 letters, getTermsById() from OntologyLookUp
	 * use the Map of terms to populate the drop-down menu
	 */
	public class OntologyTermKeyListener implements KeyListener {
		
		public void keyReleased(KeyEvent event) {

			int keyCode = event.getKeyCode();
			
			// ignore events from the Enter key, since this will be processed by ActionListener
			if (keyCode == Event.ENTER)	// return key
				 return;
			
			JTextComponent source = (JTextComponent)event.getSource();
			String input = source.getText();
			
			ontologyTermSelector.removeActionListener(termSelectionListener);
			ontologyTermSelector.removeAllItems();
			
			ontologyTermSelector.addItem(input);

			// don't auto-complete unless 3 characters have been entered
			if (input.length() < 3) return;
			
			String currentOntologyId = getCurrentOntologyId();
			
			Map autoCompleteOptions = OntologyLookUp.getTermsByName(input, currentOntologyId);
			
			for (Iterator i = autoCompleteOptions.keySet().iterator(); i.hasNext();){
				String key = (String) i.next();
				String name = key + ONTOLOGY_ID_NAME_SEPARATOR + autoCompleteOptions.get(key).toString();
				ontologyTermSelector.addItem(name);
			}
			ontologyTermSelector.setPopupVisible(true);
			source.setText(input);
			ontologyTermSelector.addActionListener(termSelectionListener);
		}
		public void keyPressed(KeyEvent arg0) {}
		public void keyTyped(KeyEvent event) {}
	}
	
	

	public class TermSelectionListener implements ActionListener {
		public void actionPerformed (ActionEvent event) {
			
			String selection = ontologyTermSelector.getSelectedItem().toString();
			
			JComboBox sourceComboBox = (JComboBox)event.getSource();
			
			/*
			 * if the user hits enter to select auto-complete, the selection will simply be the 
			 * stuff they have typed in (not an item from the list)
			 * A test for this is whether it contains the ID_NAME_SEPARATOR, contained by all list items.
			 */
			if (!selection.contains(ONTOLOGY_ID_NAME_SEPARATOR)) {
				// just use the first item from the list - if it's not empty (should be highlighted)
				if (sourceComboBox.getItemCount() > 0) {
					selection = sourceComboBox.getItemAt(0).toString();
				}
				
				// now, remove listeners, in order to manually set the selected item
				sourceComboBox.getEditor().getEditorComponent().removeKeyListener(ontologyTermListener);
				sourceComboBox.removeActionListener(termSelectionListener);
				
				// and select the item    so far so good. 
				sourceComboBox.setSelectedItem(selection);
				
				// now add back listeners
				// these both fire, but this doesn't do much (keyListener gets return key). 
				sourceComboBox.getEditor().getEditorComponent().addKeyListener(ontologyTermListener);
				sourceComboBox.addActionListener(termSelectionListener);
				
			}
			System.out.println("FormFieldOLS TermSelectionListener   selection = " + selection);
			
			
			int separatorIndex = selection.indexOf(ONTOLOGY_ID_NAME_SEPARATOR);
			
			// this will still be true if no items were in the list 
			if(separatorIndex == -1) {
				dataField.setAttribute(attributeId, selection, true);
				return;
			}
			
			String termId = selection.substring(0, separatorIndex);
			// String termName = selection.substring(separatorIndex + ONTOLOGY_ID_NAME_SEPARATOR.length());
				
			dataField.setAttribute(attributeId, termId, true);
			
			//dataField.setAttribute(DataField.ONTOLOGY_TERM_NAME, termName, false);
			
			//ontologyTermSelector.setSize(ontologyTermSelector.getPreferredSize());
		}
	}
	
	
	public String getCurrentOntologyId() {
		
		String ontologyId = ontologySelector.getSelectedItem().toString();
		
		int idNameSeparatorIndex = ontologyId.indexOf(ONTOLOGY_ID_NAME_SEPARATOR);
		if(idNameSeparatorIndex > 0) {
			ontologyId = ontologyId.substring(0, idNameSeparatorIndex);
		}
		
		System.out.println("OntologyTermSelector getCurrentOntologyId ontologyId = " + ontologyId);
		
		return ontologyId;
	}
	
	public void setCurrentOntology(String ontologyId) {
		
		if (ontologyId == null)
			return;
		
		int termCount = ontologySelector.getItemCount();
		for (int i=0; i<termCount; i++) {
			if(ontologySelector.getItemAt(i).toString().startsWith(ontologyId)) {
				ontologySelector.setSelectedIndex(i);
				return;
			}
		}
	}
	
	public void setOntologyComboBoxMaxWidth(int width) {
		ontologySelector.setMaximumWidth(width);
	}
	
	public void setLabelMinWidth(int width) {
		int h = (int)nameLabel.getPreferredSize().getHeight();
		
		nameLabel.setPreferredSize(new Dimension(width, h));
		nameLabel.setMinimumSize(new Dimension(width, h));
	}
	
	public void removeAllItems() {
		ontologyTermSelector.removeActionListener(termSelectionListener);
		ontologyTermSelector.removeKeyListener(ontologyTermListener);
		ontologyTermSelector.removeAllItems();
		ontologyTermSelector.addActionListener(termSelectionListener);
		ontologyTermSelector.addKeyListener(ontologyTermListener);
	}
	
}
