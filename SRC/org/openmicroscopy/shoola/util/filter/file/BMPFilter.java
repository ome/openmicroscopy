/*
 * org.openmicroscopy.shoola.util.filter.file.BMPFilter
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

package org.openmicroscopy.shoola.util.filter.file;



//Java imports
import java.io.File;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies

/** 
 * Filters the <code>BMP</code> files.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class BMPFilter
	extends FileFilter
{
	
	/** Possible file extensions. */
	public static final String 	BMP = "bmp";
    
    /** Possible file extensions. */
	public static final String 	DIB = "dib";
		
    /**
     * Overriden to return the description of the filter.
     * @see FileFilter#getDescription()
     */
	public String getDescription() { return "BMP images"; }
		
    /**
     * Overriden to accept file with the declared file extensions.
     * @see FileFilter#accept(File)
     */
	public boolean accept(File f)
	{
		if (f.isDirectory()) return true;
		String s = f.getName();
		String extension = null;
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length()-1)
			extension = s.substring(i+1).toLowerCase();
		if (extension != null)
			return ((extension.equals(BMP) || extension.equals(DIB)));
		return false;
	}
	
}

