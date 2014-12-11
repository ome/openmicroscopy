package org.openmicroscopy.shoola.agents.metadata.editor.maptable;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import pojos.MapAnnotationData;

@SuppressWarnings("serial")
public class MapTable extends JTable {

	public MapTable() {
		setModel(new MapTableModel(this));
		init();
	}

	private void init() {
		TableCellEditor ce = new MapTableCellEditor();
		TableCellRenderer cr = new MapTableCellRenderer();
		
		TableColumn nameColumn = getColumnModel().getColumn(0);
		TableColumn valueColumn = getColumnModel().getColumn(1);
		
		nameColumn.setCellEditor(ce);
		valueColumn.setCellEditor(ce);
		
		nameColumn.setCellRenderer(cr);
		valueColumn.setCellRenderer(cr);
	}

	public void setData(MapAnnotationData data) {
		((MapTableModel) getModel()).setData(data);
		revalidate();
	}
}
