package xmlMVC;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import xmlMVC.FieldEditor.FocusChangedListener;
import xmlMVC.FieldEditor.inputTypeSelectorListener;

public class FieldEditorDropDown extends FieldEditor {
	
	JComboBox defaultValueComboBox;
	ActionListener defaultValueSelectionListener;
	
	private AttributeMemoEditor optionsFieldEditor;
	
	public FieldEditorDropDown (DataField dataField) {
		
		super(dataField);
		
		// comma-delimited list of options
		String dropDownOptions = dataField.getAttribute(DataField.DROPDOWN_OPTIONS);
		String defaultValue = dataField.getAttribute(DataField.DEFAULT);
		String[] ddOptions = {" "};
		
		optionsFieldEditor = new AttributeMemoEditor
			("Drop-down options: (separate with commas)", dropDownOptions);
		optionsFieldEditor.setToolTipText("Add keywords, separated by commas");
		optionsFieldEditor.setTextAreaRows(4);
		attributeFieldsPanel.add(optionsFieldEditor);
		
		
//		 Drop-down selector for default value
		JPanel dropDownDefaultPanel = new JPanel();
		dropDownDefaultPanel.setBorder(new EmptyBorder(3,3,3,3));
		dropDownDefaultPanel.setLayout(new BoxLayout(dropDownDefaultPanel, BoxLayout.X_AXIS));
		
		defaultValueComboBox = new JComboBox();
		
		if (dropDownOptions != null) {
			setDropDownOptions(dropDownOptions);
			// also sets it to correct default Value
		}
		
		dropDownDefaultPanel.add(new JLabel("Default Value: "));
		dropDownDefaultPanel.add(defaultValueComboBox);
		defaultValueSelectionListener = new DefaultValueSelectionListener();
		defaultValueComboBox.addActionListener(defaultValueSelectionListener);
		attributeFieldsPanel.add(dropDownDefaultPanel);
	}
	
	
	public void setDropDownOptions(String options) {
		if (options != null) {
			String dropDownOptions = options;
			String [] ddOptions = dropDownOptions.split(",");
			for(int i=0; i<ddOptions.length; i++) {
				ddOptions[i] = ddOptions[i].trim();
			}	
			
			// so that action not fired when changing options
			defaultValueComboBox.removeActionListener(defaultValueSelectionListener);
			
			defaultValueComboBox.removeAllItems();
			for(int i=0; i<ddOptions.length; i++) {
				defaultValueComboBox.addItem(ddOptions[i]);
			}
			
//			 Set it to the current defaultValue, (if it exists in the new ddOptions)
			String defaultValue = dataField.getAttribute(DataField.DEFAULT);
			if (defaultValue != null) {
				for (int i=0; i<ddOptions.length; i++)
					if (defaultValue.equals(ddOptions[i]))
						defaultValueComboBox.setSelectedIndex(i);
			}
			
			//need to update default value (in case it wasn't in the new ddOptions)
			defaultValue = defaultValueComboBox.getSelectedItem().toString();
			
			defaultValueComboBox.addActionListener(defaultValueSelectionListener);
		}
	}
	
//	 subclasses override these if they have attributes other than name, desc, inputType.
	//	 called when focus lost
	public void updateModelsOtherAttributes() {	
		String dropDownOptions = optionsFieldEditor.getTextAreaText();

		dataField.setAttribute(DataField.DROPDOWN_OPTIONS, dropDownOptions, false);
		
		if (dropDownOptions != null)
			setDropDownOptions(dropDownOptions);
	}
	
	public class DefaultValueSelectionListener implements ActionListener {
		public void actionPerformed (ActionEvent event) {
			dataField.setAttribute(DataField.DEFAULT, defaultValueComboBox.getSelectedItem().toString(), false);
		}
	}

}
