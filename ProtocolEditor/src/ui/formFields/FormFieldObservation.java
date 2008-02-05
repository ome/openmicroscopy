package ui.formFields;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.components.DataFieldComboBox;
import ui.components.OntologyTermSelector;

public class FormFieldObservation extends FormField {
	
	public static final String FREE_TEXT = "Text";
	public static final String NUMBER = "Number";
	public static final String BOOLEAN = "True/False";
	public static String[] dataTypes = {FREE_TEXT, NUMBER, BOOLEAN};
	protected DataFieldComboBox dataTypeSelector;
	protected OntologyTermSelector attributeTermSelector;
	protected OntologyTermSelector entityTermSelector;
	protected OntologyTermSelector unitTermSelector;
	
	public FormFieldObservation(IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		int labelsMinWidth = 100;
		int ontologySelectorsMaxWidth = 75;
		
		
		JLabel dataTypeLabel = new JLabel("Data Type: ");
		// DataFieldComboBox automatically updates its changes to dataField
		dataTypeSelector = new DataFieldComboBox(dataField, DataFieldConstants.OBSERVATION_TYPE, dataTypes);
		dataTypeSelector.setMaximumWidth(100);
		horizontalBox.add(dataTypeLabel);
		horizontalBox.add(dataTypeSelector);
		
		entityTermSelector = new OntologyTermSelector(
				dataField, DataFieldConstants.OBSERVATION_ENTITY_TERM_IDNAME, "  Entity", new String[] {"GO", "CL"});
		entityTermSelector.setLabelMinWidth(labelsMinWidth);
		entityTermSelector.addPropertyChangeListener(new FocusGainedPropertyChangedListener());
		entityTermSelector.setOntologyComboBoxMaxWidth(ontologySelectorsMaxWidth);
		
		
		attributeTermSelector = new OntologyTermSelector(
				dataField, DataFieldConstants.OBSERVATION_ATTRIBUTE_TERM_IDNAME, "  Attribute", new String[] {"PATO"});
		attributeTermSelector.setLabelMinWidth(labelsMinWidth);
		attributeTermSelector.addPropertyChangeListener(new FocusGainedPropertyChangedListener());
		attributeTermSelector.setOntologyComboBoxMaxWidth(ontologySelectorsMaxWidth);
		
		
		unitTermSelector = new OntologyTermSelector(
				dataField, DataFieldConstants.OBSERVATION_UNITS_TERM_ID, "  Units", new String[] {"UO"});
		unitTermSelector.setLabelMinWidth(labelsMinWidth);
		unitTermSelector.addPropertyChangeListener(new FocusGainedPropertyChangedListener());
		unitTermSelector.setOntologyComboBoxMaxWidth(ontologySelectorsMaxWidth);
		
		Box termSelectorsVerticalBox = Box.createVerticalBox();
		termSelectorsVerticalBox.setBorder(new EmptyBorder(0,20,0,0));
		termSelectorsVerticalBox.add(entityTermSelector);
		termSelectorsVerticalBox.add(attributeTermSelector);
		termSelectorsVerticalBox.add(unitTermSelector);
		
		this.add(termSelectorsVerticalBox, BorderLayout.CENTER);
		
		// set controls etc.
		refreshUnitTermSelectorVisibility();
	}
	
	// something has changed at the dataField (eg undo/redo)
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		// change of dataType
		dataTypeSelector.setSelectedItemNoListeners(dataField.getAttribute(DataFieldConstants.OBSERVATION_TYPE));
		refreshUnitTermSelectorVisibility();
		
		entityTermSelector.setSelectedItem(dataField.getAttribute(DataFieldConstants.OBSERVATION_ENTITY_TERM_IDNAME));
		attributeTermSelector.setSelectedItem(dataField.getAttribute(DataFieldConstants.OBSERVATION_ATTRIBUTE_TERM_IDNAME));
		
	}
	
	public void refreshUnitTermSelectorVisibility() {
		boolean newVisibility = ((dataTypeSelector.getSelectedItem() != null) && 
				dataTypeSelector.getSelectedItem().equals(NUMBER));

		if (newVisibility) {
			if (unitTermSelector.isVisible()) {
				unitTermSelector.setSelectedItem(dataField.getAttribute(DataFieldConstants.OBSERVATION_UNITS_TERM_ID));
			}
			else if (unitTermSelector.getSelectedItem() != null) {
				// if units turned on (and still have values) make sure these are updated to dataField
				dataField.setAttribute(DataFieldConstants.OBSERVATION_UNITS_TERM_ID, 
						unitTermSelector.getSelectedItem().toString(), false);
			}
		} else {
			// if not using units, delete the attribute
			dataField.setAttribute(DataFieldConstants.OBSERVATION_UNITS_TERM_ID, null, false);
		}
		
		unitTermSelector.setVisible(newVisibility);
	}
}