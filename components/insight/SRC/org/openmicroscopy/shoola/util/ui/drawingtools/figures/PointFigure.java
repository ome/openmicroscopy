/*
 * org.openmicroscopy.shoola.util.ui.drawingtools.figures.PointFigure 
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
package org.openmicroscopy.shoola.util.ui.drawingtools.figures;

//Java imports
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

//Third-party libraries
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.EllipseFigure;
import org.jhotdraw.draw.Handle;
import org.jhotdraw.draw.MoveHandle;
import org.jhotdraw.draw.RelativeLocator;
import org.jhotdraw.geom.Insets2D;


//Application-internal dependencies

/** 
 * Creates a new figure type, the pointFigure is an extension of the ellipse
 * that has cross hairs on the centre. The pointFigure has also got a 
 * Creation tool. This is because the point 
 * tool should not be allowed to be sized. 
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
public class PointFigure 
	extends EllipseFigure
{
	
	/** The radius of the ellipse. */
	public final static double	POINT_SIZE = 6;
	
	/** Total size of the figure. including crosshairs. */
	public final static double	FIGURE_SIZE = 22;

	/**
	 * Draws the crosshairs of the ellipse on the graphics context.
	 * 
	 * @param g The graphics context.
	 */
	private void drawCrossHairs(Graphics2D g)
	{
		Ellipse2D.Double newEllipse =
				new Ellipse2D.Double(ellipse.getCenterX()-POINT_SIZE, 
									ellipse.getCenterY()-POINT_SIZE, 
									POINT_SIZE*2, POINT_SIZE*2);
		double cx = Math.floor(newEllipse.getCenterX());
		double cy = Math.floor(newEllipse.getCenterY());
		double x = Math.floor(newEllipse.getX());
		double y = Math.floor(newEllipse.getY());
		double width = POINT_SIZE;
		double height = POINT_SIZE;
		double loffset = 3;
		
		Line2D.Double lhline = new Line2D.Double(x-loffset*2, cy, 
													cx-loffset, cy);
		Line2D.Double rhline =
				new Line2D.Double(cx+loffset, cy, cx+width+loffset*2, cy);
		Line2D.Double tvline = new Line2D.Double(cx, y-loffset*2, 
												cx, cy-loffset);
		Line2D.Double bvline =
				new Line2D.Double(cx, cy+loffset, cx, cy+height+loffset*2);
		
		g.draw(lhline);
		g.draw(rhline);
		g.draw(tvline);
		g.draw(bvline);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 */ 
	public PointFigure(double x, double y)
	{
		super(x, y, FIGURE_SIZE, FIGURE_SIZE);
	}
	
	/**
	 * Returns the insets.
	 * 
	 * @return See above.
	 */
	public Insets2D.Double getInsets() { return new Insets2D.Double(); }
	
	/**
	 * Returns the color used to fill.
	 * 
	 * @return See above.
	 */
	public Color getFillColor()
	{
		return AttributeKeys.FILL_COLOR.get(this);
	}
	
	/**
	 * Overridden to draw the stroke of the figure.
	 * @see EllipseFigure#drawStroke(Graphics2D)
	 */
	protected void drawStroke(Graphics2D g)
	{
		Ellipse2D.Double newEllipse =
				new Ellipse2D.Double(ellipse.getCenterX()-POINT_SIZE, 
						ellipse.getCenterY()-POINT_SIZE, 
						POINT_SIZE*2, POINT_SIZE*2);
		g.draw(newEllipse);
		drawCrossHairs(g);
	}
	
	/**
	 * Overridden to draw the fill with a new ellipse.
	 * @see EllipseFigure#drawFill(Graphics2D)
	 */
	protected void drawFill(Graphics2D g)
	{
		Ellipse2D.Double newEllipse =
				new Ellipse2D.Double(ellipse.getCenterX()-POINT_SIZE, 
									ellipse.getCenterY()-POINT_SIZE, 
									POINT_SIZE*2, POINT_SIZE*2);
		g.fill(newEllipse);
		drawText(g);
	}
	
	/**
	 * Overridden to create the appropriated handles.
	 * @see EllipseFigure#createHandles(int)
	 */
	public Collection<Handle> createHandles(int detailLevel)
	{
		List<Handle> handles = new LinkedList<Handle>();
		if (detailLevel == 0)
		{
			handles.add(new MoveHandle(this, RelativeLocator.northWest()));
			handles.add(new MoveHandle(this, RelativeLocator.northEast()));
			handles.add(new MoveHandle(this, RelativeLocator.southWest()));
			handles.add(new MoveHandle(this, RelativeLocator.southEast()));
		}
		return handles;
	}
	
	
	
}
