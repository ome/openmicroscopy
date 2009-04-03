/*
 * org.openmicroscopy.shoola.util.ui.RegExFactory
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

package org.openmicroscopy.shoola.util.ui;



//Java imports
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

}
