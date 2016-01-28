/*
 * omeis.providers.re.codomain.ContrastStretchingContext
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.codomain;

/**
 * Two points <code>pStart</code> and <code>pEnd</code> define the context
 * of this transformation. We determine the equations of 3 lines (segments to be
 * correct). The first one is a line between the point with coordinates
 * (intervalStart, intervalStart) and (xStart, yStart). The second one between
 * (xStart, yStart) and (xEnd, yEnd). The third one between (xEnd, yEnd) and
 * (intervalEnd, intervalEnd).
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class ContrastStretchingContext extends CodomainMapContext {

    /** The x-coordinate of pStart. */
    private int xStart;

    /** The y-coordinate of pStart. */
    private int yStart;

    /** The x-coordinate of pEnd. */
    private int xEnd;

    /** The y-coordinate of pEnd. */
    private int yEnd;

    /** The slope and the y-intercept of the first line i.e. y = a0*x+b0. */
    private double a0, b0;

    /** The slope and the y-intercept of the second line i.e. y = a1*x+b1. */
    private double a1, b1;

    /** The slope and the y-intercept of the third line i.e. y = a2*x+b2. */
    private double a2, b2;

    /**
     * Verifies the bounds of the input interval [s, e]. This interval must be a
     * sub-interval of [intervalStart, intervalEnd].
     * 
     * @param start
     *            The lower bound of the interval.
     * @param end
     *            The upper bound of the interval.
     * @throws IllegalArgumentException
     *             If the value is not in the interval.
     */
    private void verifyInputInterval(int start, int end) {
        if (start >= end) {
            throw new IllegalArgumentException(start + " cannot greater than "
                    + end + " in contrast stretching context.");
        }
        if (start < intervalStart) {
            throw new IllegalArgumentException(start + " cannot lower than "
                    + intervalStart + " in contrast stretching context.");
        }
        if (end > intervalEnd) {
            throw new IllegalArgumentException(end + " cannot be greater than "
                    + intervalStart + " in contrast stretching context.");
        }
    }

    /**
     * Computes the slope and the y-intercept of the first line i.e. y =
     * a0*x+b0.
     * 
     * @param intervalStart
     *            The starting value of the segment.
     */
    private void setFirstLineCoefficient(int intervalStart) {
        double r = xStart - intervalStart;
        if (r == 0) {
            a0 = 0;
        } else {
            a0 = (yStart - intervalStart) / r;
        }
        b0 = intervalStart * (1 - a0);
    }

    /**
     * Computes the slope and the y-intercept of the second line i.e. y =
     * a1*x+b1.
     */
    private void setSecondLineCoefficient() {
        double r = xEnd - xStart;
        // To be on the save side, shouldn't happen.
        if (r == 0) {
            a1 = 0;
        } else {
            a1 = (yEnd - yStart) / r;
        }
        b1 = yStart - a1 * xStart;
    }

    /**
     * Computes the slope and the y-intercept of the third straight i.e. y =
     * a2*x+b2.
     * 
     * @param intervalEnd
     *            The starting value of the segment.
     */
    private void setThirdLineCoefficient(int intervalEnd) {
        double r = intervalEnd - xEnd;
        if (r == 0) {
            a2 = 0;
        } else {
            a2 = (intervalEnd - yEnd) / r;
        }
        b2 = intervalEnd * (1 - a2);
    }

    /**
     * Calculates the equations of the lines.
     * 
     * @see CodomainMapContext#buildContext()
     */
    @Override
    void buildContext() {
        if (xStart < intervalStart) {
            xStart = intervalStart;
        }
        if (yStart < intervalStart) {
            yStart = intervalStart;
        }
        if (xEnd > intervalEnd) {
            xEnd = intervalEnd;
        }
        if (yEnd > intervalEnd) {
            yEnd = intervalEnd;
        }

        setFirstLineCoefficient(intervalStart);
        setSecondLineCoefficient();
        setThirdLineCoefficient(intervalEnd);
    }

    /**
     * Implemented as specified by superclass.
     * 
     * @see CodomainMapContext#getCodomainMap()
     */
    @Override
    CodomainMap getCodomainMap() {
        return new ContrastStretchingMap();
    }

    /**
     * Implemented as specified by superclass.
     * 
     * @see CodomainMapContext#copy()
     */
    @Override
    public CodomainMapContext copy() {
        ContrastStretchingContext copy = new ContrastStretchingContext();
        copy.intervalEnd = intervalEnd;
        copy.intervalStart = intervalStart;
        copy.xStart = xStart;
        copy.yStart = yStart;
        copy.xEnd = xEnd;
        copy.yEnd = yEnd;
        copy.a0 = a0;
        copy.a1 = a1;
        copy.a2 = a2;
        copy.b0 = b0;
        copy.b1 = b1;
        copy.b2 = b2;
        return copy;
    }

    /**
     * Sets the coordinates of the points used to determine the equations of the
     * lines.
     * 
     * @param xStart
     *            The x-coodinate of the <code>pStart</code> point.
     * @param yStart
     *            The y-coodinate of the <code>pStart</code> point.
     * @param xEnd
     *            The x-coodinate of the <code>pEnd</code> point.
     * @param yEnd
     *            The y-coodinate of the <code>pEnd</code> point.
     */
    public void setCoordinates(int xStart, int yStart, int xEnd, int yEnd) {
        verifyInputInterval(xStart, xEnd);
        verifyInputInterval(yStart, yEnd);
        this.xStart = xStart;
        this.xEnd = xEnd;
        this.yStart = yStart;
        this.yEnd = yEnd;
    }

    /**
     * Sets the x-coodinate of the <code>pStart</code> point.
     * 
     * @param v
     *            The value to set.
     */
    public void setXStart(int v) {
        verifyInputInterval(v, xEnd);
        xStart = v;
    }

    /**
     * Sets the x-coodinate of the <code>pEnd</code> point.
     * 
     * @param v
     *            The value to set.
     */
    public void setXEnd(int v) {
        verifyInputInterval(xStart, v);
        xEnd = v;
    }

    /**
     * Sets the y-coodinate of the <code>pStart</code> point.
     * 
     * @param v
     *            The value to set.
     */
    public void setYStart(int v) {
        verifyInputInterval(v, yEnd);
        yStart = v;
    }

    /**
     * Sets the y-coodinate of the <code>pEnd</code> point.
     * 
     * @param v
     *            The value to set.
     */
    public void setYEnd(int v) {
        verifyInputInterval(yStart, v);
        yEnd = v;
    }

    /**
     * Returns the x-coordinate of the <code>pEnd</code> point.
     * 
     * @return See above.
     */
    public int getXEnd() {
        return xEnd;
    }

    /**
     * Returns the x-coordinate of the <code>pStart</code> point.
     * 
     * @return See above.
     */
    public int getXStart() {
        return xStart;
    }

    /**
     * Returns the y-coordinate of the <code>pEnd</code> point.
     * 
     * @return See above.
     */
    public int getYEnd() {
        return yEnd;
    }

    /**
     * Returns the y-coordinate of the <code>pStart</code> point.
     * 
     * @return See above.
     */
    public int getYStart() {
        return yStart;
    }

    /**
     * Returns the slope of the first line.
     * 
     * @return See above.
     */
    public double getA0() {
        return a0;
    }

    /**
     * Returns the slope of the second line.
     * 
     * @return See above.
     */
    public double getA1() {
        return a1;
    }

    /**
     * Returns the slope of the third line.
     * 
     * @return See above.
     */
    public double getA2() {
        return a2;
    }

    /**
     * Returns the y-intercept of the first line.
     * 
     * @return See above.
     */
    public double getB0() {
        return b0;
    }

    /**
     * Returns the y-intercept of the second line.
     * 
     * @return See above.
     */
    public double getB1() {
        return b1;
    }

    /**
     * Returns the y-intercept of the third line.
     * 
     * @return See above.
     */
    public double getB2() {
        return b2;
    }

}
