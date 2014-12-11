package org.openmicroscopy.shoola.agents.metadata.editor.maptable;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import omero.model.NamedValue;
import pojos.MapAnnotationData;

@SuppressWarnings("serial")
public class MapTableModel extends DefaultTableModel {

	private MapTable table;
	private MapAnnotationData map;
	private List<NamedValue> data;

	public MapTableModel(MapTable table) {
		this.table = table;
		setData(new MapAnnotationData());
	}

	@Override
	public int getRowCount() {
		return data == null ? 1 : data.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Name";
		case 1:
			return "Value";
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int column) {
		if(row<data.size()) {
			NamedValue d = data.get(row);
			switch (column) {
			case 0:
				return d.name;
			case 1:
				return d.value;
			}
		}
		else {
			switch (column) {
			case 0:
				return "Add name";
			case 1:
				return "Add value";
			}
		}
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int row, int column) {
		String value = (String) aValue;

		if (row < data.size()) {
			NamedValue d = data.get(row);
			switch (column) {
			case 0:
				d.name = value;
				break;
			case 1:
				d.value = value;
			}
		} else {
			NamedValue d = new NamedValue("", "");
			switch (column) {
			case 0:
				d.name = value;
				break;
			case 1:
				d.value = value;
			}
			data.add(d);
			table.revalidate();
		}
	}

	@SuppressWarnings("unchecked")
	public void setData(MapAnnotationData data) {
		this.map = data;
		this.data = (List<NamedValue>) map.getContent();
		if (this.data == null)
			this.data = new ArrayList<NamedValue>();
	}
}
