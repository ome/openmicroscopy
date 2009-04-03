package ui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import tree.DataField;
import tree.IAttributeSaver;

public class DataFieldComboBox extends CustomComboBox {

	String attribute;
	IAttributeSaver dataField;
	SelectionListener selectionListener = new SelectionListener();
	
	public DataFieldComboBox(IAttributeSaver dataField, String attribute, String[] items) {
		
		super(items);
		
		this.dataField = dataField;
		this.attribute = attribute;
		
		init();
	}
	
	protected void init() {
		setSelectedItem(dataField.getAttribute(attribute));
		this.addActionListener(selectionListener);
	}
	
	public void setSelectedItemNoListeners(Object item) {
		removeActionListener(selectionListener);
		super.setSelectedItem(item);
		addActionListener(selectionListener);
	}
	
	public void setSelectedIndexNoListeners(int index) {
		removeActionListener(selectionListener);
		super.setSelectedIndex(index);
		addActionListener(selectionListener);
	}
	
	public class SelectionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			dataField.setAttribute(attribute, getSelectedItem().toString(), true);
		}
	}
}
