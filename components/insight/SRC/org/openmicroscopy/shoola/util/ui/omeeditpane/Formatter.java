/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.Formatter 
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
package org.openmicroscopy.shoola.util.ui.omeeditpane;


//Java imports
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JEditorPane;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;

//Third-party libraries

//Application-internal dependencies

/** 
 * Interface that any formatter should implement.
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
public interface Formatter
{	
	
	/** The default color. */
	public static final Color DEFAULT_COLOR = AttributeSetFactory.DEFAULT_COLOR;
	
	/** The default color for url. */
	public static final Color DEFAULT_URL =  AttributeSetFactory.DEFAULT_URL;
	
	/** The default color for image, dataset etc linkages. */
	public static final Color DEFAULT_LINK =  AttributeSetFactory.DEFAULT_LINK;
	
	/** The default color for image, dataset etc linkages. */
	public static final Color PROTOCOL_LINK = AttributeSetFactory.PROTOCOL_LINK;
	
	/**
	 * Formats the text for the editor on segment s starting at position 
	 * <code>(p0, p1)</code>. Returns the last position of the text.
	 * 
	 * @param editor 		The editor.
	 * @param s 			The segment of text.
	 * @param x 			The x-position in the document.
	 * @param y 			The y-position in the document.
	 * @param g 			The graphics context.
	 * @param e 			The tabbed expander.
	 * @param startOffset 	The starting offset for the text.
	 * @param p0 			The starting position.
	 * @param p1 			The starting position.
	 * @return See above.
	 */
	public int formatText(JEditorPane editor, Segment s, int x, int y, 
			Graphics g, TabExpander e, int startOffset, int p0, int p1);
	
}


