/*
 * omeis.providers.re.quantum.LogarithmicMap
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

/** 
 * This class implements the {@link QuantumMap} interface. Each method
 * is a wrapper around the {@link Math#log(double)} method, which
 * returns the natural logarithm (base <i>e</i>) of a <code>double</code>
 * value.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/06/10 17:37:26 $)
 * </small>
 * @since OME2.2
 */
class LogarithmicMap
	implements QuantumMap
{
	
    /**
     * Controls if the specified is supported by the {@link Math#log(double)} 
     * method. If the value is negative, it is then set to 1.
     * 
     * @param x The value to check.
     * @return See above.
     */
    private double verifyInput(double x)
    {
        if (x <= 0) x = 1.0;
        return x;
    }
    
    /** 
     * Implemented as specified in {@link QuantumMap}. Note that in our case
     * the specified coefficient is not taken into account. 
     * @see QuantumMap#transform(int, double)
     */
	public double transform(int x, double k)
	{
		return Math.log(verifyInput(x));
	}

    /** 
     * Implemented as specified in {@link QuantumMap}. Note that in our case
     * the specified coefficient is not taken into account. 
     * @see QuantumMap#transform(double, double)
     */
	public double transform(double x, double k)
	{
		return Math.log(verifyInput(x));
	}

    /** 
     * Implemented as specified in {@link QuantumMap}. Note that in our case
     * the specified coefficient is not taken into account. 
     * @see QuantumMap#transform(float, double)
     */
	public double transform(float x, double k)
	{
		return Math.log(verifyInput(x));
	}
	

	
}

