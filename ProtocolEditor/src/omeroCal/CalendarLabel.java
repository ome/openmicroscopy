
/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

package omeroCal;

import java.awt.Font;

import javax.swing.JLabel;

public class CalendarLabel extends JLabel {

	
	public CalendarLabel(String text) {
	
		this(text, 10);
	}
	
	public CalendarLabel(String text, int size) {
		super(text);
		
		Font calendarFont = new Font("SansSerif", Font.PLAIN, size);

		this.setFont(calendarFont);
	}
	
}
