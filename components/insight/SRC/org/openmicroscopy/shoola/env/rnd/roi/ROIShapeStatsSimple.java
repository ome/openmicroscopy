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
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies

/**
 * Stores the results of some basic statistic analysis run on a given
 * 2D-selection within an XY-plane. Some of the fields are also used as
 * accumulators during the computation.
 *
 * (A less memory consuming implementation of {@link AbstractROIShapeStats} than
 * {@link ROIShapeStats} )
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ROIShapeStatsSimple extends AbstractROIShapeStats {

    /**
     * The {@link List} of {@link Point}s
     */
    private List<Point> points;

    /**
     * The corresponding values
     */
    private double values[];

    /**
     * Get the {@link Point}s in the order they have been added.
     * 
     * @return See above.
     */
    public List<Point> getPoints() {
        return points;
    }

    /**
     * Get the values in the order they have been added.
     * 
     * @return See above.
     */
    public double[] getValues() {
        return values;
    }

    /**
     * Get the value for a certain {@link Point} (Note: Not very efficient, if
     * you know the index better use {@link #getValues()}[index] )
     * 
     * @param p
     *            The {@link Point} to get the value for
     * @return See above.
     */
    public double getValue(Point p) {
        int i = points.indexOf(p);
        if (i == -1)
            return Double.NaN;
        else
            return values[i];
    }

    /**
     * Calculates the mean and standard deviation for the current
     * {@link ROIShapeStats}.
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
     * Updates the min, max, and sum values of the current {@link ROIShapeStats}
     * .
     * 
     * @see PointIteratorObserver#update(double, int, int, int, Point)
     */
    public void update(double pixelValue, int z, int w, int t, Point loc) {
        min = Math.min(pixelValue, min);
        max = Math.max(pixelValue, max);
        sum += pixelValue;
        sumOfSquares += pixelValue * pixelValue;
        values[points.size()] = pixelValue;
        points.add(loc);
    }

    /**
     * Creates a new map to store the pixel values.
     * 
     * @see PointIteratorObserver#onStartPlane(int, int, int, int)
     */
    public void onStartPlane(int z, int w, int t, int pointsCount) {
        points = new ArrayList<Point>();
        values = new double[pointsCount];
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
