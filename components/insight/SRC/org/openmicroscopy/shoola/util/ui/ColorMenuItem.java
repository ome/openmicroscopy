/*
 * org.openmicroscopy.shoola.util.ui.ColorMenuItem 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.openmicroscopy.shoola.util.ui.colourpicker.LookupTableIconUtil;

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
	
	/** The minimum width and height of the icon. */
	private static final int	MINIMUM = 4;
	
	/** The color hosted by this component. */
	private Color	color;
	
	/** THe width of the icon, by default value set to {@link #ICON_WIDTH}. */
	private int		iconWidth = ICON_WIDTH;
	
	/** THe height of the icon, by default value set to {@link #ICON_HEIGHT}. */
	private int		iconHeight = ICON_HEIGHT;
	
	/**
	 * Creates the color icon.
	 * 
	 * @return See above.
	 */
	private ImageIcon createIcon()
	{
		BufferedImage img = new BufferedImage(iconWidth, iconHeight, 
									BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, iconWidth, iconHeight);
		g.setColor(color.darker());
		g.drawRect(0, 0, iconWidth, iconHeight);
		return new ImageIcon(img);
	}
	
	/**
	 * Creates a new instance. 
	 * By default an icon of size <code>16x16</code> is created.
	 * 
	 * @param c The color hosted by the component. Mustn't be <code>null</code>.
	 */
	public ColorMenuItem(Color c)
	{
		setColor(c);
		iconWidth = ICON_WIDTH;
		iconHeight = ICON_HEIGHT;
	}
	
	/**
	 * Creates a new instance.  
	 * By default an icon of size <code>16x16</code> is created.
	 * 
	 * @param text 	The text of the menu item.
	 * @param c 	The color hosted by the component. 
	 * 				Mustn't be <code>null</code>.
	 */
	public ColorMenuItem(String text, Color c)
	{
		setColor(c);
		setText(text);
		iconWidth = ICON_WIDTH;
		iconHeight = ICON_HEIGHT;
	}
	
    /**
     * Creates a new instance. By default an icon of size <code>16x16</code> is
     * created.
     * 
     * @param text
     *            The text of the menu item.
     * @param lut
     *            The lookup table hosted by the component. Mustn't be
     *            <code>null</code>.
     */
    public ColorMenuItem(String text, String lut) {
        setLookupTable(lut);
        setText(text);
        iconWidth = ICON_WIDTH;
        iconHeight = ICON_HEIGHT;
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
     * Sets the lookup table hosted by the component. Mustn't be
     * <code>null</code>.
     * 
     * @param lut
     *            The lookup table to set.
     */
    public void setLookupTable(String lut) {
        setIcon(LookupTableIconUtil.getLUTIcon(lut, new Dimension(ICON_WIDTH,
                ICON_HEIGHT)));
    }
	
	/**
	 * Returns the color hosted by the component.
	 * 
	 * @return See above.
	 */
	public Color getColor() { return color; }
	
	/**
	 * Sets the width of the icon.
	 * 
	 * @param w The value to set. Must be greater than <code>MINIMUM</code>
	 */
	public void setIconWidth(int w)
	{
		if (w > MINIMUM) iconWidth = w;
	}
	
	/**
	 * Sets the height of the icon.
	 * 
	 * @param h The value to set. Must be greater than <code>MINIMUM</code>
	 */
	public void setIconHeight(int h)
	{
		if (h > MINIMUM) iconHeight = h;
	}
	
	/**
	 * Returns the width of the icon.
	 * 
	 * @return See above.
	 */
	public int getIconWidth() { return iconWidth; }
	
	/**
	 * Returns the height of the icon.
	 * 
	 * @return See above.
	 */
	public int getIconHeight() { return iconHeight; }
	
}
