/*
 *   Copyright 2006-2017 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.quantum;

import java.util.List;

import ome.model.core.Pixels;
import ome.model.display.QuantumDef;
import ome.model.enums.Family;
import ome.model.enums.PixelsType;

/**
 * Factory to create objects to carry out quantization for a given context. This
 * class defines the constants to be used to identify a {@link QuantumMap}
 * within a quantization context. It also defines the constants to be used to
 * define the bit depth of the quantized output interval.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
public class QuantumFactory {

    // NOTE: The bit-depth constants cannot be modified b/c they have a meaning.

    /** Flag to select a 1-bit depth (<i>=2^1-1</i>) output interval. */
    public static final int DEPTH_1BIT = 1;

    /** Flag to select a 2-bit depth (<i>=2^2-1</i>) output interval. */
    public static final int DEPTH_2BIT = 3;

    /** Flag to select a 3-bit depth (<i>=2^3-1</i>) output interval. */
    public static final int DEPTH_3BIT = 7;

    /** Flag to select a 4-bit depth (<i>=2^4-1</i>) output interval. */
    public static final int DEPTH_4BIT = 15;

    /** Flag to select a 5-bit depth (<i>=2^5-1</i>) output interval. */
    public static final int DEPTH_5BIT = 31;

    /** Flag to select a 6-bit depth (<i>=2^6-1</i>) output interval. */
    public static final int DEPTH_6BIT = 63;

    /** Flag to select a 7-bit depth (<i>=2^7-1</i>) output interval. */
    public static final int DEPTH_7BIT = 127;

    /** Flag to select a 8-bit depth (<i>=2^8-1</i>) output interval. */
    public static final int DEPTH_8BIT = 255;

    /**
     * Flag to select a linear map for the quantization process. The equation of
     * the map is of the form <i>y = a*x + b</i>. The <i>a</i> and <i>b</i>
     * coefficients depend on the input and output (codomain) interval of the
     * map.
     */
    @Deprecated
    public static final String LINEAR = Family.VALUE_LINEAR;

    /**
     * Flag to select a exponential map for the quantization process. The
     * equation of the map is of the form <i>y = a*(exp(x^k)) + b</i>. The <i>a</i>
     * and <i>b</i> coefficients depend on the input and output (codomain)
     * interval of the map. The <i>k</i> coefficient is the one specified by
     * the {@link QuantumDef context}.
     */
    @Deprecated
    public static final String EXPONENTIAL = Family.VALUE_EXPONENTIAL;

    /**
     * Flag to select a logarithmic map for the quantization process. The
     * equation of the map is of the form <i>y = a*log(x) + b</i>. The <i>a</i>
     * and <i>b</i> coefficients depend on the input and output (codomain)
     * interval of the map.
     */
    @Deprecated
    public static final String LOGARITHMIC = Family.VALUE_LOGARITHMIC;

    /**
     * Flag to select a polynomial map for the quantization process. The
     * equation of the map is of the form <i>y = a*x^k + b</i>. The <i>a</i>
     * and <i>b</i> coefficients depend on the input and output (codomain)
     * interval of the map. The <i>k</i> coefficient is the one specified by
     * the {@link QuantumDef context}. Note that {@link #LINEAR} is a special
     * case of polynomial (<i>k = 1</i>). We keep the {@link #LINEAR} constant
     * for some UI reason but we apply the same algorithm.
     */
    @Deprecated
    public static final String POLYNOMIAL = Family.VALUE_POLYNOMIAL;

    /** Default value. */
    public static final boolean NOISE_REDUCTION = true;
    
    /** Enumerated list of all families. */
    private List<Family> families;
    
    /**
     * Default constructor.
     * 
     * @param families the enumerated list of all families.
     */
    public QuantumFactory(List<Family> families)
    {
    	this.families = families;
    }
    
    /**
     * Helper method to retrieve a Family enumeration from the database.
     * 
     * @param value
     *            The enumeration value.
     * @return A family enumeration object.
     */
    public Family getFamily(String value) {
    	for (Family family : families)
    	{
    		if (family.getValue().equals(value))
    			return family;
    	}
    	throw new IllegalArgumentException("Unknown family: " + value);
    }

    /**
     * Verifies that <code>qd</code> is not <code>null</code> and has been
     * properly defined.
     * 
     * @param qd
     *            The definition to verify.
     * @param type The pixels to handle.
     * @throws IllegalArgumentException
     *             If the check fails.
     */
    private void verifyDef(QuantumDef qd, Pixels pixels) {
        if (qd == null) {
            throw new NullPointerException("No quantum definition.");
        }
        verifyBitResolution(qd.getBitResolution().intValue());
    }

    /**
     * Verifies that <code>bitResolution</code> is one of the constants
     * defined by this class.
     * 
     * @param bitResolution
     *            The value to verify.
     * @throws IllegalArgumentException
     *             If the check fails.
     */
    private void verifyBitResolution(int bitResolution) {
        boolean b = false;
        switch (bitResolution) {
            case DEPTH_1BIT:
            case DEPTH_2BIT:
            case DEPTH_3BIT:
            case DEPTH_4BIT:
            case DEPTH_5BIT:
            case DEPTH_6BIT:
            case DEPTH_7BIT:
            case DEPTH_8BIT:
                b = true;
        }
        if (!b) {
            throw new IllegalArgumentException("Unsupported bit resolution: "
                    + bitResolution + ".");
        }
    }

    /**
     * Creates a {@link QuantumStrategy} object suitable for the pixels type
     * specified by <code>pd</code>.
     * 
     * @param qd
     *            Defines the quantization context.
     * @param pixels The pixels to handle.
     * @return A {@link QuantumStrategy} object suitable for the given pixels
     *         type.
     */
    private QuantumStrategy getQuantization(QuantumDef qd, Pixels pixels) {
        String typeAsString = pixels.getPixelsType().getValue();
        if (PixelsType.VALUE_INT32.equals(typeAsString) ||
                PixelsType.VALUE_UINT32.equals(typeAsString))
            return new Quantization_32_bit(qd, pixels);
        else if (PixelsType.VALUE_FLOAT.equals(typeAsString) ||
                PixelsType.VALUE_DOUBLE.equals(typeAsString))
            return new Quantization_float(qd, pixels);
        return new Quantization_8_16_bit(qd, pixels);
    }

    /**
     * Returns a strategy to carry out the quantization process whose context is
     * defined by <code>pd</code>.
     * 
     * @param qd
     *            Defines the quantization context. Mustn't be <code>null</code>
     *            and its values must have been properly specified.
     * @param pixels The pixels to handle.
     * @return A {@link QuantumStrategy} suitable for the specified context.
     */
    public QuantumStrategy getStrategy(QuantumDef qd, Pixels pixels) {
        verifyDef(qd, pixels);
        QuantumStrategy strg = null;
        strg = getQuantization(qd, pixels);
        if (strg == null) {
            throw new IllegalArgumentException("Unsupported strategy");
        }
        return strg;
    }

}
