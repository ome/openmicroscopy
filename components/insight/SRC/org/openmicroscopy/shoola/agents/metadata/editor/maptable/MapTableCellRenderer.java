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

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * {@link TableCellRenderer} used for the {@link MapTable}
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
@SuppressWarnings("serial")
public class MapTableCellRenderer extends DefaultTableCellRenderer {

	public MapTableCellRenderer() {
	}

	@Override
	public Component getTableCellRendererComponent(final JTable table,
			final Object value, final boolean isSelected,
			final boolean hasFocus, final int row, final int column) {
		JLabel l;
		if (value instanceof Icon) {
			l = new JLabel((Icon) value);
			l.setToolTipText("Delete Entry");
		} else
			l = new JLabel((String) value);

		l.setOpaque(true);
		
		if (isSelected)
			l.setBackground(UIUtilities.SELECTED_BACKGROUND_COLOUR);
		else
			l.setBackground(row % 2 == 0 ? UIUtilities.BACKGROUND_COLOUR_EVEN
                    : UIUtilities.BACKGROUND_COLOUR_ODD);

		return l;
	}

}
