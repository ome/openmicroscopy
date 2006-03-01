/*
 * org.openmicroscopy.shoola.agents.spots.ui.java2d.GridDimensionsYIncreaseUp
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

package org.openmicroscopy.shoola.agents.spots.ui.java2d;

//Java imports
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;

/** 
 * Grid calculations in 2D, for axes where values increase going _up_ the screen.
 * 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public class GridDimensionsYIncreaseUp extends GridDimensions{
	
	
	public GridDimensionsYIncreaseUp(double min,double horizMax,double vertMax) {
		super(min,horizMax,vertMax);
		
	}

	
	public void setDimensions(int canvasWidth,int canvasHeight) {
		
		gridWidth = canvasWidth-GRID_OFFSET; 
		gridHeight= canvasHeight-GRID_OFFSET;  
		
		xStart = GRID_OFFSET; // was INSET;
		yStart = canvasHeight-GRID_OFFSET;//was -INSET;
		
		xEnd = xStart+gridWidth;
		yEnd = yStart-gridHeight; // because y increases going downwards.
		
	}
		
	public double getHorizCoord(double x) {
		double res =  xStart+((x-horizMin)/(horizMax-horizMin))*gridWidth;
		return res;
	}
	
	public double getVertCoord(double y) {
		double res  =  yStart-((y-vertMin)/(vertMax-vertMin))*gridHeight;
		return res;
	}
	
	public void drawAxes(Graphics2D g,String horizLow,String horizHigh,
		String vertLow,String vertHigh) {
		
		Paint oldcolor = g.getPaint();
		Stroke oldStroke = g.getStroke();
		g.setPaint(AXIS_COLOR);
		g.setStroke(stroke);

		// horiz
		g.drawLine(xStart,yStart,xEnd,yStart);

		//vert
		g.drawLine(xStart,yStart,xStart,yEnd);
		
		//	horiz ticks
		int xTick1 = (int) (xStart + SpotsTrajectory.LABEL_FACTOR*(xEnd-xStart));
		int horizBase = yStart;
		g.drawLine(xTick1,yStart-HALF_TICK_LENGTH,xTick1,horizBase);
	
		int xTick2 = (int) (xStart + (1-SpotsTrajectory.LABEL_FACTOR)*(xEnd-xStart));
		g.drawLine(xTick2,yStart-HALF_TICK_LENGTH,xTick2,horizBase);
	
		// horiz labels
		FontMetrics fm = g.getFontMetrics();
		//	get first val
		
		g.setFont(font);
		int width = fm.stringWidth(horizLow);
		int height = fm.getHeight();
				
		// calc position & paint it
				
		g.drawString(horizLow,xTick1-width/2,horizBase+height/2+FUDGE);

		// second label.
		
		width = fm.stringWidth(horizHigh);
		g.drawString(horizHigh,xTick2-width/2,horizBase+height/2+FUDGE);
				
		
		//	vert ticks.
	
		int yTick1 = (int) (yEnd + SpotsTrajectory.LABEL_FACTOR*(yStart-yEnd));
		g.drawLine(xStart,yTick1,xStart+HALF_TICK_LENGTH,yTick1);
	
		int yTick2 = (int) (yEnd + (1-SpotsTrajectory.LABEL_FACTOR)*(yStart-yEnd));
		g.drawLine(xStart,yTick2,xStart+HALF_TICK_LENGTH,yTick2);
	

		//vert label
		int x = xStart-FUDGE;
		int y = yTick2+fm.stringWidth(vertLow)/2;
		g.translate(x,y);
		g.rotate(ROTATION);
		g.drawString(vertLow,0,0);
		g.rotate(-ROTATION);
		g.translate(-x,-y);
		
		y = yTick1+fm.stringWidth(vertHigh)/2;
		g.translate(x,y);
		g.rotate(ROTATION);
		g.drawString(vertHigh,0,0);
		g.rotate(-ROTATION);
		g.translate(-x,-y);
		
		//g.drawString(vertHigh,xStart,yTick2);
		g.setPaint(oldcolor);
		g.setStroke(oldStroke);
	}
}