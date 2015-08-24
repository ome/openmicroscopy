/*
 * org.openmicroscopy.shoola.env.rnd.roi.ROIShapeStats 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
 * Stores the results of some basic statistic analysis run on a given
 * 2D-selection within an XY-plane. Some of the fields are also used as
 * accumulators during the computation.
 *
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ROIShapeStatsSimple extends AbstractROIShapeStats {
    /**
     * Array holding the pixels values.
     */
    private double[] pixelsValue;

    /**
     * Index pointing to the next unassigned field in the pixelsValue array
     */
    private int index;

    /**
     * Returns the pixel values.
     * 
     * @return See above.
     */
    public double[] getPixelsValue() {
        return pixelsValue;
    }

    /**
     * Calculates the mean and standard deviation for the current
     * {@link ROIShapeStatsSimple}.
     * 
     * @see PointIteratorObserver#onEndPlane(int, int, int, int)
     */
    public void onEndPlane(int z, int c, int t, int pointsCount) {
        if (pointsCount <= 0)
            return;
        mean = sum / pointsCount;
        this.pointsCount = pointsCount;
        if (0 < pointsCount - 1) {
            double sigmaSquare = (sumOfSquares - sum * sum / pointsCount)
                    / (pointsCount - 1);
            if (sigmaSquare > 0)
                standardDeviation = Math.sqrt(sigmaSquare);
        }
    }

    /**
     * Updates the min, max, and sum values of the current
     * {@link ROIShapeStatsSimple}.
     * 
     * @see PointIteratorObserver#update(double, int, int, int, Point)
     */
    public void update(double pixelValue, int z, int w, int t, Point loc) {
        min = Math.min(pixelValue, min);
        max = Math.max(pixelValue, max);
        sum += pixelValue;
        sumOfSquares += pixelValue * pixelValue;
        pixelsValue[index++] = pixelValue;
    }

    /**
     * Creates a new array to store the pixel values.
     * 
     * @see PointIteratorObserver#onStartPlane(int, int, int, int)
     */
    public void onStartPlane(int z, int w, int t, int pointsCount) {
        pixelsValue = new double[pointsCount];
        index = 0;
    }

    /**
     * Required by {@link PointIteratorObserver} interface, but no-operation
     * implementation in our case.
     * 
     * @see PointIteratorObserver#iterationStarted()
     */
    public void iterationStarted() {
    }

    /**
     * Required by {@link PointIteratorObserver} interface, but no-operation
     * implementation in our case.
     * 
     * @see PointIteratorObserver#iterationFinished()
     */
    public void iterationFinished() {
    }

}
