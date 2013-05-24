/*
 * org.openmicroscopy.shoola.agents.fsimporter.util.ChecksumTableRenderer
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.fsimporter.util;


//Java imports
import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.apache.commons.io.FilenameUtils;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Render the checksums correctly.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
class ChecksumTableRenderer
	extends DefaultTableCellRenderer
{

	/** The maximum number of characters displayed.*/
	static final int MAX_CHARACTERS = 10;
	
	/** The icon displayed when the checksums do not match.*/
	private final Icon failure;
	
	/** The icon displayed when the checksums do match.*/
	private final Icon success;
	
	/** The default foreground color.*/
	private final Color foreground;
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param failure The icon displayed when the checksums do not match.
	 * @param success The icon displayed when the checksums do match.
	 */
	ChecksumTableRenderer(Icon failure, Icon success)
	{
		this.failure = failure;
		this.success = success;
		foreground = getForeground();
		setOpaque(true);
		setHorizontalAlignment(SwingConstants.LEFT);
	}
	
	/**
	 * Overridden to set the correct renderer.
	 * @see DefaultTableCellRenderer#getTableCellRendererComponent(JTable, 
	 * Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		setIcon(null);
		setText("");
		setToolTipText("");
		setForeground(foreground);
		if (value instanceof Boolean) {
			Boolean v = (Boolean) value;
			if (v.booleanValue()) setIcon(success);
			else {
				setIcon(failure);
			}
		} else if (value instanceof String) {
			Boolean b = (Boolean) table.getValueAt(row,
					ChecksumTableModel.VALID_COLUMN);
			if (!b.booleanValue()) {
				setForeground(UIUtilities.REQUIRED_FIELDS_COLOR);
			}
			String v = (String) value;
			setToolTipText(v);
			if (column > 0) {
				if (v.length() > MAX_CHARACTERS)
					setText(v.substring(0, MAX_CHARACTERS));
				else setText(v);
			} else {
				setText(FilenameUtils.getName(v));
			}
		}
		return this;
	}

}
