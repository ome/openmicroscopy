/*
 * org.openmicroscopy.shoola.env.rnd.PlaneSlicingDef
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

package org.openmicroscopy.shoola.env.rnd;

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
public class PlaneSlicingDef
	extends CodomainMapContext
{
	/** Default value 255*/
	private int 	upperLimit;
	
	/** Default value 0*/
	private int 	lowerLimit;
	
	private int 	bitPlaneSelected;
	
	private int		bitPlanePrevious;
	
	private boolean isConstant;
	
	/**
	 * Store parameters used for the bit-plane slicing transformations.
	 * The order of the bit-plane 1-bit to 7-bit
	 * 
	 * @param lowerLevel		value (in [0, 255]) used to set
	 * 							the level for bit-plane < bit-plane selected
	 * @param upperLevel		value (in [0, 255]) used to set
	 * 							the level for bit-plane > bit-plane selected
	 */
	public PlaneSlicingDef(int lowerLimit, int upperLimit)
	{
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;	
		isConstant = false;
	}
	
	public boolean getIsConstant()
	{
		return isConstant;
	}
	public int getLowerLimit() 
	{
		return lowerLimit;
	}

	public int getUpperLimit()
	{
		return upperLimit;
	}

}

