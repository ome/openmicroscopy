/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014-2015 University of Dundee. All rights reserved.
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
import java.util.Arrays;
import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import omero.model.NamedValue;

import org.openmicroscopy.shoola.util.ui.table.Reorderable;

import pojos.MapAnnotationData;

/**
 * {@link TableModel} used for the {@link MapTable}
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class MapTableModel extends DefaultTableModel implements Reorderable {

	/** Reference to the table */
	private MapTable table;

	/** The underlying {@link MapAnnotationData} to represent */
	private MapAnnotationData map;

	/** The current data displayed */
	private List<NamedValue> data;

	/** A copy of the original data */
	private List<NamedValue> originalData;

	/**
	 * Creates a new instance
	 * 
	 * @param table
	 */
	public MapTableModel(MapTable table) {
		this.table = table;
	}

	/**
	 * Get the entry of a certain row
	 * 
	 * @param index
	 * @return
	 */
	public NamedValue getRow(int index) {
		if (index >= 0 && index < data.size()) {
			return data.get(index);
		}
		return null;
	}

	@Override
	public int getRowCount() {
		if (table == null)
			return 0;
		return data == null ? 0 : data.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Key";
		case 1:
			return "Value";
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
				// automatically add a new row at the end of the table
				if (row == data.size() - 1 && !MapUtils.isEmpty(d))
					addEntry("", "");
			}
		}
		fireTableCellUpdated(row, column);
	}

	/**
	 * Adds a new entry to the end
	 * 
	 * @param name
	 *            The name, see {@link NamedValue#name}
	 * @param value
	 *            The value, see {@link NamedValue#value}
	 */
	void addEntry(String name, String value) {
		addEntries(Arrays.asList(new NamedValue(name, value)), -1);
	}

	/**
	 * Inserts a list of entries at a certain position
	 * 
	 * @param entries
	 *            The entries to insert
	 * @param index
	 *            The position where to insert them
	 */
	public void addEntries(List<NamedValue> entries, int index) {
		if (index < 0 || index > data.size())
			index = data.size();
		data.addAll(index, entries);
		syncBackToMap();
	}

	/**
	 * Delete an entry at a certain position
	 * 
	 * @param index
	 *            The position of the entry to delete
	 */
	public void deleteEntry(int index) {
		if (index >= 0 && index < data.size()) {
			data.remove(index);
			syncBackToMap();
		}
	}

	/**
	 * Delete entries at certain positions
	 * 
	 * @param indices
	 *            The positions of the entries to delete
	 */
	public void deleteEntries(int[] indices) {
		List<NamedValue> toRemove = new ArrayList<NamedValue>();
		for (int index : indices)
			toRemove.add(getRow(index));
		data.removeAll(toRemove);
		syncBackToMap();
	}

	/**
	 * Set the {@link MapAnnotationData} to represent
	 * 
	 * @param map
	 *            The {@link MapAnnotationData}
	 */
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

		// Add a dummy row to an editable, but empty table
		if (data.size() == 0 && table.canEdit()) {
			addEntry(MapUtils.DUMMY_KEY, MapUtils.DUMMY_VALUE);
		}
	}

	/**
	 * Returns the data, but with empty rows removed
	 * 
	 * @return See above
	 */
	private List<NamedValue> getTrimmedData() {
		List<NamedValue> result = new ArrayList<NamedValue>();
		for (NamedValue nv : data) {
			if (!MapUtils.isEmpty(nv))
				result.add(nv);
		}
		return result;
	}

	/**
	 * Writes the data back to the MapAnnotation and revalidates the table
	 */
	private void syncBackToMap() {
		map.setContent(getTrimmedData());
		table.revalidate();
		fireTableDataChanged();
	}

	/**
	 * Check if the {@link MapTableModel} has been modified
	 * 
	 * @return <code>true</code> if it has been modified, <code>false</code>
	 *         otherwise
	 */
	public boolean isDirty() {
		List<NamedValue> data = getTrimmedData();

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

	/**
	 * Checks if the table contains any {@link NamedValue}s
	 * 
	 * @return <code>true</code> if it doesn't, <code>false</code> if it does.
	 */
	public boolean isEmpty() {
		return data.isEmpty();
	}

	/**
	 * Get the {@link MapAnnotationData} represented by this
	 * {@link MapTableModel}
	 * 
	 * @return
	 */
	public MapAnnotationData getMap() {
		if (isDirty()) {
			map.setContent(getTrimmedData());
		}
		return map;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == 2)
			return false;
		return table.canEdit();
	}

	@Override
	public int reorder(int fromIndices[], int toIndex) {

		List<NamedValue> values = new ArrayList<NamedValue>();
		int offset = 0;

		for (int fromIndex : fromIndices) {
			if (toIndex < 0)
				toIndex = 0;

			if (toIndex > data.size())
				toIndex = data.size();

			if (fromIndex >= 0 && fromIndex < data.size()) {
				values.add(data.get(fromIndex));
				if (fromIndex <= toIndex)
					offset--;
			}

		}

		int newIndex = toIndex + offset;

		if (!values.isEmpty()) {
			data.removeAll(values);
			data.addAll(newIndex, values);
		}

		syncBackToMap();

		return newIndex;
	}
}
