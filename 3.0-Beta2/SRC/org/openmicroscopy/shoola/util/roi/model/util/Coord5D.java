/*
 * org.openmicroscopy.shoola.util.roi.model.util.Coord5D 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.roi.model.util;

//Java imports
import java.awt.Point;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.model.util.Coord5D;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class Coord5D 
{
	public int c;
	public int t;
	public int z;
	
	public Point point;
	
	public int getChannel()
	{
		return c;
	}
	
	public int getTimePoint()
	{
		return t;
	}
	
	public int getZSection()
	{
		return z;
	}
	
	public double getX()
	{
		return point.getX();
	}
	
	public double getY()
	{
		return point.getY();
	}
	
	public boolean equals(Coord5D obj)
	{
		if(obj.c == c && obj.t == t && obj.z == z && obj.point.equals(point))
			return true;
		return false;
	}
	
}


