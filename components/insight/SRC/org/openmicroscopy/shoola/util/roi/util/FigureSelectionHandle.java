/*
* org.openmicroscopy.shoola.util.roi.util.FigureSelectionHandle
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
package org.openmicroscopy.shoola.util.roi.util;


//Java imports
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

//Third-party libraries
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.BoundsOutlineHandle;
import org.jhotdraw.draw.Figure;

//Application-internal dependencies

/**
 * Handles the selection of the figures.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class FigureSelectionHandle 
	extends BoundsOutlineHandle
{
	
	/**
	 * Creates a new instance.
	 * 
	 * @param figure The figure to handle.
	 */
	public FigureSelectionHandle(Figure figure) 
	{
		super(figure);
	}

	/**
	 * Overridden to modify the stroke of the figure is selected.
	 * @see BoundsOutlineHandle#draw(Graphics2D)
	 */
	public void draw(Graphics2D g) 
	{
		Figure f = getOwner();
		Color colour = (Color) f.getAttribute(AttributeKeys.STROKE_COLOR);
		double width = (Double) f.getAttribute(AttributeKeys.STROKE_WIDTH);
		Color strokeColour = colour.brighter();
		if (strokeColour == colour) strokeColour = Color.red;
				
		f.setAttribute(AttributeKeys.STROKE_COLOR, strokeColour);
		f.setAttribute(AttributeKeys.STROKE_WIDTH, width+1);
		AffineTransform at = g.getTransform();
		if (f.isVisible()) {
			g.scale(view.getScaleFactor(), view.getScaleFactor());
			f.draw(g);
		}
		g.setTransform(at);
		f.setAttribute(AttributeKeys.STROKE_COLOR, colour);
		f.setAttribute(AttributeKeys.STROKE_WIDTH, width);
	}
	
}