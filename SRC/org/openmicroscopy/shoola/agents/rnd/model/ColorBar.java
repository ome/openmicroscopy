/*
 * org.openmicroscopy.shoola.agents.rnd.model.ColorBar
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.rnd.model;

//Java imports
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ColorBar
	extends JPanel
{
	/** Color of the cursor. */
	private static final Color	CURSOR_COLOR = Color.GRAY;
	
	private static final int	triangleW = ColorPalette.triangleW, 
								triangleH = ColorPalette.triangleH;
						
	/** The width and height of the panel. */
	private static final int	w = ColorPalette.WIDTH_BAR; 
	private static final int 	h = ColorPalette.HEIGHT_BAR;

	/** cursor's coordinate. */
	private int 				xCursor;
	private int					yCursor;
	
	/** paint from colorStart to colorEnd. */
	private Color 				colorEnd, colorStart;
   
	private String				type;
    
	/**
	 * Creates a new instance.
	 * 
	 * @param x				x-coordinate of the cursor in the range [0, w].
	 * @param colorStart	color.
	 * @param colorEnd		color.
	 */
	ColorBar(int x, Color colorStart, Color colorEnd, String type)
	{
		this.colorEnd = colorEnd;
		this.colorStart = colorStart;
		this.type = type;
		yCursor = 0;
		xCursor = ColorPalette.leftBorder+x;       
	}
    
	/** 
	 * Positions the line on the color panel.
	 * 
	 * @param x		x-coordinate.
	 */
	void setLineLocation(int x)
	{
		xCursor = x;
	}
    
	/** 
	 * Set the colorEnd. 
	 * 
	 * @param c		color's value.
	 */
	void setColor(Color c)
	{
		colorEnd = c;
		repaint();
	}
    
	/** Overrides the paintComponent method. */
	public void paintComponent(Graphics g)
	{
		Graphics2D g2D = (Graphics2D) g;
		g2D.setPaint(Color.black);
		g2D.drawString(type, 0, h);
		g2D.setColor(getBackground());
		GradientPaint startToEnd = new GradientPaint(ColorPalette.leftBorder,
													  0, colorStart, w,
													  h, colorEnd);
		g2D.setPaint(startToEnd);
		g2D.fillRect(ColorPalette.leftBorder, 0, w, h);
		// draw cursor
		g2D.setColor(CURSOR_COLOR);
		g2D.drawLine(xCursor, yCursor, xCursor, yCursor+h);
		int xPoints[] = {xCursor, xCursor-triangleW, xCursor+triangleW};
		int yPoints[] = {yCursor+h, yCursor+h+triangleH, yCursor+h+triangleH};
		GeneralPath filledPolygon = new GeneralPath();
		filledPolygon.moveTo(xPoints[0], yPoints[0]);
		for (int index = 1; index < xPoints.length; index++)
			filledPolygon.lineTo(xPoints[index], yPoints[index]);
		filledPolygon.closePath();
		g2D.fill(filledPolygon);    
	}
    
}

