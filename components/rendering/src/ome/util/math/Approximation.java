/*
 * ome.util.math.Approximation
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.math;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * Utility class. Collection of static methods.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/09 15:01:17 $) </small>
 * @since OME2.2
 */
public class Approximation {

    /**
     * Returns the nearest integer to the specified value e.g. 1.2 returns 1,
     * 1.6 returns 2.
     * 
     * @param v
     *            The value to analyze.
     * @return The nearest integer.
     */
    public static double nearestInteger(double v) {
        double d = Math.floor(v);
        double diff = Math.abs(v - d);
        double value = d;
        if (diff > 0.5) {
            value++;
        }
        return value;
    }

    /**
     * Returns the smallest integer. This method calls
     * {@link Math#floor(double)}.
     * 
     * @param v
     *            The value to analyze.
     * @return The smallest integer.
     */
    public static double smallestInteger(double v) {
        return Math.floor(v);
    }

    /**
     * Returns the largest integer. This method calls {@link Math#ceil(double)}.
     * 
     * @param v
     *            The value to analyze.
     * @return The largest integer.
     */
    public static double largestInteger(double v) {
        return Math.ceil(v);
    }

}
