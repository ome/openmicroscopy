/*
 * omeis.providers.re.quantum.Quantization_8_16_bit
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.quantum;

import ome.model.core.Pixels;
import ome.model.display.QuantumDef;

/**
 * Quantization process. In charge of building a look-up table for each active
 * wavelength. The mapping process is done in three mapping steps, for some
 * computer reasons, we cannot compose (in the mathematical sense) the three
 * maps directly. Each wavelength initializes a strategy, in order to preserve
 * the 5D-notion of OME image, we first compute the normalized parameters. We
 * determine a pseudo-decile (not decile in maths terms) interval and compute
 * the associated parameters to reduce the irrelevant values (noiseReduction).
 * 
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/20 14:12:20 $) </small>
 * @since OME2.2
 */
public class Quantization_8_16_bit extends QuantumStrategy {

    /** The look-up table. */
    private byte[] LUT;

    /** The lowest pixel intensity value. */
    private int min;

    /** The uppest pixel intensity value. */
    private int max;

    /** The lower bound of the table. */
    private int lutMin;

    /** The upper bound of the table. */
    private int lutMax;

    /** The input start normalized value. */
    private double ysNormalized;

    /** The input end normalized value. */
    private double yeNormalized;

    /** The slope of the normalized map. */
    private double aNormalized;

    /** The lower bound of the decile interval. */
    private double Q1;

    /** The upper bound of the decile interval. */
    private double Q9;

    /**
     * The mapping parameters from the sub-interval of [Q1, Q9] to the device
     * space.
     */
    private double aDecile, bDecile;

    /**
     * The device space sub-interval. The values aren't the ones stored in
     * {@link QuantumDef} if the noise reduction flag is <code>true</code>.
     */
    private int cdStart, cdEnd;

    /**
     * Initializes the LUT. Comparable getGlobalMin and getGlobalMax assumed to
     * be Integer, QuantumStrategy enforces min &lt; max. QuantumFactory makes
     * sure 0 &lt; max-min &lt; 2^N where N = 8 or N = 16. LUT size is at most
     * 256 bytes if N = 8 or 2^16 bytes = 2^6Kb = 64Kb if N = 16.
     *
     * @param s The lower bound.
     * @param e The upper bound.
     */
    private void initLUT(int s, int e)
    {
        min = (int) getGlobalMin();
        max = (int) getGlobalMax();

        lutMin = (int) getPixelsTypeMin();
        lutMax = (int) getPixelsTypeMax();
        if (lutMax == 0) { //couldn't initialize the value
            if (s < min) lutMin = s;
            else lutMin = min;
            if (e > max) lutMax = e;
            else lutMax = max;
        }
        int range = lutMax - lutMin;
        if (range > MAX_SIZE_LUT) 
        {
            // We want to avoid *huge* memory allocations below so if we
            // couldn't initialize the value above and our lutMax and lutMin
            // have been assigned out of range values we want to choke, not
            // cause the server to throw a java.lang.OutOfMemory exception.
            // *** Ticket #1353 -- Chris Allan <callan@blackcat.ca> ***
            //This should not happen since we have now strategy for float
            //and 32bit.
            throw new IllegalArgumentException(String.format(
                    "Lookup table of size %d greater than supported size %f",
                    range, MAX_SIZE_LUT));
        }
        LUT = new byte[lutMax-lutMin+1];
    }

    /**
     * Resets the LUT. We rebuild the LUT if and only if the 
     * pixels type range was not determined at init time.
     *
     * @param s The lower bound.
     * @param e The upper bound.
     */
    private void resetLUT(int s, int e)
    {
        int pMax = (int) getPixelsTypeMax();
        if (pMax != 0) return;
        if (s < lutMin && e > lutMax) {
            lutMin = s;
            lutMax = e;
            LUT = new byte[lutMax-lutMin+1];
        } else if (s < lutMin && e <= lutMax) {
            lutMin = s;
            LUT = new byte[lutMax-lutMin+1];
        } else if (s >= lutMin && e > lutMax) {
            lutMax = e;
            LUT = new byte[lutMax-lutMin+1];
        }
    }

    /**
     * Initializes the coefficient of the normalize mapping operation.
     *
     * @param k
     *            The coefficient of the selected curve.
     */
    private void initNormalizedMap(double k) {
        ysNormalized = valueMapper.transform(MIN, k);
        yeNormalized = valueMapper.transform(MAX, k);
        aNormalized = qDef.getBitResolution().intValue()
                / (yeNormalized - ysNormalized);
    }

    /**
     * Initializes the parameter to map the pixels intensities to the device
     * space and returned the default initial depending on the value of the
     * noise reduction flag.
     *
     * @param dStart
     *            The input window start.
     * @param dEnd
     *            The input window end.
     * @return See above.
     */
    private double initDecileMap(double dStart, double dEnd) {
        cdStart = qDef.getCdStart().intValue();
        cdEnd = qDef.getCdEnd().intValue();
        double denum = dEnd - dStart, num = MAX;

        double v = 0, b = dStart;
        int e = 0;
        double startMin = min;
        double startMax = max;
        Q1 = min;
        Q9 = max;

        if (dStart <= startMin) {
            Q1 = dStart;
        }
        if (dEnd >= startMax) Q9 = dEnd;
        if (startMin == startMax) v = 1;
        double decile = (startMax - startMin) / DECILE;
        if (getNoiseReduction()) {
            Q1 += decile;
            Q9 -= decile;
            denum = Q9 - Q1;
            v = DECILE;
            e = DECILE;
            num = MAX - 2 * DECILE;
            b = Q1;
            if (dStart >= Q1 && dEnd > Q9) {
                denum = Q9 - dStart;
                b = dStart;
            } else if (dStart >= Q1 && dEnd <= Q9) {
                denum = dEnd - dStart;
                b = dStart;
            } else if (dStart < Q1 && dEnd <= Q9) {
                denum = dEnd - Q1;
            }
            if (cdStart < DECILE) {
                cdStart = DECILE;
            }
            if (cdEnd > MAX - DECILE) {
                cdEnd = MAX - DECILE;
            }
        }
        aDecile = num / denum;
        bDecile = aDecile * b - e;

        return v;
    }

    /**
     * Maps the input interval onto the codomain [cdStart, cdEnd] sub-interval
     * of [0, 255]. Since the user can select the bitResolution 2^n-1 where n =
     * 1..8, 2 steps. The mapping is the composition of 2 transformations: The
     * first one <code>f</code> is one of the map selected by the user
     * f:[inputStart, inputEnd]-&lt;[0, 2^n-1]. The second one <code>g</code>
     * is a linear map: y = a1*x+b1 where b1 = codomainStart and a1 =
     * (qDef.cdEnd-qDef.cdStart)/((double) qDef.bitResolution); g: [0,
     * 2^n-1]-&lt;[cdStart, cdEnd]. For some reasons, we cannot compute directly
     * gof.
     */
    private void buildLUT() {
    	double dStart = getWindowStart(), dEnd = getWindowEnd();
        if (LUT == null) {
            initLUT((int) dStart, (int) dEnd);
        } else {
            resetLUT((int) dStart, (int) dEnd);
        }
        // Comparable assumed to be Integer
        // domain

        double k = getCurveCoefficient();
        double a1 = (qDef.getCdEnd().intValue() - qDef.getCdStart().intValue())
                / qDef.getBitResolution().doubleValue();

        // Initializes the normalized map.
        initNormalizedMap(k);
        // Initializes the decile map.
        double v = initDecileMap(dStart, dEnd);

        // Build the LUT
        int x = lutMin;
        for (; x < dStart; ++x) {
            LUT[x - lutMin] = (byte) cdStart;
        }

        boolean doTransform = true;
        if (valueMapper instanceof PolynomialMap && k == 1.0) {
            doTransform = false;
        }
        for (; x < dEnd; ++x) {
        	if (x > Q1) {
                if (x <= Q9) {
                    v = aDecile * x - bDecile;
                } else {
                    v = cdEnd;
                }
            } else {
                v = cdStart;
            }
        	
            if (doTransform) {
                v = aNormalized * (valueMapper.transform(v, k) - ysNormalized);
            } else {
                v = aNormalized * (v - ysNormalized);
            }
            v = Math.round(v);
            v = Math.round(a1 * v + cdStart);
            LUT[x - lutMin] = (byte) v;
        }

        for (; x <= lutMax; ++x) {
            LUT[x - lutMin] = (byte) cdEnd;
        }
    }

    /** The input window size changed, rebuild the LUT. */
    @Override
    protected void onWindowChange() {
        buildLUT();
    }

    /**
     * Creates a new strategy.
     * 
     * @param qd
     *            Quantum definition object, contained mapping data.
     * @param pixels
     *            The pixels
     */
    public Quantization_8_16_bit(QuantumDef qd, Pixels pixels) {
        super(qd, pixels);
    }

    /**
     * Implemented as specified in {@link QuantumStrategy}.
     * 
     * @see QuantumStrategy#quantize(double)
     */
    @Override
    public int quantize(double value) throws QuantizationException {
        int x = (int) value;
        if (x < lutMin) {
            double r = getOriginalGlobalMax()-getOriginalGlobalMin();
            if (r != 0) {
                double f = (lutMax-lutMin)/r;
                x = (int) (f*(x-lutMin));
                if (x < lutMin) x = lutMin;
            } else x = lutMin;

        }
        if (x > lutMax) {
            double r = getOriginalGlobalMax()-getOriginalGlobalMin();
            if (r != 0) {
                double f = (lutMax-lutMin)/r;
                x = (int) (f*(x-lutMin));
                if (x > lutMax) x = lutMax;
            } else x = lutMax;
        }
        int i = LUT[x - lutMin];
        return i & 0xFF;
    }

}
