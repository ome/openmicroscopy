/*
 * omeis.providers.re.metadata.PixTStatsEntry
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

package omeis.providers.re.metadata;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Struct-like class to store some statistics calculated on <i>all</i> stacks
 * at a given wavelength (channel) within a pixels set. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/06/09 12:54:23 $)
 * </small>
 * @since OME2.2
 */
public class PixTStatsEntry 
{
	
	/** 
     * Minimum pixel intensity of <i>all</i> the stacks.
     * That is, the minimum pixel intensity calculated on all timepoints
     * within a given wavelength. 
     */
    public final double  globalMin;
	
    /** 
     * Maximum pixel intensity of <i>all</i> the stacks.
     * That is, the maximum pixel intensity calculated on all timepoints
     * within a given wavelength. 
     */
	public final double  globalMax;
     
 
    /**
     * Creates a new object to store the passed stats entry.
     * 
     * @param globalMin Minimum pixel intensity of <i>all</i> the stacks.
     * @param globalMax Maximum pixel intensity of <i>all</i> the stacks.
     */
	public PixTStatsEntry(double globalMin, double globalMax)
	{
		this.globalMin = globalMin;
		this.globalMax = globalMax;
	}
    
}
