/*
 * org.openmicroscopy.shoola.agents.rnd.QuantumDef
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

package org.openmicroscopy.shoola.agents.rnd;

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
public class QuantumDef
{
    
	final int       family;
	final int       pixelType;
	final double    curveCoefficient;
	final int       cdStart;
	final int       cdEnd;
	final int       bitResolution;
	final int       approximation; //TODO: Do we keep it
	
	public QuantumDef(int family, int pixelType, double curveCoefficient,
				int cdStart, int cdEnd, int bitResolution, int approximation)
	{ 
		this.family = family;
		this.pixelType = pixelType;
		this.curveCoefficient = curveCoefficient;
		this.cdStart = cdStart;
		this.cdEnd = cdEnd;
		this.bitResolution = bitResolution;
		this.approximation = approximation;
	}

	public int getApproximation()
	{
		return approximation;
	}

	public int getBitResolution()
	{
		return bitResolution;
	}

	public int getCdEnd()
	{
		return cdEnd;
	}

	public int getCdStart()
	{
		return cdStart;
	}

	public double getCurveCoefficient()
	{
		return curveCoefficient;
	}

	public int getFamily()
	{
		return family;
	}

	public int getPixelType()
	{
		return pixelType;
	}

}

