/*
 * org.openmicroscopy.shoola.env.rnd.roi.ROIStats 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import omero.model.Pixels;


/** 
 * collects statistics for a given roi
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ROIStats
    implements PointIteratorObserver
{

    /** 
     * The dimensions of the pixels set over which the statistic will be
     * computed.
     */
    private Pixels dims;

    /**
     * Maps a {@link #linearize(int, int, int) linearized} <code>(z, w, t)
     * </code> tuple identifying a plane onto the statistics calculated for the
     * 2D-selection contained in that plane. 
     */
    private Map<Integer, ROIShapeStats>	arrayMap;

    /**
     * Transforms 3D coordinates into linear coordinates.
     * The returned value <code>L</code> is calculated as follows:
     * <code>L = sizeZ*sizeW*t + sizeZ*w + z</code>.
     * 
     * @param z The z coordinate. Must be in the range <code>[0, sizeZ)</code>.
     * @param w The w coordinate. Must be in the range <code>[0, sizeW)</code>.
     * @param t The t coordinate. Must be in the range <code>[0, sizeT)</code>.
     * @return The linearized value corresponding to <code>(z, w, t)</code>.
     */
    private Integer linearize(int z, int w, int t)
    {
        int sizeZ = dims.getSizeZ().getValue();
        int sizeC = dims.getSizeC().getValue();
        if (z < 0 || sizeZ <= z) 
            throw new IllegalArgumentException(
                    "z out of range [0, "+sizeZ+"): "+z+".");
        if (w < 0 || sizeC <= w) 
            throw new IllegalArgumentException(
                    "w out of range [0, "+sizeC+"): "+w+".");
        if (t < 0 || dims.getSizeT().getValue() <= t) 
            throw new IllegalArgumentException(
                    "t out of range [0, "+dims.getSizeT()+"): "+t+".");
        return Integer.valueOf(sizeZ*sizeC*t + sizeZ*w + z);
    }

    /**
     * Creates a new object to collect statistics for a given ROI.
     * 
     * @param dims The dimensions of the pixels set over which the statistics 
     *             will be computed. Mustn't be <code>null</code>.
     */
    public ROIStats(Pixels dims)
    {
        if (dims == null) throw new NullPointerException("No dims.");
        this.dims = dims;
    }

    /**
     * Returns the statistics, if any, that were calculated against 
     * the 2D-selection within the specified plane.
     * 
     * @param z The z coordinate. Must be in the range <code>[0, sizeZ)</code>.
     * @param w The w coordinate. Must be in the range <code>[0, sizeW)</code>.
     * @param t The t coordinate. Must be in the range <code>[0, sizeT)</code>.
     * @return A {@link ROIPlaneStats} object holding the statistics for the
     *          2D-selection in the specified plane. If no selection was
     *          made in that plane, then <code>null</code> is returned instead.
     */
    public AbstractROIShapeStats getPlaneStats(int z, int w, int t)
    {
        Integer index = linearize(z, w, t);
        return arrayMap.get(index);
    }

    /**
     * Creates a new map to store the {@link ROIShapeStats} entries that are
     * about to be calculated.
     * @see PointIteratorObserver#iterationStarted()
     */
    public void iterationStarted() 
    {
        arrayMap = new HashMap<Integer, ROIShapeStats>();
    }

    /**
     * Creates a new {@link ROIShapeStats} entry for the plane selection that
     * is about to be iterated.
     * @see PointIteratorObserver#onStartPlane(int, int, int, int)
     */
    public void onStartPlane(int z, int w, int t, int pointsCount)
    {
        ROIShapeStats planeStats = new ROIShapeStats();
        Integer index = linearize(z, w, t);
        arrayMap.put(index, planeStats);
    }

    /**
     * Updates the min, max, and sum values of the current {@link ROIShapeStats}
     * entry as needed.
     * @see PointIteratorObserver#update(double, int, int, int, Point)
     */
    public void update(double pixelValue, int z, int w, int t, Point loc)
    {
        Integer index = linearize(z, w, t);
        AbstractROIShapeStats planeStats = arrayMap.get(index);
        //planeStats can't be null, see onStartPlane().
        if (pixelValue < planeStats.getMin())
            planeStats.setMin(pixelValue);
        if (planeStats.getMax() < pixelValue)
            planeStats.setMax(pixelValue);
        planeStats.addToSum(pixelValue);
        planeStats.addToSumOfSquares(pixelValue);
    }

    /** 
     * Calculates the mean and standard deviation for the current 
     * {@link ROIShapeStats} entry.
     * @see PointIteratorObserver#onEndPlane(int, int, int, int)
     */
    public void onEndPlane(int z, int w, int t, int pointsCount)
    {
        Integer index = linearize(z, w, t);
        AbstractROIShapeStats ps = arrayMap.get(index);
        //planeStats can't be null, see onStartPlane().
        if (0 < pointsCount) {
            ps.setMean(ps.getSum()/pointsCount);
            ps.setPointsCount(pointsCount);
            if (0 < pointsCount-1) {
                double sigmaSquare =
                   (ps.getSumOfSquares() - ps.getSum()*ps.getSum()/pointsCount)
                     /(pointsCount-1);
                if (sigmaSquare > 0)
                    ps.setStandardDeviation(Math.sqrt(sigmaSquare));
            }  
        }
    }

    /**
     * Required by {@link PointIteratorObserver} interface, but no-operation
     * implementation in our case.
     * @see PointIteratorObserver#iterationFinished()
     */
    public void iterationFinished() {}

}
