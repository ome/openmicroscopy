package xmlMVC;

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;

public class AttributesDialog extends JDialog{
	
	JComponent parent;
	DataField dataField;
	
	Box customAttributesBox;
	Boolean textChanged = false;
	
	TextChangedListener textChangedListener = new TextChangedListener();
	FocusChangedListener focusChangedListener = new FocusChangedListener();
	
	ArrayList<AttributeEditor> customAttributesFields = new ArrayList<AttributeEditor>();
	
	
	public AttributesDialog(JComponent parent, DataField dataField) {
		
		this.parent = parent;
		this.dataField = dataField;
		
		setModal(false);
		setUndecorated(true);
		
		customAttributesBox = Box.createVerticalBox();
		
		displayAllAttributes();
		
		getContentPane().add(customAttributesBox, BorderLayout.CENTER);
		
		pack();
		//setLocationRelativeTo(parent);
		
		//setVisible(true);
		
		System.out.println("AttributesDialog Constructor..");
	}
	
	
	public void closeAttributeDialog() {
		// save attributes back to parent FormFieldCustom, then...
		dispose();
	}
	
	public void showAttributesDialog() {
		setLocationRelativeTo(parent);
		setVisible(true);
	}
	
	public void displayAllAttributes() {
		LinkedHashMap<String, String> allAttributes = dataField.getAllAttributes();
		
		Iterator keyIterator = allAttributes.keySet().iterator();
		
		while (keyIterator.hasNext()) {
			String name = (String)keyIterator.next();
			String value = allAttributes.get(name);
			
			// don't display these attributes
			if ((name.equals(DataField.ELEMENT_NAME )) || (name.equals(DataField.INPUT_TYPE))
					|| (name.equals(DataField.SUBSTEPS_COLLAPSED))) continue;
			
			AttributeEditor attributeEditor = new AttributeEditor(name, value, textChangedListener, focusChangedListener);
			// keep a list of fields
			customAttributesFields.add(attributeEditor);
			customAttributesBox.add(attributeEditor);
		}
	}
	
	public void updateDataField() {
		for (AttributeEditor field: customAttributesFields) {
			dataField.setAttribute(field.getAttributeName(), field.getTextFieldText(), false);
		}
	}

	public class TextChangedListener implements KeyListener {
		public void keyTyped(KeyEvent event) {
			textChanged = true;
		}
		public void keyPressed(KeyEvent event) {}
		public void keyReleased(KeyEvent event) {}
	}
	
	public class FocusChangedListener implements FocusListener {
		
		public void focusLost(FocusEvent event) {
			if (textChanged) {
				updateDataField();
				textChanged = false;
			}
		}
		public void focusGained(FocusEvent event) {}
	}

}
