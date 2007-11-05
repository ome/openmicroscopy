package ui;

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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

import org.w3c.dom.*;

import tree.DataField;
import tree.DataFieldObserver;

public class FieldEditor extends AbstractDataFieldPanel implements DataFieldObserver {
	
	public static final Dimension MINIMUM_SIZE = new Dimension(290,300);
	
	JPanel attributeFieldsPanel;
	JPanel inputTypePanel;
	JComboBox inputTypeSelector;
	AttributeEditor nameFieldEditor;
	AttributeMemoEditor descriptionFieldEditor;
	AttributeEditor urlFieldEditor;
	
	FocusChangedListener focusChangedListener;
	TextChangedListener textChangedListener;
	
	//XMLView xmlView; 	// the UI container for displaying this panel
	
	public FieldEditor() {	// a blank 
		this.setPreferredSize(MINIMUM_SIZE);
		this.setMinimumSize(MINIMUM_SIZE);
	}
	
	public FieldEditor(DataField dataField) {
		this.dataField = dataField;
		dataField.addDataFieldObserver(this);
		buildPanel();
	}
	
	public void buildPanel() {
		
		attributeFieldsPanel = new JPanel();	// a sub-panel to hold all components
		attributeFieldsPanel.setLayout(new BoxLayout(attributeFieldsPanel, BoxLayout.Y_AXIS));
		attributeFieldsPanel.setBorder(new EmptyBorder(5, 5, 5,5));
		
		textChangedListener = new TextChangedListener();
		focusChangedListener = new FocusChangedListener();
		
		nameFieldEditor = new AttributeEditor("Field Name: ", DataField.ELEMENT_NAME, dataField.getName());
		attributeFieldsPanel.add(nameFieldEditor);
		
		// Drop-down selector of input-type. 
		inputTypePanel = new JPanel();
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
		
		descriptionFieldEditor = new AttributeMemoEditor("Description: ", DataField.DESCRIPTION, dataField.getDescription());
		attributeFieldsPanel.add(descriptionFieldEditor);
		
		urlFieldEditor = new AttributeEditor("Url: ", DataField.URL, dataField.getURL());
		attributeFieldsPanel.add(urlFieldEditor);
		
		this.setLayout(new BorderLayout());
		this.add(attributeFieldsPanel, BorderLayout.NORTH);
		
		this.setPreferredSize(MINIMUM_SIZE);
		this.setMinimumSize(MINIMUM_SIZE);
		this.validate();
	}
	
	// called by dataField when something changes, eg undo() previous editing
	public void dataFieldUpdated() {
		nameFieldEditor.setTextFieldText(dataField.getAttribute(DataField.ELEMENT_NAME));
		descriptionFieldEditor.setTextAreaText(dataField.getAttribute(DataField.DESCRIPTION));
		urlFieldEditor.setTextFieldText(dataField.getAttribute(DataField.URL));
	}
	
	// called when focus lost
	public void updateDataField() {
		dataField.setAttribute(DataField.ELEMENT_NAME, nameFieldEditor.getTextFieldText(), true);
		// dataField.setAttribute(DataField.DESCRIPTION, descriptionFieldEditor.getTextAreaText());

		String url = urlFieldEditor.getTextFieldText();
		if ((url.length() > 0) && !(url.startsWith("http"))) url = "http://" + url;
		// dataField.setAttribute(DataField.URL, url);
		

		
		dataField.notifyDataFieldObservers();
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
		dataField.notifyXmlObservers();		// updates UI with new formField & fieldEditor
	}

}
