package ui;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import ols.OntologyLookUp;

import tree.DataField;

public class FormFieldOLS extends FormField {
	
	String ontologyId;
	JLabel ontologyIdLabel;
	JComboBox ontologyTermField;
	JLabel termDescriptionLabel;
	
	ActionListener termSelectionListener;
	
	public FormFieldOLS(DataField dataField) {
		
		super(dataField);
		
		ontologyId = dataField.getAttribute(DataField.ONTOLOGY_ID);
		ontologyIdLabel = new JLabel(ontologyId);
		
		horizontalBox.add(ontologyIdLabel);
		
		ontologyTermField = new JComboBox();
		ontologyTermField.setEditable(true);
		ontologyTermField.getEditor().getEditorComponent().addKeyListener(new OntologyTermListener());
		termSelectionListener = new TermSelectionListener();
		ontologyTermField.addActionListener(termSelectionListener);
		
		termDescriptionLabel = new JLabel();
		
		horizontalBox.add(ontologyTermField);
		this.add(termDescriptionLabel, BorderLayout.SOUTH);
	}
	
	
	public class OntologyTermListener implements KeyListener {
		
		public void keyReleased(KeyEvent event) {

			JTextComponent source = (JTextComponent)event.getSource();
			String input = source.getText();
			System.out.println("FormFieldOLS input: " + input);
			
			if (input.length() < 3) return;
			
			ontologyTermField.removeActionListener(termSelectionListener);
			ontologyTermField.removeAllItems();
			
			Map autoCompleteOptions = OntologyLookUp.getTermsByName(input, ontologyId);
			
			for (Iterator i = autoCompleteOptions.keySet().iterator(); i.hasNext();){
				String key = (String) i.next();
				String name = key + ", " + autoCompleteOptions.get(key).toString();
				ontologyTermField.addItem(name);
			}
			ontologyTermField.setPopupVisible(true);
			source.setText(input);
			ontologyTermField.addActionListener(termSelectionListener);
		}
		public void keyPressed(KeyEvent arg0) {}
		public void keyTyped(KeyEvent event) {}
	}
	
	
	public class TermSelectionListener implements ActionListener {
		public void actionPerformed (ActionEvent event) {
			String selection = ontologyTermField.getSelectedItem().toString();
			int firstCommaIndex = selection.indexOf(",");
			String termId = selection.substring(0, firstCommaIndex);
			
			System.out.println(termId);
			Map termMetaData = OntologyLookUp.getTermMetadata(termId, ontologyId);
			
			String htmlMetaDataLabelText = "<html>";
			String definitionLabel = "";
			for (Iterator i = termMetaData.keySet().iterator(); i.hasNext();){
				String key = (String) i.next();
				String name = key + ", " + termMetaData.get(key).toString();
				if (key.equals("definition")) 
					definitionLabel = name;
				else
					htmlMetaDataLabelText = htmlMetaDataLabelText + name + "<br>";
			}
			htmlMetaDataLabelText = htmlMetaDataLabelText + "</html>";
			
			termDescriptionLabel.setText(definitionLabel);
			termDescriptionLabel.setToolTipText(htmlMetaDataLabelText);
		}
	}
	
//	 overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		ontologyId = dataField.getAttribute(DataField.ONTOLOGY_ID);
		ontologyIdLabel.setText(ontologyId);
	}

}
