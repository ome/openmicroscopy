/*
 * org.openmicroscopy.shoola.util.ui.ColorMenuItem 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

//Third-party libraries

//Application-internal dependencies

/** 
 * Creates a menu item displayed a color as an icon.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ColorMenuItem 	
	extends JMenuItem
{

	/** The width of the icon. */
	private static final int	ICON_WIDTH = 16;
	
	/** The height of the icon. */
	private static final int	ICON_HEIGHT = 16;
	
	/** The color hosted by this component. */
	private Color	color;
	
	/**
	 * Creates the color icon.
	 * 
	 * @return See above.
	 */
	private ImageIcon createIcon()
	{
		BufferedImage img = new BufferedImage(ICON_WIDTH, ICON_HEIGHT, 
									BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, ICON_WIDTH, ICON_HEIGHT);
		g.setColor(color.darker());
		g.drawRect(0, 0, ICON_WIDTH, ICON_HEIGHT);
		return new ImageIcon(img);
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param c The color hosted by the component. Mustn't be <code>null</code>.
	 */
	public ColorMenuItem(Color c)
	{
		setColor(c);
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param text 	The text of the menu item.
	 * @param c 	The color hosted by the component. 
	 * 				Mustn't be <code>null</code>.
	 */
	public ColorMenuItem(String text, Color c)
	{
		setColor(c);
		setText(text);
	}
	
	/**
	 * Sets the color hosted by the component. Mustn't be <code>null</code>.
	 * 
	 * @param c The value to set.
	 */
	public void setColor(Color c)
	{
		if (c == null) 
			throw new IllegalArgumentException("No color specified.");
		color = c;
		setIcon(createIcon());
	}
	
	/**
	 * Returns the color hosted by the component.
	 * 
	 * @return See above.
	 */
	public Color getColor() { return color; }
	
}
