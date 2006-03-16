/*
 * ome.util.math.Approximation
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

package ome.util.math;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Utility class. Collection of static methods.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/06/09 15:01:17 $)
 * </small>
 * @since OME2.2
 */
public class Approximation
{

	/** 
	 * Returns the nearest integer to the specified value e.g. 
	 * 1.2 returns 1, 1.6 returns 2.
     * 
     * @param v The value to analyze.
     * @return  The nearest integer.
	 */
	public static double nearestInteger(double v)
	{
		double d = Math.floor(v);
		double diff = Math.abs(v-d);
		double value = d;
		if (diff > 0.5) value++; 
		return value;
	}
    
	/** 
     * Returns the smallest integer. 
     * This method calls {@link Math#floor(double)}.
     * 
     * @param v The value to analyze.
     * @return  The smallest integer.
     */
	public static double smallestInteger(double v) { return Math.floor(v); }
	
    /** 
     * Returns the largest integer. 
     * This method calls {@link Math#ceil(double)}.
     * 
     * @param v The value to analyze.
     * @return  The largest integer.
     */ 
	public static double largestInteger(double v) { return Math.ceil(v); }

}
