/*
 * omeis.io.StackStatistics
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

package omeis.io;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * A struct-like class to hold some statistics for each stack in a pixels set.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/05/30 19:49:55 $)
 * </small>
 * @since OME2.2
 */
public class StackStatistics
{

    /**
     * Holds the minima of all the stacks in the pixels set.
     * The array is indexed in <code>[c][t]</code> order &#151; that is
     * <code>min[c][t]</code> holds the minimum of the stack in channel
     * <code>c</code> at timepoint <code>t</code>.
     */
    public double[][]   min;
    
    /**
     * Holds the maxima of all the stacks in the pixels set.
     * The array is indexed in <code>[c][t]</code> order &#151; that is
     * <code>max[c][t]</code> holds the maximum of the stack in channel
     * <code>c</code> at timepoint <code>t</code>.
     */
    public double[][]   max;
    
}
