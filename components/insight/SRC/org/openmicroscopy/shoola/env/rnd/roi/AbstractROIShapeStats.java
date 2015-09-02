/*
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public abstract class AbstractROIShapeStats implements PointIteratorObserver {

    /** The minimum value within the 2D-selection. */
    protected double min = Double.MAX_VALUE;
    
    /** The maximum value within the 2D-selection. */
    protected double max = Double.MIN_VALUE;
    
    /** The sum of all values within the 2D-selection. */
    protected double sum;
    
    /** The sum of the squares of all values within the 2D selection. */
    protected double sumOfSquares;
    
    /** The mean value within the 2D-selection. */
    protected double mean;
    
    /** The standard deviation within the 2D-selection. */
    protected double standardDeviation;
    
    /** The number of points contained within the 2D-selection. */
    protected int pointsCount;

    public AbstractROIShapeStats() {
        super();
    }

    /**
     * Sets the minimum value.
     * 
     * @param min
     *            The value to set.
     */
    protected void setMin(double min) {
        this.min = min;
    }

    /**
     * Sets the maximum value.
     * 
     * @param max
     *            The value to set.
     */
    protected void setMax(double max) {
        this.max = max;
    }

    /**
     * Adds the passed value to the sum.
     * 
     * @param value
     *            The value to add.
     */
    protected void addToSum(double value) {
        this.sum += value;
    }

    /**
     * Adds the passed value to the sum of squares.
     * 
     * @param value
     *            The value to add.
     */
    protected void addToSumOfSquares(double value) {
        this.sumOfSquares += value * value;
    }

    /**
     * Sets the mean value.
     * 
     * @param mean
     *            The value to set.
     */
    protected void setMean(double mean) {
        this.mean = mean;
    }

    /**
     * Sets the number of points counted.
     * 
     * @param pointsCount
     *            The value to set.
     */
    protected void setPointsCount(int pointsCount) {
        this.pointsCount = pointsCount;
    }

    /**
     * Returns the sum of squares.
     * 
     * @return See above.
     */
    protected double getSumOfSquares() {
        return sumOfSquares;
    }

    /**
     * Sets the standard deviation.
     * 
     * @param standardDeviation
     *            The value to set.
     */
    protected void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    /**
     * Returns the minimum value within the 2D-selection.
     * 
     * @return See above.
     */
    public double getMin() {
        return min;
    }

    /**
     * Returns the maximum value within the 2D-selection.
     * 
     * @return See above.
     */
    public double getMax() {
        return max;
    }

    /**
     * Returns the mean value within the 2D-selection.
     * 
     * @return See above.
     */
    public double getMean() {
        return mean;
    }

    /**
     * Returns the standard deviation within the 2D-selection.
     * 
     * @return See above.
     */
    public double getStandardDeviation() {
        return standardDeviation;
    }

    /**
     * Returns the sum of all values within the 2D-selection.
     * 
     * @return See above.
     */
    public double getSum() {
        return sum;
    }

    /**
     * Returns the number of points contained within the 2D-selection.
     * 
     * @return See above.
     */
    public int getPointsCount() {
        return pointsCount;
    }

}