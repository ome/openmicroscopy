/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.FormatHighlighter 
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
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

//Third-party libraries

//Application-internal dependencies

/** 
 * Highlight text and draws an underline.
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
class FormatHighlighter
	implements Highlighter.HighlightPainter
 {
	
	/** The thickness of the line. */
	private int		thickness;
	
	/** Creates a new instance. */
	FormatHighlighter() 
    {
		this(2);
    }
	
	/**
	 * Creates a new instance.
	 * 
	 * @param thickness The thickness of the underline.
	 */
	FormatHighlighter(int thickness) 
    {
		this.thickness = thickness;
    }
	
	/**
	 * Paints a thick line under one line of text, from r extending rightward to
	 * x2
	 * 
	 * @param g 	The graphics context
	 * @param r 	The rectangle to draw.
	 * @param x2 	The final extend of the text. 
	 */
	private void paintLine(Graphics g, Rectangle r, int x2) 
	{
		int ytop = r.y+r.height-3;
	    g.fillRect(r.x, ytop, x2-r.x, thickness);
	}

	/**
	 * Overridden to paint the formatter for the component c, with graphics 
	 * context g and from text elements p0 to p1.
	 * @see Highlighter.HighlightPainter#paint(Graphics, int, int, Shape, 
	 * JTextComponent)
	 */
	public void paint(Graphics g, int p0, int p1, Shape bounds, 
					JTextComponent c) 
	{
		Rectangle r0 = null, r1 = null, rbounds = bounds.getBounds();
		int xmax = rbounds.x + rbounds.width; // x coordinate of right edge
		try 
		{ 
			// convert positions to pixel coordinates
			r0 = c.modelToView(p0);
			r1 = c.modelToView(p1);
		} 
		catch (BadLocationException ex) 
		{
			return;
		}
		if ((r0 == null) || (r1 == null))
			return;
		
		g.setColor(c.getSelectionColor());

		// special case if p0 and p1 are on the same line
		if (r0.y == r1.y)
		{
			paintLine(g, r0, r1.x);
			return;
		}
		
		// first line, from p1 to end-of-line
		paintLine(g, r0, xmax);
		
		// all the full lines in between, if any (assumes that all lines have
		// the same height--not a good assumption with JEditorPane/JTextPane)
		r0.y += r0.height; // move r0 to next line
		r0.x = rbounds.x; // move r0 to left edge
		while (r0.y < r1.y) 
		{
			paintLine(g, r0, xmax);
			r0.y += r0.height; // move r0 to next line
		}
		
		// last line, from beginning-of-line to p1
		paintLine(g, r0, r1.x);
	}
	
}


