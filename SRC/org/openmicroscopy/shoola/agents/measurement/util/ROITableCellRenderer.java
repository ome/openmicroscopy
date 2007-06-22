/*
 * org.openmicroscopy.shoola.agents.measurement.util.ColorCellRenderer 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.util;


//Java imports
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.openmicroscopy.shoola.util.roi.model.util.FigureType;
import org.openmicroscopy.shoola.util.ui.PaintPot;

//Third-party libraries

//Application-internal dependencies

/** 
 * Basic cell renderer displaying color in a cell.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ROITableCellRenderer 
	extends JComponent
	implements TableCellRenderer
{

	/**
	 * Creates a new instance. Sets the opacity of the label to 
	 * <code>true</code>.
	 */
	public ROITableCellRenderer()
	{
		setOpaque(true);
	}
	
	/**
	 * @see TableCellRenderer#getTableCellRendererComponent(JTable, Object, 
	 * 										boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		Component thisComponent = new JLabel();
		if ((value instanceof Integer) || (value instanceof Long) ||
				(value instanceof Double) || (value instanceof String) ||
				(value instanceof FigureType))
		{
			JLabel label = new JLabel();
			label.setOpaque(true);
    		label.setText(value+"");
    		thisComponent = label;
    	} else if (value instanceof Color) {
    		PaintPot paintPot = new PaintPot((Color)value);
    		thisComponent = paintPot;
    	}
    	else if( value instanceof Boolean)
    	{
    		JCheckBox checkBox = new JCheckBox();
    		checkBox.setSelected((Boolean)value);
    		thisComponent = checkBox;
    	}
		if (!(value instanceof Color)) {
			RendererUtils.setRowColor(thisComponent, table.getSelectedRow(), 
									row);
		}
		return thisComponent;
	}

}
