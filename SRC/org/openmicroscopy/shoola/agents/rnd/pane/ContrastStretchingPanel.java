/*
 * org.openmicroscopy.shoola.agents.rnd.pane.ContrastStretchingPanel
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

package org.openmicroscopy.shoola.agents.rnd.pane;



//Java imports
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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
class ContrastStretchingPanel
	extends JPanel
{
	static final int            WIDTH = 220, HEIGHT = 200;
	static final int			topBorder = 20, leftBorder = 40, square = 140, 
								bottomBorder = 30, rightBorder = 10,
								lS = leftBorder+square, tS = topBorder+square,
								lS2 = leftBorder+square/2;
	/** cursor sizes. */
	static final int 			triangleW = 7, triangleH = 8;
	
	/** Background color of the panel. */
	private static final Color	bgColor = Color.WHITE;
	
	/** Color of the lines. */
	private static final Color	lineColor = Color.RED;
	
	/** Color of the startCursor (input & output). */
	private static final Color	startColor = Color.BLACK;
	
	/** Color of the endCursor (input & output). */
	private static final Color	endColor = Color.GRAY;
	
	/** grid color. */
	private static final Color	gridColor = Color.LIGHT_GRAY;
	
	/** Axis color. */
	private static final Color	axisColor = Color.GRAY;
	
	/** Controls points. */
	private Point2D             startPt, endPt, staticStartPt, staticEndPt;

	/** cursors' coordinates. */
	private int                 xStart1, xStart2, xStart3, 
								yStart1, yStart2, yStart3,
								xEnd1, xEnd2, xEnd3, yEnd1, yEnd2, yEnd3,
								xStartOutput1, xStartOutput2, xStartOutput3, 
								yStartOutput1, yStartOutput2, yStartOutput3,
								xEndOutput1, xEndOutput2, xEndOutput3,
								yEndOutput1, yEndOutput2, yEndOutput3;
								
	ContrastStretchingPanel(int xStart, int xEnd, int yStart, int yEnd)
	{
		setCursorStart(xStart, tS+10);
		setCursorEnd(xEnd, tS+10);
		//output cursor
		setCursorOutputStart(leftBorder-10, yStart);
		setCursorOutputEnd(leftBorder-10, yEnd);
		startPt = new Point2D.Double();
		endPt = new Point2D.Double();
		staticStartPt = new Point2D.Double();
		staticEndPt = new Point2D.Double();
		// bottom-left corner
		staticStartPt.setLocation((double) leftBorder, (double) tS);
		startPt.setLocation((double) xStart, (double) yStart);
		// top-right corner
		staticEndPt.setLocation((double) lS, (double) topBorder);
		endPt.setLocation((double) xEnd, (double) yEnd);
		super.repaint();
	}
	
	/**
	 * Position the inputStart cursor.
	 * 
	 * 
	 * @param x		x-coordinate.
	 * @param y		y-coordinate.
	 */
	void setCursorStart(int x, int y)
	{  
		xStart1 = x;
		xStart2 = x-triangleW;
		xStart3 = x+triangleW;
		yStart1 = y;
		yStart2 = y+triangleH;
		yStart3 = y+triangleH;
	}
	
	/**
	 * Position the inputEnd cursor.
	 * 
	 * @param x		x-coordinate.
	 * @param y		y-coordinate.
	 */
	void setCursorEnd(int x, int y)
	{
		xEnd1 = x;
		xEnd2 = x-triangleW;
		xEnd3 = x+triangleW;
		yEnd1 = y;
		yEnd2 = y+triangleH;
		yEnd3 = y+triangleH;
	}
	
	/**
	 * Position the outputStart cursor.
	 * 
	 * @param x		x-coordinate.
	 * @param y		y-coordinate.
	 */
	void setCursorOutputStart(int x, int y)
	{  
		xStartOutput1 = x;
		xStartOutput2 = x-triangleH;
		xStartOutput3 = x-triangleH;
		yStartOutput1 = y;
		yStartOutput2 = y-triangleW;
		yStartOutput3 = y+triangleW;
	}
	
	/**
	 * Position the outputEnd cursor.
	 * 
	 * @param x		x-coordinate.
	 * @param y		y-coordinate.
	 */
	void setCursorOutputEnd(int x, int y)
	{
		xEndOutput1 = x;
		xEndOutput2 = x-triangleH;
		xEndOutput3 = x-triangleH;
		yEndOutput1 = y;
		yEndOutput2 = y-triangleW;
		yEndOutput3 = y+triangleW;
	}
	
	/**
	 * Position the inputStart cursor and redraw the lines.
	 * 
	 * @param x		x-coordinate.
	 */
	void updateStartCursor(int x)
	{
		xStart1 = x;
		xStart2 = x-triangleW;
		xStart3 = x+triangleW;
		startPt.setLocation((double) x, startPt.getY());
		super.repaint();
	}
	
	/**
	 * Position the inputEnd cursor and redraw the lines.
	 * 
	 * @param x		x-coordinate.
	 */
	void updateEndCursor(int x)
	{
		xEnd1 = x;
		xEnd2 = x-triangleW;
		xEnd3 = x+triangleW;
		endPt.setLocation((double) x, endPt.getY());
		super.repaint();
	}
	
	/**
	 * Position the outputStart cursor and redraw the lines.
	 * 
	 * @param y		y-coordinate.
	 */
	void updateStartOutputCursor(int y)
	{
		yStartOutput1 = y;
		yStartOutput2 = y-triangleW;
		yStartOutput3 = y+triangleW;
		startPt.setLocation(startPt.getX(), (double) y);
		super.repaint();
	}
	
	/**
	 * Position the outputEnd cursor and redraw the lines.
	 * 
	 * @param y		y-coordinate.
	 */
	void updateEndOutputCursor(int y)
	{
		yEndOutput1 = y;
		yEndOutput2 = y-triangleW;
		yEndOutput3 = y+triangleW;
		endPt.setLocation(endPt.getX(), (double) y);
		super.repaint();
	}
	
	/** Overrides the paintComponent() method. */
	public void paintComponent(Graphics g)
	{
		Graphics2D g2D = (Graphics2D) g;
		g2D.setColor(bgColor);
		g2D.fillRect(0, 0, WIDTH, HEIGHT);
		
		Font font = g2D.getFont();
		FontMetrics fontMetrics = g2D.getFontMetrics();
		int hFont = fontMetrics.getHeight();
		Rectangle2D rInput = font.getStringBounds("Input", 
									g2D.getFontRenderContext());
		int wInput = (int) rInput.getWidth();
		
		// grid
		AffineTransform transform = new AffineTransform();
		// 140/10 =14 then  middle = 14/2
		transform.translate(leftBorder+70, topBorder+70); 
		transform.scale(1, -1);
		transform.scale(10, 10);       
		g2D.setPaint(gridColor);
		GeneralPath path = new GeneralPath();
		for (int i = -7; i <= 7; i++) {
			path.moveTo(i, -7);
			path.lineTo(i, 7);
		}
		for (int i = -7; i <= 7; i++) {
			path.moveTo(-7, i);
			path.lineTo(7, i);
		}
		g2D.draw(transform.createTransformedShape(path));
		g2D.setColor(axisColor);
		
		//y-axis
		g2D.drawLine(leftBorder, topBorder-8, leftBorder, tS+5);
		g2D.drawLine(leftBorder, topBorder-8, leftBorder-3, topBorder-5);
		g2D.drawLine(leftBorder, topBorder-8, leftBorder+3, topBorder-5);
		g2D.drawLine(leftBorder-5, topBorder, leftBorder, topBorder);
		
		// x-axis
		g2D.drawLine(leftBorder-5, tS, lS+8, tS);
		g2D.drawLine(lS+5, tS-3, lS+8, tS);
		g2D.drawLine(lS+5, tS+3, lS+8, tS);
		g2D.drawLine(lS, tS, lS, tS+5);
		
		//input cursor start
		int xStartPoints[] = {xStart1, xStart2, xStart3};
		int yStartPoints[] = {yStart1, yStart2, yStart3};
		GeneralPath filledPolygonStart = new GeneralPath();
		filledPolygonStart.moveTo(xStartPoints[0], yStartPoints[0]);
		for (int index = 1; index < xStartPoints.length; index++) {
			filledPolygonStart.lineTo(xStartPoints[index], yStartPoints[index]);
		}
		filledPolygonStart.closePath();
		g2D.setColor(startColor);
		g2D.fill(filledPolygonStart);
		
		//input cursor end 
		int xEndPoints[] = {xEnd1, xEnd2, xEnd3};
		int yEndPoints[] = {yEnd1, yEnd2, yEnd3};
		GeneralPath filledPolygonEnd = new GeneralPath();
		filledPolygonEnd.moveTo(xEndPoints[0], yEndPoints[0]);
		for (int index = 1; index < xEndPoints.length; index++) {
			filledPolygonEnd.lineTo(xEndPoints[index], yEndPoints[index]);
		}
		filledPolygonEnd.closePath();
		g2D.setColor(endColor);
		g2D.fill(filledPolygonEnd);
		
		// output cursor start 
		int xStartOutputPoints[] = {xStartOutput1, xStartOutput2, 
									xStartOutput3};
		int yStartOutputPoints[] = {yStartOutput1, yStartOutput2, 
									yStartOutput3};
		GeneralPath filledPolygonStartOutput = new GeneralPath();
		filledPolygonStartOutput.moveTo(xStartOutputPoints[0], 
										yStartOutputPoints[0]);
		for (int index = 1; index < xStartOutputPoints.length; index++) {
			filledPolygonStartOutput.lineTo(xStartOutputPoints[index],
											 yStartOutputPoints[index]);
		}
		filledPolygonStartOutput.closePath();
		g2D.setColor(startColor);
		g2D.fill(filledPolygonStartOutput);
		//output cursor end
		int xEndOutputPoints[] = {xEndOutput1, xEndOutput2, xEndOutput3};
		int yEndOutputPoints[] = {yEndOutput1, yEndOutput2, yEndOutput3};
		GeneralPath filledPolygonEndOutput = new GeneralPath();
		filledPolygonEndOutput.moveTo(xEndOutputPoints[0], yEndOutputPoints[0]);
		for (int index = 1; index < xEndOutputPoints.length; index++) {
			filledPolygonEndOutput.lineTo(xEndOutputPoints[index], 
										yEndOutputPoints[index]);
		}
		filledPolygonEndOutput.closePath();
		g2D.setColor(startColor);
		g2D.fill(filledPolygonEndOutput);
		g2D.drawString("Input", lS2-wInput/2, tS+bottomBorder/2+hFont);
		// set line color
		g2D.setColor(lineColor);
		g2D.setStroke(new BasicStroke(1.5f));
		// draw line
		g2D.drawLine((int) staticStartPt.getX(), (int) staticStartPt.getY(),
					(int) startPt.getX(), (int) startPt.getY());
		g2D.drawLine((int) endPt.getX(), (int) endPt.getY(), 
					(int) staticEndPt.getX(), (int) staticEndPt.getY());
		g2D.drawLine((int) startPt.getX(), (int) startPt.getY(), 
					(int) endPt.getX(), (int) endPt.getY()); 
	}
    
}
