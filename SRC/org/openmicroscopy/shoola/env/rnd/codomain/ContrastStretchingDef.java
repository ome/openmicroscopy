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
public class ContrastStretchingDef
	extends CodomainMapContext
{
	private int xStart;
	private int yStart;
	private int xEnd;
	private int yEnd;
	
	/** coefficient of the first line with equation y = a0*x+b0. */
	private double a0, b0;
	
	/** coefficient of the second line with equation y = a1*x+b1. */
	private double a1, b1;
	
	/** coefficient of the third line with equation y = a2*x+b2. */
	private double a2, b2;
	
	public ContrastStretchingDef(int xStart, int yStart, int xEnd, int yEnd)
	{
		this.xStart = xStart;
		this.yStart = yStart;
		this.xEnd = xEnd;
		this.yEnd = yEnd;
	}
	
	void onCodomainChange()
	{
		setFirstLineCoefficient(intervalStart);
		setSecondLineCoefficient();
		setThirdLineCoefficient(intervalEnd);
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

	/** Compute the coefficient of the first straight y = a0*x+b0.  */
	private void setFirstLineCoefficient(int intervalStart)
	{
		double r = xStart-intervalStart;
		if (r == 0) a0 = 0;
		else a0 = (yStart-intervalStart)/r;
		b0 = intervalStart*(1-a0);
	}
	
	/** Compute the coefficient of the first straight y = a0*x+b0.  */
	private void setSecondLineCoefficient()
	{
		double r = xEnd-xStart;
		//To be on the save side, shouldn't happen.
		if (r == 0) a1 = 0;
		else a1 = (yEnd-yStart)/r;
		b1 = yStart-a1*xStart;
	}
	
	/** Compute the coefficient of the first straight y = a0*x+b0.  */
	private void setThirdLineCoefficient(int intervalEnd)
	{
		double r = intervalEnd-xEnd;
		if (r == 0) a2 = 0;
		else a2 = (intervalEnd-yEnd)/r;
		b2 = intervalEnd*(1-a2);
	}
	
}

