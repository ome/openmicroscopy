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

import javax.swing.DropMode;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
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
@SuppressWarnings("serial")
public class MapTable extends JTable {

	public static int PERMISSION_NONE = 0;
	public static int PERMISSION_EDIT = 1;
	public static int PERMISSION_MOVE = 2;
	public static int PERMISSION_DELETE = 4;

	private MapTableCellEditor cellEditor;

	private MapTableCellRenderer cellRenderer;

	private int permissions = PERMISSION_NONE;

	private List<NamedValue> copiedValues = new ArrayList<NamedValue>();

	public MapTable() {
		this(PERMISSION_NONE);
	}

	public MapTable(int permissions) {
		this.permissions = permissions;
		setModel(new MapTableModel(this));
		init();
	}

	private void init() {
		getTableHeader().setReorderingAllowed(false);

		setSelectionModel(new MapTableSelectionModel(this));

		cellEditor = new MapTableCellEditor();
		cellRenderer = new MapTableCellRenderer();

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

		// increase default row height by 3px (otherwise JTextAreas are cut off)
		setRowHeight(getRowHeight() + 3);
	}

	public void setData(MapAnnotationData data) {
		((MapTableModel) getModel()).setData(data);
		revalidate();
	}

	public MapAnnotationData getData() {
		return ((MapTableModel) getModel()).getMap();
	}

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

	public void addEntry(NamedValue entry) {
		addEntry(entry, -1);
	}
	
	public void addEntry(NamedValue entry, int index) {
		addEntries(Arrays.asList(entry), index);
	}

	public void addEntries(List<NamedValue> entries, int index) {
		MapTableModel model = (MapTableModel) getModel();
		model.addEntries(entries, index);
	}

	public void deleteSelected() {
		deleteEntries(getSelectedRows());
	}

	public void deleteEntry(int index) {
		MapTableModel model = (MapTableModel) getModel();
		model.deleteEntry(index);
	}

	public void deleteEntries(int[] indices) {
		MapTableModel model = (MapTableModel) getModel();
		model.deleteEntries(indices);
	}

	public void setDoubleClickEdit(boolean doubleClickEdit) {
		cellEditor.setDoubleClickEdit(doubleClickEdit);
	}

	public boolean canEdit() {
		return (permissions & PERMISSION_EDIT) == PERMISSION_EDIT;
	}

	public boolean canMove() {
		return (permissions & PERMISSION_MOVE) == PERMISSION_MOVE;
	}

	public boolean canDelete() {
		return (permissions & PERMISSION_DELETE) == PERMISSION_DELETE;
	}
}
