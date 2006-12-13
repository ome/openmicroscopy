/*
 * org.openmicroscopy.shoola.util.ui.ColorCheckBoxMenuItem 
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;

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
public class ColorCheckBoxMenuItem 
	extends JCheckBoxMenuItem
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
	public ColorCheckBoxMenuItem(Color c)
	{
		setColor(c);
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
