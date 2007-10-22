package xmlMVC;

import java.awt.event.FocusListener;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class AttributeEditor extends JPanel {
	
	JTextField attributeTextField;
	JLabel attributeName;
	// constructor creates a new panel and adds a name and text field to it.
	public AttributeEditor(String Label, String Value, KeyListener textChangedListener, FocusListener focusChangedListener) {
		this.setBorder(new EmptyBorder(3,3,3,3));
		attributeName = new JLabel(Label);
		attributeTextField = new JTextField(Value);
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
