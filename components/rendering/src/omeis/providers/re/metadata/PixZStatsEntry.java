/*
 * omeis.providers.re.metadata.PixZStatsEntry
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
 * Struct-like class to store some statistics calculated on a stack within
 * a given pixels set.
 * The stack is relative to a given wavelength (channel) and timepoint. 
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
public class PixZStatsEntry
{
	
	/** Minimum pixel intensity of the stack. */
	public final double		min;
	
	/** Maximum pixel intensity of the stack. */
	public final double  	max;
    
    
	/** 
	 * Creates a new object to store the passed stats entry.
	 *
	 * @param min  Minimum pixel intensity of the stack.
	 * @param max  Maximum pixel intensity of the stack.
	 */  
	public PixZStatsEntry(double min, double max)
	{       
		this.min = min;
		this.max = max;
	}
	
}
