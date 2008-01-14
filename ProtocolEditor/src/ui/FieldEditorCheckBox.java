package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import tree.DataField;
import tree.DataFieldConstants;

public class FieldEditorCheckBox extends FieldEditor {
	
	JCheckBox defaultCheckBox;
	
	boolean defaultValue;
	
	public FieldEditorCheckBox(DataField dataField) {
		
		super(dataField);
		
		defaultValue = dataField.isAttributeTrue(DataFieldConstants.DEFAULT);
		defaultCheckBox = new JCheckBox("Default checked", defaultValue);
		
		// if no default value is set (null), set it to false;
		if (dataField.getAttribute(DataFieldConstants.DEFAULT) == null) 
			dataField.setAttribute(DataFieldConstants.DEFAULT, DataFieldConstants.FALSE, false);
		
		defaultCheckBox.addActionListener(new DefaultCheckBoxListener());
		
		attributeFieldsPanel.add(defaultCheckBox);
	}

	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
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
}
