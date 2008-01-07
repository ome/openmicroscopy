package ui.components;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

import tree.DataField;

public class AttributeEditor extends JPanel {
	
boolean textChanged;
	
	DataField dataField;
	
	TextChangedListener textChangedListener = new TextChangedListener();
	FocusListener focusChangedListener = new FocusChangedListener();
	
	JTextField attributeTextField;
	JLabel attributeName;
	
	// constructor creates a new panel and adds a name and text field to it.
	public AttributeEditor(DataField dataField, String attribute, String value) {
		this(dataField, attribute, attribute, value);
	}
	
	public AttributeEditor(DataField dataField, String label, String attribute, String value) {
		
		this.dataField = dataField;
		
		this.setBorder(new EmptyBorder(3,3,3,3));
		attributeName = new JLabel(label);
		attributeTextField = new JTextField(value);
		attributeTextField.setName(attribute);
		attributeTextField.setColumns(15);
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		attributeTextField.addKeyListener(textChangedListener);
		attributeTextField.addFocusListener(focusChangedListener);
		this.add(attributeName);
		this.add(attributeTextField);
	}
		
	public String getTextFieldText() {
			return attributeTextField.getText();
	}
	public String getAttributeName() {
		return attributeName.getText();
	}
	public void setTextFieldText(String text) {
		attributeTextField.setText(text);
	}
	// to allow more precise manipulation of this field
	public JTextField getTextField() {
		return attributeTextField;
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
