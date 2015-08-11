/*
 * omeis.providers.re.quantum.PolynomialMap
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.quantum;

/**
 * This class implements the {@link QuantumMap} interface. Each method is a
 * wrapper around the {@link Math#pow(double, double)} method, which returns the
 * value of the first argument raised to the power of the second argument.
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
public class PolynomialMap implements QuantumMap {

    /**
     * Implemented as specified in {@link QuantumMap}.
     * 
     * @see QuantumMap#transform(int, double)
     */
    public double transform(int x, double k) {
        return Math.pow(x, k);
    }

    /**
     * Implemented as specified in {@link QuantumMap}.
     * 
     * @see QuantumMap#transform(double, double)
     */
    public double transform(double x, double k) {
        return Math.pow(x, k);
    }

    /**
     * Implemented as specified in {@link QuantumMap}.
     * 
     * @see QuantumMap#transform(float, double)
     */
    public double transform(float x, double k) {
        return Math.pow(x, k);
    }

}
