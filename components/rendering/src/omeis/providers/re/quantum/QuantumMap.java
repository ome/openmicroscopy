/*
 * omeis.providers.re.quantum.QuantumMap
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.quantum;

/**
 * Provides methods to map value. Each method is wrapper around a method exposed
 * by the {@link Math} class. Each value mapper should implements this I/F.
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
public interface QuantumMap {

    /**
     * Maps the specified value.
     * 
     * @param x
     *            The input value.
     * @param k
     *            The selected curve in the family.
     * @return The mapped value.
     */
    public double transform(int x, double k);

    /**
     * Maps the specified value.
     * 
     * @param x
     *            The input value.
     * @param k
     *            The selected curve in the family.
     * @return The mapped value.
     */
    public double transform(double x, double k);

    /**
     * Maps the specified value.
     * 
     * @param x
     *            The input value.
     * @param k
     *            The selected curve in the family.
     * @return The mapped value.
     */
    public double transform(float x, double k);

}
