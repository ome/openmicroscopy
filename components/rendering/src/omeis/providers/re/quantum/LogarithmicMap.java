/*
 * omeis.providers.re.quantum.LogarithmicMap
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.quantum;

/**
 * This class implements the {@link QuantumMap} interface. Each method is a
 * wrapper around the {@link Math#log(double)} method, which returns the natural
 * logarithm (base <i>e</i>) of a <code>double</code> value.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/10 17:37:26 $) </small>
 * @since OME2.2
 */
public class LogarithmicMap implements QuantumMap {

    /**
     * Controls if the specified is supported by the {@link Math#log(double)}
     * method. If the value is negative, it is then set to 1.
     * 
     * @param x
     *            The value to check.
     * @return See above.
     */
    private double verifyInput(double x) {
        if (x <= 0) {
            x = 1.0;
        }
        return x;
    }

    /**
     * Implemented as specified in {@link QuantumMap}. Note that in our case
     * the specified coefficient is not taken into account.
     * 
     * @see QuantumMap#transform(int, double)
     */
    public double transform(int x, double k) {
        return Math.log(verifyInput(x));
    }

    /**
     * Implemented as specified in {@link QuantumMap}. Note that in our case
     * the specified coefficient is not taken into account.
     * 
     * @see QuantumMap#transform(double, double)
     */
    public double transform(double x, double k) {
        return Math.log(verifyInput(x));
    }

    /**
     * Implemented as specified in {@link QuantumMap}. Note that in our case
     * the specified coefficient is not taken into account.
     * 
     * @see QuantumMap#transform(float, double)
     */
    public double transform(float x, double k) {
        return Math.log(verifyInput(x));
    }

}
