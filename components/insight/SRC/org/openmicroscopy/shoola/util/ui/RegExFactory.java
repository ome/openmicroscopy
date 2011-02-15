/*
 * org.openmicroscopy.shoola.util.ui.RegExFactory
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.util.ui;



//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Third-party libraries

//Application-internal dependencies

/** 
 * A factory to create and handle {@link Pattern}s.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class RegExFactory
{
    
	/** The collection of escaping characters we allow in the search. */
	private static final List<Character>	SUPPORTED_SPECIAL_CHAR;
	
	static {
		SUPPORTED_SPECIAL_CHAR = new ArrayList<Character>();
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('-'));
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('['));
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf(']'));
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('?'));
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('+'));
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('*'));
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('.'));
	}
	
    /**
     * Attempts to find the next subsequence of the input sequence that matches
     * the pattern.
     * 
     * @param p 	The pattern.
     * @param input The character sequence to be matched
     * @return <code>true</code> if, and only if, a subsequence of the input
     *          sequence matches this matcher's pattern.
     */
    public static boolean find(Pattern p, String input)
    {
        Matcher m = p.matcher(input);
        return m.find();
    }
    
    /**
     * Helper method to create an insensitive pattern.
     * According to the Java doc, {@link Pattern#compile(String, int)} throws an
     * {@link java.util.regex.PatternSyntaxException} if it encounters the 
     * metacharacters like +, ? and *.
     * 
     * @param regEx The expression to be compiled.
     * @return See above.
     * @throws java.util.regex.PatternSyntaxException
     */
    public static Pattern createCaseInsensitivePattern(String regEx)
    {
        return Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
    }
    
    /**
     * Helper method to create a pattern.
     * According to the Java doc, {@link Pattern#compile(String, int)} throws an
     * {@link java.util.regex.PatternSyntaxException} if it encounters the 
     * metacharacters like +, ? and *.
     * 
     * @param regEx The expression to be compiled.
     * @return See above.
     * @throws java.util.regex.PatternSyntaxException
     */
    public static Pattern createPattern(String regEx)
    {
        return Pattern.compile(regEx);
    }

    /**
     * Formats the passed collection of terms.
     * 
     * @param terms The collection to handle.
     * @return See above.
     */
    public static String[] formatSearchText(List<String> terms)
    {
    	if (terms == null) return new String[0];
    	String[] formattedTerms = new String[terms.size()];
    	String value;
    	Iterator<String> i = terms.iterator();
    	int j = 0;
    	int n;
    	char[] arr;
    	String v;
    	StringBuffer buffer;
    	while (i.hasNext()) {
			value = i.next();
			if (value == null) value = "";
			n = value.length();
			arr = new char[n];
			v = "";
			value.getChars(0, n, arr, 0);  
			buffer = new StringBuffer();
			for (int k = 0; k < arr.length; k++) {
				if (SUPPORTED_SPECIAL_CHAR.contains(arr[k])) 
					buffer.append("\\"+arr[k]);
				else buffer.append(arr[k]);
			}
			formattedTerms[j] = buffer.toString();
			j++;
		}
    	return formattedTerms;
    }
    
    /**
     * Formats the passed collection of terms.
     * 
     * @param terms The collection to handle.
     * @return See above.
     */
    public static String formatSearchTextAsString(List<String> terms)
    {
    	if (terms == null) return "";
    	String[] formattedTerms = formatSearchText(terms);
    	StringBuffer text = new StringBuffer();
    	for (int i = 0; i < formattedTerms.length; i++) 
			text.append(formattedTerms[i]);
		
    	return text.toString();
    }
    
}
