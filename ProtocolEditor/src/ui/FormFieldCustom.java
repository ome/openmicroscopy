package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JTextField;

import tree.DataField;
import util.ImageFactory;

public class FormFieldCustom extends FormField {
	
	
	AttributesDialog attDialog;
	JButton showAttributesButton;
	JTextField textInput;
	boolean attributesDialogVisible = false;

	public FormFieldCustom(DataField dataField) {
		super(dataField);
		
		if (areAnyCustomAttributes()) {
			showAttributesButton = new JButton(ImageFactory.getInstance().getIcon(ImageFactory.NOTE_PAD));
			//showAttributesButton.addMouseListener(new FormPanelMouseListener());
			showAttributesButton.addActionListener(new ShowAttributesListener());
			showAttributesButton.setToolTipText("Display attributes");
			showAttributesButton.setBorder(null);
			horizontalBox.add(showAttributesButton);
		}
		
		textInput = new JTextField();
		textInput.addMouseListener(new FormPanelMouseListener());
		textInput.setName(DataField.TEXT_NODE_VALUE);
		textInput.addFocusListener(focusChangedListener);
		textInput.addKeyListener(textChangedListener);
		checkForTextNodeValue();
		
	}

	public void checkForTextNodeValue() {
		String textNodeValue = dataField.getAttribute(DataField.TEXT_NODE_VALUE);
		//System.out.println("FormFieldCustom constructor: textNodeValue = " + textNodeValue);
		if (textNodeValue != null) {
			textInput.setText(textNodeValue);
			horizontalBox.add(textInput);
		}
	}
	
	// overridden by subclasses (when focus lost) if they have values that need saving 
	public void updateDataField() {
		if (textInput != null)	
			dataField.setAttribute(DataField.TEXT_NODE_VALUE, textInput.getText(), true);
	}
	
	// overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdatedOtherAttributes() {
		checkForTextNodeValue();
	}
	
	// toggle visibility of attributes dialog
	public class ShowAttributesListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			
			// first select the field (clears ALL fields - all dialogs hidden) then highlights this field
			int clickType = event.getModifiers();
			if (clickType == XMLView.SHIFT_CLICK) {
				panelClicked(false);
			} else
				panelClicked(true);
			
			// ...NOW show this dialog
			attributesDialogVisible = !attributesDialogVisible;
			showAttributes(attributesDialogVisible);
		}
	}
	
	public void showAttributes(boolean visible) {
		
		if (attDialog == null) attDialog = new AttributesDialog(this, dataField);
		
		attributesDialogVisible = visible;
		
		if (visible) attDialog.showAttributesDialog();	// also repositions dialog
		else attDialog.dispose();
	}
	
	// called when user clicks on panel
	public void setHighlighted(boolean highlight) {
		super.setHighlighted(highlight);
		
		// always hide attributes dialog when de-selecting a field
		if (!highlight) showAttributes(false);
	}
	
	// check to see if there are any custom attributes to display / edit
	public boolean areAnyCustomAttributes() {
		Iterator keyIterator = dataField.getAllAttributes().keySet().iterator();
		
		while (keyIterator.hasNext()) {
			String name = (String)keyIterator.next();
			// don't count these attributes
			if ((name.equals(DataField.ELEMENT_NAME )) || (name.equals(DataField.INPUT_TYPE))
					|| (name.equals(DataField.SUBSTEPS_COLLAPSED))) continue;
			return true;
		}
		return false;
	}
}
