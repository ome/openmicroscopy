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
abstract class QuantumStrategy
{
    
	private Comparable      		globalMin;
	private Comparable      		globalMax;
	private Comparable      		windowStart;
	private Comparable      		windowEnd;
	protected final QuantumDef      qDef;                  

	protected QuantumStrategy(QuantumDef qd)
	{
		windowStart = globalMin = new Integer(0);
		windowEnd = globalMax = new Integer(1);
		if (qd == null)    
			throw new NullPointerException("No quantum definition");
		this.qDef = qd;
	}
    
	private void verifyInterval(Comparable globalMin, Comparable globalMax)
	{
		boolean     b = false;
		if (globalMin != null && globalMax != null && 
			0 < globalMax.compareTo(globalMin)) {
			/**
			 * min and max could be out of pixel type range 
			 * b/c of an error occured in stats calculations
			 */
			/*
			switch (qDef.pixelType) { 
				case Pixels.INT8:
				case Pixels.UINT8:
					if (globalMin instanceof Integer && 
						globalMax instanceof Integer) {
						int m = ((Integer) globalMin).intValue(),
							M = ((Integer) globalMax).intValue();
						if (m < M && M-m < 0x100)  b = true; 
					}
					break;
				case Pixels.INT16:
				case Pixels.UINT16:
					if (globalMin instanceof Integer && 
						globalMax instanceof Integer) {
						int m = ((Integer) globalMin).intValue(),
							M = ((Integer) globalMax).intValue();
						if (m < M && M-m < 0x10000)  b = true; 
					}
					break;
				case Pixels.INT32:
					if (globalMin instanceof Integer &&
						globalMax instanceof Integer) {
						int m = ((Integer) globalMin).intValue(),
							M = ((Integer) globalMax).intValue();
						if (m < M && M-m < 0x100000000L)  b = true; 
					}
					break;
				case Pixels.UINT32:
					if (globalMin instanceof Long &&
						globalMax instanceof Long) {
						long m = ((Long) globalMin).longValue(),
							 M = ((Long) globalMax).longValue();
						if (m < M && M-m < 0x100000000L)  b = true; 
					}
					break;
				//TODO: checking all when we support these types
				case Pixels.BIT:
				case Pixels.FLOAT:  
				case Pixels.DOUBLE:
				case Pixels.COMPLEX:
				case Pixels.DOUBLE_COMPLEX: break;
			}
			*/
		}
		if (!b)
			throw new IllegalArgumentException("Pixel interval not consistent");
	}
  
	/** 
	 * Sets the maximum range of the input window. 
	 * 
	 * @param globalMin		minimum for a specified stack of all minima.
	 * @param globalMax		maximum for a specified stack of all maxima.
	 */
	void setExtent(Comparable globalMin, Comparable globalMax)
	{
		 verifyInterval(globalMin, globalMax); 
		 this.globalMin = globalMin;
		 this.globalMax = globalMax;
		 this.windowStart = globalMin;
		 this.windowEnd = globalMax;
		 onWindowChange();
	}
	
	/**
	 * Sets the inputWindow interval.
	 * 
	 * @param start		lower bound.
	 * @param end		upper bound.
	 */
	void setWindow(Comparable start, Comparable end)
	{
		if (end.compareTo(start) <= 0 || start.compareTo(globalMin) < 0 ||
			globalMax.compareTo(end) < 0)
			throw new IllegalArgumentException("Wrong interval definition");
		windowStart = start;
		windowEnd = end;
		onWindowChange();
	}
	
	/** Returns the globalMin. */
	Comparable getGlobalMin()
	{
		return globalMin;
	}
    
	/** Returns the globalMax. */
	Comparable getGlobalMax()
	{
		return globalMax;
	}
    
	/** Returns the input start value. */
	Comparable getWindowStart() {
		return windowStart;
	}
	
	/** Returns the input end value. */
	Comparable getWindowEnd()
	{
		return windowEnd;
	}
    
	protected abstract void onWindowChange();
	abstract int quantize(Object value);
	
}


