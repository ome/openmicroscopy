/*
 * org.openmicroscopy.shoola.agents.rnd.model.ColorPaletteBar
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
class ColorPaletteBar 
	extends JPanel
{
	
	/** Color of the knob. */
	private static final Color	KNOB_COLOR = Color.GRAY;
	
	private static final int  	border = ColorPalette.leftBorder;
	private static final int	triangleW = ColorPalette.triangleW, 
								triangleH = ColorPalette.triangleH;
    
	/** The width and height of the panel. */
	private static final int	w = ColorPalette.WIDTH_BAR, 
								h = ColorPalette.HEIGHT_BAR;
	private static final int	p = w/6;
    
	private static final String	type = "H";
    
	/** knobs' coordinate. */
	private int 		xKnob, yKnob;
    
	/**
	 * Creates a new intance.
	 * 
	 * @param x		x-coordinate of the knob in the range [0, w]
	 */
	ColorPaletteBar(int x)
	{
		yKnob = 0;
		xKnob = ColorPalette.leftBorder+x;
	}
    
	/** 
	 * Positions the line on the color panel.
	 * 
	 * @param x		x-coordinate.
	 */
	void setLineLocation(int x)
	{
		xKnob = x;
	}
    
	/** Overrides the paintComponent method. */
	public void paintComponent(Graphics g)
	{
		Graphics2D g2D = (Graphics2D) g;
		g2D.setPaint(Color.black);
		g2D.drawString(type, 0, h);
		int k = 0;        
		GradientPaint redToYellow = new GradientPaint(border+k*p, yKnob, 
										Color.red, border+(k+1)*p, yKnob, 
										Color.yellow);
		g2D.setPaint(redToYellow);
		g2D.fillRect(border+k*p, yKnob, p, h);
		k++;
		GradientPaint yellowToGreen = new GradientPaint(border+k*p, yKnob, 
											Color.yellow, border+(k+1)*p, 
											yKnob, Color.green);
		g2D.setPaint(yellowToGreen);
		g2D.fillRect(border+k*p, yKnob, p, h);
		k++;
		GradientPaint greenToCyan = new GradientPaint(border+k*p, yKnob,
										Color.green, border+(k+1)*p, yKnob,
										Color.cyan);
		g2D.setPaint(greenToCyan);
		g2D.fillRect(border+k*p, yKnob, p, h);
		k++;
		GradientPaint cyanToBlue = new GradientPaint(border+k*p, yKnob,
										Color.cyan, border+(k+1)*p, yKnob,
										Color.blue);
		g2D.setPaint(cyanToBlue);
		g2D.fillRect(border+k*p, yKnob, p, h);
		k++;
		GradientPaint blueToMagenta = new GradientPaint(border+k*p, yKnob, 
											Color.blue, border+(k+1)*p, yKnob,
											Color.magenta);
		g2D.setPaint(blueToMagenta);
		g2D.fillRect(border+k*p, yKnob, p, h);
		k++;
		GradientPaint magentaToRed = new GradientPaint(border+k*p, yKnob, 
											Color.magenta, border+(k+1)*p,
											yKnob, Color.red);
		g2D.setPaint(magentaToRed);
		g2D.fillRect(border+k*p, yKnob, p, h);
		//draw knob
		g2D.setColor(KNOB_COLOR);
		g2D.drawLine(xKnob, yKnob, xKnob, yKnob+h);
		int xPoints[] = {xKnob, xKnob-triangleW, xKnob+triangleW};
		int yPoints[] = {yKnob+h, yKnob+h+triangleH, yKnob+h+triangleH};
		GeneralPath filledPolygon = new GeneralPath();
		filledPolygon.moveTo(xPoints[0], yPoints[0]);
		for (int index = 1; index < xPoints.length; index++)
			filledPolygon.lineTo(xPoints[index], yPoints[index]);
		filledPolygon.closePath();
		g2D.fill(filledPolygon);
	}
    
}

