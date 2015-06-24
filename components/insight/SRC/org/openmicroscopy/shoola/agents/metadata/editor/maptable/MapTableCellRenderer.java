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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * {@link TableCellRenderer} used for the {@link MapTable} This basically is
 * just a wrapper around the original default TableCellRenderer.
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class MapTableCellRenderer implements TableCellRenderer {

	/** Selection color for inactive table row */
	public static final Color GREY = new Color(200, 200, 200);

	/** Inactive table row color for even rows */
	public static final Color LIGHT_GREY = new Color(230, 230, 230);

	/** Inactive table row color for odd rows */
	public static final Color LIGHTER_GREY = new Color(238, 238, 238);

	/** Italic font used for the 'add entry' row */
	private static final Font ITALIC = (new JLabel()).getFont().deriveFont(
			Font.ITALIC);
	
	/** Default JLabel font */
	private static final Font DEFAULT_FONT = (new JLabel()).getFont();

	/** Default JLabel font color */
	private static final Color DEFAULT_FONT_COLOR = (new JLabel()).getForeground();
	
	/** Reference to the original TableCellRenderer */
	private TableCellRenderer original;

	/**
	 * Creates a new instance
	 */
	public MapTableCellRenderer(TableCellRenderer original) {
		this.original = original;
	}

	@Override
	public Component getTableCellRendererComponent(final JTable table,
			final Object value, final boolean isSelected,
			final boolean hasFocus, final int row, final int column) {

		JLabel l = (JLabel) original.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (row == table.getRowCount() - 1
				&& (MapUtils.DUMMY_KEY.equals(value) || MapUtils.DUMMY_VALUE
						.equals(value))) {
			l.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
			l.setFont(ITALIC);
		}
		else {
			l.setForeground(DEFAULT_FONT_COLOR);
			l.setFont(DEFAULT_FONT);
		}

		boolean editable = ((MapTable) table).canEdit();

		Color selColor = editable ? UIUtilities.SELECTED_BACKGROUND_COLOUR
				: GREY;
		Color evenColor = editable ? UIUtilities.BACKGROUND_COLOUR_EVEN
				: LIGHT_GREY;
		Color oddColor = editable ? UIUtilities.BACKGROUND_COLOUR_ODD
				: LIGHTER_GREY;

		if (isSelected)
			l.setBackground(selColor);
		else
			l.setBackground(row % 2 == 0 ? evenColor : oddColor);

		return l;
	}

}
