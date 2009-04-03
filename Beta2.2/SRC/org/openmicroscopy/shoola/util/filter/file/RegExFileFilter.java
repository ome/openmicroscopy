/*
 * org.openmicroscopy.shoola.util.filter.file.RegExFileFilter 
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
package org.openmicroscopy.shoola.util.filter.file;


//Java imports
import java.io.File;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies

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
/**
 * This is a regular expression file filter. 
 */
public class RegExFileFilter 
	extends CustomizedFileFilter 
{

	/** Pattern matching class to perform reg Ex. */
    private Pattern pattern;

    /** The regular expression used to match files, this could be a converted
     * version if user using wildCards */
	String regEx;
	
	/** The original expression entered before conversion for wildCards. */
	String originalEx; 
    
	/**
	 * Parse the wildCard expresison into regular expression. 
	 * @param ex see above.
	 * @return see above.
	 */
    private String parse(String ex)
    {
    	String newString = new String();
    	
    	for (int i = 0 ; i < ex.length(); i++)	
    	{
    		if (ex.charAt(i) == '*')
    			newString = newString + ".*";
    		else if(ex.charAt(i) == '.')
    			newString = newString +"[.]";
    		else if (ex.charAt(i) == '?')
    			newString = newString + ".";
    		else if (ex.charAt(i) == '{')
    			newString = newString + "[{]";
    		else if (ex.charAt(i) == '}')
    			newString = newString + "[}]";
    		else if (ex.charAt(i) == '(')
    			newString = newString + "[(]";
    		else if (ex.charAt(i) == ')')
    			newString = newString + "[)]";
    		else if (ex.charAt(i) == '_')
    			newString = newString + "[_]";
    		else if (ex.charAt(i) == '+')
    			newString = newString + "[+]";
    		else if (ex.charAt(i) == '-')
    			newString = newString + "[-]";
    		else
    			newString = newString + ex.charAt(i);
    	}
    	return newString;
    }
    
    /**
     * Instantiate the Regular expression file filter. 
     * 
     * @param regEx the expression
     * @param wildCardFilter is this a wildCard expression to be converted
     * to RegEx by filter.
     */
    public RegExFileFilter(String regEx, boolean wildCardFilter) 
    {
    	if (regEx == null)
    		throw new IllegalArgumentException("RegEx cannot be null.");
    	originalEx = regEx;
    	if (wildCardFilter)
    		this.regEx = parse(regEx.toLowerCase());
    	else
    		this.regEx = regEx;
        this.pattern = Pattern.compile(this.regEx);
    }
    
    /**
     * Instantiate the Regular expression file filter. 
     * @param regEx the expression
     */
    public RegExFileFilter(String regEx) 
    {
    	this(regEx, false);
    }

    /**
     * Overridden 
     * @see CustomizedFileFilter#accept(File)
     */
    public boolean accept(File fileName) 
    {
        String name = fileName.getName();
        return this.pattern.matcher(name.toLowerCase()).matches();
    }

    /**
	 * 	Overriden to return the extension of the filter.
	 * 	@see CustomizedFileFilter#getExtension()
	 */
	public String getExtension() { return originalEx; }
	
    /**
     * Overriden to return the description of the filter.
     * @see FileFilter#getDescription()
     */
	public String getDescription() { return originalEx; }
		
	/**
	 * Return the regular expression of the method.
	 * @return see above.
	 */
	public String getRegExpression() 
	{
		return this.pattern.pattern();
	}
	
	/**
	 * Set the file filter of the regEx to the new RegEx. 
	 * @param filter see above.
	 * @param wildCardFilter convert to wildcard.
	 */
	public void setFilter(String filter, boolean wildCardFilter)
	{
	 	if (regEx == null)
    		throw new IllegalArgumentException("RegEx cannot be null.");
    	originalEx = filter;
    	if (wildCardFilter)
    		this.regEx = parse(filter);
    	else
    		this.regEx = filter;
        this.pattern = Pattern.compile(this.regEx);
	}
}

