/*
 * org.openmicroscopy.shoola.util.ui.ColorListRenderer 
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

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

/** 
 * ColourListRenderer will render the colour icons and colour names in the list
 * box.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @since 3.0-Beta4
 */
public class ColorListRenderer 	
	extends JLabel  
	implements ListCellRenderer  
{
	
	/** The gap between the icon and the text. */
	private static final int  GAP = 20;
	
	/** Create the icon which will hold the colours. */
	private static ColourIcon icon;
	
	/** Border colour of the cell when the icon is selected. */
	private Border lineBorder;
	
	/** Border colour of the cell when the icon is not selected. */
	private Border emptyBorder;

	/** Creates a new instance. Sets the background to opaque. */
	public ColorListRenderer()
	{
		setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
        setIconTextGap(GAP);
        icon = new ColourIcon(12, 12);
        lineBorder = BorderFactory.createLineBorder(Color.gray, 1);
        emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
    }
	
	/** 
     * Overridden method to set the color icon.
	 * @see ListCellRenderer#getListCellRendererComponent(JList, Object, int, 
     *                                                  boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean hasFocus) 
	{
		if (value == null) return this;
		Object [] array = (Object[]) value;
		if (array.length != 2) return this;
		Color c = (Color) array[0];
		if (c != null) 
			icon.setColour(new Color(c.getRed(), c.getGreen(), c.getBlue()));
		
		setIcon(icon);
		setText((String) array[1]);
		if (isSelected) {
			setForeground(list.getSelectionForeground());
			setBackground(list.getSelectionBackground());
		} else {
			setForeground(list.getForeground());
			setBackground(list.getBackground());
		}
		
		if (hasFocus) setBorder(lineBorder);
		else setBorder(emptyBorder);
		return this;
	}
	
}
