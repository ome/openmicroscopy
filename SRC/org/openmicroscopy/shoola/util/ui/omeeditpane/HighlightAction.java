/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.HighlightAction 
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

import javax.swing.text.BadLocationException;

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
public class HighlightAction
	implements EditAction
{

	/** The colour of the highlight painter. */
	Color colour;
	
	/** The highlightpainter of the highlighter, this paints the highlight. */
	EditHighlightPainter highlightPainter;
	
	/** 
	 * Define the highlight painter with colour.
	 * @param colour see above.
	 */
	public HighlightAction(Color colour)
	{
		this.colour = colour;
		highlightPainter = new EditHighlightPainter(colour);
	}
	
	/* (non-Javadoc)
	 * @see HighlightUI.EditAction#actionPerformed(javax.swing.text.Document, int, int)
	 */
	public void actionPerformed(final OMEEditPane pane, final int start, final int end) throws BadLocationException
	{
		pane.getHighlighter().addHighlight(start, end, highlightPainter);
	}
	
}


