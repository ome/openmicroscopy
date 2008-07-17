package ui.formFields;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import fields.FieldPanel;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.components.DataFieldComboBox;
import ui.components.OntologyTermSelector;

public class FormFieldObservation extends FieldPanel {
	
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
		
		/*
         * Want to add the termSelectorBox to the SOUTH of contentsPanel (where descriptionLabel is). 
         * Create new panel to hold both. 
         */
        JPanel termBoxContainer = new JPanel(new BorderLayout());
        termBoxContainer.setBackground(null);
        termBoxContainer.add(descriptionLabel, BorderLayout.NORTH);
        termBoxContainer.add(termSelectorsVerticalBox, BorderLayout.SOUTH);
		
		contentsPanel.add(termBoxContainer, BorderLayout.SOUTH);
		
		// set controls etc.
		refreshUnitTermSelectorVisibility();
	
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
		
		if (dataTypeSelector != null)	// just in case!
			dataTypeSelector.setEnabled(enabled);
		
		if (entityTermSelector != null)	// just in case!
			entityTermSelector.setEnabled(enabled);
		
		if (attributeTermSelector != null)	// just in case!
			attributeTermSelector.setEnabled(enabled);
		
		if (unitTermSelector != null)	// just in case!
			unitTermSelector.setEnabled(enabled);
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
		return new String[] {DataFieldConstants.OBSERVATION_TYPE,
				DataFieldConstants.OBSERVATION_ENTITY_TERM_IDNAME,
				DataFieldConstants.OBSERVATION_ATTRIBUTE_TERM_IDNAME,
				DataFieldConstants.OBSERVATION_UNITS_TERM_ID};
	}
	
	/**
	 * This method tests to see whether the field has been filled out. 
	 * Observation field requires Observation type, Entity term and Attribute term.
	 * Units is only required if Observation type is NUMBER.
	 * 
	 * @see FormField.isFieldFilled()
	 * @return	True if the field has been filled out by user (Required values are not null)
	 */
	public boolean isFieldFilled() {
		if (dataField.getAttribute(DataFieldConstants.OBSERVATION_TYPE) == null) return false;
		if (dataField.getAttribute(DataFieldConstants.OBSERVATION_ENTITY_TERM_IDNAME) == null) return false;
		if (dataField.getAttribute(DataFieldConstants.OBSERVATION_ATTRIBUTE_TERM_IDNAME) == null) return false;
		if ((dataField.getAttribute(DataFieldConstants.OBSERVATION_TYPE).equals(NUMBER)) &&
				(dataField.getAttribute(DataFieldConstants.OBSERVATION_UNITS_TERM_ID) == null)) return false;
		return true;
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