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

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

/**
 * {@link TableCellEditor} used for the {@link MapTable}
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
@SuppressWarnings({ "serial" })
public class MapTableCellEditor extends AbstractCellEditor implements
		TableCellEditor {

	private JComponent component = null;

	private boolean doubleClickEdit = false;

	private boolean editable = true;

	public MapTableCellEditor() {
		this(true);
	}

	public MapTableCellEditor(boolean editable) {
		this.editable = editable;
	}

	public void setDoubleClickEdit(boolean doubleClickEdit) {
		this.doubleClickEdit = doubleClickEdit;
	}

	@Override
	public Object getCellEditorValue() {
		return ((JTextField) component).getText();
	}

	@Override
	public Component getTableCellEditorComponent(final JTable table,
			final Object value, final boolean isSelected, final int row,
			final int column) {

		String text = (String) value;
		if (text.equals(MapTableModel.KEY_DUMMY)
				|| text.equals(MapTableModel.VALUE_DUMMY))
			text = "";

		component = new JTextField(text);
		return component;
	}

	@Override
	public boolean isCellEditable(EventObject e) {
		if (!editable)
			return false;
		
		if (e instanceof MouseEvent && doubleClickEdit) {
			return ((MouseEvent) e).getClickCount() > 1;
		}
		return super.isCellEditable(e);
	}

}
