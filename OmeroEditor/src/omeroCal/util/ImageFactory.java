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

package omeroCal.util;

import javax.swing.Icon;
import javax.swing.ImageIcon;

// for returning icons.
// Icons come from Nuvola. http://icon-king.com/?p=15

public class ImageFactory {
	
	// singleton
	private static ImageFactory uniqueInstance = new ImageFactory();
	// private constructor
	private ImageFactory() {};
	// return uniqueInstance
	public static ImageFactory getInstance() {
		return uniqueInstance;
	}
	
	public static final String ICONS_FILE = "../graphx/";
	
	public static final String ALARM_GIF_64 = ICONS_FILE + "kalarmAnimated64.gif";
	public static final String NEXT_ICON = ICONS_FILE + "next.png";
	public static final String PREV_ICON = ICONS_FILE + "previous.png";
	
	
	public Icon getIcon(String iconPathName) {
		try {
			return new ImageIcon(ImageFactory.class.getResource(iconPathName));
		} catch (NullPointerException ex) {
			System.out.println("Could not find Icon at " + iconPathName);
			return null;
		}
	}

	public ImageIcon getImageIcon(String iconPathName) {
		return (ImageIcon)getIcon(iconPathName);
	}
}

