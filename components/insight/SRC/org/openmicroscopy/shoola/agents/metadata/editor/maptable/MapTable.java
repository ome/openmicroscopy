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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DropMode;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
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

	private MapTableCellEditor cellEditor;
	
	private MapTableCellRenderer cellRenderer;

	public MapTable() {
		this(false);
	}

	public MapTable(boolean editable) {
		setModel(new MapTableModel(this, editable));
		init(editable);
	}

	private void init(boolean editable) {
		cellEditor = new MapTableCellEditor(editable);
		cellRenderer = new MapTableCellRenderer(editable);
		
		TableColumn nameColumn = getColumnModel().getColumn(0);
		TableColumn valueColumn = getColumnModel().getColumn(1);
		TableColumn deleteColumn = getColumnModel().getColumn(2);

		nameColumn.setCellEditor(cellEditor);
		valueColumn.setCellEditor(cellEditor);
		deleteColumn.setCellEditor(cellEditor);

		nameColumn.setCellRenderer(cellRenderer);
		valueColumn.setCellRenderer(cellRenderer);
		deleteColumn.setCellRenderer(cellRenderer);
		
		deleteColumn.setPreferredWidth(16);
		deleteColumn.setMaxWidth(16);

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		if (editable) {
			setDragEnabled(true);
			setDropMode(DropMode.INSERT_ROWS);
			setTransferHandler(new TableRowTransferHandler(this));

			// handle click on delete icons
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					int column = MapTable.this.getSelectedColumn();
					int row = MapTable.this.getSelectedRow();
					if (column == 2) {
						((MapTableModel) MapTable.this.getModel()).deleteEntry(row);
					}
				}
			});
		}
		
		// increase default row height by 3px (otherwise JTextAreas are cut off)
		setRowHeight(getRowHeight()+3);
	}

	public void setData(MapAnnotationData data) {
		((MapTableModel) getModel()).setData(data);
		revalidate();
	}

	public void setDoubleClickEdit(boolean doubleClickEdit) {
		cellEditor.setDoubleClickEdit(doubleClickEdit);
	}

}
