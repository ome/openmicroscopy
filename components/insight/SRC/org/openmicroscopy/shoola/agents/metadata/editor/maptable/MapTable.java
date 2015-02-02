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

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.InputMap;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import omero.model.MapAnnotation;
import omero.model.NamedValue;

import org.openmicroscopy.shoola.util.ui.table.TableRowTransferHandler;

import pojos.MapAnnotationData;

/**
 * Table for displaying a {@link MapAnnotation}
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class MapTable extends JTable {

	/** No permission bit (read-only) */
	public static int PERMISSION_NONE = 0;

	/** Edit permission bit */
	public static int PERMISSION_EDIT = 1;

	/** Move permission bit */
	public static int PERMISSION_MOVE = 2;

	/** Delete permission bit */
	public static int PERMISSION_DELETE = 4;

	/** Permissions bits */
	private int permissions = PERMISSION_NONE;

	/**
	 * Creates a read-only table
	 */
	public MapTable() {
		this(PERMISSION_NONE);
	}

	/**
	 * Creates a new MapTable with certain actions enabled, see permissions
	 * parameter
	 * 
	 * @param permissions
	 *            The permissions
	 */
	public MapTable(int permissions) {
		this.permissions = permissions;
		setModel(new MapTableModel(this));
		init();
	}

	/**
	 * Initializes the component
	 */
	private void init() {
		getTableHeader().setReorderingAllowed(false);

		setSelectionModel(new MapTableSelectionModel(this));

		final TableCellRenderer orgRend = getDefaultRenderer(String.class);
		final TableCellEditor orgEdit = getDefaultEditor(String.class);

		TableCellRenderer cellRenderer = new MapTableCellRenderer(orgRend);
		TableCellEditor cellEditor = new MapTableCellEditor(orgEdit);

		TableColumn nameColumn = getColumnModel().getColumn(0);
		TableColumn valueColumn = getColumnModel().getColumn(1);

		nameColumn.setCellEditor(cellEditor);
		valueColumn.setCellEditor(cellEditor);

		nameColumn.setCellRenderer(cellRenderer);
		valueColumn.setCellRenderer(cellRenderer);

		if (canMove()) {
			setDragEnabled(true);
			setDropMode(DropMode.INSERT_ROWS);
			setTransferHandler(new TableRowTransferHandler(this));
		}

		// Change 'enter' behaviour to act like 'tab'
		KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		InputMap im = getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		im.put(enter, im.get(tab));
	}

	/**
	 * Sets the {@link MapAnnotationData} to display
	 * 
	 * @param data
	 *            The {@link MapAnnotationData} to display
	 */
	public void setData(MapAnnotationData data) {
		((MapTableModel) getModel()).setData(data);
		revalidate();
	}

	/**
	 * Get the current {@link MapAnnotationData}
	 * 
	 * @return See above
	 */
	public MapAnnotationData getData() {
		return ((MapTableModel) getModel()).getMap();
	}

	/**
	 * Get the list of currently selected entries
	 * 
	 * @return See above
	 */
	public List<NamedValue> getSelection() {
		MapTableModel model = (MapTableModel) getModel();
		List<NamedValue> result = new ArrayList<NamedValue>();
		for (int row : getSelectedRows()) {
			NamedValue nv = model.getRow(row);
			if (nv != null)
				result.add(model.getRow(row));
		}
		return result;
	}

	/**
	 * Adds an entry to the end
	 * 
	 * @param entry
	 *            The {@link NamedValue} to add
	 */
	public void addEntry(NamedValue entry) {
		addEntry(entry, -1);
	}

	/**
	 * Adds an entry at a certain position
	 * 
	 * @param entry
	 *            The {@link NamedValue} to add
	 * @param index
	 *            The position to insert it
	 */
	public void addEntry(NamedValue entry, int index) {
		addEntries(Arrays.asList(entry), index);
	}

	/**
	 * Adds a list of entries to a certain position
	 * 
	 * @param entries
	 *            The {@link NamedValue}s to add
	 * @param index
	 *            The position to insert them
	 */
	public void addEntries(List<NamedValue> entries, int index) {
		MapTableModel model = (MapTableModel) getModel();
		model.addEntries(entries, index);
		revalidate();
	}

	/**
	 * Delete the current selection
	 */
	public void deleteSelected() {
		deleteEntries(getSelectedRows());
	}

	/**
	 * Delete the entry at a certain position
	 * 
	 * @param index
	 *            The position of the entry to delete
	 */
	public void deleteEntry(int index) {
		MapTableModel model = (MapTableModel) getModel();
		model.deleteEntry(index);
	}

	/**
	 * Delete a specific set of entries
	 * 
	 * @param indices
	 *            The positions of the entries to delete
	 */
	public void deleteEntries(int[] indices) {
		MapTableModel model = (MapTableModel) getModel();
		model.deleteEntries(indices);
	}

	/**
	 * Checks if the table contains any {@link NamedValue}s
	 * @return <code>true</code> if it doesn't, <code>false</code> if it does.
	 */
	public boolean isEmpty() {
		return ((MapTableModel) getModel()).isEmpty();
	}
	
	/**
	 * Checks if edit flag is set
	 * 
	 * @return See above
	 */
	public boolean canEdit() {
		return (permissions & PERMISSION_EDIT) == PERMISSION_EDIT;
	}

	/**
	 * Checks if move flag is set
	 * 
	 * @return See above
	 */
	public boolean canMove() {
		return (permissions & PERMISSION_MOVE) == PERMISSION_MOVE;
	}

	/**
	 * Checks if delete flag is set
	 * 
	 * @return See above
	 */
	public boolean canDelete() {
		return (permissions & PERMISSION_DELETE) == PERMISSION_DELETE;
	}
}
