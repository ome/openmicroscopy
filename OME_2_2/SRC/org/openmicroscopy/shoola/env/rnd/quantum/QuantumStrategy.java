/*
 * org.openmicroscopy.shoola.env.rnd.quantum.QuantumStrategy
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
import org.openmicroscopy.shoola.env.rnd.data.DataSink;
import org.openmicroscopy.shoola.env.rnd.defs.QuantumDef;

/** 
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
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public abstract class QuantumStrategy
{
	
    /** Minimum of all minima. */
	private Comparable      		globalMin;
	
	/** Maximum of all maxima. */
	private Comparable      		globalMax;
	
	/** The lower limit of the input Interval i.e. pixel intensity interval. */
	private Comparable      		windowStart;
	
	/** The upper limit of the input Interval i.e. pixel intensity interval. */
	private Comparable      		windowEnd;
	
	/** Reference to a quantumDef object. */
	protected final QuantumDef      qDef;   
	
	protected QuantumMap			valueMapper;               

	protected QuantumStrategy(QuantumDef qd)
	{
		windowStart = globalMin = new Integer(0);
		windowEnd = globalMax = new Integer(1);
		if (qd == null)    
			throw new NullPointerException("No quantum definition");
		this.qDef = qd;
	}
	
	/**
	 * min and max could be out of pixel type range 
	 * b/c of an error occured in stats calculations.
	 * 
	 * @param min	lower bound.
	 * @param max	upper bound.
	 */
	private void verifyInterval(Comparable min, Comparable max)
	{
		boolean b = false;
		if (min != null && max != null && 0 < max.compareTo(min)) {
			switch (qDef.pixelType) { 
				case DataSink.INT8:
				case DataSink.UINT8:
					if (min instanceof Integer && max instanceof Integer) {
						int m = ((Integer) min).intValue(),
							M = ((Integer) max).intValue();
						if (m < M && M-m < 0x100)  b = true; 
					}
					break;
				case DataSink.INT16:
				case DataSink.UINT16:
					if (min instanceof Integer && max instanceof Integer) {
						int m = ((Integer) min).intValue(),
							M = ((Integer) max).intValue();
						if (m < M && M-m < 0x10000)  b = true; 
					}
					break;
				case DataSink.INT32:
					if (min instanceof Integer && max instanceof Integer) {
						int m = ((Integer) min).intValue(),
							M = ((Integer) max).intValue();
						if (m < M && M-m < 0x100000000L)  b = true; 
					}
					break;
				case DataSink.UINT32:
					if (min instanceof Long && max instanceof Long) {
						long m = ((Long) min).longValue(),
							 M = ((Long) max).longValue();
						if (m < M && M-m < 0x100000000L)  b = true; 
					}
					break;
					//TODO: checking all when we support these types
					/*
					case Pixels.BIT:
					case Pixels.FLOAT:  
					case Pixels.DOUBLE: break;
					*/
			}
		}
		if (!b)
			throw new IllegalArgumentException("Pixel interval not consistent");
	}
  
	/** 
	 * Sets the maximum range of the input window. 
	 * 
	 * @param globalMin		minimum of all minima for a specified stack.
	 * @param globalMax		maximum of all maxima for a specified stack.
	 */
	public void setExtent(Comparable globalMin, Comparable globalMax)
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
	public void setWindow(Comparable start, Comparable end)
	{
		verifyInterval(start, end);
		if (start.compareTo(globalMin) < 0 || globalMax.compareTo(end) < 0)
			throw new IllegalArgumentException("Wrong interval definition");
		windowStart = start;
		windowEnd = end;
		onWindowChange();
	}
	
	void setMap(QuantumMap qMap) { valueMapper = qMap; }
	
	/** Returns the globalMin. */
	Comparable getGlobalMin() { return globalMin; }
    
	/** Returns the globalMax. */
	Comparable getGlobalMax() { return globalMax; }
    
	/** Returns the input start value. */
	public Comparable getWindowStart() { return windowStart; }
	
	/** Returns the input end value. */
	public Comparable getWindowEnd() { return windowEnd; }
    
    /** Notify when the input interval has changed. */
	protected abstract void onWindowChange();
	
	/** 
	 * Map a value (in [windowStart, windowEnd]) to a value in the codomain
	 * interval.
	 * 
	 * @param value	pixel intensity value.
	 * @return int value in the codomain interval i.e. sub-interval of [0, 255]
	 */
	public abstract int quantize(Object value) throws QuantizationException;
	
}


