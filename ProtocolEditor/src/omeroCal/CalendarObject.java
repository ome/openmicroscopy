
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

import java.awt.Color;

public class CalendarObject {

	
	
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
	
	/**
	 * Creates a new instance of CalendarObject
	 * 
	 * @param calendarName	Name of the Calendar, eg "Home" or "Work"
	 * @param calendarInfo	A short description
	 */
	public CalendarObject() {
		
		// create a random color
		int red = (int)Math.floor(Math.random() * 256);
		int green = (int)Math.floor(Math.random() * 256);
		int blue = (int)Math.floor(Math.random() * 256);
		calendarColour = new Color(red, green, blue);
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

}
