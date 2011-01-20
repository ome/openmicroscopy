/*
 * org.openmicroscopy.shoola.agents.measurement.util.ui.ShapeRenderer 
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
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.FigureUtil;

/** 
 * Table Cell renderer for the ROIShape type in the ROI Manager.
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
	extends JPanel
	implements TableCellRenderer
{
	
	/** Helper reference. */
	private IconManager icons;
	
	/** Component hosting the icon representing the shape. */
	private JLabel label;
	
	/**
	 * Adds the appropriate shape icon to the label.
	 * 
	 * @param shape above.
	 */
	private void makeShapeIcon(String shape)
	{
		if (FigureUtil.SCRIBBLE_TYPE.equals(shape)) 
			label.setIcon(icons.getIcon(IconManager.SCRIBBLE));
		else if (FigureUtil.LINE_TYPE.equals(shape)) 
			label.setIcon(icons.getIcon(IconManager.LINE_16));
		else if (FigureUtil.LINE_CONNECTION_TYPE.equals(shape)) 
			label.setIcon(icons.getIcon(IconManager.CONNECTION));
		else if (FigureUtil.POLYGON_TYPE.equals(shape)) 
			label.setIcon(icons.getIcon(IconManager.POLYGON_16));
		else if (FigureUtil.POINT_TYPE.equals(shape)) 
			label.setIcon(icons.getIcon(IconManager.POINT_16));
		else if (FigureUtil.RECTANGLE_TYPE.equals(shape)) 
			label.setIcon(icons.getIcon(IconManager.RECTANGLE));
		else if (FigureUtil.ELLIPSE_TYPE.equals(shape)) 
			label.setIcon(icons.getIcon(IconManager.ELLIPSE_16));
		else if (FigureUtil.TEXT_TYPE.equals(shape)) 
			label.setIcon(icons.getIcon(IconManager.TEXT_16));
		else if (FigureUtil.MASK_TYPE.equals(shape)) 
			label.setIcon(icons.getIcon(IconManager.MASK));
	}
	
	/**
	 * Creates a new instance. Sets the opacity of the label to
	 * <code>true</code>.
	 */
	public ShapeRenderer()
	{
		icons = IconManager.getInstance();
		label = new JLabel();
		FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
		layout.setVgap(0);
        setLayout(layout);
		setOpaque(true);
		add(label);
	}
	
	/**
	 * Overridden to set the icon corresponding to the shape.
	 * @see TableCellRenderer#getTableCellRendererComponent(JTable, Object,
	 *      boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		makeShapeIcon((String) value);
		return this;
	}

}
