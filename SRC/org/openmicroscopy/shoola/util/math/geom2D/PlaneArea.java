/*
 * org.openmicroscopy.shoola.util.math.geom2D.PlaneArea
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

package org.openmicroscopy.shoola.util.math.geom2D;


//Java imports
import java.awt.Shape;

//Third-party libraries

//Application-internal dependencies

/** 
 * Interface that all areas of the Euclidean space <b>R</b><sup>2</sup> must
 * implement. 
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
public interface PlaneArea
    extends Shape
{

    /** 
     * Sets the bounding <code>Rectangle</code> of the planeArea
     * to the specified x, y, width, and height.
     */
    public void setBounds(int x, int y, int width, int height);
    
    /** 
     * Resets the bounding <code>Rectangle</code> of the planeArea
     * according to the specified factor.
     */
    public void scale(double factor);
    
    /** Return an array of {@link PlanePoint} contained in the PlaneArea. */
    public PlanePoint[] getPoints();
    
}
