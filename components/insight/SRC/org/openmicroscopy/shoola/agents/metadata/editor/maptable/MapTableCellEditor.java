/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.Component;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * {@link TableCellEditor} used for the {@link MapTable} This basically is just
 * a wrapper around the original default TableCellEditor.
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class MapTableCellEditor implements TableCellEditor {

	/** Reference to the original TableCellEditor */
	private TableCellEditor original;

	/**
	 * Creates a new instance
	 */
	public MapTableCellEditor(TableCellEditor original) {
		this.original = original;
	}

	@Override
	public Object getCellEditorValue() {
		return original.getCellEditorValue();
	}

	@Override
	public boolean isCellEditable(EventObject anEvent) {
		return original.isCellEditable(anEvent);
	}

	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
		return original.shouldSelectCell(anEvent);
	}

	@Override
	public boolean stopCellEditing() {
		return original.stopCellEditing();
	}

	@Override
	public void cancelCellEditing() {
		original.cancelCellEditing();

	}

	@Override
	public void addCellEditorListener(CellEditorListener l) {
		original.addCellEditorListener(l);

	}

	@Override
	public void removeCellEditorListener(CellEditorListener l) {
		original.removeCellEditorListener(l);

	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		JTextField t = (JTextField) original.getTableCellEditorComponent(table,
				value, isSelected, row, column);

		if (isSelected)
			t.setBackground(UIUtilities.SELECTED_BACKGROUND_COLOUR);
		else
			t.setBackground(row % 2 == 0 ? UIUtilities.BACKGROUND_COLOUR_EVEN
					: UIUtilities.BACKGROUND_COLOUR_ODD);

		String text = (String) value;
		if (text.equals(MapUtils.DUMMY_KEY)
				|| text.equals(MapUtils.DUMMY_VALUE))
			t.setText("");
		else if(!CommonsLangUtils.isEmpty(text))
			t.selectAll();
		
		return t;
	}

}
