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
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;

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
public abstract class Plane2D
{  
	
	private BytesConverter strategy;
	private byte[]		wavelengthStack;
	protected PlaneDef	planeDef;
	protected int		bytesPerPixel;
	protected PixelsDimensions dims;
	

	protected Plane2D(PlaneDef pDef, PixelsDimensions dims, int bytesPerPixel, 
						byte[] wavelengthStack, BytesConverter strategy)
	{
		this.planeDef = pDef;
		this.dims = dims;
		this.bytesPerPixel = bytesPerPixel;
		this.wavelengthStack = wavelengthStack;
		this.strategy = strategy;
	}
	
	protected abstract int calculateOffset(int x1, int x2);

	public Object getPixelValue(int x1, int x2)
	{
		int offset = calculateOffset(x1, x2);
		return strategy.pack(wavelengthStack, offset, bytesPerPixel);
	}
	
}
