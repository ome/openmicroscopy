/*
 * org.openmicroscopy.shoola.util.ui.search.SearchUtil 
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
package org.openmicroscopy.shoola.util.ui.search;


//Java imports
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies

/** 
 * Helper class used to handle the search components.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class SearchUtil
{

	/** Separator between words. */
	public static final String		SEARCH_SEPARATOR =",";
	
	/** The separator between the first name and the last name. */
	public static final String		NAME_SEPARATOR = " ";
	
	/**
	 * Splits the passed string around matches of the given pattern.
	 * Returns a list of elements
	 * 
	 * @param text		The string to split.
	 * @param pattern	The delimiting regular expression.
	 * @return See above.
	 */
	public static List<String> splitTerms(String text, String pattern)
	{
		List<String> l = new ArrayList<String>();
		if (text == null) return l;
		text = text.trim();
		String[] r = text.split(pattern);
		String value; 
		for (int i = 0; i < r.length; i++) {
			value = r[i];
			if (value != null) {
				value = value.trim();
				if (value.length() != 0) l.add(value);
			}
		}
		return l;
	}
	
}
