/*
 * org.openmicroscopy.shoola.util.StringComparator 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util;


//Java imports
import java.io.Serializable;
import java.util.Comparator;

//Third-party libraries

//Application-internal dependencies

/** 
 * Basic String comparator.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class StringComparator 
	implements Comparator, Serializable
{

	/**
	 * Implemented as specified by the {@link Comparator} I/F.
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(Object o1, Object o2)
	{
		if (o1 == null && o2 == null) return 0;
		else if (o1 == null) return -1;
		else if (o2 == null) return 1;
		String s1 = o1.toString();
		String s2 = o2.toString();
		if (s1 == null && s2 == null) return 0;
		else if (s1 == null) return -1;
		else if (s2 == null) return 1;
		return s1.compareTo(s2);
	}
	
}
