/*
 * ome.tools.StringUtils
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package ome.tools;

//Java imports

//Third-party libraries

//Application-internal dependencies


public class StringUtils {

	public static String getClassName(final Class arg0) {
		
		if (arg0 ==null)
			throw new IllegalArgumentException("Class argument cannot be null.");
		
		String klass = arg0.getName();
		int last = klass.lastIndexOf(".");
		if (last == -1)	
			return klass;
		
		return klass.substring(last+1);
	}
	
}
