/*
 * org.openmicroscopy.shoola.agents.rnd.pane.HistogramPanel
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
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStatsEntry;

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
class HistogramPanel
	extends JPanel
{
	
	/** Graphic constant. */
	static final int            	WIDTH = 420, HEIGHT = 250;
	static final int				topBorder = 20, leftBorder = 80, 
									bottomBorder = 30, rightBorder = 40, 
									window = 10, heightStat = 200, 
									widthStat = 300,
									triangleW = 10, triangleH = 11,
									lS = leftBorder+widthStat,
									tS = topBorder+heightStat;
	 
	/** Color of the Histogram bins. */
	private static final Color  	binColor = Color.BLUE;
	
	/** Background color of the panel. */
	private static final Color		bgColor = Color.BLACK;

	/** Axis color. */
	private static final Color		axeColor = Color.GRAY;
	
	/** Color of the input start cursor. */
	private static final Color  	startColor = 
										GraphicsRepresentation.iStartColor;
	/** Color of the input start cursor. */
	private static final Color		endColor = 
										GraphicsRepresentation.iEndColor;
									
	/** 
	 * Color of the layer painted on top of the histogram.
	 * Light_gray with alpha component.
	 */
	private static final Color      layerColor = new Color(192, 192, 192, 90);
	
	
	private int                 	controlOutputStart, controlOutputEnd, 
									heightStart, heightEnd;
	private int                 	xStartOutput1, xStartOutput2, xStartOutput3, 
									yStartOutput1, yStartOutput2, yStartOutput3;
	private int                 	xEndOutput1, xEndOutput2, xEndOutput3,
									yEndOutput1, yEndOutput2, yEndOutput3;
	private String              	min, max, curMin, curMax;
	
	
	private PixelsStatsEntry[]  	histogramData;
	private int                 	sizeBin;  
	private HistogramDialogManager	manager;
	
	/**
	 * 
	 * @param mini		minimum value of the input window.
	 * @param maxi		maximum value of the input window.
	 * @param startReal	real value input window value.	
	 * @param endReal	real value input window value.	
	 * @param yStart
	 * @param yEnd
	 * @param histogramData
	 */
	HistogramPanel(HistogramDialogManager manager, int mini, int maxi, 
					int startReal, int endReal, int yStart, int yEnd, 
					PixelsStatsEntry[] histogramData)
	{
		this.histogramData = histogramData;
		this.manager = manager;
		sizeBin = (int) (widthStat/histogramData.length);
		setWindowLimits(mini, maxi);
		setInputWindow(startReal, endReal);
		
		//control output window
		controlOutputStart = yStart;
		heightStart = tS-yStart; 
		heightEnd = yEnd-topBorder;
		controlOutputEnd = yEnd;
		
		//output knob
		setKnobOutputStart(lS+10, yStart);
		setKnobOutputEnd(lS+10, yEnd);
		super.repaint();
	}

	/**
	 * Resets the current Minimum value and repaint the leftBorder.
	 *
	 * @param v     minimum value.
	 */
	void setCurMin(int v)
	{
		curMin = "start: "+v;
		super.repaint(0, 0, leftBorder, HEIGHT);
	}
	
	/** 
	 * Resets the current Maximum value and repaint the leftBorder.
	 *
	 * @param v     minimum value
	 */
	void setCurMax(int v)
	{
		curMax = "end: "+v;
		super.repaint(0, 0, leftBorder, HEIGHT);
	}
	
	/**
	 * Position the outputStart knob.
	 *
	 * @param x     x-coordinate.
	 * @param y     y-coordinate.
	 */
	void setKnobOutputStart(int x, int y)
	{  
		xStartOutput1 = x;
		xStartOutput2 = x+triangleH;
		xStartOutput3 = x+triangleH;
		yStartOutput1 = y;
		yStartOutput2 = y-triangleW;
		yStartOutput3 = y+triangleW;
	}
	
	/**
	 * Position the outputEnd knob.
	 *
	 * @param x     x-coordinate.
	 * @param y     y-coordinate.
	 */    
	void setKnobOutputEnd(int x, int y)
	{
		xEndOutput1 = x;
		xEndOutput2 = x+triangleH;
		xEndOutput3 = x+triangleH;
		yEndOutput1 = y;
		yEndOutput2 = y-triangleW;
		yEndOutput3 = y+triangleW;
	}
	
	/** 
	 * Sets the y-coordinate of the outputStart knob.
	 *
	 * @param y     y-coordinate.
	 */    
	void updateStartOutputKnob(int y)
	{
		yStartOutput1 = y;
		yStartOutput2 = y-triangleW;
		yStartOutput3 = y+triangleW;
	}
	
	/** 
	 * Sets the y-coordinate of the outputEnd knob.
	 *
	 * @param y     y-coordinate.
	 */ 
	void updateEndOutputKnob(int y)
	{
		yEndOutput1 = y;
		yEndOutput2 = y-triangleW;
		yEndOutput3 = y+triangleW;
	}
	
	/** 
	 * Reset the location of the outputStart knob
	 * and display the real value that corresponds to the location.
	 *
	 * @param y         y-coordinate.
	 * @param yReal   	real value.
	 */          
	void updateInputStart(int y, int yReal)
	{
		controlOutputStart = y;
		heightStart = tS-y;
		curMin = "start: "+yReal;
		updateStartOutputKnob(y);
		super.repaint();
	}
	
	/** 
	 * Reset the location of the outputEnd knob
	 * and display the real value that corresponds to the location.
	 *
	 * @param y         y-coordinate.
	 * @param yReal   	real value.
	 */      
	void updateInputEnd(int y, int yReal)
	{
		controlOutputEnd = y;
		heightEnd = y-topBorder;
		curMax = "end: "+yReal;
		updateEndOutputKnob(y);
		super.repaint();
	}
	
	/** 
	 * Display the maximum and minimum values for the input window. 
	 * 
	 * @param m		minimum.
	 * @param M		maximum.
	 */
	private void setWindowLimits(int m, int M)
	{
		min = "Min: "+m;
		max = "Max: "+M;
	}
	
	/** 
	 * Display the current input window. 
	 * 
	 * @param s		input start.
	 * @param e		input end.
	 */
	private void setInputWindow(int s, int e)
	{
		curMin = "start: "+s;
		curMax = "end: "+e;
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
		Rectangle2D rStart = font.getStringBounds(min, 
											g2D.getFontRenderContext());
		int hStart = (int) rStart.getHeight();
		Rectangle2D rEnd = font.getStringBounds(max, 
											g2D.getFontRenderContext());
		int hEnd = (int) rEnd.getHeight();
		Rectangle2D rInput = font.getStringBounds("timepoint", 
											g2D.getFontRenderContext());
		int wInput = (int) rInput.getWidth();
		g2D.setColor(axeColor);
		
		//y-axis
		g2D.drawLine(leftBorder, topBorder-8, leftBorder, tS+5);
		g2D.drawLine(leftBorder, topBorder-8, leftBorder-3, topBorder-5);
		g2D.drawLine(leftBorder, topBorder-8, leftBorder+3, topBorder-5);
		
		// x-axis
		g2D.drawLine(leftBorder-5, tS, lS+8, tS);
		g2D.drawLine(lS+5, tS-3, lS+8, tS);
		g2D.drawLine(lS+5, tS+3, lS+8, tS);
		
		// draw output interval
		g2D.drawString(min, 5, tS-window); 
		g2D.drawLine(leftBorder-5, tS-window, leftBorder, tS-window);
		g2D.drawString(max, 5, topBorder+window);
		g2D.drawLine(leftBorder-5, topBorder+window, leftBorder, 
						topBorder+window);				
		g2D.drawString("timepoint", leftBorder+widthStat/2-wInput/2, 
						tS+hFont+5);
						
		//knob start output
		int xStartOutputPoints[] = {xStartOutput1, xStartOutput2, xStartOutput3};
		int yStartOutputPoints[] = {yStartOutput1, yStartOutput2, yStartOutput3};
		GeneralPath filledPolygonStartOutput = new GeneralPath();
		filledPolygonStartOutput.moveTo(xStartOutputPoints[0], 
										yStartOutputPoints[0]);
		for (int index = 1; index < xStartOutputPoints.length; index++)
			filledPolygonStartOutput.lineTo(xStartOutputPoints[index], 
										yStartOutputPoints[index]);
		filledPolygonStartOutput.closePath();
		
		// paint with selected color
		g2D.setColor(startColor);
		g2D.drawString(curMin, 10, tS-hStart-hFont-window); 
		g2D.fill(filledPolygonStartOutput);
		
		//knob end output
		int xEndOutputPoints[] = {xEndOutput1, xEndOutput2, xEndOutput3};
		int yEndOutputPoints[] = {yEndOutput1, yEndOutput2, yEndOutput3};
		GeneralPath filledPolygonEndOutput = new GeneralPath();
		filledPolygonEndOutput.moveTo(xEndOutputPoints[0], yEndOutputPoints[0]);
		for (int index = 1; index < xEndOutputPoints.length; index++)
			filledPolygonEndOutput.lineTo(xEndOutputPoints[index], 
										yEndOutputPoints[index]);
		filledPolygonEndOutput.closePath();
		g2D.setColor(endColor);
		g2D.drawString(curMax, 10, topBorder+hEnd+hFont+window);    
		g2D.fill(filledPolygonEndOutput);      
		//paint histogram
		g2D.setColor(binColor);
		g2D.drawString("Pixel", 5, topBorder+heightStat/2);
		g2D.drawString("intensity", 5, topBorder+heightStat/2+hEnd+5);
		PixelsStatsEntry entry;
		int min, max;
		int[] bin = new int[histogramData.length];
		for (int i = 0; i < histogramData.length; i++) {
			entry = histogramData[i];
			min = manager.convertRealIntoGraphics((int) entry.min);
			max = manager.convertRealIntoGraphics((int) entry.max);
			bin[i] = max;
			g2D.fillRect(leftBorder+i*sizeBin, max, sizeBin, min-max); 
		}
		g2D.setColor(axeColor);
		int y, x;
		for (int i = 0; i < histogramData.length; i++) {
			 y = bin[i];
			 x = leftBorder+(i+1)*sizeBin;
			 g2D.drawLine(x, y, x, topBorder+heightStat+5);
		}
		
		// paint layered rectangles 
		g2D.setColor(layerColor);
		
		//end rectangle
		Rectangle2D endOutput = new Rectangle2D.Double(leftBorder, 
										topBorder, widthStat, heightEnd);
		g2D.fill(endOutput);
		
		//start rectangle 
		Rectangle2D startOutput = new Rectangle2D.Double(leftBorder, 
									controlOutputStart, widthStat, heightStart);
		g2D.fill(startOutput);
		g2D.setColor(endColor);
		g2D.draw(endOutput);
		g2D.setColor(startColor);
		g2D.draw(startOutput);                              
	}
    
}
