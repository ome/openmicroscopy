/*
 * org.openmicroscopy.shoola.env.rnd.Plane2D
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

package org.openmicroscopy.shoola.env.rnd.data;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;

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
public class Plane2D
{
	/** Number of pixels on the X1 axis. */
	private int     	sizeX1;
	
	/** Number of pixels on the X2 axis. */
	private int     	sizeX2;   

	/** Reference to a plane def. */
	private PlaneDef	pDef;
	

	Plane2D(PlaneDef pDef, int sizeX1, int sizeX2)
	{
		this.pDef = pDef;
		this.sizeX1 = sizeX1;
		this.sizeX2 = sizeX2;
	}
	
	/** 
	* Retrieves the PixelValue of a given point in a Plane2D.
	*
	* @param x1		x1-coordinate of the pixel.
	* @param x2     x2-coordinate of the pixel.
	* @return	Object related to the selected BytesConverter strategy
	*/

	public Object getPixelValue(int x1, int x2)
	{
		int offset;
		return null;
	}
	
}
