/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.HTMLFormatAction 
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
import javax.swing.text.SimpleAttributeSet;

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
public class FormatHighlightAction
	implements EditAction
{	
	/** The attribute associated with the FormatAction(@link FormatAction). */
	SimpleAttributeSet 		attr;
	
	/** Colour associated with Highlight action. */
	Color		 			colour;
	
	/** The HighlightAction for this action. */
	HighlightAction 		highlightAction;
	
	/** The FormatAction for this action. */
	FormatAction			formatAction;
	
	/**
	 * Define the format and highlight action for this composite action.
	 * @param colour Colour of the highlight.
	 * @param attr Attributes for the format of the text.
	 */
	public FormatHighlightAction(Color colour, SimpleAttributeSet attr)
	{
		this.attr = attr;
		this.colour = colour;
		highlightAction = new HighlightAction(colour);
		formatAction = new FormatAction(attr);
	}

	/* (non-Javadoc)
	 * @see HighlightUI.EditAction#actionPerformed(HighlightUI.OMEEditPane, int, int)
	 */
	public void actionPerformed(OMEEditPane pane, int start, int end) throws BadLocationException
	{
		highlightAction.actionPerformed(pane, start, end);
		formatAction.actionPerformed(pane, start, end);
	}
	
}


