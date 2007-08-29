package xmlMVC;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import xmlMVC.FormField.focusLostUpdatDataFieldListener;
import xmlMVC.FormField.formPanelMouseListener;

public class FormFieldNumber extends FormField {
	
	JTextField numberTextBox;
	JLabel unitsLabel;
	
	
	public FormFieldNumber (DataField dataField) {
		super(dataField);
		
		String valueString = dataField.getAttribute(DataField.VALUE);
		
		String units = dataField.getAttribute(DataField.UNITS);
		
		numberTextBox = new JTextField(valueString);
		numberTextBox.addMouseListener(new formPanelMouseListener());
		numberTextBox.addFocusListener(new NumberCheckerListener());
		numberTextBox.addFocusListener(new focusLostUpdatDataFieldListener());
		numberTextBox.setToolTipText("Must enter a number");
		
		unitsLabel = new JLabel(units);
		
		horizontalBox.add(numberTextBox);
		horizontalBox.add(Box.createHorizontalStrut(10));
		horizontalBox.add(unitsLabel);
		
	}
	
	public void setUnits(String units) {
		unitsLabel.setText(units);
	}
	//used to load default values etc. Have to update dataField. 
	public void setValue(String newValue) {
		numberTextBox.setText(newValue);
		updateDataField();
	}
	
	
	public class NumberCheckerListener implements FocusListener {
		public void focusLost(FocusEvent event){	
			String number = numberTextBox.getText();
			float value;
			try {
				if (number.length() > 0) {
					value = Float.parseFloat(number);
					numberTextBox.setBackground(Color.WHITE);
				}
			}catch (Exception ex) {
				numberTextBox.setBackground(Color.RED);
			}
		}
		public void focusGained(FocusEvent event){	}
	}
	
	// overridden by subclasses (when focus lost) if they have values that need saving 
	public void updateDataField() {
		System.out.println("FormFieldNumber.updateDataField");
		dataField.setAttribute(DataField.VALUE, numberTextBox.getText(), false);
	}
	
//	 overridden by subclasses that have input components
	public void setExperimentalEditing(boolean enabled) {
		
		if (enabled) numberTextBox.setForeground(Color.BLACK);
		else numberTextBox.setForeground(Color.WHITE);
		
		numberTextBox.setEditable(enabled);
	}
	
//	 overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdatedOtherAttributes() {
		numberTextBox.setText(dataField.getAttribute(DataField.VALUE));
		setUnits(dataField.getAttribute(DataField.UNITS));
	}

}
