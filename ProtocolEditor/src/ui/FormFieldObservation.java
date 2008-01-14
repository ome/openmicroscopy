package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.components.DataFieldComboBox;
import ui.components.OntologyTermSelector;

public class FormFieldObservation extends FormField {
	
	public static final String FREE_TEXT = "Text";
	public static final String NUMBER = "Number";
	public static final String BOOLEAN = "True/False";
	public static String[] dataTypes = {FREE_TEXT, NUMBER, BOOLEAN};
	DataFieldComboBox dataTypeSelector;
	OntologyTermSelector unitTermSelector;
	
	public FormFieldObservation(IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		int labelsMinWidth = 100;
		int ontologySelectorsMaxWidth = 75;
		
		
		JLabel dataTypeLabel = new JLabel("Data Type: ");
		dataTypeSelector = new DataFieldComboBox(dataField, DataFieldConstants.OBSERVATION_TYPE, dataTypes);
		dataTypeSelector.addActionListener(new DataTypeSelectorListener());
		dataTypeSelector.setMaximumWidth(100);
		horizontalBox.add(dataTypeLabel);
		horizontalBox.add(dataTypeSelector);
		
		OntologyTermSelector entityTermSelector = new OntologyTermSelector(
				dataField, DataFieldConstants.OBSERVATION_ENTITY_TERM_ID, "  Entity", new String[] {"GO", "CL"});
		entityTermSelector.setLabelMinWidth(labelsMinWidth);
		entityTermSelector.setOntologyComboBoxMaxWidth(ontologySelectorsMaxWidth);
		
		
		OntologyTermSelector attributeTermSelector = new OntologyTermSelector(
				dataField, DataFieldConstants.OBSERVATION_ATTRIBUTE_TERM_ID, "  Attribute", new String[] {"PATO"});
		attributeTermSelector.setLabelMinWidth(labelsMinWidth);
		attributeTermSelector.setOntologyComboBoxMaxWidth(ontologySelectorsMaxWidth);
		
		
		unitTermSelector = new OntologyTermSelector(
				dataField, DataFieldConstants.OBSERVATION_UNITS_TERM_ID, "  Units", new String[] {"UO"});
		unitTermSelector.setLabelMinWidth(labelsMinWidth);
		unitTermSelector.setOntologyComboBoxMaxWidth(ontologySelectorsMaxWidth);
		
		Box termSelectorsVerticalBox = Box.createVerticalBox();
		termSelectorsVerticalBox.add(entityTermSelector);
		termSelectorsVerticalBox.add(attributeTermSelector);
		termSelectorsVerticalBox.add(unitTermSelector);
		
		this.add(termSelectorsVerticalBox, BorderLayout.CENTER);
		
		// set controlls etc.
		String observationDataType = dataField.getAttribute(DataFieldConstants.OBSERVATION_TYPE);
		if (observationDataType != null) {
			dataTypeSelector.setSelectedItem(observationDataType);
		} else
			// causes unitTermSelector to be set invisible (untis not required) 
			dataTypeSelector.setSelectedItem(FREE_TEXT);	// causes dataField attribute to be set to this default
		
		
	}
	
	public class DataTypeSelectorListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (dataTypeSelector.getSelectedItem().equals(NUMBER)) {
				setUnitTermSelectorVisibility(true);
			} else {
				// if not visible, reset attribute value to null. 
				dataField.setAttribute(DataFieldConstants.OBSERVATION_UNITS_TERM_ID, null, false);
				unitTermSelector.removeAllItems();
				setUnitTermSelectorVisibility(false);
			}
		}
	}
	
	public void setUnitTermSelectorVisibility(boolean visible) {
		unitTermSelector.setVisible(visible);
	}
}