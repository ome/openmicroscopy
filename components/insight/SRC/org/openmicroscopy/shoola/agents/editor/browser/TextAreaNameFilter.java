 /*
 * org.openmicroscopy.shoola.agents.editor.browser.TextAreaNameFilter 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.browser;

//Java imports

import java.awt.Toolkit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

//Third-party libraries

//Application-internal dependencies

/** 
 * This filter simply prevents addition of a new line with "Enter"
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class TextAreaNameFilter 
	extends DocumentFilter {

	/**
	 * Creates an instance 
	 */
	TextAreaNameFilter() 
	{
	}
	
	/**
	 * Overridden method, to prevent 'Enter' insertion (new line)
	 * 
	 * @see DocumentFilter#insertString(FilterBypass, int, String, AttributeSet)
	 */
	public void insertString(FilterBypass fb, int offs, String str, AttributeSet a)
	throws BadLocationException {
		
		if (!str.contains("\n"))
			super.insertString(fb, offs, str, a);
		
	}

	/**
	 * Overridden method, to prevent 'Enter' insertion (new line)
	 * 
	 * @see DocumentFilter#replace(FilterBypass, int, int, String, AttributeSet)
	 */
	public void replace(FilterBypass fb, int offs, int length, 
       String str, AttributeSet a)
	throws BadLocationException {
		
		if (!str.contains("\n"))
			super.replace(fb, offs, length, str, a);
		
	}
	
}