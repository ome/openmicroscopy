/*
 * org.openmicroscopy.shoola.env.rnd.codomain.PlaneSlicingDef
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

package org.openmicroscopy.shoola.env.rnd.codomain;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * We consider that the image is composed of eight 1-bit planes
 * ranging from bit-plane 0 for the least significant bit to bit-plane 7
 * for the most significant bit.
 * The BIT_* constants cannot be modified b/c they have a meaning. 
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
public class PlaneSlicingContext
	extends CodomainMapContext
{
	
	/** bit-plane 0, corresponding value 2^1-1 . */
	public static final int 		BIT_ZERO = 0;
	
	/** bit-plane 1, corresponding value 2^1. */
	public static final int 		BIT_ONE = 2;
	
	/** bit-plane 2, corresponding value 2^2. */
	public static final int 		BIT_TWO = 4;
	
	/** bit-plane 3, corresponding value 2^3. */
	public static final int 		BIT_THREE = 8;
	
	/** bit-plane 4, corresponding value 2^4. */
	public static final int 		BIT_FOUR = 16;
	
	/** bit-plane 5, corresponding value 2^5. */
	public static final int 		BIT_FIVE = 32;

	/** bit-plane 6, corresponding value 2^6. */
	public static final int 		BIT_SIX = 64;	
	
	/** bit-plane 7, corresponding value 2^7. */
	public static final int 		BIT_SEVEN = 128;
	
	/** bit-plane 8, corresponding value 2^8-1. */
	public static final int 		BIT_EIGHT = 255;	
	
	/** 
	 * Constant level for bit-planes &gt; planeSelected w.r.t the 
	 * higher-order bits.
	 */
	private int 					upperLimit;
	
	/** 
	 * Constant level for bit-planes &lt; planeSelected w.r.t the 
	 * higher-order bits.
	 */
	private int 					lowerLimit;
	
	/** value corresponding to the index of the bit plane selected. */
	private int 					planeSelected;
	
	/** 
	 * value corresponding to the index of the bit-plane ranged just 
	 * before the one selected.
	 */
	private int						planePrevious;
	
	/** 
	 * <code>false</code> if bit-planes aren't mapped to a specified level
	 * <code>true</code> otherwise.
	 */
	private boolean 				constant;
	
	public PlaneSlicingContext(int planePrevious, int planeSelected,
							 	boolean constant)
	{
		if (planePrevious > planeSelected)
			throw new IllegalArgumentException("Not a valid plane selection");	
		verifyBitPlanes(planePrevious);
		verifyBitPlanes(planeSelected);
		this.planePrevious = planePrevious;
		this.planeSelected = planeSelected;
		this.constant = constant;
	}

	/** Contructor used to make a copy of the object. */
	private PlaneSlicingContext() {}
	

	/** 
	 * Implemented as specified by superclass.
	 * 
	 * @see CodomainMapContext#buildContext()
	 */
	void buildContext() {}

	/** 
	 * Implemented as specified by superclass.
	 * @see CodomainMapContext#buildContext()
	 */
	CodomainMap getCodomainMap() 
	{
		return new PlaneSlicingMap();
	}

	/** 
	 * Implemented as specified by superclass.
	 * @see CodomainMapContext#buildContext()
	 */
	public CodomainMapContext copy() 
	{
		PlaneSlicingContext copy = new PlaneSlicingContext();
		copy.upperLimit = upperLimit;
		copy.lowerLimit = lowerLimit;
		copy.planeSelected = planeSelected;
		copy.planePrevious = planePrevious;
		copy.constant = constant;
		return copy;
	}
	
	public int getPlanePrevious()
	{
		return planePrevious;
	}

	public int getPlaneSelected()
	{
		return planeSelected;
	}
	
	public boolean IsConstant()
	{
		return constant;
	}
	
	public int getLowerLimit() 
	{
		return lowerLimit;
	}

	public int getUpperLimit()
	{
		return upperLimit;
	}
	
	/** 
	 * Set the limits.
	 *
	 * @param lowerLevel		value (in [intervalStart, intervalEnd]) 
	 * 							used to set the level of the
	 * 							bit-plane &lt; bit-plane selected.
	 * @param upperLevel		value (in [intervalStart, intervalEnd]) 
	 * 							used to set the level of the
	 * 							bit-plane &gt; bit-plane selected.
	 */
	public void setLimits(int lowerLimit, int upperLimit)
	{
		verifyInput(lowerLimit);
		verifyInput(upperLimit);
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;	
	}
	
	public void setLowerLimit(int ll)
	{
		verifyInput(ll);
		lowerLimit = ll;
	}
	
	public void setUpperLimit(int ul)
	{
		verifyInput(ul);
		upperLimit = ul;
	}
	
	public void setPlanes(int planePrevious, int planeSelected)
	{
		if (planePrevious > planeSelected)
			throw new IllegalArgumentException("Not a valid plane selection");	
		verifyBitPlanes(planePrevious);
		verifyBitPlanes(planeSelected);
		this.planePrevious = planePrevious;
		this.planeSelected = planeSelected;
	}
	
	public void setConstant(boolean b) 
	{
		constant = b;
	}
	
	/** Verify if bitPlane is one the contants defined above. */
	private void verifyBitPlanes(int bitPlane)
	{
		boolean b = false;
		switch (bitPlane) {
			case BIT_ZERO:	b = true; break;
			case BIT_ONE:   b = true; break;
			case BIT_TWO:  	b = true; break;
			case BIT_THREE: b = true; break;
			case BIT_FOUR:  b = true; break;
			case BIT_FIVE: 	b = true; break;
			case BIT_SIX: 	b = true; break;
			case BIT_SEVEN: b = true; break;
			case BIT_EIGHT: b = true;
		}
		if (!b) throw new IllegalArgumentException("Not a valid plane");
	}
	
	private void verifyInput(int x)
	{
		if (x < intervalStart || x > intervalEnd)
			throw new IllegalArgumentException("Value not in the interval.");
	}

}


