/*
 * org.openmicroscopy.shoola.env.rnd.roi.ROIStats
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
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;

/** 
 * 
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
public class ROIStats
    implements PointIteratorObserver
{

    private PixelsDimensions    dims;
    private Map                 arrayMap;
    
    
    /**
     * Transforms 3D coords into linear coords.
     * The returned value <code>L</code> is calculated as follows: 
     * <nobr><code>L = sizeZ*sizeW*t + sizeZ*w + z</code></nobr>.
     * 
     * @param z   The z coord.  Must be in the range <code>[0, sizeZ)</code>.
     * @param w   The w coord.  Must be in the range <code>[0, sizeW)</code>.
     * @param t   The t coord.  Must be in the range <code>[0, sizeT)</code>.
     * @return The linearized value corresponding to <code>(z, w, t)</code>.
     */
    private Integer linearize(int z, int w, int t) {
        if (z < 0 || dims.sizeZ <= z) 
            throw new IllegalArgumentException(
                    "z out of range [0, "+dims.sizeZ+"): "+z+".");
        if (w < 0 || dims.sizeW <= w) 
            throw new IllegalArgumentException(
                    "w out of range [0, "+dims.sizeW+"): "+w+".");
        if (t < 0 || dims.sizeT <= t) 
            throw new IllegalArgumentException(
                    "t out of range [0, "+dims.sizeT+"): "+t+".");
        return new Integer(dims.sizeZ*dims.sizeW*t + dims.sizeZ*w + z);
    }
    
    public ROIStats(PixelsDimensions dims)
    {
        if (dims == null) throw new NullPointerException("No dims.");
        this.dims = dims;
    }
    
    /**
     * Returns the stats, if any, that were calculated against the 2D selection
     * within the specified plane.
     * 
     * @param z
     * @param w
     * @param t
     * @return
     */
    public ROIPlaneStats getPlaneStats(int z, int w, int t)
    {
        Integer index = linearize(z, w, t);
        return (ROIPlaneStats) arrayMap.get(index);
    }
    
    /**
     * Creates a new map to store the {@link ROIPlaneStats} entries that are
     * about to be calculated.
     * @see PointIteratorObserver#iterationStarted()
     */
    public void iterationStarted() 
    {
        arrayMap = new HashMap();
    }

    /**
     * Creates a new {@link ROIPlaneStats} entry for the plane selection that
     * is about to be iterated.
     * @see PointIteratorObserver#onStartPlane(int, int, int, int)
     */
    public void onStartPlane(int z, int w, int t, int pointsCount)
    {
        ROIPlaneStats planeStats = new ROIPlaneStats();
        Integer index = linearize(z, w, t);
        arrayMap.put(index, planeStats);
    }

    /**
     * Updates the min, max, and sum values of the current {@link ROIPlaneStats}
     * entry as needed.
     * @see PointIteratorObserver#update(double, int, int, int)
     */
    public void update(double pixelValue, int z, int w, int t)
    {
        Integer index = linearize(z, w, t);
        ROIPlaneStats planeStats = (ROIPlaneStats) arrayMap.get(index);
        //planeStats can't be null, see onStartPlane().
        if (pixelValue < planeStats.min) planeStats.min = pixelValue;
        if (planeStats.max < pixelValue) planeStats.max = pixelValue;
        planeStats.sum += pixelValue;
        planeStats.sumOfSquares = pixelValue*pixelValue;
    }

    /** 
     * Calculates the mean and standard deviation for the current 
     * {@link ROIPlaneStats} entry.
     * @see PointIteratorObserver#onEndPlane(int, int, int, int)
     */
    public void onEndPlane(int z, int w, int t, int pointsCount)
    {
        Integer index = linearize(z, w, t);
        ROIPlaneStats ps = (ROIPlaneStats) arrayMap.get(index);
        //planeStats can't be null, see onStartPlane().
        if (0 < pointsCount) {
            ps.mean = ps.sum/pointsCount;
            if (0 < pointsCount-1)
                ps.standardDeviation = Math.sqrt(
                        (ps.sumOfSquares - ps.sum*ps.sum/pointsCount)
                        /(pointsCount-1));
        }
    }

    /**
     * No-op implementation.
     * Required by the observer interface, but not needed in our case.
     * @see PointIteratorObserver#iterationFinished()
     */
    public void iterationFinished() {}

}
