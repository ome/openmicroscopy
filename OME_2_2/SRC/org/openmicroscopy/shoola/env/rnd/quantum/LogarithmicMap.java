/*
 * org.openmicroscopy.shoola.env.rnd.quantum.LogarithmicMap
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
 * Logarithmic mapping i.e. log(x).
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
class LogarithmicMap
	implements QuantumMap
{
	
	/** Implemented as specified in {@link QuantumMap}. */
	public double transform(int x, double k)
	{
		return Math.log(verifyInput(x));
	}

	/** Implemented as specified in {@link QuantumMap}. */
	public double transform(double x, double k)
	{
		return Math.log(verifyInput(x));
	}

	/** Implemented as specified in {@link QuantumMap}. */
	public double transform(float x, double k)
	{
		return Math.log(verifyInput(x));
	}
	
	private double verifyInput(double x)
	{
		if (x <= 0) x = 1.0;
		return x;
	}
	
}

