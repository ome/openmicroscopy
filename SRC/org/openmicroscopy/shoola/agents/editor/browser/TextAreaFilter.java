 /*
 * org.openmicroscopy.shoola.agents.editor.browser.TextAreaFilter 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument.RunElement;

//Third-party libraries

//Application-internal dependencies

/** 
 * Filter for editing the text of a field. 
 * This filter prevents editing within a particular HTML tag. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TextAreaFilter 
	extends DocumentFilter {
	
	/**
	 * The document this filter is applied to.
	 */
	private StyledDocument 		document;
	
	/**
	 * The tag within which you are prevented from editing. 
	 */
	private Tag tag;

	/**
	 * Returns true if the character indicated by <code>offset</code> 
	 * is within the tag specified in the constructor. 
	 * Also true if the offset is before the first character of the tag. 
	 * 
	 * @param offset	position within the document. 
	 * @return			see above. 
	 */
	private boolean isOffsetWithinTag(int offset)
	{
		Element el = document.getCharacterElement(offset);
		
		if (el instanceof RunElement) {
			RunElement rE = (RunElement)el;
			Object ob = rE.getAttribute(tag);
			if (ob != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates an instance 
	 * @param doc		The StyledDocument that is queried to determine 
	 * 					whether the insert or replace operations are due to 
	 * 					be performed. This should be the same document instance
	 * 					that this filter is applied to. 
	 * @param tag		The HTML tag that you want to prevent editing within. 
	 */
	TextAreaFilter(StyledDocument doc, Tag tag) 
	{
		document = doc;
		this.tag = tag;
	}
	
	/**
	 * Overridden method, to prevent String insertion if within a tag. 
	 * 
	 * @see DocumentFilter#insertString(FilterBypass, int, String, AttributeSet)
	 */
	public void insertString(FilterBypass fb, int offs, String str, AttributeSet a)
	throws BadLocationException {
 
		boolean edit = canEdit(offs);
		
		if (edit)
			super.insertString(fb, offs, str, a);
		else
			Toolkit.getDefaultToolkit().beep();
		}

	/**
	 * Overridden method, to prevent String replacement if within a tag. 
	 * 
	 * @see DocumentFilter#replace(FilterBypass, int, int, String, AttributeSet)
	 */
	public void replace(FilterBypass fb, int offs, int length, 
       String str, AttributeSet a)
	throws BadLocationException {
		
		boolean edit = canEdit(offs);
		
		if (edit) {
			// check that the previous char is not within tag. 
			boolean charEndOfATag = isOffsetWithinTag(offs -1);
			// if it is, we need to set the attributes of new edit to null,
			// so that the new text does not extend the <tag>. 
			if (charEndOfATag) {
				a = null;
			}
			super.replace(fb, offs, length, str, a);
		}
		// if this is true, we are before the first character of tag.
		else if (canEdit(offs - 1)){
			// therefore, allow edit (won't affect tag).
			a = null;	// make sure it won't extend the tag
			super.replace(fb, offs, length, str, a);
		}
		else {
			Toolkit.getDefaultToolkit().beep();
		}
	}
	
	/**
	 * Overridden method, to prevent String removal if within a tag. 
	 * 
	 * @see DocumentFilter#remove(FilterBypass, int, int)
	 */
	public void remove(FilterBypass fb, int offset, int length) 
	throws BadLocationException {
		
		int end = offset + length;
		
		if (canEdit(offset)) {
			// if the end is right before the start of tag, allow edit
			if (canEdit(end) || canEdit(end-1)) {
				super.remove(fb, offset, length);
			}
		}
		
		else
			Toolkit.getDefaultToolkit().beep();
	}
	
	/**
	 * Returns the opposite of {@link #isOffsetWithinTag(int)}
	 * 
	 * @param offset	The position of the character
	 * @return		see above. 
	 */
	protected boolean canEdit(int offset) 
	{
		return (! isOffsetWithinTag(offset));
	}
}
