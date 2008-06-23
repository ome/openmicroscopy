
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

package omeroCal.model;

import java.awt.Color;

public class CalendarObject {

	
	/**
	 * A unique ID to identify this object in the DB.
	 * This is only set when this object is return from the DB.
	 */
	private int calendarID;
	
	/**
	 * A name for display purposes
	 */
	private String calendarName;
	
	/**
	 * Additional info or description etc. 
	 */
	private String calendarInfo;
	
	/**
	 * Display color
	 */
	private Color calendarColour;
	
	/**
	 * Boolean to determine whether a calenar is displayed
	 */
	private boolean calendarVisible = true;
	
	
	public static final Color[] calColours = {
		new Color(230, 62,171),
		new Color(237, 55, 73),
		new Color(243,163,49),
		new Color(243, 202, 49),
		new Color(243, 119, 49),
		new Color(201, 238, 41),
		new Color(90, 227, 29),
		new Color(29, 179, 227),
		new Color(41, 29, 227),
		new Color(140, 29, 227)
	};
	
	private static int colourIndex = 0;
	
	/**
	 * Creates a new instance of CalendarObject
	 * 
	 * @param calendarName	Name of the Calendar, eg "Home" or "Work"
	 * @param calendarInfo	A short description
	 */
	public CalendarObject() {
		
		// create a random color
		/*
		int red = 0;
		int green = 0;
		int blue = 0;
		
		while ((red + blue + green) < 100 || (red + blue + green) > 500) {
			red = (int)Math.floor(Math.random() * 256);
			green = (int)Math.floor(Math.random() * 256);
			blue = (int)Math.floor(Math.random() * 256);
		}
		*/
				
		//int index = (int)Math.floor(Math.random() * calColours.length);
		
		colourIndex = (colourIndex+1) % calColours.length;
		calendarColour = calColours[colourIndex];
	}
	
	
	/**
	 * Creates a new instance of CalendarObject, with a name and info
	 * 
	 * @param calendarName	Name of the Calendar, eg "Home" or "Work"
	 * @param calendarInfo	A short description
	 */
	public CalendarObject(String calendarName, String calendarInfo) {
		this();
		this.calendarInfo = calendarInfo;
		this.calendarName = calendarName;
	}
	
	
	public void setColour(Color colour) {
		calendarColour = colour;
	}
	
	public void setColour(int colorRGB) {
		calendarColour = new Color(colorRGB);
	}
	
	/**
	 * Returns the color of the Calendar, using color.getRGB()
	 * @return	the color of the Calendar
	 */
	public int getColourInt() {
		return calendarColour.getRGB();
	}
	
	/**
	 * Returns the color of the Calendar
	 * @return	the color of the Calendar
	 */
	public Color getColour() {
		return calendarColour;
	}
	
	
	public void setVisibile(boolean visible) {
		calendarVisible = visible;
	}
	
	/**
	 * Used for display purposes
	 * @return	the boolean visible
	 */
	public boolean isVisible() {
		return calendarVisible;
	}
	
	public void setName(String name) {
		calendarName = name;
	}
	
	public String getName() {
		return calendarName;
	}
	
	public void setInfo(String info) {
		calendarInfo = info;
	}
	
	public String getInfo() {
		return calendarInfo;
	}
	
	public int getCalendarID() {
		return calendarID;
	}
	
	public void setCalendarID(int calID) {
		calendarID = calID;
	}

}
