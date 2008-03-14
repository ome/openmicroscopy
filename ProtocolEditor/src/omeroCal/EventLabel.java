
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
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.text.DateFormat;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.event.ChangeEvent;


public class EventLabel 
	extends JButton 
	implements Observer {
	
	CalendarEvent calendarEvent;
	
	Color backgroundColor = null;
	
	Color foregroundColor;
	
	public EventLabel (CalendarEvent event) {
		
		if(event instanceof Observable) {
			((Observable)event).addObserver(this);
		}
		
		calendarEvent = event;
		
		setText(event.getName());
		
		foregroundColor = event.getCalendarColour();
		
		String time = DateFormat.getTimeInstance().format(event.getStartTime());
		setToolTipText(time);
		
		Font calendarFont = new Font("SansSerif", Font.PLAIN, 10);
		this.setFont(calendarFont);
		this.setBackground(backgroundColor);
		this.setForeground(foregroundColor);
		this.setBorder(null);
		this.setFocusable(true);
		this.addFocusListener(new FocusGainedListener());
	}

	public void update(Observable o, Object arg) {
		
		System.out.println("EventLabel update() " + calendarEvent.getName());
		
	}
	
	public class FocusGainedListener implements FocusListener {
		public void focusGained(FocusEvent e) {
			setSelected(true);
		}

		public void focusLost(FocusEvent e) {
			setSelected(false);
		}
		
	}
	
	public void setSelected(boolean selected) {
		boolean oldValue = isSelected();
		
		super.setSelected(selected);
		
		setBackground(selected ? foregroundColor : backgroundColor);
		setForeground(selected ? Color.WHITE : foregroundColor);
		
		PropertyChangeEvent selectionChanged = new PropertyChangeEvent(this, "selected", oldValue, selected);
		
		calendarEvent.selectionChanged(selectionChanged);
	}

}
