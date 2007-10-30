package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.w3c.dom.Element;

import tree.DataField;
import ui.FormField.FocusLostUpdatDataFieldListener;

public class FormFieldMemo extends FormField {
	
	JTextArea textInput;
	
	public FormFieldMemo(DataField dataField) {
		super(dataField);
		
		String value = dataField.getAttribute(DataField.VALUE);
		
		textInput = new JTextArea(value);
		visibleAttributes.add(textInput);
		textInput.setRows(3);
		textInput.setLineWrap(true);
		textInput.setWrapStyleWord(true);
		JScrollPane textScroller = new JScrollPane(textInput);
		textInput.setMargin(new Insets(3,3,3,3));
		textInput.setPreferredSize(new Dimension(300, 100));
		textInput.addMouseListener(new FormPanelMouseListener());
		textInput.addFocusListener(new FocusLostUpdatDataFieldListener());
		horizontalBox.add(textScroller);
		
		setExperimentalEditing(false);	// default created as uneditable
	}
	
	// overridden by subclasses (when focus lost) if they have values that need saving 
	public void updateDataField() {
		dataField.setAttribute(DataField.VALUE, textInput.getText(), false);
	}
	
//	 overridden by subclasses if they have a value and text field
	public void setValue(String newValue) {
		textInput.setText(newValue);
		updateDataField();
	}
	
//	 overridden by subclasses that have input components
	public void setExperimentalEditing(boolean enabled) {
		
		if (enabled) textInput.setForeground(Color.BLACK);
		else textInput.setForeground(textInput.getBackground());
		
		textInput.setEditable(enabled);
	}
}
