/*
 * omeis.providers.re.quantum.QuantumMap
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
 * Provides methods to map value. Each method is wrapper around a method
 * exposed by the {@link Math} class. Each value mapper should implements 
 * this I/F.
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
interface QuantumMap
{
	
	/** 
	 * Maps the specified value.
	 *
	 * @param x		The input value.
	 * @param k		The selected curve in the family.
     * @return      The mapped value.
	 */
	public double transform (int x, double k);
	
    /** 
     * Maps the specified value.
     *
     * @param x     The input value.
     * @param k     The selected curve in the family.
     * @return      The mapped value.
     */
	public double transform (double x, double k);
	
    /** 
     * Maps the specified value.
     *
     * @param x     The input value.
     * @param k     The selected curve in the family.
     * @return      The mapped value.
     */
	public double transform (float x, double k);
	
}

