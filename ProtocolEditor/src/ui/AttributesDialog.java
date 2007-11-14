package ui;

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
import javax.swing.JLabel;

import tree.DataField;
import tree.DataFieldObserver;

public class AttributesDialog extends JDialog implements DataFieldObserver{
	
	JComponent parent;
	AttributesPanel attributesPanel;
	
	
	public AttributesDialog(JComponent parent, DataField dataField) {
		
		this.parent = parent;
		dataField.addDataFieldObserver(this);
		
		setModal(false);
		setUndecorated(true);
		
		attributesPanel = new AttributesPanel(dataField);
		
		getContentPane().add(attributesPanel, BorderLayout.CENTER);
		
		pack();
		
	}
	
	public void showAttributesDialog() {
		setLocationRelativeTo(parent);
		setVisible(true);
	}
	
	public void dataFieldUpdated() {
		attributesPanel.updateValues();
		showAttributesDialog();
	}
	
	
	public class AttributesPanel extends AbstractDataFieldPanel {
		
		Box customAttributesBox;
		ArrayList<AttributeEditor> customAttributesFields = new ArrayList<AttributeEditor>();
		
		
		public AttributesPanel(DataField dataField) {
			this.dataField = dataField;
			customAttributesBox = Box.createVerticalBox();
			displayAllAttributes();
			this.add(customAttributesBox);
		}
	
		public void displayAllAttributes() {
			LinkedHashMap<String, String> allAttributes = dataField.getAllAttributes();
		
			Iterator keyIterator = allAttributes.keySet().iterator();
		
			while (keyIterator.hasNext()) {
				String name = (String)keyIterator.next();
				String value = allAttributes.get(name);
			
				// don't display these attributes
				if ((name.equals(DataField.ELEMENT_NAME )) || (name.equals(DataField.INPUT_TYPE))
						|| (name.equals(DataField.SUBSTEPS_COLLAPSED)) || (name.equals(DataField.TEXT_NODE_VALUE))) continue;
			
				AttributeEditor attributeEditor = new AttributeEditor(name, value);
			
				// keep a list of fields
				customAttributesFields.add(attributeEditor);
				customAttributesBox.add(attributeEditor);
			}
		}
		
		public void updateValues() {
			for (AttributeEditor field: customAttributesFields) {
				String attribute = field.getTextField().getName();
				String value = dataField.getAttribute(attribute);
				field.getTextField().setText(value);
			}
		}
	}
	
}
