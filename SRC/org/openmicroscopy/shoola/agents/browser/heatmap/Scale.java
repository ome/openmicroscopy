/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.Scale
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.heatmap;

import java.awt.Color;
import java.awt.geom.Point2D;

/**
 * Abstracts a scale and methods to interpolate based on min/max values.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public interface Scale
{
    /**
     * Returns the minimum of the scale.
     * @return See above.
     */
    public double getMin();
    
    /**
     * Returns the maximum of the scale.
     * @return See above.
     */
    public double getMax();
    
    /**
     * Gets the projected value of value onto a new min/max scale.
     * @param value The value relative to this scale's new min/max.
     * @param min The min of the new base.
     * @param max The max of the new base.
     * @return See above.
     */
    public double getScalar(double value, double min, double max);
    
    /**
     * Project a value onto a particular color.
     * @param value The value to check.
     * @param minColor The color corresponding to this scale's min.
     * @param maxColor The color corresponding to this scale's max.
     * @return The color corresponding to val.
     */
    public Color getColor(double value, Color minColor, Color maxColor);
    
    /**
     * Project a value onto a particular line (between the two points)
     * @param value The value to check.
     * @param minPoint The point corresponding to this scale's min.
     * @param maxPoint The point corresponding to this scale's max.
     * @return The point corresponding to val.
     */
    public Point2D getPoint(double value, Point2D minPoint, Point2D maxPoint);
}
