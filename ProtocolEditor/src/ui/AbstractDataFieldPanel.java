package ui;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

import tree.DataField;

// this class is extended by FormField, FieldEditor and AttributesDialog.
// It defines panels of label + text-field, and listeners that update dataField with attributes entered

public abstract class AbstractDataFieldPanel extends JPanel{

	boolean textChanged;
	
	DataField dataField;
	
	TextChangedListener textChangedListener = new TextChangedListener();
	FocusListener focusChangedListener = new FocusChangedListener();

	// a simple panel that contains a label and text field. Used in Field Editor and Attributes Dialog panels
	public class AttributeEditor extends JPanel {
		
		JTextField attributeTextField;
		JLabel attributeName;
		// constructor creates a new panel and adds a name and text field to it.
		public AttributeEditor(String attribute, String value) {
			this(attribute, attribute, value);
		}
		
		public AttributeEditor(String label, String attribute, String value) {
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
	}

	
	public class AttributeMemoEditor extends JPanel{
		JTextArea attributeTextField;
		// constructor creates a new panel and adds a name and text area to it.
		public AttributeMemoEditor(String attribute, String value) {
			this(attribute, attribute, value);
		}
		public AttributeMemoEditor(String label, String attribute, String value) {
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
	}

	
	public class TextChangedListener implements KeyListener {
		
		public void keyTyped(KeyEvent event) {
			
			char keyChar = event.getKeyChar();
			int keyCharacter = (int)keyChar;
			if (keyCharacter == 10) {	// == "Enter"
				
				textChanged = false;	// stops FocusChangedListener from updating dataField
				
				JTextComponent source = (JTextComponent)event.getSource();
				
				dataField.setAttribute(source.getName(), source.getText(), true);
				
				// need to stop focus going elsewhere. Get it back to source of event
				source.requestFocus();
			} else {
				textChanged = true;		// some character was typed, so set this flag
			}

		}
		public void keyPressed(KeyEvent event) {}
		public void keyReleased(KeyEvent event) {}
	
	}
	
	public class FocusChangedListener implements FocusListener {
		
		public void focusLost(FocusEvent event) {
			if (textChanged) {
				JTextComponent source = (JTextComponent)event.getSource();
				
				dataField.setAttribute(source.getName(), source.getText(), true);
				
				textChanged = false;
			}
		}
		public void focusGained(FocusEvent event) {}
	}
}
