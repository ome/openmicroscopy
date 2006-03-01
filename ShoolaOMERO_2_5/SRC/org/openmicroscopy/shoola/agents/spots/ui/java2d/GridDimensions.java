/*
 * org.openmicroscopy.shoola.agents.spots.ui.java2d.GridDimensions;
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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

//Third-party libraries

//Application-internal dependencies

/** 
 * Grid calculations in 2D 
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



public abstract class GridDimensions {
	
	public static final int GRID_OFFSET=15;
	public static final Color AXIS_COLOR=Color.BLACK;
	
	protected static final int HALF_TICK_LENGTH=7;
	
	protected final static BasicStroke stroke = new BasicStroke(1.0f);
	public static final Font font = new Font("Helvetica",Font.PLAIN,10); 
    public static final int FUDGE=3;
	
	protected double horizMin;
	protected double vertMin;
	protected double horizMax;
	protected double vertMax;
	

	protected int gridWidth;
	protected int gridHeight;

	protected int xStart;
	protected int yStart;
	protected int xEnd;
	protected int yEnd;

	protected static final double ROTATION = Math.toRadians(-90.0);
	
	public GridDimensions(double min,double horizMax,double vertMax) {
		this.horizMin = min;
		this.vertMin = min;
		this.horizMax = horizMax;
		this.vertMax = vertMax;
	}

	
	public abstract void setDimensions(int canvasWidth,int canvasHeight);
	
	
	public abstract double getHorizCoord(double x);
	
	public abstract double getVertCoord(double y);
	
	public abstract void drawAxes(Graphics2D g,
		String horizLow,String horizHigh,String vertLow,String vertHigh);
	
	public void setExtents(int hMin,int hMax,int vMin,int vMax) {
		horizMin=hMin;
		horizMax =hMax;
		vertMin = vMin;
		vertMax = vMax;
	}
}