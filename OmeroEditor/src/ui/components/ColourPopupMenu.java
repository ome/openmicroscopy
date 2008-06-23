
/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */


package ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class ColourPopupMenu extends JPopupMenu {

	public ColourPopupMenu(Color[] colours, ActionListener colourListener) {
		
		JMenuItem noColourItem = new JMenuItem("No colour");
		noColourItem.setBackground(null);
		noColourItem.addActionListener(colourListener);
		this.add(noColourItem);
		
		for (int i=0; i<colours.length; i++) {
			
			JMenuItem menuItem = new ColourMenuItem(" ");
			menuItem.setBackground(colours[i]);
			menuItem.setPreferredSize(new Dimension(40,20));
			menuItem.addActionListener(colourListener);
			this.add(menuItem);
			
		}
	}
}
