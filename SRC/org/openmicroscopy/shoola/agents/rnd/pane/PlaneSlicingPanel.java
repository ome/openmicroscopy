/*
 * org.openmicroscopy.shoola.agents.rnd.pane.PlaneSlicingPanel
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
class PlaneSlicingPanel
	extends JPanel
{
	/** Graphics constants. */
	static final int    			WIDTH = 220, HEIGHT = 200;
	static final int				topBorder = 20, leftBorder = 40,
									square = 140, bottomBorder = 30, 
									rightBorder = 10,
									lS = leftBorder+square, 
									tS = topBorder+square, 
									tS2 = topBorder+square/2,
									rStart = leftBorder+square/2-20+1, 
									rEnd = leftBorder+square/2+20;
									
	/** Cursor sizes. */
	static final int 				triangleW = 7, triangleH = 8;
	
	/** Background color of the panel. */
	static final Color				bgColor = Color.WHITE;
	
	/** Color of the border painted when the panel is selected. */
	static final Color				borderColor = Color.BLUE;
	
	/** Color of the fixed line. */
	static final Color				fixedLineColor = Color.BLACK;
	
	/** grid color. */
	static final Color				gridColor = Color.LIGHT_GRAY;
	
	/** Axis color. */
	static final Color				axisColor = Color.GRAY;
	
	/** Color of the lines. */
	static final Color				lineColor = Color.RED;
	
	/** 
	 * Color of the layer painted on top of the panel wher the panel 
	 * is not selected.
	 * Light_gray with alpha component.
	 */
	static final Color				layerColor = new Color(192, 192, 192, 80);
	
	/** Color of the startCursor (input & output). */
	private static final Color		ostartColor = Color.BLACK;
	
	/** Color of the endCursor (input & output). */
	private static final Color		oendColor = Color.GRAY;
	
	
	
	/** Control points. */
	private Point2D 				startPt, endPt;
	
	private int 					xStartOutput1, xStartOutput2, xStartOutput3, 
									yStartOutput1, yStartOutput2, yStartOutput3;
	private int 					xEndOutput1, xEndOutput2, xEndOutput3,
									yEndOutput1, yEndOutput2, yEndOutput3;
									
	private boolean					isSelected;

	PlaneSlicingPanel(int yStart, int yEnd)
	{
		isSelected = true;
		setCursorOutputStart(leftBorder-10, yStart);
		setCursorOutputEnd(lS+10, yEnd);
		startPt = new Point2D.Double();
		endPt = new Point2D.Double();
		startPt.setLocation((double) leftBorder, (double) yStart);
		endPt.setLocation((double) lS, (double) yEnd);
		super.repaint();
	}
	
	/** 
	 * Sets the selection control, <code>true</code> if the panel is selected 
	 * <code>false</code> otherwise.
	 * 
	 * @param b		true/false.
	 */
	void setIsSelected(boolean b)
	{
		isSelected = b;
		super.repaint();
	}

	/** 
	 * Position the start cursor.
	 * 
	 * @param x	x-coordinate.
	 * @param y y-coordinate.
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
	 * Position the end cursor.
	 * 
	 * @param x	x-coordinate.
	 * @param y y-coordinate.
	 */
	void setCursorOutputEnd(int x, int y)
	{
		xEndOutput1 = x;
		xEndOutput2 = x+triangleH;
		xEndOutput3 = x+triangleH;
		yEndOutput1 = y;
		yEndOutput2 = y-triangleW;
		yEndOutput3 = y+triangleW;
	}
	
	/** 
	 * Reposition the start cursor and the control point.
	 * 
	 * @param x	x-coordinate.
	 * @param y y-coordinate.
	 */
	void updateOutputStart(int y)
	{
		yStartOutput1 = y;
		yStartOutput2 = y-triangleW;
		yStartOutput3 = y+triangleW;
		startPt.setLocation(startPt.getX(), (double) y);
		super.repaint();
	} 
	
	/** 
	 * Reposition the end cursor and the control point.
	 * 
	 * @param x	x-coordinate.
	 * @param y y-coordinate.
	 */
	void updateOutputEnd(int y)
	{
		yEndOutput1 = y;
		yEndOutput2 = y-triangleW;
		yEndOutput3 = y+triangleW;
		endPt.setLocation(endPt.getX(), (double) y);
		super.repaint();
	}
	
	/** Overrides the paintComponent method. */ 
	 public void paintComponent(Graphics g)
	 {
		Graphics2D g2D = (Graphics2D) g;
		g2D.setColor(bgColor);
		g2D.fillRect(0, 0, WIDTH, HEIGHT); 
		Font font = g2D.getFont();
		FontMetrics fontMetrics = g2D.getFontMetrics();
		int hFont = fontMetrics.getHeight();
		Rectangle2D rInput = font.getStringBounds("(1) Input", 
									g2D.getFontRenderContext());
		Rectangle2D rUpper= font.getStringBounds("upper", 
									g2D.getFontRenderContext());							
		int wInput = (int) rInput.getWidth();
		int wUpper = (int) rUpper.getWidth();
		 
		// grid
		AffineTransform transform = new AffineTransform();
		// 140/10 = 14 then middle = 14/2
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
		 
		//x-axis
		g2D.drawLine(leftBorder-5, tS, lS+8, tS);
		g2D.drawLine(lS+5, tS-3, lS+8, tS);
		g2D.drawLine(lS+5, tS+3, lS+8, tS);
		g2D.drawLine(lS, tS, lS, tS+5);
		 
		//output cursor start 
		int xStartOutputPoints[] = {xStartOutput1, xStartOutput2, 
								xStartOutput3};
		int yStartOutputPoints[] = {yStartOutput1, yStartOutput2, 
								yStartOutput3};
		GeneralPath filledPolygonStartOutput = new GeneralPath();
		filledPolygonStartOutput.moveTo(xStartOutputPoints[0], 
									yStartOutputPoints[0]);
		for (int index = 1; index < xStartOutputPoints.length; index++)
			filledPolygonStartOutput.lineTo(xStartOutputPoints[index], 
											yStartOutputPoints[index]);
		filledPolygonStartOutput.closePath();
		g2D.setColor(ostartColor);
		g2D.fill(filledPolygonStartOutput);
		//output cursor end output
		int xEndOutputPoints[] = {xEndOutput1, xEndOutput2, xEndOutput3};
		int yEndOutputPoints[] = {yEndOutput1, yEndOutput2, yEndOutput3};
		GeneralPath filledPolygonEndOutput = new GeneralPath();
		filledPolygonEndOutput.moveTo(xEndOutputPoints[0], 
									yEndOutputPoints[0]);
		for (int index = 1; index < xEndOutputPoints.length; index++)
			filledPolygonEndOutput.lineTo(xEndOutputPoints[index], 
										yEndOutputPoints[index]);
		filledPolygonEndOutput.closePath();
		g2D.setColor(oendColor);
		g2D.fill(filledPolygonEndOutput);
		g2D.drawString("lower", leftBorder+10, tS+hFont);
		g2D.drawString("upper", lS-wUpper-10, tS+hFont);
		g2D.drawString("(1) Input", leftBorder+square/2-wInput/2, 
					tS+bottomBorder/2+hFont);
		g2D.drawLine(rStart-1, (int) startPt.getY(), rStart-1, tS2);
		g2D.drawLine(rEnd, tS2, rEnd, (int) endPt.getY());
		//set line color
		g2D.setColor(lineColor);
		g2D.setStroke(new BasicStroke(1.5f));
		//drawline
		g2D.drawLine(leftBorder+1, (int) startPt.getY(), rStart-1,
					(int) startPt.getY());
		g2D.drawLine(rEnd+1, (int) endPt.getY(), lS, (int) endPt.getY());
		 
		//Fixed line.
		g2D.setColor(fixedLineColor);
		g2D.drawLine(rStart, tS2, rEnd, tS2);
		if (isSelected) {
			g2D.setColor(borderColor);
			g2D.draw(new Rectangle2D.Double(0, 0, WIDTH, HEIGHT));
		} else {
			g2D.setColor(layerColor);
			g2D.fillRect(0, 0, WIDTH, HEIGHT);
		}
	}
	 
}
