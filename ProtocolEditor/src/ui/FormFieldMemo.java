package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.w3c.dom.Element;

import tree.DataField;

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
		textInput.setName(DataField.VALUE);
		textInput.addFocusListener(focusChangedListener);
		textInput.addKeyListener(textChangedListener);
		horizontalBox.add(textScroller);
		
		//setExperimentalEditing(false);	// default created as uneditable
	}
	
	
	// overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		textInput.setText(dataField.getAttribute(DataField.VALUE));
	}
	
//	 overridden by subclasses that have input components
	public void setExperimentalEditing(boolean enabled) {
		
		if (enabled) textInput.setForeground(Color.BLACK);
		else textInput.setForeground(textInput.getBackground());
		
		textInput.setEditable(enabled);
	}
}
