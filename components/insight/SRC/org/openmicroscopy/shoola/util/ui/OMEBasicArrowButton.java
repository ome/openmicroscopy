/*
 * org.openmicroscopy.shoola.util.ui.OMEBasicArrowButton 
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
package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.plaf.basic.BasicArrowButton;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class OMEBasicArrowButton 
	extends BasicArrowButton
{
	
	/**
	 * Creates a new instance.
	 * 
	 * @param direction The direction of the arrow. One of the constants defined
	 *                  by the BasicArrowButton class.
	 */
	public OMEBasicArrowButton(int direction)
	{
		super(direction);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param direction  The direction of the arrow. One of the constants 
	 *                   defined by the BasicArrowButton class.
	 * @param background The background color.
	 * @param shadow     The shadow color.
	 * @param darkShadow The darker version of the shadow color.
	 * @param highlight  The highlight color.
	 */
	public OMEBasicArrowButton(int direction, Color background, 
			Color shadow, Color darkShadow, Color highlight)
	{
		super(direction, background, shadow, darkShadow, highlight);
	}
	
	/** 
	 * Overridden to remove the border and paint a double arrow.
	 * @see BasicArrowButton#paint(Graphics)
	 */
	public void paint(Graphics g)
	{
		Color origColor;
		boolean isPressed, isEnabled;
		int size;

		Dimension d = getPreferredSize();
		int w = d.width;
		int h = d.height;
		origColor = g.getColor();
		isPressed = getModel().isPressed();
		isEnabled = isEnabled();

		g.setColor(getBackground());
		g.fillRect(1, 1, w-2, h-2);

		// If there's no room to draw arrow, bail
		d = getMinimumSize();
		if (h < d.width || w < d.height) {
			g.setColor(origColor);
			return;
		}

		if (isPressed) g.translate(1, 1);
		size = Math.min((h-4)/2, (w-4)/2);
		size = Math.max(size, 2);
		size = size/2;
		int middle = h/4;
		if (direction == SOUTH) {
			paintTriangle(g, (w-size)/2, middle+(h-size)/2-1, size, direction, 
							isEnabled);
			paintTriangle(g, (w-size)/2, (h-size)/2-1, size, NORTH, isEnabled);
		}
	}
	
}
