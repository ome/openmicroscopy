/*
 * org.openmicroscopy.shoola.env.rnd.codomain.ContrastStretchingDef
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
 * Two points pStart and pEnd define the context of this transformation. 
 * We determine the equations of 3 lines (segments to be correct).
 * The first one is a line between the point with coordinates 
 * (intervalStart, intervalStart) and (xStart, yStart).
 * The second one between (xStart, yStart) and (xEnd, yEnd).
 * The third one between (xEnd, yEnd) and (intervalEnd, intervalEnd).
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
public class ContrastStretchingContext
	extends CodomainMapContext
{
	
	/** x-coordinate of pStart. */
	private int 					xStart;
	
	/** y-coordinate of pStart. */	
	private int 					yStart;
	
	/** x-coordinate of pEnd. */
	private int 					xEnd;
	
	/** y-coordinate of pEnd. */
	private int 					yEnd;
	
	/** coefficients of the first line with equation y = a0*x+b0. */
	private double 					a0, b0;
	
	/** coefficients of the second line with equation y = a1*x+b1. */
	private double 					a1, b1;
	
	/** coefficients of the third line with equation y = a2*x+b2. */
	private double 					a2, b2;
	
	/** 
	 * Verify the bounds of the input interval[s,e]. This interval must be a
	 * sub-interval of [intervalStart, intervalEnd]. 
	 * 
	 * @param start		Lower bound of the interval.
	 * @param end		Upper bound of the interval.
	 */
	private void verifyInputInterval(int start, int end)
	{
		if (start >= end || start < intervalStart || end > intervalEnd)
			throw new IllegalArgumentException("Interval not consistent.");
	}
	
	/** Compute the coefficients of the first straight y = a0*x+b0.  */
	private void setFirstLineCoefficient(int intervalStart)
	{
		double r = xStart-intervalStart;
		if (r == 0) a0 = 0;
		else a0 = (yStart-intervalStart)/r;
		b0 = intervalStart*(1-a0);
	}
	
	/** Compute the coefficients of the first straight y = a0*x+b0.  */
	private void setSecondLineCoefficient()
	{
		double r = xEnd-xStart;
		//To be on the save side, shouldn't happen.
		if (r == 0) a1 = 0;
		else a1 = (yEnd-yStart)/r;
		b1 = yStart-a1*xStart;
	}
	
	/** Computes the coefficient of the first straight y = a0*x+b0.  */
	private void setThirdLineCoefficient(int intervalEnd)
	{
		double r = intervalEnd-xEnd;
		if (r == 0) a2 = 0;
		else a2 = (intervalEnd-yEnd)/r;
		b2 = intervalEnd*(1-a2);
	}
	
	/** 
	 * Implemented as specified by superclass.
	 * Calculates the equations of the lines.
	 * 
	 * @see CodomainMapContext#buildContext()
	 */
	void buildContext() 
	{
		setFirstLineCoefficient(intervalStart);
		setSecondLineCoefficient();
		setThirdLineCoefficient(intervalEnd);
	}

	/** 
	 * Implemented as specified by superclass.
	 * @see CodomainMapContext#buildContext()
	 */
	CodomainMap getCodomainMap() 
	{
		return new ContrastStretchingMap();
	}

	/** 
	 * Implemented as specified by superclass.
	 * @see CodomainMapContext#buildContext()
	 */
	public CodomainMapContext copy() 
	{
		ContrastStretchingContext copy = new ContrastStretchingContext();
		copy.xStart = xStart;
		copy.yStart = yStart;
		copy.xEnd = xEnd;
		copy.yEnd = yEnd;
		copy.a0 = a0;
		copy.a1 = a1;
		copy.a2 = a2;
		copy.b0 = b0;
		copy.b1 = b1;
		copy.b2 = b2;
		return copy;
	}
	
	public void setCoordinates(int xStart, int yStart, int xEnd, int yEnd)
	{
		verifyInputInterval(xStart, xEnd);
		verifyInputInterval(yStart, yEnd);
		this.xStart = xStart;
		this.xEnd = xEnd;
		this.yStart = yStart;
		this.yEnd = yEnd;
	}
	
	public void setXStart(int xs)
	{
		verifyInputInterval(xs, xEnd);
		xStart = xs;
	}
	
	public void setXEnd(int xe)
	{
		verifyInputInterval(xStart, xe);
		xEnd = xe;
	}
	
	public void setYStart(int ys)
	{
		verifyInputInterval(ys, yEnd);
		yStart = ys;
	}
	
	public void setYEnd(int ye)
	{
		verifyInputInterval(yStart, ye);
		yEnd = ye;
	}
	
	public int getXEnd()
	{
		return xEnd;
	}

	public int getXStart()
	{
		return xStart;
	}

	public int getYEnd()
	{
		return yEnd;
	}

	public int getYStart() 
	{
		return yStart;
	}

	public double getA0() 
	{
		return a0;
	}

	public double getA1()
	{
		return a1;
	}

	public double getA2()
	{
		return a2;
	}

	public double getB0()
	{
		return b0;
	}

	public double getB1() 
	{
		return b1;
	}

	public double getB2()
	{
		return b2;
	}
	
}

