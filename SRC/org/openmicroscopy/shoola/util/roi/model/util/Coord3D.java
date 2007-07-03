/*
 * org.openmicroscopy.shoola.util.roi.model.util.Coord3D 
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
import java.util.Comparator;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

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
public class Coord3D 
	implements Comparator
{
	//public int c;
	private int t;
	private int z;
	
	public Coord3D()
	{
		this(0, 0);
	}
	
	public Coord3D(int zsec, int time)
	{
	//	c = ch;
		t = time;
		z = zsec;
	}
	
	
	//public int getChannel()
	//{
	//	return c;
	//}
	
	public int getTimePoint()
	{
		return t;
	}
	
	public int getZSection()
	{
		return z;
	}
	
	public boolean equals(Object obj)
	{
		if(!(obj instanceof Coord3D))
			return false;
		Coord3D comp = (Coord3D)obj;
//		return (comp.c == this.c && comp.t == this.t && comp.z == this.z );
		return (comp.t == this.t && comp.z == this.z );
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) 
	{
		Coord3D a = (Coord3D)o1;
		Coord3D b = (Coord3D)o2;
	//	if(a.c < b.c)
	//		return -1;
	//	else if(a.c > b.c)
	//		return 1;
		if(a.t < b.t)
			return -1;
		else if(a.t > b.t)
			return 1;
		else if(a.z<b.z)
			return -1;
		else if(a.z>b.z)
			return 1;
		else 
			return 0;
	}
	
	public Coord3D clone()
	{
//		return new Coord3D(this.c, this.t, this.z);
		return new Coord3D(this.t, this.z);
	}
	
	
}


