/*
 * org.openmicroscopy.shoola.env.rnd.roi.ROIPlaneStats
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

package org.openmicroscopy.shoola.env.rnd.roi;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Stores the reslts of some basic statistic analysis run on a given 2D
 * selection within an XY plane.
 * Some of the fields are also used as accumulators during the computation.
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
public class ROIPlaneStats
{
    
    //NOTE: fields have package visibility so they can easily be accessed
    //during the computation.  However, we only supply getters to outside
    //clients b/c these fields are read-only after the computation is done.
    
    /** The minimum value within the 2D selection. */
    double  min;
    
    /** The maximum value within the 2D selection. */
    double  max;
    
    /** The sum of all values within the 2D selection. */
    double  sum;  //Only useful during computation, hence no getter provided.
    
    /** The sum of the squares of all values within the 2D selection. */
    double  sumOfSquares;  //Only useful during computation, hence no getter.
    
    /** The mean value within the 2D selection. */
    double  mean;
    
    /** The standard deviation within the 2D selection. */
    double  standardDeviation;
    

    /**
     * Returns the minimum value within the 2D selection.
     * @return  See above.
     */
    public double getMin()
    {
        return min;
    }
    
    /**
     * Returns the maximum value within the 2D selection.
     * @return  See above.
     */
    public double getMax()
    {
        return max;
    }
    
    /**
     * Returns the mean value within the 2D selection.
     * @return  See above.
     */
    public double getMean()
    {
        return mean;
    }

    /**
     * Returns the standard deviation within the 2D selection.
     * @return  See above.
     */
    public double getStandardDeviation()
    {
        return standardDeviation;
    }
    
}
