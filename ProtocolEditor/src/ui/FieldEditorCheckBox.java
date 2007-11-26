package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import tree.DataField;

public class FieldEditorCheckBox extends FieldEditor {
	
	JCheckBox defaultCheckBox;
	
	boolean defaultValue;
	
	public FieldEditorCheckBox(DataField dataField) {
		
		super(dataField);
		
		// if no default value is set (null), set it to false;
		if (dataField.getAttribute(DataField.DEFAULT) == null) 
			dataField.setAttribute(DataField.DEFAULT, DataField.FALSE, false);
		
		defaultValue = dataField.isAttributeTrue(DataField.DEFAULT);
		
		defaultCheckBox = new JCheckBox("Default checked", defaultValue);
		defaultCheckBox.addActionListener(new DefaultCheckBoxListener());
		
		attributeFieldsPanel.add(defaultCheckBox);
	}

	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		defaultValue = dataField.isAttributeTrue(DataField.DEFAULT);
		defaultCheckBox.setSelected(defaultValue);
	} 
	
	public class DefaultCheckBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			 defaultValue = defaultCheckBox.isSelected();
			 
			 String defaultBoolean = Boolean.toString(defaultValue);
			 dataField.setAttribute(DataField.DEFAULT, defaultBoolean, true);
		}
		
	}
}
