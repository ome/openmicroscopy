/*
 * org.openmicroscopy.shoola.agents.measurement.util.ROIAssistantCellRenderer 
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
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;

/** 
 * 
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
public 	class ROIAssistantCellRenderer
		extends JComponent
		implements TableCellRenderer
{

	private final static Color SELECTED_COLOUR = new Color(255, 206, 206);
	private final static Color FOCUS_COLOUR = new Color(255, 135, 135);
	
	/**
	 * Creates a new instance. Sets the opacity of the label to 
	 * <code>true</code>.
	 */
	public ROIAssistantCellRenderer()
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
		JComponent thisComponent;
		
		JLabel label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setOpaque(true);
		label.setBackground(Color.white);
		if(column == 0)
		{
			if(value instanceof String)
				label.setText((String)value);
			return label;
		}
		if(value instanceof String)
		{
			if(value.equals(ROIFigure.ELLIPSE_TYPE))
			{
				ImageIcon i = IconManager.getInstance().getImageIcon(IconManager.ELLIPSE);
				label.setIcon(i);
			}
			else if(value.equals(ROIFigure.RECTANGLE_TYPE))
			{
				ImageIcon i = IconManager.getInstance().getImageIcon(IconManager.SQUARE);
				label.setIcon(i);
			}
			else if(value.equals(ROIFigure.SCRIBBLE_TYPE))
			{
				ImageIcon i = IconManager.getInstance().getImageIcon(IconManager.POLYLINE);
				label.setIcon(i);
			}
			else if(value.equals(ROIFigure.POLYGON_TYPE))
			{
				ImageIcon i = IconManager.getInstance().getImageIcon(IconManager.POLYGON);
				label.setIcon(i);
			}
			else if(value.equals(ROIFigure.LINE_TYPE))
			{
				ImageIcon i = IconManager.getInstance().getImageIcon(IconManager.LINE);
				label.setIcon(i);
			}
			else if(value.equals(ROIFigure.LINE_CONNECTION_TYPE))
			{
				ImageIcon i = IconManager.getInstance().getImageIcon(IconManager.LINECONNECTION);
				label.setIcon(i);
			}
			else if(value.equals(ROIFigure.POINT_TYPE))
			{
				ImageIcon i = IconManager.getInstance().getImageIcon(IconManager.POINT);
				label.setIcon(i);
			}
			else if(value.equals(ROIFigure.TEXT_TYPE))
			{
				ImageIcon i = IconManager.getInstance().getImageIcon(IconManager.TEXT);
				label.setIcon(i);
			}
			else 
			{
				label.setText((String)value);
				label.setHorizontalTextPosition(SwingConstants.CENTER);
			}
		}
		
		if(isSelected)
		{
			label.setBackground(SELECTED_COLOUR);
		}
		if(hasFocus)
		{
			label.setBackground(FOCUS_COLOUR);
		}
		//RendererUtils.setRowColor(label, table.getSelectedRow(), row);
		thisComponent = label;
		return thisComponent;
	}

}