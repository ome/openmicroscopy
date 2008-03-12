
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

package calendar;

import java.awt.Color;
import java.awt.Font;
import java.text.DateFormat;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;

public class EventLabel 
	extends JButton 
	implements Observer {
	
	CalendarEvent calendarEvent;
	
	Color backgroundColor = Color.WHITE;
	
	public EventLabel (CalendarEvent event) {
		
		if(event instanceof Observable) {
			((Observable)event).addObserver(this);
		}
		
		calendarEvent = event;
		
		setText(event.getName());
		
		String time = DateFormat.getTimeInstance().format(event.getStartTime());
		setToolTipText(time);
		
		Font calendarFont = new Font("SansSerif", Font.PLAIN, 10);
		this.setFont(calendarFont);
		this.setBackground(backgroundColor);
		this.setBorder(null);
		this.setFocusable(true);
	}

	public void update(Observable o, Object arg) {
		
		
	}
	
	
	
	public void setSelected(boolean selected) {
		setBackground(selected ? Color.RED : backgroundColor);
		setForeground(selected ? Color.WHITE : Color.BLACK);
	}

}
