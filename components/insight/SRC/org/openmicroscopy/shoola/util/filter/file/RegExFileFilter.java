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

import java.io.File;
import java.util.regex.Pattern;
import javax.swing.filechooser.FileFilter;

/** 
 * A file filter for regular expressions.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class RegExFileFilter 
	extends CustomizedFileFilter 
{

	/** Pattern matching class to perform reg Ex. */
    private Pattern pattern;

    /** The regular expression used to match files, this could be a converted
     * version if user using wildCards */
	private String regEx;
	
	/** The original expression entered before conversion for wildCards. */
	private String originalEx; 
    
	/**
	 * Parse the wildCard expression into regular expression. 
	 * 
	 * @param ex The expression to parse.
	 * @return See above.
	 */
    private String parse(String ex)
    {
    	String newString = "";
    	
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
     * Instantiates the Regular expression file filter. 
     * 
     * @param regEx 			The expression. Mustn't be <code>null</code>.
     * @param wildCardFilter	The wildCard expression to be converted
     * 							to RegEx by filter.
     */
    public RegExFileFilter(String regEx, boolean wildCardFilter) 
    {
    	if (regEx == null)
    		throw new IllegalArgumentException("RegEx cannot be null.");
    	originalEx = regEx;
    	if (wildCardFilter) this.regEx = parse(regEx.toLowerCase());
    	else this.regEx = regEx;
        pattern = Pattern.compile(this.regEx);
    }
    
    /**
     * Instantiates the Regular expression file filter. 
     * 
     * @param regEx The expression
     */
    public RegExFileFilter(String regEx) 
    {
    	this(regEx, false);
    }

	/**
	 * Returns the regular expression of the method.
	 * 
	 * @return See above.
	 */
	public String getRegExpression() { return pattern.pattern(); }
	
	/**
	 * Sets the file filter of the regEx to the new RegEx. 
	 * 
	 * @param filter 			The value to set. Mustn't be <code>null</code>.
	 * @param wildCardFilter 	Pass <code>true</code> to parser the filter, 
	 * 							<code>false</code> otherwise.
	 */
	public void setFilter(String filter, boolean wildCardFilter)
	{
	 	if (regEx == null)
    		throw new IllegalArgumentException("RegEx cannot be null.");
    	originalEx = filter;
    	if (wildCardFilter) this.regEx = parse(filter);
    	else this.regEx = filter;
        pattern = Pattern.compile(this.regEx);
	}
	
    /**
     * Overridden to control is the file is supported.
     * @see CustomizedFileFilter#accept(File)
     */
    public boolean accept(File file) 
    {
    	if (file == null) return false;
    	return accept(file.getName());
    }

	/**
	 * Returns the extension of the filter.
	 */
	public String[] getExtensions()
	{ 
		String[] extensions = new String[1];
		extensions[0] = originalEx;
		return extensions; 
	}
	
	/**
	 * 	Overridden to return the MIME type.
	 * 	@see CustomizedFileFilter#getMIMEType()
	 */
	public String getMIMEType() { return ""; }
	
    /**
	 * Overridden to return the extension of the filter.
	 * @see CustomizedFileFilter#getExtension()
	 */
	public String getExtension() { return originalEx; }
	
    /**
     * Overridden to return the description of the filter.
     * @see FileFilter#getDescription()
     */
	public String getDescription() { return originalEx; }
	
	/**
	 * Overridden to accept the file identified by its name.
	 * @see CustomizedFileFilter#accept(String)
	 */
	public boolean accept(String name)
	{
		if (name == null) return false;
		return pattern.matcher(name.toLowerCase()).matches();
	}
	
}

