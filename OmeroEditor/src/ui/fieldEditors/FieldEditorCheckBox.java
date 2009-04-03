package ui.fieldEditors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;

public class FieldEditorCheckBox extends FieldEditor {
	
	JCheckBox defaultCheckBox;
	
	boolean defaultValue;
	
	public FieldEditorCheckBox(IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		defaultValue = dataField.isAttributeTrue(DataFieldConstants.DEFAULT);
		defaultCheckBox = new JCheckBox("Default checked", defaultValue);
		
		// if no default value is set (null), set it to false;
		if (dataField.getAttribute(DataFieldConstants.DEFAULT) == null) 
			dataField.setAttribute(DataFieldConstants.DEFAULT, DataFieldConstants.FALSE, false);
		
		defaultCheckBox.addActionListener(new DefaultCheckBoxListener());
		
		attributeFieldsPanel.add(defaultCheckBox);
		
		// this is called by the super() constructor, but at that time
		// not all components will have been instantiated. Calls enableEditing()
		refreshLockedStatus();
	}

	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();	// also calls refreshLockedStatus() and enableEditing()
		defaultValue = dataField.isAttributeTrue(DataFieldConstants.DEFAULT);
		defaultCheckBox.setSelected(defaultValue);
	} 
	
	public class DefaultCheckBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			 defaultValue = defaultCheckBox.isSelected();
			 
			 String defaultBoolean = Boolean.toString(defaultValue);
			 dataField.setAttribute(DataFieldConstants.DEFAULT, defaultBoolean, true);
		}
		
	}
	
	/**
	 * This is called by the superclass FieldEditor.dataFieldUpdated().
	 * Need to refresh the enabled status of additional components in this subclass. 
	 */
	public void enableEditing(boolean enabled) {
		super.enableEditing(enabled);
		
		// need to check != null because this is called by the super() constructor
		// before all subclass components have been instantiated. 
		if (defaultCheckBox != null) {
			defaultCheckBox.setEnabled(enabled);
		}
	}
}
