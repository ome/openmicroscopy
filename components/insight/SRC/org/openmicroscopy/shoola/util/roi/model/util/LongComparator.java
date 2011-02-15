/*
 * org.openmicroscopy.shoola.util.roi.model.util.LongComparator 
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
import java.io.Serializable;
import java.util.Comparator;

//Third-party libraries

//Application-internal dependencies

/** 
 * Compares <code>Long</code>.
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
public class LongComparator 
	implements Comparator, Serializable
{

	/** 
	 * Compares the passed objects.
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(Object o1, Object o2)
	{
		if (!(o1 instanceof Long || o2 instanceof Long))
			return 0;
		Long a = (Long) o1;
		Long b = (Long) o2;
		if (a < b) return -1;
		else if (a > b) return 1;
		return 0;
	}
	
}



