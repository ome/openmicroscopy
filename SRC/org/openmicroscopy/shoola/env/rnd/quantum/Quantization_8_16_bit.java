/*
 * org.openmicroscopy.shoola.env.rnd.quantum.Quantization_8_16_bit
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

package org.openmicroscopy.shoola.env.rnd.quantum;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.defs.QuantumDef;
import org.openmicroscopy.shoola.util.math.Approximation;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class Quantization_8_16_bit
	extends QuantumStrategy
{
    private static final int        EXTRA = 10;
    
    private static final double     SUB_INT = 10;
    
	//	current lookup table
	private byte[]      LUT;
	private int         min;
	private int         max;
	
	/**
	 * @param qd 	Quantum definition object, contained mapping data.
	 */
	public Quantization_8_16_bit(QuantumDef qd) { super(qd); }
	
	/**
	 * Initializes the LUT. 
	 * Comparable getGlobalMin and getGlobalMax
	 * assumed to be Integer, QuantumStrategy enforces min &lt; max.
	 * QuantumFactory makes sure 0 &lt; max-min &lt; 2^N where N = 8 or N = 16.
	 * LUT size is at most 256 bytes if N = 8 or 
	 * 2^16 bytes = 2^6Kb = 64Kb if N = 16.
	 */
	private void init()
	{	
		min = (int) getGlobalMin();  
		max = (int) getGlobalMax();
		LUT = new byte[max-min+1];  
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
		if (LUT == null) init();
		// Comparable assumed to be Integer
		//domain
		double dStart = getWindowStart(), dEnd = getWindowEnd(); 
		double k = getCurveCoefficient();
		double a1 = (qDef.cdEnd-qDef.cdStart)/((double) qDef.bitResolution); 
		int x = min;
        
        QuantumMap normalize = new PolynomialMap();
        double ysNorm = valueMapper.transform(0, k);
        double yeNorm = valueMapper.transform(255, k);
        double aNorm = qDef.bitResolution/(yeNorm-ysNorm);
        
        double v = 0, extra = 0;
        double c0, denum = (dEnd-dStart), num = 255;
        double S1 = dStart;
        double decile = (max-min)/SUB_INT;
        double Q1 = min, Q9 = max;
        int cdStart = qDef.cdStart, cdEnd = qDef.cdEnd;
        if (qDef.noiseReduction) {
            v = EXTRA;
            extra = EXTRA;
            Q1 = min+decile;
            Q9 = max-decile;
            S1 = Q1;
            num = 255-2*EXTRA;
            denum = Q9-Q1;
            if (dStart >= Q1 && dEnd > Q9) {
                denum = (Q9-dStart);
                S1 = dStart;
            } else if (dStart >= Q1 && dEnd <= Q9) {
                denum = dEnd-dStart;
                S1 = dStart;
            } else if (dStart < Q1 && dEnd <= Q9)
                denum = dEnd-Q1;
            
            if (qDef.cdStart < EXTRA) cdStart = EXTRA;
            if (qDef.cdEnd > 255-EXTRA) cdEnd = 255-EXTRA;
        }
        
        c0 = num/denum;
		
		for(; x < dStart; ++x)   LUT[x-min] = (byte) cdStart;
		
		for(; x < dEnd; ++x) { 
            if (x > Q1) {
                if (x <= Q9)
                    v = c0*(normalize.transform(x, 1)-S1)+extra;
                else v = cdEnd;
            } else v = cdStart;
            v = Approximation.nearestInteger(
                                aNorm*(valueMapper.transform(v, k)-ysNorm));
            
            v = Approximation.nearestInteger(a1*v+qDef.cdStart);
            LUT[x-min] = (byte) v;
		}
		
		for(; x <= max; ++x)   LUT[x-min] = (byte) cdEnd; 
	}

	/** The input window size changed, rebuild the LUT. */
	protected void onWindowChange() { buildLUT(); }
	
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

