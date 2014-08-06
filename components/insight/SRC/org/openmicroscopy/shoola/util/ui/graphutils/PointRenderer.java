/*
 * org.openmicroscopy.shoola.util.ui.graphutils.PointRenderer 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.graphutils;


//Java imports
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

//Third-party libraries
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;

//Application-internal dependencies


/** 
 * Customized point renderer.
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
class PointRenderer
	extends StandardXYItemRenderer
{
	
	/** Set the size of the shapes to be rendered. */
	public final static int	SHAPESIZE = 4;
	
	/** The colors. */
	private List<Color> colours;
	
	/** The shapes for each series. */
	private List<Shape> itemShapes;

	/** 
	 * Creates an ellipse.
	 * 
	 * @return See above.
	 */
	private Ellipse2D.Double ellipse()
	{
		return new Ellipse2D.Double(-SHAPESIZE/2, -SHAPESIZE/2, 
													SHAPESIZE, SHAPESIZE);
	}
	
	/**
	 * Creates a rectangle 
	 * 
	 * @return See above.
	 */
	private Rectangle2D.Double rectangle()
	{
		return new Rectangle2D.Double(-SHAPESIZE/2, -SHAPESIZE/2, 
													  SHAPESIZE, SHAPESIZE);
	}
	
	/**
	 * Creates a new renderer.
	 *
	 * @param colours  the colors.
	 * @param shapes the shapes to render.
	 */
	public PointRenderer(List<Color> colours, List<Shape> shapes) 
	{
		if (colours == null)
			throw new IllegalArgumentException("List of colors cannot " +
					"be null.");
		if (shapes == null)
			throw new IllegalArgumentException("List of shapes cannot " +
					"be null.");
		setBaseShapesVisible(true);
		setPlotLines(false);
		this.colours = colours;
		this.itemShapes = shapes;
	}
	
	/**
	 * Sets the shapes for a series. 
	 * 
	 * @param series the series to apply the shape to.
	 * @param shape The shape. 
	 */
	public void setSeriesShape(int series, Shape shape)
	{
		itemShapes.set(series, shape);
	}
	
	/**
	 * Sets the shapes for a series. 
	 * 
	 * @param series the series to apply the shape to.
	 * @param shapeType The shape. 
	 */
	public void setSeriesShape(int series, ShapeType shapeType)
	{
		switch(shapeType)
		{
			case ELLIPSE:
				itemShapes.set(series, ellipse());
				break;
			case RECTANGLE:
				itemShapes.set(series, rectangle());
				break;
		}
	}
	
	/**
	 * Returns the shape for the current item to be rendered.
	 * 
	 * @param series current series being rendered.
	 * @param item 	item being rendered.
	 * @return shape to render.
	 */
	public Shape getItemShape(int series, int item)
	{
		return itemShapes.get(series);
	}
	
	/**
	 * Returns the paint for an item. 
	 * Overrides the default behavior inherited from AbstractSeriesRenderer.
	 *
	 * @param row  the series.
	 * @param column  the category.
	 * @return The item color.
	 */
	public Paint getItemPaint(final int row, final int column) 
	{
		return colours.get(row % colours.size());
	}
	
}