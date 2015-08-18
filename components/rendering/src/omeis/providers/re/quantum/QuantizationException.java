/*
 * omeis.providers.re.quantum.QuantizationException
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.quantum;

/**
 * This exception is thrown during the quantization process if something goes
 * wrong. For example, quantization strategies that depend on an interval
 * <code>[min,
 * max]</code> where <code>min</code> (<code>max</code>) is,
 * in general, the minimum (maximum) of all minima (maxima) calculated in a
 * given stack (for a given wavelength and timepoint).
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
public class QuantizationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1474572990450040235L;
    /** The OME index of the wavelength that coudln't be rendered. */
    private int wavelength;

    /** Creates a new exception. */
    public QuantizationException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param message
     *            Short explanation of the problem.
     */
    public QuantizationException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause.
     * 
     * @param cause
     *            The exception that caused this one to be risen.
     */
    public QuantizationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message
     *            Short explanation of the problem.
     * @param cause
     *            The exception that caused this one to be risen.
     */
    public QuantizationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Sets the index of the wavelength that couldn't be rendered.
     * 
     * @param index
     *            The index of the wavelength.
     */
    public void setWavelength(int index) {
        wavelength = index;
    }

    /**
     * Returns the index of the wavelength that couldn't be rendered.
     * 
     * @return See above.
     */
    public int getWavelength() {
        return wavelength;
    }

}
