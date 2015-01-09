/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.metadata.editor.maptable;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import omero.model.NamedValue;

import org.apache.commons.lang.StringUtils;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.table.Reorderable;

import pojos.MapAnnotationData;

/**
 * {@link TableModel} used for the {@link MapTable}
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
@SuppressWarnings("serial")
public class MapTableModel extends DefaultTableModel implements Reorderable {

	private MapTable table;
	private MapAnnotationData map;
	private List<NamedValue> data;
	private List<NamedValue> originalData;

	public static final String DUMMY_KEY = "Add Key";
	public static final String DUMMY_VALUE = "Add Value";

	private String newKey = DUMMY_KEY;
	private String newValue = DUMMY_VALUE;

	private Icon deleteIcon = IconManager.getInstance().getIcon(
			IconManager.DELETE_12);

	public MapTableModel(MapTable table) {
		this.table = table;
	}

	@Override
	public int getRowCount() {
		if (table == null)
			return 0;

		int rows = data == null ? 0 : data.size();

		// +1 for the "Add Key"|"Add Value" column
		if (table.canEdit())
			rows += 1;

		return rows;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Key";
		case 1:
			return "Value";
		case 2:
			return "";
		}
		return null;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return getValueAt(0, columnIndex).getClass();
	}

	@Override
	public Object getValueAt(int row, int column) {
		if (row < data.size()) {
			NamedValue d = data.get(row);
			switch (column) {
			case 0:
				return d.name;
			case 1:
				return d.value;
			case 2:
				return table.canDelete() ? deleteIcon : null;
			}
		} else {
			switch (column) {
			case 0:
				return newKey;
			case 1:
				return newValue;
			case 2:
				return null;
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
			fireTableCellUpdated(row, column);
		} else if (!StringUtils.isEmpty(value)) {

			switch (column) {
			case 0:
				newKey = value;
				newValue = (String) getValueAt(row, 1);
				break;
			case 1:
				newKey = (String) getValueAt(row, 0);
				newValue = value;
			}

			if (!newKey.equals(DUMMY_KEY) && !newValue.equals(DUMMY_VALUE)) {
				addEntry(newKey, newValue);
			}
		}
	}

	private void addEntry(String key, String value) {
		NamedValue d = new NamedValue(key, value);
		data.add(d);
		map.setContent(data);
		table.revalidate();
		newKey = DUMMY_KEY;
		newValue = DUMMY_VALUE;
		fireTableDataChanged();
	}

	@SuppressWarnings("unchecked")
	public void setData(MapAnnotationData map) {
		this.map = map;
		this.data = (List<NamedValue>) map.getContent();
		if (this.data == null) {
			this.data = new ArrayList<NamedValue>();
			this.originalData = new ArrayList<NamedValue>();
		} else {
			this.originalData = new ArrayList<NamedValue>(this.data.size());
			for (NamedValue tmp : data) {
				this.originalData.add(new NamedValue(tmp.name, tmp.value));
			}
		}
	}

	public boolean isDirty() {
		if (data.size() != originalData.size())
			return true;

		for (int i = 0; i < data.size(); i++) {
			NamedValue nv1 = data.get(i);
			NamedValue nv2 = originalData.get(i);
			if (!nv1.name.equals(nv2.name) || !nv1.value.equals(nv2.value))
				return true;
		}

		return false;
	}

	public MapAnnotationData getMap() {
		if (isDirty()) {
			map.setContent(data);
		}
		return map;
	}

	public void deleteEntry(int index) {
		if (index >= 0 && index < data.size()) {
			data.remove(index);
			map.setContent(data);
			table.revalidate();
			fireTableDataChanged();
		}
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == 2)
			return false;
		return table.canEdit();
	}

	@Override
	public void reorder(int fromIndex, int toIndex) {
		if (fromIndex >= 0 && fromIndex < data.size()) {
			NamedValue v = data.remove(fromIndex);

			if (fromIndex < toIndex)
				toIndex--;

			if (toIndex < 0)
				toIndex = 0;

			if (toIndex > data.size())
				toIndex = data.size();

			data.add(toIndex, v);

			map.setContent(data);
			table.revalidate();
			fireTableDataChanged();
		}
	}

}
