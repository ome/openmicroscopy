
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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.text.DateFormat;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import omeroCal.model.CalendarEvent;


public class EventLabel 
	extends JButton {
	
	CalendarEvent calendarEvent;
	
	/**
	 * This controller should be notified of changes to this label (eg clicked or double-clicked)
	 * so that the UI can be updated appropriately.
	 */
	IEventListener eventController;
	
	Color backgroundColor = null;
	
	Color foregroundColor;
	
	public static final String SELECTION_PROPERTY = "selectionProperty";
	
	public static final String DOUBLE_CLICKED = "doubleClicked";
	
	public EventLabel (CalendarEvent event) {
		
		
		calendarEvent = event;
		
		setText("<html>" + event.getName() + "</html>");
		
		setHorizontalAlignment(SwingConstants.LEFT); 

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
		this.addMouseListener(new EventMouseAdapter());
	}

	public void update(Observable o, Object arg) {
		
		System.out.println("EventLabel update() " + calendarEvent.getName());
		
	}
	
	
	public class EventMouseAdapter extends MouseAdapter {
		
		/** 
		 * Brings up a dialog box indicating to download 
		 * the file associated to the component.
		 */
		public void mouseReleased(MouseEvent e) {
			Object src = e.getSource();
			
			// double-click
			if (e.getClickCount() == 2) {
				
				if (eventController != null)
					eventController.calendarEventChanged(calendarEvent, DOUBLE_CLICKED, true);
			}
			
			// right-click
			else if (e.isPopupTrigger()) {
				
				//System.out.println("EventLabel rightClick");
			}
		}
		
		/** 
		 * Sets the selected label and shows the menu.
		 */
		public void mousePressed(MouseEvent e) {
			Object src = e.getSource();
			if (e.isPopupTrigger()) {
				
				//System.out.println("EventLabel rightClick");
			}
		}
	
		/**
		 * Modifies the cursor when entered.
		 */
		public void mouseEntered(MouseEvent e) {
			Object src = e.getSource();
			
		//	System.out.println("EventLabel mouseEntered");
			
		}
	
	}
	
	
	public class FocusGainedListener implements FocusListener {
		public void focusGained(FocusEvent e) {
			setSelected(true);
		}

		public void focusLost(FocusEvent e) {
			setSelected(false);
		}
		
	}
	
	/** 
	 * Called when the calendar, indicated by calendarID, or an event from that calendar, 
	 * is selected / deselected
	 * 
	 * @param calendarID
	 */
	public void calendarSelected(int calendarID, boolean selected) {
		
		//System.out.println("EventLabel calendarSelected() " + selected + " calID: " + calendarID );
		
		// if this label's calendarID matches the calendar that has been selected...
		if (calendarID == calendarEvent.getCalendarID()) {
			
			setBackground(selected ? lighten(foregroundColor) : backgroundColor);
		}
		
	}
	
	public void setSelected(boolean selected) {
		
		if (eventController != null)
			eventController.calendarEventChanged(calendarEvent, SELECTION_PROPERTY, selected);
		
		setBackground(selected ? foregroundColor : backgroundColor);
		setForeground(selected ? Color.WHITE : foregroundColor);
		
	}
	
	/**
	 * Set a controller, to listen for changes to this label etc.
	 * 
	 * @param eventController	The new eventController
	 */
	public void setEventController(IEventListener eventController) {
		this.eventController = eventController;
	}
	
	public static Color lighten(Color colour) {
		
		double factor = 1.25;
		
		int r = colour.getRed();
		int g = colour.getGreen();
		int b = colour.getBlue();
		
		r = (int)Math.floor(r + (256-r)/factor);
		g = (int)Math.floor(g + (256-g)/factor);
		b = (int)Math.floor(b + (256-b)/factor);
		
		return new Color(r,g,b);
	}

}
