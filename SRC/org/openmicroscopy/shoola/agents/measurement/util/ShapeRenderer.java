/*
 * org.openmicroscopy.shoola.util.ui.treetable.renderers.ShapeRenderer 
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

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.FigureUtil;

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
public class ShapeRenderer
	extends JLabel 
	implements TableCellRenderer
{
	/**
	 * Creates a new instance. Sets the opacity of the label to
	 * <code>true</code>.
	 */
	public ShapeRenderer()
	{
		setOpaque(true);
	}
	
	/**
	 * @see TableCellRenderer#getTableCellRendererComponent(JTable, Object,
	 *      boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		setBackground(Color.red);
		setForeground(Color.blue);
		makeShapeIcon(this, (String) value);
		return this;
	}
	
	/**
	 * Add the approriate shape icon to the label.
	 * 
	 * @param label see above.
	 * @param shape above.
	 */
	private void makeShapeIcon(JLabel label, String shape)
	{
		if (shape.equals(FigureUtil.SCRIBBLE_TYPE)) label.setIcon(IconManager
			.getInstance().getIcon(IconManager.SCRIBBLE16));
		if (shape.equals(FigureUtil.LINE_TYPE)) label.setIcon(IconManager
			.getInstance().getIcon(IconManager.LINE16));
		if (shape.equals(FigureUtil.LINE_CONNECTION_TYPE)) label
			.setIcon(IconManager.getInstance()
				.getIcon(IconManager.CONNECTION16));
		if (shape.equals(FigureUtil.POLYGON_TYPE)) label.setIcon(IconManager
			.getInstance().getIcon(IconManager.POLYGON16));
		if (shape.equals(FigureUtil.POINT_TYPE)) label.setIcon(IconManager
			.getInstance().getIcon(IconManager.POINT16));
		if (shape.equals(FigureUtil.RECTANGLE_TYPE)) label.setIcon(IconManager
			.getInstance().getIcon(IconManager.RECTANGLE16));
		if (shape.equals(FigureUtil.ELLIPSE_TYPE)) label.setIcon(IconManager
			.getInstance().getIcon(IconManager.ELLIPSE16));
		if (shape.equals(FigureUtil.TEXT_TYPE)) label.setIcon(IconManager
			.getInstance().getIcon(IconManager.TEXT16));
	}
	
}
