/*
 * org.openmicroscopy.shoola.util.math.geom2D.RectangleAreaAdapter
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.util.math.geom2D;

//Java imports
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.openmicroscopy.shoola.util.ui.UIUtilities;

//Third-party libraries

//Application-internal dependencies


/** 
 * This following class is the <code>body</code> of the 
 * {@link RectangleArea handle}.
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
class RectangleAreaAdapter
    extends Rectangle 
    implements PlaneArea
{


    /**
     * Returns <code>true</code> if the difference between the number is less
     * than {@link UIUtilities#EPSILON}, <code>false</code> otherwise.
     * 
     * @param a One of the values to handle.
     * @param b One of the values to handle.
     * @return See above.
     */
    private boolean inBounds(double a, double b)
    {
    	return Math.abs(a-b) < UIUtilities.EPSILON;
    }
    
    /** 
     * Constructs a new {@link Rectangle} whose top-left corner is (0, 0) 
     * and whose width and height are both zero.
     */
    RectangleAreaAdapter()
    {
        super();
    }
    
    /** 
     * Constructs a new {@link Rectangle} whose top-left corner is specified 
     * as (x, y) and whose width and height are specified by 
     * the arguments of the same name.
     * 
     * @param x The x-coordinate of the top-left corner.
     * @param y The y-coordinate of the top-left corner.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     */
    RectangleAreaAdapter(int x, int y, int width, int height)
    {
        super(x, y, width, height);
    }
    
    /** 
     * Implemented as specified in the {@link PlaneArea} I/F.
     * @see PlaneArea#scale(double)
     */
    public void scale(double factor)
    {
        Rectangle r = getBounds();
        setBounds((int) (r.x*factor), (int) (r.y*factor), 
                  (int) (r.width*factor), (int) (r.height*factor)); 
    }

    /**
     * Implemented as specified by the {@link PlaneArea} I/F.
     * @see PlaneArea#getPoints()
     */
    public PlanePoint[] getPoints()
    {
        Rectangle r = getBounds();
        List vector = new ArrayList(r.height*r.width);
        int xEnd = r.x+r.width, yEnd = r.y+r.height;
        int x, y;
        for (y = r.y; y < yEnd; ++y) 
            for (x = r.x; x < xEnd; ++x) 
                if (contains(x, y)) vector.add(new PlanePoint(x, y));
        return (PlanePoint[]) vector.toArray(new PlanePoint[vector.size()]);
    }
    
    /** 
     * Implemented as specified in the {@link PlaneArea} I/F. 
     * @see PlaneArea#onBoundaries(double, double)
     */
    public boolean onBoundaries(double x, double y)
    {
        double xCorner = getX(), yCorner = getY();
        double w = getWidth(), h = getHeight();
        return ((inBounds(x, xCorner) && y >= yCorner && y <= yCorner+h) ||
                (inBounds(x, xCorner+w)  && y >= yCorner && y <= yCorner+h) ||
                (inBounds(y, yCorner) && x >= xCorner && x <= xCorner+w) ||
                (inBounds(y, yCorner+h) && x >= xCorner && x <= xCorner+w));
    }
    
    /** 
     * Implemented as specified in the {@link Copiable} I/F. 
     * @see Copiable#copy()
     */
    public Object copy() { return super.clone(); }

}
