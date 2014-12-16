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

import javax.swing.DropMode;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import omero.model.MapAnnotation;

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

	public MapTable() {
		this(false);
	}

	public MapTable(boolean editable) {
		setModel(new MapTableModel(this, editable));
		init(editable);
	}

	private void init(boolean editable) {
		TableCellEditor ce = new MapTableCellEditor();
		TableCellRenderer cr = new MapTableCellRenderer();

		TableColumn nameColumn = getColumnModel().getColumn(0);
		TableColumn valueColumn = getColumnModel().getColumn(1);

		nameColumn.setCellEditor(ce);
		valueColumn.setCellEditor(ce);

		nameColumn.setCellRenderer(cr);
		valueColumn.setCellRenderer(cr);

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		if (editable) {
			setDragEnabled(true);
			setDropMode(DropMode.INSERT_ROWS);
			setTransferHandler(new TableRowTransferHandler(this));
		}
	}

	public void setData(MapAnnotationData data) {
		((MapTableModel) getModel()).setData(data);
		revalidate();
	}
}
