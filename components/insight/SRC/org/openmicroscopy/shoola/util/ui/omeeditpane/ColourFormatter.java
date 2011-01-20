/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.RedFormatter 
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
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;


//Third-party libraries

//Application-internal dependencies

/** 
 * Formats the color.
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
public class ColourFormatter
	implements Formatter
{

	/** Colour of the formatter. */
	private Color 				colour;
	
	/** Underline the text in the formatter. */
	private boolean 			underline;
	
	/** Formatter of the hightlighter. */
	private FormatHighlighter 	formatHighlighter;
	
	/**
	 * Sets the colour and no underline for the formatter.  
	 * 
	 * @param colour see above.
	 */
	public ColourFormatter(Color colour)
	{
		this(colour, false);
	}
	
	/**
	 * Set the colour and underline for the formatter.  
	 * @param colour see above.
	 * @param underline see above.
	 */
	public ColourFormatter(Color colour, boolean underline)
	{
		if (colour == null)
			this.colour = Formatter.DEFAULT_COLOR;
		this.colour = colour;
		this.underline = underline;
		formatHighlighter = new FormatHighlighter();
	}
	
	/**
	 * Implemented as specified by the {@link Formatter} I/F.
	 * @see Formatter#formatText(JEditorPane, Segment, int, int, Graphics, 
	 * 		TabExpander, int, int, int)
	 */
    public int formatText(JEditorPane editor, Segment s, int x, int y, 
    		Graphics g, TabExpander e, int startOffset, int p0, int p1) 
    {
 		int newX;
    	g.setColor(colour);
    	newX =  Utilities.drawTabbedText(s, x, y, g, e, startOffset);
    	
    	Highlighter hilite = editor.getHighlighter();
    	if (underline) {
    		try
    		{
    			hilite.addHighlight(p0, p1, formatHighlighter);
    		}
    		catch (BadLocationException e1) {}
    	}
    	return newX;
    }
	
}

