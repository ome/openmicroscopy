
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

package omeroCal.view;

import java.awt.Color;
import java.awt.Font;

import org.jdesktop.swingx.JXDatePicker;

public class DatePicker extends JXDatePicker {

	/** The selected date format. */
	private static final String		DATE_FORMAT = "dd/MMM/yyyy";//"MM/dd/yy";
	
	/** The tooltip of the calendar button. */
	private static final String		DATE_TOOLTIP = "Bring up a calendar.";

	
	public static Font timeFont = new Font("SansSerif", Font.PLAIN, 10);
	
	/**
	 * Creates a date picker.
	 */
	public DatePicker() {
		
		String[] dateFormats = new String[1];
		dateFormats[0] = DATE_FORMAT;
		getEditor().setEditable(false);
		getEditor().setFont(timeFont);
		// setEditable(false);
		setFormats(dateFormats);

		getEditor().setBackground(Color.WHITE);
	}

}
