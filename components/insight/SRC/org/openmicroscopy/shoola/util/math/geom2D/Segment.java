/*
 * org.openmicroscopy.shoola.util.math.geom2D.Segment
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * A segment in the Euclidean space <b>R</b><sup>2</sup>.
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
public class Segment
{
    
    /** The origin point of the segment. */
    public final PlanePoint  origin;
    
    /** The end point of the segment. */
    public final PlanePoint  direction;
    
    /**
     * Creates a new instance.
     * 
     * @param o The origin of the segment.
     * @param e The end of the segment.
     */
    public Segment(PlanePoint o, PlanePoint e)
    {
        if (o == null) throw new NullPointerException("No origin.");
        if (e == null) throw new NullPointerException("No end p.");
        if (o.equals(e))
            throw new IllegalArgumentException("Need two different points.");
        origin = o;
        direction = origin.vec(e);
    }
    
    /**
     * Returns the point of this line defined by <code>k</code>.
     * More precisely, this method returns the 
     * <code>{@link #origin}+k{@link #direction}</code> point.
     * 
     * @param k The coefficient to select the point. Must be in the range 
     *          <code>[0, 1]</code>.
     * @return  See above.
     */
    public PlanePoint getPoint(double k)
    {
        if (k < 0 || k > 1) 
            throw new IllegalArgumentException("Coefficient must be in the " +
                    "range [0, 1].");
        return new PlanePoint(origin.x1 + k*direction.x1, 
                                origin.x2 + k*direction.x2);
    }
    
    /**
     * Tells whether the specified point lies on this line.
     * 
     * @param p The point to test. Mustn't be <code>null</code>.
     * @return  <code>true</code> if <code>p</code> lies on this line,
     *          <code>false</code> otherwise.
     */
    public boolean lies(PlanePoint p)
    {
        if (p == null) throw new NullPointerException("No point.");
        boolean result = false;
        double k1, k2;
        if (direction.x1 == 0 && direction.x2 !=0) {
            k2 = (p.x2-origin.x2)/direction.x2;
            if (k2 < 0 || k2 > 1) result = false;
            else result = (p.x1 == origin.x1);
        } else if (direction.x1 != 0 && direction.x2 ==0) {
            k1 = (p.x1-origin.x1)/direction.x1;
            if (k1 < 0 || k1 > 1) result = false;
            else result = (p.x2 == origin.x2);
        } else if (direction.x1 != 0 && direction.x2 !=0) {
            k1 = (p.x1-origin.x1)/direction.x1;
            k2 = (p.x2-origin.x2)/direction.x2;
            if (Math.abs(k1-k2) < UIUtilities.EPSILON) {
                return (!(k1 < 0 || k1 > 1));
            }
        }
        return result;
    }
    
    /**
     * Overridden to reflect equality of abstract values (data object) as 
     * opposite to object identity.
     * @see Object#equals(Object)
     */
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof Line)) return false;
        Line other = (Line) o;
        return (origin == other.origin && direction == other.direction);
    }
    
    /**
     * Overridden to reflect equality of abstract values (data object) as 
     * opposite to object identity.
     * @see Object#hashCode()
     */
    public int hashCode() { return origin.hashCode(); }
    
}
