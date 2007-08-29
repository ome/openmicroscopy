package xmlMVC;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.w3c.dom.*;

public class FieldEditor extends JPanel{
	
	public static final Dimension MINIMUM_SIZE = new Dimension(290,300);
	
	JPanel attributeFieldsPanel;
	JComboBox inputTypeSelector;
	AttributeEditor nameFieldEditor;
	AttributeMemoEditor descriptionFieldEditor;
	AttributeEditor urlFieldEditor;

	boolean textChanged;
	
	//XMLView xmlView; 	// the UI container for displaying this panel
	
	DataField dataField;
	
	public FieldEditor() {	// a blank 
		this.setPreferredSize(MINIMUM_SIZE);
		this.setMinimumSize(MINIMUM_SIZE);
	}
	
	public FieldEditor(DataField dataField) {
		this.dataField = dataField;
		buildPanel();
	}
	
	public void buildPanel() {
		
		attributeFieldsPanel = new JPanel();	// a sub-panel to hold all components
		attributeFieldsPanel.setLayout(new BoxLayout(attributeFieldsPanel, BoxLayout.Y_AXIS));
		attributeFieldsPanel.setBorder(new EmptyBorder(5, 5, 5,5));
		
		nameFieldEditor = new AttributeEditor("Field Name: ", dataField.getName());
		attributeFieldsPanel.add(nameFieldEditor);
		
		// Drop-down selector of input-type. 
		JPanel inputTypePanel = new JPanel();
		inputTypePanel.setBorder(new EmptyBorder(3,3,3,3));
		inputTypePanel.setLayout(new BoxLayout(inputTypePanel, BoxLayout.X_AXIS));
		
		inputTypeSelector = new JComboBox(DataField.UI_INPUT_TYPES);
		// Set it to the current input type
		if (dataField.getInputType() != null) {
			for (int i=0; i<DataField.UI_INPUT_TYPES.length; i++)
				if (dataField.getInputType().equals(DataField.INPUT_TYPES[i]))
					inputTypeSelector.setSelectedIndex(i);
		}
		
		inputTypePanel.add(new JLabel("Field Type: "));
		inputTypePanel.add(inputTypeSelector);
		// add Listener to drop-down AFTER setting it to the correct input type!
		inputTypeSelector.addActionListener(new inputTypeSelectorListener());
		attributeFieldsPanel.add(inputTypePanel);
		
		descriptionFieldEditor = new AttributeMemoEditor("Description: ", dataField.getDescription());
		attributeFieldsPanel.add(descriptionFieldEditor);
		
		urlFieldEditor = new AttributeEditor("Url: ", dataField.getURL());
		attributeFieldsPanel.add(urlFieldEditor);
		
		this.setLayout(new BorderLayout());
		this.add(attributeFieldsPanel, BorderLayout.NORTH);
		
		this.setPreferredSize(MINIMUM_SIZE);
		this.setMinimumSize(MINIMUM_SIZE);
		this.validate();
	}
	
	// called by dataField when something changes. 
	public void dataFieldUpdated() {
		nameFieldEditor.setTextFieldText(dataField.getAttribute(DataField.ELEMENT_NAME));
		descriptionFieldEditor.setTextAreaText(dataField.getAttribute(DataField.DESCRIPTION));
		urlFieldEditor.setTextFieldText(dataField.getAttribute(DataField.URL));
	}
	
	// called when focus lost
	public void updateDataField() {
		dataField.setName(nameFieldEditor.getTextFieldText(), false);
		dataField.setAttribute(DataField.DESCRIPTION, descriptionFieldEditor.getTextAreaText(), false);

		String url = urlFieldEditor.getTextFieldText();
		if ((url.length() > 0) && !(url.startsWith("http"))) url = "http://" + url;
		dataField.setAttribute(DataField.URL, url, false);
		
		updateModelsOtherAttributes();	// takes care of other attributes
		
		dataField.notifyDataFieldObservers();
	}
	
	//	called when focus lost. Overridden by subclasses if they have other attribute fields
	public void updateModelsOtherAttributes() {	}
	
	public class AttributeEditor extends JPanel {
			
		JTextField attributeTextField;
		JLabel attributeName;
		// constructor creates a new panel and adds a name and text field to it.
		public AttributeEditor(String Label, String Value) {
			this.setBorder(new EmptyBorder(3,3,3,3));
			attributeName = new JLabel(Label);
			attributeTextField = new JTextField(Value);
			attributeTextField.setColumns(15);
			this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			attributeTextField.addKeyListener(new textChangedListener());
			attributeTextField.addFocusListener(new focusChangedListener());
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
	}
		
	public class AttributeMemoEditor extends JPanel{
		JTextArea attributeTextField;
		// constructor creates a new panel and adds a name and text area to it.
		public AttributeMemoEditor(String Label, String Value) {
			this.setBorder(new EmptyBorder(3,3,3,3));
			JLabel attributeName = new JLabel(Label);
			attributeTextField = new JTextArea(Value);
			attributeTextField.setRows(5);
			attributeTextField.setLineWrap(true);
			attributeTextField.setWrapStyleWord(true);
			attributeTextField.setMargin(new Insets(3,3,3,3));
			this.setLayout(new BorderLayout());
			attributeTextField.addKeyListener(new textChangedListener());
			attributeTextField.addFocusListener(new focusChangedListener());
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
	
	public class textChangedListener implements KeyListener {
		
		public void keyTyped(KeyEvent event) {
			textChanged = true;

			char keyChar = event.getKeyChar();
			int keyCharacter = (int)keyChar;
			if (keyCharacter == 10) {	// == "Enter"
				updateDataField();
			}

		}
		public void keyPressed(KeyEvent event) {}
		public void keyReleased(KeyEvent event) {}
	
	}
	
	public class focusChangedListener implements FocusListener {
		
		public void focusLost(FocusEvent event) {
			if (textChanged) {
				updateDataField();
				textChanged = false;
			}
		}
		public void focusGained(FocusEvent event) {}
	}
	
	public class inputTypeSelectorListener implements ActionListener {
		
		public void actionPerformed (ActionEvent event) {
			JComboBox source = (JComboBox)event.getSource();
			int selectedIndex = source.getSelectedIndex();
			String newType = DataField.INPUT_TYPES[selectedIndex];
			inputTypeSelectorChanged(newType);
		}
	}	
	
	public void inputTypeSelectorChanged(String newType) {
		dataField.changeDataFieldInputType(newType);
		// the above call doesn't notify others (mostly don't want to)
		dataField.notifyDataFieldObservers();
	}

}
