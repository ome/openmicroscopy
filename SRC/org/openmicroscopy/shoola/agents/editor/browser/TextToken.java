 /*
 * org.openmicroscopy.shoola.agents.editor.browser.TextToken 
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

import javax.swing.text.html.parser.Element;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a simple representation of a piece of text within a document. 
 * It can be used to model an HTML element, although it currently only 
 * has: text, start, stop and id attributes. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TextToken {
	
	/** The textual content of this token / element */
	private String textContent;
	
	/** The start index of this text within it's parent document */
	private int startIndex;
	
	/** The end index of this text within it's parent document */
	private int stopIndex;
	
	/** The id attribute of this element */
	private String id;
	
	/**
	 * Creates an instance. 
	 * @param start		Start position of text within parent document
	 * @param end		End position of text within parent document
	 * @param text		The text that this element contains
	 */
	public TextToken(int start, int end, String text) {
		this(start, end, text, null);
	}
	
	/**
	 * Creates an instance. 
	 * @param start		Start position of text within parent document
	 * @param end		End position of text within parent document
	 * @param text		The text that this element contains
	 * @param id 		An identifier for this token / element
	 */
	public TextToken(int start, int end, String text, String id) {
		textContent = text;
		startIndex = start;
		stopIndex = end;
		if(id != null) {
			this.id = id;
		}
	}
	
	/**
	 * Sets the {@link #id} attribute
	 * 
	 * @param id	An identifier for this token / element
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Gets the {@link #id} attribute
	 * 
	 * @return	 see above
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Gets the {@link #startIndex} of this text token within it's parent
	 * document
	 * 
	 * @return	 see above
	 */
	public int getStart() {
		return startIndex;
	}
	
	/**
	 * Gets the {@link #stopIndex} of this text token within it's parent
	 * document
	 *  
	 * @return	 see above
	 */
	public int getEnd() {
		return stopIndex;
	}
	
	/**
	 * Gets the {@link #textContent} of this token
	 * 
	 * @return	 see above
	 */
	public String getText() {
		return textContent;
	}

}
