/*
 * org.openmicroscopy.shoola.env.rnd.defs.PlaneDef
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

package org.openmicroscopy.shoola.env.rnd.defs;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Identifies a 2D-plane in the XYZ moving frameof the 3D-Stack.
 * Tells which plane the wavelengths to render belong to.
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
public class PlaneDef
{
	
	/** Select a XY-plane. */
	public static final int		XY = 0 ;
	
	/** Select a YZ-plane. */
	public static final int		YZ = 1 ;
	
	/** Select a XZ-plane. */
	public static final int		XZ = 2 ;
	
	/** Normal of the XY-plane. The constant corresponds to the XY one. */
	private static final int	Z_NORMAL = 0;
	
	/** Normal of the YZ-plane. The constant corresponds to the YZ one.*/
	private static final int	X_NORMAL = 1;
	
	/** Normal of the XZ-plane. The constant corresponds to the XZ one.*/
	private static final int	Y_NORMAL = 2;
	
	/** Tells which kind of plane. One of the constant defined above.  */
	private int	slice;
	
	/** 
	 * Normal direction (i.e. X-axis) of the YZ-plane. 
	 * Only relevant in the context of this plane.
	 */
	private int	x;
	
	/** 
	 * Normal direction (i.e. Y-axis) of the XZ-plane. 
	 * Only relevant in the context of this plane.
	 */
	private int	y;
	
	/** 
	 * Normal direction (i.e. Z-axis) of the XY-plane. 
	 * Only relevant in the context of this plane.
	 */
	private int	z;
	
	/** t the Timepoint. */
	private int	t;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param slice		one the constant defined above.
	 * @param t			selected timepoint.
	 */
	public PlaneDef(int slice, int t)
	{
		if (slice == XY || slice == YZ || slice == XZ)
			this.slice = slice;
		else new IllegalArgumentException("Plane selection not available.");
		this.t = t;
	}

	/** Set the normal of the YZ-plane. */
	public void setX(int x)
	{
		verifyNormal(X_NORMAL);
		this.x = x;
	}

	/** Set the normal of the XZ-plane. */
	public void setY(int y)
	{
		verifyNormal(Y_NORMAL);
		this.y = y;
	}
	
	/** Set the normal of the XY-plane. */
	public void setZ(int z)
	{
		verifyNormal(Z_NORMAL);
		this.z = z;
	}

	public int getSlice() { return slice; }

	public int getT() { return t; }

	public int getX() { return x; }

	public int getY() { return y; }

	public int getZ() { return z; }

	private void verifyNormal(int normal)
	{
		if (normal != slice)
			throw new IllegalArgumentException("Cannot set the normal");
	}
	
}
