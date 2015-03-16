/*
 * omeis.providers.re.quantum.Quantization_8_16_bit
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.utests;

// Java imports

// Third-party libraries
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import ome.model.core.Pixels;
// Application-internal dependencies
import ome.model.display.QuantumDef;
import ome.model.enums.Family;
import omeis.providers.re.quantum.PolynomialMap;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumMap;
import omeis.providers.re.quantum.QuantumStrategy;

public class OnTheFlyStrategy extends QuantumStrategy {

    /** The logger for this particular class */
    private static Logger log = LoggerFactory.getLogger(OnTheFlyStrategy.class);

    /** The lower bound of the decile interval. */
    private double Q1;

    /** The upper bound of the decile interval. */
    private double Q9;
    
    /** The lowest pixel intensity value. */
    private int min;

    /** The uppest pixel intensity value. */
    private int max;
    
    /** The input start normalized value. */
    private double ysNormalized;

    /** The input end normalized value. */
    private double yeNormalized;

    /** The slope of the normalized map. */
    private double aNormalized;
    
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
    
    private double dStart;
    
    private double dEnd;
    
    private double k;
    
    private double v;
    
    private double a1;
    
    private QuantumMap normalize;
    
    @Override
    protected void onWindowChange()
    {
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
     * Creates a new strategy.
     * 
     * @param qd
     *            Quantum definition object, contained mapping data.
     * @param type
     *            The pixels
     */
    public OnTheFlyStrategy(QuantumDef qd, Pixels pixels) {
        super(qd, pixels);
        
        min = (int) getGlobalMin();
        max = (int) getGlobalMax();
        
    	dStart = getWindowStart();
    	dEnd = getWindowEnd();
    	k = getCurveCoefficient();
        a1 = (qDef.getCdEnd().intValue() - qDef.getCdStart().intValue())
        	  / qDef.getBitResolution().doubleValue();
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
    
    @Override
    public void setMapping(Family family, double k, boolean noiseReduction)
    
    {
    	super.setMapping(family, k, noiseReduction);
    	
        // Initializes the normalized map.
        initNormalizedMap(k);
    	
        // Initializes the decile map.
        v = initDecileMap(dStart, dEnd);
        normalize = new PolynomialMap();
    }

    /**
     * Implemented as specified in {@link QuantumStrategy}.
     * 
     * @see QuantumStrategy#quantize(double)
     */
    @Override
    public int quantize(double value) throws QuantizationException
    {
    	if (value > Q1) {
            if (value <= Q9) {
                value = aDecile * normalize.transform(value, 1) - bDecile;
            } else {
                value = cdEnd;
            }
        } else {
            value = cdStart;
        }
    	
        v = aNormalized * (valueMapper.transform(v, k) - ysNormalized);
        v = Math.round(v);
        v = Math.round(a1 * v + cdStart);
        return (byte) v;
    }

}
