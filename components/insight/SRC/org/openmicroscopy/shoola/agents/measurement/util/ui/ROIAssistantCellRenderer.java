/*
 * org.openmicroscopy.shoola.agents.measurement.util.ui.ROIAssistantCellRenderer 
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
package org.openmicroscopy.shoola.agents.measurement.util.ui;


//Java imports
import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.FigureUtil;

/** 
 * Table renderer for the Assistant.
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
public class ROIAssistantCellRenderer
	extends JComponent
	implements TableCellRenderer
{

	/** The colour of the selected cell. */
	private final static Color SELECTED_COLOUR = new Color(255, 206, 206);
	
	/** The colour of the focused cell. */
	private final static Color FOCUS_COLOUR = new Color(255, 135, 135);
	
	/** Reference to the <code>Ellipse</code> icon. */
	private static final Icon ELLIPSE_ICON;
	
	/** Reference to the <code>Square</code> icon. */
	private static final Icon SQUARE_ICON;
	
	/** Reference to the <code>Polyline</code> icon. */
	private static final Icon POLYLINE_ICON;
	
	/** Reference to the <code>Polygon</code> icon. */
	private static final Icon POLYGON_ICON;
	
	/** Reference to the <code>Line</code> icon. */
	private static final Icon LINE_ICON;
	
	/** Reference to the <code>Point</code> icon. */
	private static final Icon POINT_ICON;
	
	/** Reference to the <code>Line connection</code> icon. */
	private static final Icon LINECONNECTION_ICON;
	
	/** Reference to the <code>Text</code> icon. */
	private static final Icon TEXT_ICON;
	
	static { 
		IconManager icons = IconManager.getInstance();
		ELLIPSE_ICON = icons.getIcon(IconManager.ELLIPSE);
		SQUARE_ICON = icons.getIcon(IconManager.SQUARE);
		POLYLINE_ICON = icons.getIcon(IconManager.POLYLINE);
		POLYGON_ICON = icons.getIcon(IconManager.POLYGON);
		LINE_ICON = icons.getIcon(IconManager.LINE);
		POINT_ICON = icons.getIcon(IconManager.POINT);
		LINECONNECTION_ICON = icons.getIcon(IconManager.LINECONNECTION);
		TEXT_ICON = icons.getIcon(IconManager.TEXT);
	}
	
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
		if (value instanceof String)
		{
			if (value.equals(FigureUtil.ELLIPSE_TYPE))
			{
				label.setIcon(ELLIPSE_ICON);
			}
			else if(value.equals(FigureUtil.RECTANGLE_TYPE))
			{
				label.setIcon(SQUARE_ICON);
			}
			else if(value.equals(FigureUtil.SCRIBBLE_TYPE))
			{
				label.setIcon(POLYLINE_ICON);
			}
			else if (value.equals(FigureUtil.POLYGON_TYPE))
			{
				label.setIcon(POLYGON_ICON);
			}
			else if(value.equals(FigureUtil.LINE_TYPE))
			{
				label.setIcon(LINE_ICON);
			}
			else if(value.equals(FigureUtil.LINE_CONNECTION_TYPE))
			{
				label.setIcon(LINECONNECTION_ICON);
			}
			else if(value.equals(FigureUtil.POINT_TYPE))
			{
				label.setIcon(POINT_ICON);
			}
			else if(value.equals(FigureUtil.TEXT_TYPE))
			{
				label.setIcon(TEXT_ICON);
			}
			else 
			{
				label.setText((String) value);
				label.setHorizontalTextPosition(SwingConstants.CENTER);
			}
		}
		
		if (isSelected) label.setBackground(SELECTED_COLOUR);
		if (hasFocus) label.setBackground(FOCUS_COLOUR);
		thisComponent = label;
		return thisComponent;
	}

}