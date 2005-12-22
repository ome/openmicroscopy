/*
 * omeis.providers.re.quantum.QuantumStrategy
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

import tmp.PixelsConstants;


/** 
 * TODO: review javadoc.
 * Subclasses 
 * Work on explicit pixel types.
 * Taking into account the pixel types, transform the pixel intensity value
 * passed to {@link #quantize} by delegating to the configured quantum map.
 * Encapsulate a computation strategy for the quantization process i.e. LUT and 
 * Approximation.
 * Implement {@link #onWindowChange} to get notified when the input interval 
 * changes.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision: 1.2 $ $Date: 2005/06/20 14:12:05 $)
 * </small>
 * @since OME2.2
 */
public abstract class QuantumStrategy
{

    /** 
     * Determines the number of sub-intervals 
     * of the [globalMin, globalMax] interval.
     */
    public static final int         DECILE = 10;
    
    /** Minimum of all minima. */
	private double    				globalMin;
	
	/** Maximum of all maxima. */
	private double      			globalMax;
	
	/** The lower limit of the input Interval i.e. pixel intensity interval. */
	private double      			windowStart;
	
	/** The upper limit of the input Interval i.e. pixel intensity interval. */
	private double      			windowEnd;
	
    /** 
     * Identifies a family of maps. 
     * One of the constants defined by {@link QuantumFactory}.
     */
    private int                     family;
    
    /** Selects a curve in the family. */
    private double                  curveCoefficient;
    
    private boolean                 noiseReduction;
    
	/** Reference to a quantumDef object. */
	protected final QuantumDef      qDef;   
	
    protected final PixelsType      type;
    
	protected QuantumMap			valueMapper;               

	protected QuantumStrategy(QuantumDef qd, PixelsType pt)
	{
		windowStart = globalMin = 0.0;
		windowEnd = globalMax = 1.0;
        family = QuantumFactory.LINEAR;
        curveCoefficient = 1.0;
		if (qd == null)    
			throw new NullPointerException("No quantum definition");
		this.qDef = qd;
        if (pt == null)
            throw new NullPointerException("No pixel type");
        this.type = pt;
	}
	
    /** Define the value mapper. */
    private void defineMapper(int family)
    {
        verifyFamily(family);
        switch (family) {
            case QuantumFactory.LINEAR:
            case QuantumFactory.POLYNOMIAL:
                valueMapper = new PolynomialMap();
                break;
            case QuantumFactory.LOGARITHMIC:
                valueMapper = new LogarithmicMap();
                break;
            case QuantumFactory.EXPONENTIAL:
                valueMapper = new ExponentialMap(); 
                break;
            default: 
                // never reached: verify throws exception.
        } 
    }
    
    /** 
     * The family must be one of the constant defined in
     * {@link QuantumFactory}.
     *
     */
    private static void verifyFamily(int family)
    {
        if (family != QuantumFactory.LINEAR && 
            family != QuantumFactory.LOGARITHMIC && 
            family != QuantumFactory.EXPONENTIAL
            && family != QuantumFactory.POLYNOMIAL)  
            throw new IllegalArgumentException("Unsupported family type");
    }

	/**
	 * min and max could be out of pixel type range 
	 * b/c of an error occured in stats calculations.
	 * 
	 * @param min	lower bound.
	 * @param max	upper bound.
	 */
	private void verifyInterval(double min, double max)
	{
		boolean b = false;
		if (min <= max) {
            double range = max-min;
			switch (PixelsConstants.convertPixelType(type)) { 
				case PixelsConstants.INT8:
				case PixelsConstants.UINT8:
					if (range < 0x100) b = true; 
					break;
				case PixelsConstants.INT16:
				case PixelsConstants.UINT16:
                    if (range < 0x10000) b = true; 
                    break;
				case PixelsConstants.INT32:
				case PixelsConstants.UINT32:
                    if (range < 0x100000000L) b = true; 
                    break;
				case PixelsConstants.FLOAT:   //range doesn't matter here.
				case PixelsConstants.DOUBLE:  //range doesn't matter here. 
				case PixelsConstants.BIT:  //This has never been impl server-side.
			}
		}
		if (!b)
			throw new IllegalArgumentException("Pixel interval not supported");
	}
  
	/** 
	 * Sets the maximum range of the input window. 
	 * 
	 * @param globalMin		minimum of all minima for a specified stack.
	 * @param globalMax		maximum of all maxima for a specified stack.
	 */
	public void setExtent(double globalMin, double globalMax)
	{
		 verifyInterval(globalMin, globalMax); 
		 this.globalMin = globalMin;
		 this.globalMax = globalMax;
		 this.windowStart = globalMin;
		 this.windowEnd = globalMax;
	}
	
	/**
	 * Sets the inputWindow interval.
	 * 
	 * @param start		the lower bound of the interval.
	 * @param end		the upper bound of the interval.
	 */
	public void setWindow(double start, double end)
	{
		if (start < globalMin || globalMax < end)
			throw new IllegalArgumentException("Wrong interval definition");
        verifyInterval(start, end);
		windowStart = start;
		windowEnd = end;
		onWindowChange();
	}
    
    /** 
     * Set the selected family, one of the constants defined by 
     * {@link QuantumFactory}, and the curve coefficient.
     */
	public void setMapping(int family, double k, boolean noiseReduction)
    {
        defineMapper(family);
        this.family = family;
        curveCoefficient = k;
        this.noiseReduction = noiseReduction;
    }
    
    /** 
     * Set the selected family, one of the constants defined by 
     * {@link QuantumFactory}, and the curve coefficient.
     */
    public void setQuantizationMap(int family, double k, boolean noiseReduction)
    {
        defineMapper(family);
        this.family = family;
        curveCoefficient = k;
        this.noiseReduction = noiseReduction;
        onWindowChange();
    }
    
	void setMap(QuantumMap qMap) { valueMapper = qMap; }
	
    int getFamily() { return family; }
    
    double getCurveCoefficient() { return curveCoefficient; }
    
    boolean getNoiseReduction() { return noiseReduction; }
    
	/** Returns the globalMin. */
	double getGlobalMin()
	{ 
		//needed b/c of float value
		double d = globalMin-Math.floor(globalMin);
		if (d != 0) globalMin = Math.floor(globalMin);
		return globalMin;
	}
    
	/** Returns the globalMax. */
	double getGlobalMax() { return globalMax; }
    
	/** Returns the input start value. */
	public double getWindowStart() { return windowStart; }
	
	/** Returns the input end value. */
	public double getWindowEnd() { return windowEnd; }
    
    /** Notify when the input interval has changed. */
	protected abstract void onWindowChange();
	
	/** 
	 * Map a value (in [windowStart, windowEnd]) to a value in the codomain
	 * interval.
	 * 
	 * @param value	pixel intensity value.
	 * @return int value in the codomain interval i.e. sub-interval of [0, 255]
	 */
	public abstract int quantize(double value) throws QuantizationException;
	
}


