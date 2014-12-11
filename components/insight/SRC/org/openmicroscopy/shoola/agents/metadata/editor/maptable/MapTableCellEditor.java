package org.openmicroscopy.shoola.agents.metadata.editor.maptable;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

//import ca.odell.glazedlists.GlazedLists;
//import ca.odell.glazedlists.swing.AutoCompleteSupport;

@SuppressWarnings({ "rawtypes", "serial" })
public class MapTableCellEditor extends AbstractCellEditor implements
		TableCellEditor {

	private JComboBox box = null;

	public MapTableCellEditor() {
	}

	@Override
	public Object getCellEditorValue() {
		return box.getSelectedItem();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, final int row, final int column) {

		String[] items = new String[] { "Ester", "Jordi", "Jordina", "Jorge",
				"Sergi" };

		for (int i = 0; i < items.length; i++)
			items[i] = items[i] + "_" + row + "_" + column;

//		box = (JComboBox) AutoCompleteSupport.createTableCellEditor(
//				GlazedLists.eventListOf(items)).getComponent();
		box = new JComboBox();
		return box;
	}

}
