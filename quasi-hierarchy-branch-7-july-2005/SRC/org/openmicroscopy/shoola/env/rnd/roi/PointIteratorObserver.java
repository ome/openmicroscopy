/*
 * org.openmicroscopy.shoola.env.rnd.roi.PointIteratorObserver
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

package org.openmicroscopy.shoola.env.rnd.roi;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Monitors a {@link PointIterator} as it proceeds in a point by point
 * iteration of a {@link org.openmicroscopy.shoola.util.image.roi.ROI5D}.
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
     * @param z  The z coord (stack frame) of the plane.
     * @param w  The w coord (channel) of the plane.
     * @param t  The t coord (timepoint) of the plane.
     * @param pointsCount  How many points are contained in the selection that
     *                      is about to be iterated.
     */
    public void onStartPlane(int z, int w, int t, int pointsCount);
    
    /**
     * Called to notify the observer of the current pixel being iterated.
     * The coords of the pixel belong to the 2D selection within the plane
     * specified by the last call to {@link #onStartPlane(int, int, int, int)}. 
     * 
     * @param pixelValue  The value of the current pixel. 
     * @param z  The z coord (stack frame) of the plane.
     * @param w  The w coord (channel) of the plane.
     * @param t  The t coord (timepoint) of the plane.
     */
    public void update(double pixelValue, int z, int w, int t);
    
    /**
     * Called just after the 2D selection within a given plane has been 
     * iterated.
     * 
     * @param z  The z coord (stack frame) of the plane.
     * @param w  The w coord (channel) of the plane.
     * @param t  The t coord (timepoint) of the plane.
     * @param pointsCount  How many points are contained in the selection that
     *                      has been iterated.
     */
    public void onEndPlane(int z, int w, int t, int pointsCount);
    
    /**
     * Called just after the end of the iteration.
     */
    public void iterationFinished();
    
}
