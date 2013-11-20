/*
 * org.openmicroscopy.shoola.env.rnd.roi.PointIteratorObserver 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.env.rnd.roi;


//Java imports
import java.awt.Point;

//Third-party libraries

//Application-internal dependencies

/** 
 * Monitors a {@link PointIterator} as it proceeds in a point by point
 * iteration of an <code>ROI</code>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public interface PointIteratorObserver
{

    /**
     * Called just after the iteration begins, but before any pixels is
     * retrieved.
     */
    public void iterationStarted();

    /**
     * Called just before the 2D selection within a given plane is iterated.
     * 
     * @param z The z coordinate (stack frame) of the plane.
     * @param w The w coordinate (channel) of the plane.
     * @param t The t coordinate (timepoint) of the plane.
     * @param pointsCount How many points are contained in the selection that
     *                    is about to be iterated.
     */
    public void onStartPlane(int z, int w, int t, int pointsCount);

    /**
     * Called to notify the observer of the current pixel being iterated.
     * The coordinates of the pixel belong to the 2D selection within the plane
     * specified by the last call to {@link #onStartPlane(int, int, int, int)}.
     * 
     * @param pixelValue The value of the current pixel.
     * @param z The z coordinate (stack frame) of the plane.
     * @param w The w coordinate (channel) of the plane.
     * @param t The t coordinate (timepoint) of the plane.
     * @param loc The location of the pixelValue on the 2D-selection.
     */
    public void update(double pixelValue, int z, int w, int t, Point loc);

    /**
     * Called just after the 2D selection within a given plane has been
     * iterated.
     * 
     * @param z The z coordinate (stack frame) of the plane.
     * @param w The w coordinate (channel) of the plane.
     * @param t The t coordinate (timepoint) of the plane.
     * @param pointsCount How many points are contained in the selection that
     *                    has been iterated.
     */
    public void onEndPlane(int z, int w, int t, int pointsCount);

    /** Called just after the end of the iteration. */
    public void iterationFinished();

}
