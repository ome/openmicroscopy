/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.DefaultFormatter 
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
import java.awt.Graphics;
import javax.swing.JEditorPane;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;

//Third-party libraries

//Application-internal dependencies

/** 
 * Default formatter, set the default color.
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
 */class DefaultFormatter
	implements Formatter
{

	/**
	 * Implemented as specified by the {@link Formatter} I/F.
	 * @see Formatter#formatText(JEditorPane, Segment, int, int, Graphics, 
	 * 		TabExpander, int, int, int)
	 */
    public int formatText(JEditorPane editor, Segment s, int x, int y, 
    		Graphics g, TabExpander e, int startOffset, int p0, int p1) 
    {
 	
    	if (editor != null) g.setColor(editor.getForeground());
    	return Utilities.drawTabbedText(s, x, y, g, e, startOffset);
    }
	
}


