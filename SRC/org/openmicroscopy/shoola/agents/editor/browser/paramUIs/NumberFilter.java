 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.NumberFilter 
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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs;

import java.awt.Toolkit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class NumberFilter extends DocumentFilter {

	private Document 		doc;
	
	/**
	 * Creates an instance 
	 */
	public NumberFilter(Document doc)
	{
		this.doc = doc;
	}
	
	private boolean canInsert(String str) 
		throws BadLocationException{
		
		String currentVal = doc.getText(0, doc.getLength());
		
		if (str.contains(".")) {
			if (currentVal.contains(".")) {
				return false;
			}
		}
		
		if (str.contains("-")) {
			if (currentVal.contains("-")) {
				return false;
			}
		}
		
		if ((".".equals(str)) || ("-".equals(str))) {
			return true;
		}
		
		try {
			Float s = Float.valueOf(str);
			return true;
		}
		catch (NumberFormatException ex) {
			return false;
		}
	}
	
	/**
	 * Overridden method, to prevent 'Enter' insertion (new line)
	 * 
	 * @see DocumentFilter#insertString(FilterBypass, int, String, AttributeSet)
	 */
	public void insertString(FilterBypass fb, int offs, String str, AttributeSet a)
	throws BadLocationException {

		if (canInsert(str))
			super.insertString(fb, offs, str, a);
		
		else
			Toolkit.getDefaultToolkit().beep();
	}

	/**
	 * Overridden method, to prevent 'Enter' insertion (new line)
	 * 
	 * @see DocumentFilter#replace(FilterBypass, int, int, String, AttributeSet)
	 */
	public void replace(FilterBypass fb, int offs, int length, 
       String str, AttributeSet a)
	throws BadLocationException {
		
		if (canInsert(str))
			super.insertString(fb, offs, str, a);
		
		else
			Toolkit.getDefaultToolkit().beep();
	}
	
	/**
	 * Overridden method, to prevent String removal if within a tag. 
	 * 
	 * @see DocumentFilter#remove(FilterBypass, int, int)
	 */
	public void remove(FilterBypass fb, int offset, int length) 
	throws BadLocationException {
		
		super.remove(fb, offset, length);		
	}
}