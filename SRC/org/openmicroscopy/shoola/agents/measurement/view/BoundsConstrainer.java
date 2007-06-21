/*
 * org.openmicroscopy.shoola.agents.measurement.view.BoundsConstrainer 
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
package org.openmicroscopy.shoola.agents.measurement.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jhotdraw.draw.Constrainer;
import org.jhotdraw.draw.DrawingView;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
public class BoundsConstrainer 
	implements Constrainer 
{
    private double width, height;
    /**
    * Creates a new instance.
    * @param width The width of a grid cell.
	* @param height The height of a grid cell.
	*/
	public BoundsConstrainer(double width, double height) 
	{
		if (width <= 0 || height <= 0) throw new IllegalArgumentException("Width or height is <= 0");
	    this.width = width;
	    this.height = height;
	}
	    
	public double getWidth()
	{
		return width;
	}
	  
	public double getHeight() {
		return height;
	}
	
	public Point2D.Double constrainPoint(Point2D.Double p) {
		// FIXME - This works only for integer widths!

		System.err.println("Width : " + width + " Height : " + height);
		
		double x, y;
		x = p.getX();
		y = p.getY();
		System.err.println(" x : " + x + " y " + y);
		if(x>width)
			x = width;
		if(y>height)
			y = height;
		if(x<0)
			x = 0;
		if(y<0)
			y = 0;
		p.setLocation(x, y);
		return p;
	}
	public String toString() {
		return super.toString()+"["+width+","+height+"]";
	}
	
	public boolean isVisible() {
		return (width > 1 && height > 1);
	}
	
	public void draw(Graphics2D g, DrawingView view) 
	{
		
	}
}


