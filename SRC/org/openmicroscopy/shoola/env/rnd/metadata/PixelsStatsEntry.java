/*
 * org.openmicroscopy.shoola.env.rnd.metadata.ImgStatsEntry
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

package org.openmicroscopy.shoola.env.rnd.metadata;

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
public class PixelsStatsEntry
{
	/** Minimum pixel intensity of the stack. */
	public final double  min;
	
	/** Maximum pixel intensity of the stack.*/
	public final double  max;
    
    /** Geometric mean of the pixel intensities computed for the all stack. */
    public final double geoMean;
    
    /** 
     * Geometric sigma. i.e. distance between points and geometric mean.
     */
    public final double geoSigma;
	/** 
	 * Creates a new object to store the passed stats entry.
	 *
	 * @param   min  minimum pixel intensity of the stack.
	 * @param   max  maximum pixel intensity of the stack.
	 * @param	geoMean
	 * @param	geoSigma
	 */  
	public PixelsStatsEntry(double min, double max, double geoMean, 
							double geoSigma)
	{       
		this.min = min;
		this.max = max;
		this.geoMean = geoMean;
		this.geoSigma = geoSigma;
	}
	
}
