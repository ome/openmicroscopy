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
import java.util.Iterator;
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

	/** Separator used to remove users. */
	public static final String		MINUS_SEPARATOR ="-";
	
	/** Separator between words. */
	public static final String		COMMA_SEPARATOR =",";
	
	/** The separator between the first name and the last name. */
	public static final String		SPACE_SEPARATOR = " ";
	
	/** The separator between the first name and the last name. */
	public static final String		QUOTE_SEPARATOR = "\"";
	
	/** The default text value. */
	static final String				ALL = "All";
	
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
				if (value.length() != 0 && !value.equals(ALL)) l.add(value);
			}
		}
		return l;
	}
	
	
	/**
	 * Splits the passed string around matches of the given pattern.
	 * Returns a list of elements
	 * 
	 * @param text		The string to split.
	 * @return See above.
	 */
	public static List<String> splitTerms(String text)
	{
		List<String> l = new ArrayList<String>();
		if (text == null) return l;
		text = text.trim();
		String pattern = SPACE_SEPARATOR;
		if (text.contains(QUOTE_SEPARATOR)) pattern = QUOTE_SEPARATOR;
		String[] r = text.split(pattern);
		String value; 
		for (int i = 0; i < r.length; i++) {
			value = r[i];
			if (value != null) {
				value = value.trim();
				if (value.length() != 0 && !value.equals(ALL) && 
						!value.equals(COMMA_SEPARATOR)) {
					if (value.contains(",")) value = value.replace(",", "");
					l.add(value);
				}
			}
		}
		return l;
	}
	
	/**
	 * Adds the passed string to the list of the terms.
	 * 
	 * @param termToAdd	The value to add.
	 * @param terms		Collection of terms to handle.
	 * @return See above.
	 */
	public static String formatString(String termToAdd, List<String> terms)
	{
		if (terms == null) return termToAdd;
    	int n = terms.size();
    	if (n == 0) return termToAdd;
    	String result = "";
    	Iterator<String> i = terms.iterator();
		boolean exist = false;
		String value;
		while (i.hasNext()) {
			value = i.next();
			if (value != null && value.equals(termToAdd))
				exist = true;
		}
		StringBuffer buffer = new StringBuffer();
		if (exist) {
			i = terms.iterator();
			int index = 0;
			//n = n-1;
			
			while (i.hasNext()) {
    			value = i.next();
    			buffer.append(value);
				if (index != n) 
					buffer.append(COMMA_SEPARATOR+SPACE_SEPARATOR);
				index++;
			}
			result = buffer.toString();
			return result;
		}
		//terms.remove(n-1);
		i = terms.iterator();
		while (i.hasNext()) {
			buffer.append(i.next());
			buffer.append(COMMA_SEPARATOR+SPACE_SEPARATOR);
		}
		result += buffer.toString();
		result += termToAdd;
		return result;
	}
	
}
