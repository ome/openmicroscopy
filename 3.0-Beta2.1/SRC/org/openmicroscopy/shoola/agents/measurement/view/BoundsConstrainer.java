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


//Java imports
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

//Third-party libraries
import org.jhotdraw.draw.Constrainer;
import org.jhotdraw.draw.DrawingView;

//Application-internal dependencies

/** 
 * Helper class used to constrain the ROI within the drawing bounds.
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
class BoundsConstrainer 
	implements Constrainer 
{
	
	/** The width of a grid cell. */
    private double width;
    
    /** The height of a grid cell. */
    private double height;
    
    /**
    * Creates a new instance.
    * 
    * @param width	The width of a grid cell.
	* @param height The height of a grid cell.
	*/
	BoundsConstrainer(double width, double height) 
	{
		if (width <= 0 || height <= 0) 
			throw new IllegalArgumentException("Width or height is <= 0");
	    this.width = width;
	    this.height = height;
	}
	    
	/**
	 * Returns the width of a grid cell.
	 * 
	 * @return See above.
	 */
	double getWidth() { return width; }
	  
	/**
	 * Returns the width of a grid cell.
	 * 
	 * @return See above.
	 */
	double getHeight() { return height; }
	
	/**
	 * Implemented as specified by the {@link Constrainer} I/F.
	 * @see Constrainer#constrainPoint(java.awt.geom.Point2D.Double)
	 */
	public Point2D.Double constrainPoint(Point2D.Double p)
	{
		// FIXME - This works only for integer widths!
		double x, y;
		x = p.getX();
		y = p.getY();
		if (x > width) x = width;
		if (y > height) y = height;
		if (x < 0) x = 0;
		if (y < 0) y = 0;
		p.setLocation(x, y);
		return p;
	}

	/**
	 * Implemented as specified by the {@link Constrainer} I/F.
	 * @see Constrainer#isVisible()
	 */
	public boolean isVisible() { return (width > 1 && height > 1); }
	
	/**
	 * Required by the {@link Constrainer} I/F, no-op implementation
	 * in our case.
	 * @see Constrainer#draw(Graphics2D, DrawingView)
	 */
	public void draw(Graphics2D g, DrawingView view) {}
	
	/**
	 * Overridden to return a stringified version displaying the width and
	 * height of the grid.
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return super.toString()+"["+width+","+height+"]";
	}
	
}


