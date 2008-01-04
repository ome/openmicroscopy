package ui;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

import tree.DataField;

public class AttributeMemoEditor extends JPanel {
	
boolean textChanged;
	
	DataField dataField;
	
	TextChangedListener textChangedListener = new TextChangedListener();
	FocusListener focusChangedListener = new FocusChangedListener();
	
	JTextArea attributeTextField;
	
	// constructor creates a new panel and adds a name and text area to it.
	public AttributeMemoEditor(DataField dataField, String attribute, String value) {
		this(dataField, attribute, attribute, value);
	}
	public AttributeMemoEditor(DataField dataField, String label, String attribute, String value) {
		
		this.dataField = dataField;
		
		this.setBorder(new EmptyBorder(3,3,3,3));
		JLabel attributeName = new JLabel(label);
		attributeTextField = new JTextArea(value);
		attributeTextField.setName(attribute);
		attributeTextField.setRows(5);
		attributeTextField.setLineWrap(true);
		attributeTextField.setWrapStyleWord(true);
		attributeTextField.setMargin(new Insets(3,3,3,3));
		this.setLayout(new BorderLayout());
		attributeTextField.addKeyListener(textChangedListener);
		attributeTextField.addFocusListener(focusChangedListener);
		this.add(attributeName, BorderLayout.NORTH);
		this.add(attributeTextField, BorderLayout.CENTER);
	}
	
	public String getTextAreaText() {
		return attributeTextField.getText();
	}
	public void setTextAreaText(String text) {
		attributeTextField.setText(text);
	}
	public void setTextAreaRows(int rows) {
		attributeTextField.setRows(rows);
	}
	public JTextArea getTextArea() {
		return attributeTextField;
	}
	public void removeFocusListener() {
		attributeTextField.removeFocusListener(focusChangedListener);
	}
	
	
	public class TextChangedListener implements KeyListener {
		
		public void keyTyped(KeyEvent event) {
			textChanged = true;		// some character was typed, so set this flag
		}
		public void keyPressed(KeyEvent event) {}
		public void keyReleased(KeyEvent event) {}
	
	}
	
	public class FocusChangedListener implements FocusListener {
		
		public void focusLost(FocusEvent event) {
			if (textChanged) {
				JTextComponent source = (JTextComponent)event.getSource();
				
				setDataFieldAttribute(source.getName(), source.getText(), true);
				
				textChanged = false;
			}
		}
		public void focusGained(FocusEvent event) {}
	}
	
	// called to update dataField with attribute
	protected void setDataFieldAttribute(String attributeName, String value, boolean notifyUndoRedo) {
		dataField.setAttribute(attributeName, value, notifyUndoRedo);
	}
}
