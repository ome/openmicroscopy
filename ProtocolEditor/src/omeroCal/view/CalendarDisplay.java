
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

import java.awt.BorderLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JPanel;

import omeroCal.model.CalendarEvent;
import omeroCal.model.CalendarObject;
import omeroCal.model.ICalendarModel;

public class CalendarDisplay
	extends JPanel 
	implements Observer,
	IEventListener {
	
	/**
	 * A Model of this month.
	 */
	ICalendarModel controller;
	
	/**
	 * The view that goes in the Center. 
	 * The default is MonthView, but could also be WeekView or DayView etc.
	 */
	JPanel centreComponent;
	
	/**
	 * The view that goes to the east.
	 * The default is EventInfoPanel
	 */
	JPanel infoPanelEast;
	
	
	public CalendarDisplay(ICalendarModel controller, boolean mainApplicationWindow) {
		
		this.controller = controller;
		
		if (controller instanceof Observable) {
			((Observable)controller).addObserver(this);
		}
		if (controller instanceof Controller) {
			((Controller)controller).addEventListener(this);
		}
		
		this.setLayout(new BorderLayout());
		
		centreComponent = new MonthView(controller);
		this.add(centreComponent, BorderLayout.CENTER);
		
		infoPanelEast = new EventInfoPanel(null);
		this.add(infoPanelEast, BorderLayout.EAST);
		
		
		JFrame frame = new JFrame("OMERO Calendar");
		frame.getContentPane().add(this);
		
		if (mainApplicationWindow) {
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		
		frame.pack();
		frame.setVisible(true);
		
	}

	
	public void calendarEventChanged(CalendarEvent calendarEvent, String propertyChanged, Object newProperty) {
		
		int calendarID = calendarEvent.getCalendarID();
		
		//System.out.println("CalendarFrame calendarEventChanged() ID: " + calendarID + " " + propertyChanged + " " + newProperty);
	
		this.remove(infoPanelEast);
		infoPanelEast = new EventInfoPanel(calendarEvent);
		
		CalendarObject calendar = controller.getCalendarForEvent(calendarEvent);
		((EventInfoPanel)infoPanelEast).setCalendar(calendar);
		
		this.add(infoPanelEast, BorderLayout.EAST);
		
		infoPanelEast.validate();
		infoPanelEast.repaint();
	}

	
	/**
	 * The data has changed, so need to refresh view
	 */
	public void update(Observable o, Object arg) {
		
		// need to tell UI Components that DB has updated. 
		if (centreComponent instanceof Observer) {
			((Observer)centreComponent).update(o, arg);
		}
		
	}

}
