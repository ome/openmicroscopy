/*
 * omeis.providers.re.quantum.Quantization_8_16_bit
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package omeis.providers.re.quantum;


//Java imports

//Third-party libraries

//Application-internal dependencies
import ome.model.display.QuantumDef;
import ome.model.enums.PixelsType;
import ome.util.math.Approximation;


/** 
 * Quantization process. In charge of buildng a look-up table for each active
 * wavelength.
 * The mapping process is done in three mapping steps, 
 * for some computer reasons, we cannot 
 * compose (in the mathematical sense) the three maps directly.
 * Each wavelength initializes a strategy, in order to preserve the 5D-notion
 * of OME image, we first compute the normalized parameters.
 * We determine a pseudo-decile (not decile in maths terms) interval and 
 * compute the associated parameters to reduce the irrelevant 
 * values (noiseReduction). 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision: 1.2 $ $Date: 2005/06/20 14:12:20 $)
 * </small>
 * @since OME2.2
 */
public class Quantization_8_16_bit
	extends QuantumStrategy
{
    
    /** 
     * Maximum value (<code>255</code>) allowed for the upper bound of the 
     * codomain interval.
     */
    private static final int    MAX = 255;
    
    /** 
     * Minimum value (<code>0</code>) allowed for the lower bound of the 
     * codomain interval.
     */
    private static final int    MIN = 0;

    /** The look-up table. */
	private byte[]      LUT;
    
    /** The lower bound of the table i.e. the lowest pixel intensity value. */
	private int         min;
    
     /** The upper bound of the table i.e. the uppest pixel intensity value. */
	private int         max;
	
    /** The input start normalized value. */
    private double      ysNormalized;
    
    /** The input end normalized value. */
    private double      yeNormalized;
    
    /** The slope of the normalized map. */
    private double      aNormalized;
    
    /** The decile interval. */
    private double      Q1, Q9;
    
    /**
     * The mapping parameters from the sub-interval of [Q1, Q9] to the 
     * device space.
     */ 
    private double      aDecile, bDecile;

	/**
	 * Initializes the LUT. 
	 * Comparable getGlobalMin and getGlobalMax
	 * assumed to be Integer, QuantumStrategy enforces min &lt; max.
	 * QuantumFactory makes sure 0 &lt; max-min &lt; 2^N where N = 8 or N = 16.
	 * LUT size is at most 256 bytes if N = 8 or 
	 * 2^16 bytes = 2^6Kb = 64Kb if N = 16.
	 */
	private void initLUT()
	{	
		min = (int) getGlobalMin();  
		max = (int) getGlobalMax();
		LUT = new byte[max-min+1];  
	}

    /**
     * Initializes the coefficient of the normalize mapping operation.
     *
     * @param k The coefficient of the selected curve.
     */
    private void initNormalizedMap(double k)
    {
        ysNormalized = valueMapper.transform(MIN, k);
        yeNormalized = valueMapper.transform(MAX, k);
        aNormalized = qDef.getBitResolution().floatValue()/(yeNormalized-ysNormalized);
    }
    
    /**
     * Initializes the parameter to map the pixels intensities to the device 
     * space.
     * 
     * @param dStart The input window start.
     * @param dEnd The input window end.
     */
    private void initDecileMap(double dStart, double dEnd)
    {
        double denum = (dEnd-dStart), num = MAX;
        double decile = ((double) (max-min))/DECILE;
        Q1 = min;
        Q9 = max;
        bDecile = dStart;
        if (getNoiseReduction()) {
            Q1 = min+decile;
            Q9 = max-decile;
            denum = Q9-Q1;
            if (dStart >= Q1 && dEnd > Q9) {
                denum = (Q9-dStart);
                bDecile = dStart;
            } else if (dStart >= Q1 && dEnd <= Q9) {
                denum = dEnd-dStart;
                bDecile = dStart;
            } else if (dStart < Q1 && dEnd <= Q9) {
                denum = dEnd-Q1;
                bDecile = Q1;
            }   
        }
        aDecile = num/denum;
    }
    
	/** 
	 * Maps the input interval onto the codomain [cdStart, cdEnd] sub-interval
	 * of [0, 255].
	 * Since the user can select the bitResolution 2^n-1 where n = 1..8, 
	 * 2 steps.
	 * The mapping is the composition of 2 transformations:
	 * The first one <code>f</code> is one of the map selected by the user
	 * f:[inputStart, inputEnd]-&lt;[0, 2^n-1].
	 * The second one <code>g</code> is a linear map: y = a1*x+b1 
	 * where b1 = codomainStart and 
	 * a1 = (qDef.cdEnd-qDef.cdStart)/((double) qDef.bitResolution); 
	 * g: [0, 2^n-1]-&lt;[cdStart, cdEnd].
	 * For some reasons, we cannot compute directly gof.
	 */
	private void buildLUT()
	{
		if (LUT == null) initLUT();
		// Comparable assumed to be Integer
		//domain
		double dStart = getWindowStart(), dEnd = getWindowEnd(); 
		double k = getCurveCoefficient();
		double a1 = (qDef.getCdEnd().intValue()-qDef.getCdStart().intValue())/qDef.getBitResolution().doubleValue(); 

        //Initializes the normalized map.
        initNormalizedMap(k);
        //Initializes the decile map.
        initDecileMap(dStart, dEnd);
        QuantumMap normalize = new PolynomialMap();
        int x = min;
        double v;
        
        //Build the LUT
		for(; x < dStart; ++x)   LUT[x-min] = (byte) qDef.getCdStart().intValue();
		
		for(; x < dEnd; ++x) { 
            if (x > Q1) {
                if (x <= Q9) {
                    v = aDecile*(normalize.transform(x, 1)-bDecile);
                    v = aNormalized*(valueMapper.transform(v, k)-ysNormalized);
                    v = Approximation.nearestInteger(v);
                    v = Approximation.nearestInteger(a1*v+qDef.getCdStart().intValue());
                }    
                else v = qDef.getCdEnd().intValue();
            } else v = qDef.getCdStart().intValue();
            
            LUT[x-min] = (byte) v;
		}
		
		for(; x <= max; ++x)   LUT[x-min] = (byte) qDef.getCdEnd().intValue(); 
	}

    
	/** The input window size changed, rebuild the LUT. */
	protected void onWindowChange() { buildLUT(); }
	
    /**
     * Creates a new strategy.
     * 
     * @param qd    Quantum definition object, contained mapping data.
     */
    public Quantization_8_16_bit(QuantumDef qd, PixelsType type) { super(qd,type); }
    
    
	/** Implemented as specified in {@link QuantumStrategy}. */
	public int quantize(double value)
		throws QuantizationException
	{
		int x = (int) value;
		if (x < min || x > max)
			throw new QuantizationException(
					"The value "+x+" is not in the interval ["+min+","+max+"]");
		int i = LUT[x-min];
		return (i & 0xFF);  //assumed x in [min, max]
	}

}

