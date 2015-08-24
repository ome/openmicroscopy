/*
 * org.openmicroscopy.shoola.env.rnd.roi.ROIShapeStats 
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
import java.util.LinkedHashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/** 
 * Stores the results of some basic statistic analysis run on a given 
 * 2D-selection within an XY-plane.
 * Some of the fields are also used as accumulators during the computation.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class ROIShapeStats extends AbstractROIShapeStats
{

    //NOTE: fields have package visibility so they can easily be accessed
    //during the computation.  However, we only supply getters to outside
    //clients b/c these fields are read-only after the computation is done.

    /** 
     * Map whose keys are the point on the plane and the values are 
     * the corresponding pixels value.
     */
    private Map<Point, Double> pixelsValue;

    /**
     * Returns the map storing the pixel coordinates and the corresponding 
     * pixel value.
     * 
     * @return See above.
     */
    public Map<Point, Double> getPixelsValue() { return pixelsValue; }

    /** 
     * Calculates the mean and standard deviation for the current 
     * {@link ROIShapeStats}.
     * @see PointIteratorObserver#onEndPlane(int, int, int, int)
     */
    public void onEndPlane(int z, int c, int t, int pointsCount)
    {
        if (pointsCount <= 0) return;
        mean = sum/pointsCount;
        this.pointsCount = pointsCount;
        if (0 < pointsCount-1) {
            double sigmaSquare = 
                    (sumOfSquares-sum*sum/pointsCount)/(pointsCount-1);
            if (sigmaSquare > 0)
                standardDeviation = Math.sqrt(sigmaSquare);
        }
    }

    /**
     * Updates the min, max, and sum values of the current
     * {@link ROIShapeStats}.
     * @see PointIteratorObserver#update(double, int, int, int, Point)
     */
    public void update(double pixelValue, int z, int w, int t, Point loc)
    {
        min = Math.min(pixelValue,min);
        max = Math.max(pixelValue,max);
        sum += pixelValue;
        sumOfSquares += pixelValue*pixelValue;
        pixelsValue.put(loc, new Double(pixelValue));
    }

    /**
     * Creates a new map to store the pixel values. 
     * @see PointIteratorObserver#onStartPlane(int, int, int, int)
     */
    public void onStartPlane(int z, int w, int t, int pointsCount)
    {
        pixelsValue = new LinkedHashMap<Point, Double>(pointsCount);
    }

    /**
     * Required by {@link PointIteratorObserver} interface, but no-operation
     * implementation in our case.
     * @see PointIteratorObserver#iterationStarted()
     */
    public void iterationStarted() {}

    /**
     * Required by {@link PointIteratorObserver} interface, but no-operation
     * implementation in our case.
     * @see PointIteratorObserver#iterationFinished()
     */
    public void iterationFinished() {}

}
