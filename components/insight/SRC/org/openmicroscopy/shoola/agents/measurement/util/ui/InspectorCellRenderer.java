/*
 * org.openmicroscopy.shoola.agents.measurement.util.InspectorCellRenderer 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.util.ui;

//Java imports
import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.util.model.AttributeField;
import org.openmicroscopy.shoola.agents.measurement.util.model.ValueType;
import org.openmicroscopy.shoola.util.roi.model.util.FigureType;
import org.openmicroscopy.shoola.util.ui.PaintPot;

/** 
 * The renderer for the table displayed in the inspector.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class InspectorCellRenderer
	extends JComponent 
	implements TableCellRenderer
{
	
	/** The size of the font in this cell. */
	private static final float 	FONTSIZE = 10;
	
	/**
	 * Creates a new instance. Sets the opacity of the label to
	 * <code>true</code>.
	 */
	public InspectorCellRenderer()
	{
		setOpaque(true);
	}
	
	/**
	 * Overridden to set the correct component depending on the type.
	 * @see TableCellRenderer#getTableCellRendererComponent(JTable, Object,
	 *      boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		if (!(table instanceof FigureTable))
			return new JLabel();
		FigureTable figureTable = (FigureTable) table;
		AttributeField field = figureTable.getFieldAt(row);
		Component thisComponent = new JLabel();
		if (column == 0)
		{
			JLabel label = new JLabel();
			label.setOpaque(true);
			label.setText(value+"");
			label.setFont(label.getFont().deriveFont(FONTSIZE));
			thisComponent = label;
		} else if (field.getValueType() == ValueType.DEFAULT) {
			if (value instanceof Double || value instanceof String
				|| value instanceof FigureType || value instanceof Integer
				|| value instanceof Long)
			{
				JTextField text = new JTextField();
				text.setOpaque(false);
				text.setBorder(BorderFactory.createEmptyBorder());
				text.setText(value+"");
				text.setFont(text.getFont().deriveFont(FONTSIZE));
				thisComponent = text;
			} else if (value instanceof Color)
			{
				PaintPot paintPot = new PaintPot((Color) value);
				thisComponent = paintPot;
			} else if (value instanceof Boolean)
			{
				JCheckBox checkBox = new JCheckBox();
				checkBox.setSelected((Boolean) value);
				thisComponent = checkBox;
			}
			if (!(value instanceof Color))
			{
				RendererUtils.setRowColor(thisComponent, table.getSelectedRow(),
				row);
			}
		} else if (field.getValueType() == ValueType.ENUM) {
			JComboBox comboBox = new JComboBox();
			comboBox.setFont(comboBox.getFont().deriveFont(FONTSIZE));
			List valueRange = field.getValueRange();
			if (valueRange != null) {
				int index = 0;
				for (int i = 0 ; i < valueRange.size(); i++)
				{
					comboBox.addItem(valueRange.get(i));
					if (valueRange.get(i).equals(value))
						index = i;
				}
				comboBox.setSelectedIndex(index);
			}
			thisComponent = comboBox;
		}
		
		return thisComponent;
	}
	
}
