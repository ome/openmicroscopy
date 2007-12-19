package ui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import tree.DataField;

public class DataFieldComboBox extends CustomComboBox {

	String attribute;
	DataField dataField;
	
	public DataFieldComboBox(DataField dataField, String attribute, String[] items) {
		
		super(items);
		
		this.dataField = dataField;
		this.attribute = attribute;
		
		init();
	}
	
	protected void init() {
		this.addActionListener(new SelectionListener());
	}
	
	public class SelectionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			dataField.setAttribute(attribute, getSelectedItem().toString(), true);
		}
	}
}
