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

import tmp.PixelTypeHelper;


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
     * Maximum value (<code>255</code>) allowed for the upper bound of the 
     * codomain interval.
     */
    public static final int     MAX = 255;
    
    /** 
     * Minimum value (<code>0</code>) allowed for the lower bound of the 
     * codomain interval.
     */
    public static final int     MIN = 0;
    
    /** 
     * Determines the number of sub-intervals 
     * of the [globalMin, globalMax] interval.
     */
    public static final int     DECILE = 10;
    
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
    
    /**
     * Identifies the noise reduction algorithm, value set to <code>true</code>
     * if the noise reduction algorithm is applied, <code>false</code>
     * otherwise.
     */
    private boolean                 noiseReduction;
    
	/** Reference to a quantumDef object. */
	protected final QuantumDef      qDef;   
	
    /** The type of pixels this strategy is for. */
    protected final PixelsType      type;
    
    /** Reference to the value mapper. */
	protected QuantumMap			valueMapper;               

    
    /** 
     * Defines the value mapper corresponding to the specified
     * family.
     * 
     * @param family The family identifying the value mapper.
     */
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
     * Controls if the specified family is supported.
     * The family must be one of the constant defined in
     * {@link QuantumFactory}.
     * 
     * @param family The family to control
     */
    private static void verifyFamily(int family)
    {
        switch (family) {
            case QuantumFactory.LINEAR:
            case QuantumFactory.LOGARITHMIC:
            case QuantumFactory.EXPONENTIAL:
            case QuantumFactory.POLYNOMIAL:
                return;
            default:
                throw new IllegalArgumentException("Unsupported family type");
        }
    }

    /**
     * Controls if the specified interval is valid depending on the pixel type.
     * 
     * The min value and max value could be out of pixel type range 
     * b/c of an error occured in stats calculations.
     * 
     * @param min   The lower bound of the interval.
     * @param max   The upper bound of the interval.
     */
    private void verifyInterval(double min, double max)
    {
        boolean b = false;
        if (min <= max) {
            double range = max-min;
            if (PixelTypeHelper.in(type, new String[] { "int8", "uint8" })) {
                if (range < 0x100) b = true;
            } else if (PixelTypeHelper.in(type, 
                    new String[] { "int16", "uint16" })) {
                if (range < 0x10000) b = true;
            } else if (PixelTypeHelper.in(type,
                    new String[] { "int32", "uint32" })) {
                if (range < 0x100000000L) b = true;
            } else if (PixelTypeHelper.in(type,
                    new String[] { "float", "double" }))
                b = true;
        }
        if (!b)
            throw new IllegalArgumentException("Pixel interval not supported");
    }
    
    /**
     * Creates a new instance.
     * 
     * @param qd    The {@link QuantumDef} this strategy is for.
     * @param pt
     */
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

  
	/** 
	 * Sets the maximum range of the input window. 
	 * 
	 * @param globalMin    The minimum of all minima for a specified stack.
	 * @param globalMax    The maximum of all maxima for a specified stack.
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
	 * Sets the input window interval.
	 * 
	 * @param start		The lower bound of the interval.
	 * @param end		The upper bound of the interval.
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
     * Sets the selected family, the curve coefficient and the noise reduction
     * flag.
     * 
     * @param family            The mapping family. One of the constants defined
     *                          by {@link QuantumFactory}. 
     * @param k                 The curve coefficient.
     * @param noiseReduction    The noise reduction flag.
     */
	public void setMapping(int family, double k, boolean noiseReduction)
    {
        defineMapper(family);
        this.family = family;
        curveCoefficient = k;
        this.noiseReduction = noiseReduction;
    }
    
    /** 
     * Sets the selected family, the curve coefficient and the noise reduction
     * flag and rebuilds the look-up table.
     * 
     * @param family            The mapping family. One of the constants defined
     *                          by {@link QuantumFactory}. 
     * @param k                 The curve coefficient.
     * @param noiseReduction    The noise reduction flag.
     */
    public void setQuantizationMap(int family, double k, boolean noiseReduction)
    {
        setMapping(family, k, noiseReduction);
        onWindowChange();
    }
    
	void setMap(QuantumMap qMap) { valueMapper = qMap; }
	
    int getFamily() { return family; }
    
    double getCurveCoefficient() { return curveCoefficient; }
    
    boolean getNoiseReduction() { return noiseReduction; }
    
	/**
     * Returns the minimum of all minima.
     * 
     * @return See above.
     */
	double getGlobalMin()
	{ 
		//needed b/c of float value
		double d = globalMin-Math.floor(globalMin);
		if (d != 0) globalMin = Math.floor(globalMin);
		return globalMin;
	}
    
    /**
     * Returns the maximum of all maxima.
     * 
     * @return See above.
     */
	double getGlobalMax() { return globalMax; }
    
	/** 
     * Returns the lower bound of the input interval. 
     * 
     * @return See above.
     */
	public double getWindowStart() { return windowStart; }
	
    /** 
     * Returns the upper bound of the input interval. 
     * 
     * @return See above.
     */
	public double getWindowEnd() { return windowEnd; }
    
    /** 
     * Notifies when the input interval has changed or the mapping strategy
     * has changed.
     */
	protected abstract void onWindowChange();
	
	/** 
	 * Maps a value from [windowStart, windowEnd] to a value in the codomain
	 * interval.
	 * 
	 * @param value    The pixel intensity value.
	 * @return int     The value in the codomain interval i.e. sub-interval of 
     *                 [0, 255].
     * @throws QuantizationException If the specified value is not 
     * in the interval [globalMin, globalMax].
	 */
	public abstract int quantize(double value) throws QuantizationException;
	
}


