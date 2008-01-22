package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;

public class FormFieldCheckBox extends FormField {
	
	boolean checkedValue;
	JCheckBox checkBox;
	
	public FormFieldCheckBox (IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		checkedValue = dataField.isAttributeTrue(DataFieldConstants.VALUE);
		
		checkBox = new JCheckBox(" ", checkedValue);
		checkBox.addActionListener(new CheckBoxListener());
		checkBox.addFocusListener(componentFocusListener);
		checkBox.setBackground(null);
		
		horizontalBox.add(checkBox);
	}
	
	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		checkedValue = dataField.isAttributeTrue(DataFieldConstants.VALUE);
		checkBox.setSelected(checkedValue);
	} 
	
	public class CheckBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			 checkedValue = checkBox.isSelected();
			 
			 String value = Boolean.toString(checkedValue);
			 dataField.setAttribute(DataFieldConstants.VALUE, value, true);
		}
		
	}
	
}
