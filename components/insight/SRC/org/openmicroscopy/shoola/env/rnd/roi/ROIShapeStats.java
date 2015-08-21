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
public class ROIShapeStats
    implements PointIteratorObserver
{

    //NOTE: fields have package visibility so they can easily be accessed
    //during the computation.  However, we only supply getters to outside
    //clients b/c these fields are read-only after the computation is done.

    /** The minimum value within the 2D-selection. */
    private double min = Double.MAX_VALUE;

    /** The maximum value within the 2D-selection. */
    private double max = Double.MIN_VALUE;

    /** The sum of all values within the 2D-selection. */
    private double sum;

    /** The sum of the squares of all values within the 2D selection. */
    private double sumOfSquares;  //Only useful during computation, hence no getter.

    /** The mean value within the 2D-selection. */
    private double mean;

    /** The standard deviation within the 2D-selection. */
    private  double standardDeviation;

    /** The number of points contained within the 2D-selection. */
    private int pointsCount;

    /** 
     * Array holding the pixels values.
     */
    private double[] pixelsValue;
    
    /**
     * Index pointing to the next unassigned field in the pixelsValue array
     */
    private int index;

    /**
     * Sets the minimum value.
     * 
     * @param min The value to set.
     */
    void setMin(double min) { this.min = min; }

    /**
     * Sets the maximum value.
     * 
     * @param max The value to set.
     */
    void setMax(double max) { this.max = max; }

    /**
     * Adds the passed value to the sum.
     * 
     * @param value The value to add.
     */
    void addToSum(double value) { this.sum += value; }

    /**
     * Adds the passed value to the sum of squares.
     * 
     * @param value The value to add.
     */
    void addToSumOfSquares(double value) { this.sumOfSquares += value*value; }

    /**
     * Sets the mean value.
     * 
     * @param mean The value to set.
     */
    void setMean(double mean) { this.mean = mean; }

    /**
     * Sets the number of points counted.
     * 
     * @param pointsCount The value to set.
     */
    void setPointsCount(int pointsCount) { this.pointsCount = pointsCount; }

    /**
     * Returns the sum of squares.
     * 
     * @return See above.
     */
    double getSumOfSquares() { return sumOfSquares; }

    /**
     * Sets the standard deviation.
     * 
     * @param standardDeviation The value to set.
     */
    void setStandardDeviation(double standardDeviation)
    {
        this.standardDeviation = standardDeviation;
    }

    /**
     * Returns the minimum value within the 2D-selection.
     * 
     * @return See above.
     */
    public double getMin() { return min; }

    /**
     * Returns the maximum value within the 2D-selection.
     * 
     * @return See above.
     */
    public double getMax() { return max; }

    /**
     * Returns the mean value within the 2D-selection.
     * 
     * @return See above.
     */
    public double getMean() { return mean; }

    /**
     * Returns the standard deviation within the 2D-selection.
     * 
     * @return See above.
     */
    public double getStandardDeviation() { return standardDeviation; }

    /**
     * Returns the sum of all values within the 2D-selection.
     * 
     * @return See above.
     */
    public double getSum() { return sum; }

    /** 
     * Returns the number of points contained within the 2D-selection. 
     * 
     * @return See above.
     */
    public int getPointsCount() { return pointsCount; }

    /**
     * Returns the pixel values.
     * 
     * @return See above.
     */
    public double[] getPixelsValue() { return pixelsValue; }

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
        pixelsValue[index++] = pixelValue;
    }

    /**
     * Creates a new array to store the pixel values. 
     * @see PointIteratorObserver#onStartPlane(int, int, int, int)
     */
    public void onStartPlane(int z, int w, int t, int pointsCount)
    {
        pixelsValue = new double[pointsCount];
        index = 0;
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
