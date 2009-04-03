
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
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import omeroCal.model.CalendarEvent;
import omeroCal.model.ICalendarModel;

import calendar.CalendarFile;


import ui.components.AlignedComponent;


public class MonthView 
	extends JPanel 
	implements Observer,
	IEventListener,
	ActionListener {

	/**
	 * A Model of the calendar
	 */
	ICalendarModel controller;
	
	/**
	 * A reference to the Year, Month and Day currently selected.
	 * eg used to switch between MonthView and WeekView, 
	 * while keeping same date highlighted
	 */
	GregorianCalendar currentDate;
	
	/**
	 * The first date displayed for the MonthView. This will usually be a few days before the 
	 * start of the current month
	 */
	GregorianCalendar firstDisplayDate;
	
	/**
	 * The last date displayed for the MonthView. This will usually be a few days after the end 
	 * of the current month.
	 */
	GregorianCalendar lastDisplayDate;
	
	/**
	 * The current moment in time. Used to highlight "Today" in the month view.
	 */
	GregorianCalendar now = new GregorianCalendar();
	
	/**
	 * Each week starts on Monday!
	 */ 
	public static final int FIRST_DAY_OF_WEEK = GregorianCalendar.MONDAY;
	
	/**
	 * A label at the top, displaying month and year, eg "March 2008"
	 */
	JLabel monthYearLabel;
	
	/**
	 * A list of all the calendarEvents displayed by this class.
	 */
	ArrayList<EventLabel> eventsDisplayed;
	
	/**
	 * An array of the days this month.
	 */
	IDayDisplay[] days;
	
	/**
	 * This is the number of days from the end of last month, that are displayed
	 * in the first week of this month. 
	 */
	int daysRemainingLastMonth;
	
	/**
	 * The number of days in the currently displayed month.
	 */
	int daysThisMonth;
	/**
	 * The grid that displays the days
	 */
	JPanel daysGridPanel;
	
	/**
	 * An action command: Change display to next month.
	 */
	public static final String NEXT_MONTH_CMD = "nextMonth";
	
	/**
	 * An action command: Change display to previous month.
	 */
	public static final String PREV_MONTH_CMD = "prevMonth";
	
	/**
	 * The day renderer. Creates new IDayDisplay components. 
	 */
	private DayRenderer dayRenderer;
	
	/**
	 * The UI component that displays the Month name and has buttons
	 * for browsing next/previous month
	 */
	private MonthViewHeader monthHeader;
	
	/**
	 * A bound property of this class.
	 * Property change fired when a day is clicked.
	 */
	public static final String DAY_CLICKED_PROPERTY = "dayClickedProperty";
	
	/**
	 * Creates a new instance of MonthView, with the month and year set to current time.
	 */
	public MonthView() {
		
		this(null, new DayRenderer(DayRenderer.DAY_PANEL), null);
	}
	
	public MonthView(ICalendarModel controller) {
		this (controller, new DayRenderer(DayRenderer.DAY_PANEL),
				new MonthViewHeader());
	}
	
	/**
	 * Creates a new instance of the Month view. 
	 * 
	 * @param controller		For querying the model	
	 * @param dayRenderer		Defines how a day is rendered. 
	 * @param monthHeader		A component to display Month, Year etc.
	 */
	public MonthView(ICalendarModel controller, 
			DayRenderer dayRenderer,
			MonthViewHeader monthHeader) {
		
		
		if (controller instanceof Observable) {
			((Observable)controller).addObserver(this);
		}
		if (controller instanceof Controller) {
			((Controller)controller).addEventListener(this);
		}
		
		this.dayRenderer = dayRenderer;
		this.monthHeader = monthHeader;
		this.controller = controller;
		
		buildUI();	// sets thisMonth to now
		
		addCalendarEvents();
		refreshHeader();
	}
	
	
	/**
	 * Build the MonthView UI.
	 * If the date of thisMonth has not been initialized, it is set to new GregorianCalendar().
	 * This is the current time!
	 * The Month is laid out in a grid of days, with 7 columns (one week) per row.
	 */
	public void buildUI() {
		
		this.setLayout(new BorderLayout());
		
		if (currentDate == null)
			currentDate = new GregorianCalendar();
		
		
		// GridLayout to hold the days, one week = 7 columns
		daysGridPanel = new JPanel(new GridLayout(0, 7));
		
		// add the days to the grid 
		addDaysToGrid();
		
		this.add(daysGridPanel, BorderLayout.CENTER);
		
		this.add(monthHeader, BorderLayout.NORTH);
	}
	
	
	public void addDaysToGrid() {
		
		// if refreshing, need to remove all old days from grid
		daysGridPanel.removeAll();
		
		// Initialize display dates (if null)
		if (firstDisplayDate == null)
			firstDisplayDate = new GregorianCalendar();
		if (lastDisplayDate == null)
			lastDisplayDate = new GregorianCalendar();
		
		
		// Get the start of first day of the month
		firstDisplayDate.setTime(currentDate.getTime());
		firstDisplayDate.set(Calendar.DAY_OF_MONTH, 1);
		firstDisplayDate.set(Calendar.HOUR_OF_DAY, 0);
		firstDisplayDate.set(Calendar.MINUTE, 0);
		firstDisplayDate.set(Calendar.SECOND, 0);
		firstDisplayDate.set(Calendar.MILLISECOND, 0);
		int firstDayOfMonth = firstDisplayDate.get(Calendar.DAY_OF_WEEK);
		// Calculate the number of days to display from the previous month
		daysRemainingLastMonth = firstDayOfMonth - FIRST_DAY_OF_WEEK;
		if (daysRemainingLastMonth < 0)
			daysRemainingLastMonth = daysRemainingLastMonth + 7;
		// now move the first display date to include these last days of the previous month
		firstDisplayDate.add(Calendar.DAY_OF_MONTH, daysRemainingLastMonth * -1);
		
		
		// now do the last day of the month
		lastDisplayDate.setTime(currentDate.getTime());
		daysThisMonth = lastDisplayDate.getActualMaximum(Calendar.DAY_OF_MONTH);
		lastDisplayDate.set(Calendar.DAY_OF_MONTH, daysThisMonth);
		lastDisplayDate.set(Calendar.HOUR_OF_DAY, 23);
		lastDisplayDate.set(Calendar.MINUTE, 59);
		lastDisplayDate.set(Calendar.SECOND, 59);
		// what day of the week is the last day of the month?
		int lastDayOfMonth = lastDisplayDate.get(Calendar.DAY_OF_WEEK);
		// calculate number of days to display from start of next month
		int daysFromStartOfNextMonth = FIRST_DAY_OF_WEEK + 6 - lastDayOfMonth;
			if (daysFromStartOfNextMonth > 6)
				daysFromStartOfNextMonth = daysFromStartOfNextMonth - 7;
		lastDisplayDate.add(Calendar.DAY_OF_MONTH, daysFromStartOfNextMonth);
		
		
		int totalDaysDisplayed = daysRemainingLastMonth + daysThisMonth +
			daysFromStartOfNextMonth;
			

		
		// create an array to hold the days of the month, for easy reference
	//	int daysThisMonth = currentDate.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
		days = new IDayDisplay[totalDaysDisplayed + 1];
		
		int index;	// keep track of days added to grid
		
		// Add the days from last month to the grid 
		Calendar newDayDate = new GregorianCalendar();
		newDayDate.setTime(firstDisplayDate.getTime());
		
		index=0;
		while (newDayDate.compareTo(lastDisplayDate) < 0) {
			days[index] = dayRenderer.getDayComponent(newDayDate);
			if (newDayDate.get(Calendar.MONTH) != currentDate.get(Calendar.MONTH)) {
				days[index].setDayFromOtherMonth(true);
			}
			addDayToGrid((JComponent)days[index]);
			newDayDate.add(Calendar.DAY_OF_MONTH, 1);
			index++;
		}
		
		
		// if the current month is being displayed, highlight Today
		if ((now.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)) && 
				(now.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH))) {
			int today = now.get(Calendar.DAY_OF_MONTH);
			days[today + daysRemainingLastMonth - 1].setToday(true);
		}
		
		// required if the view has been refreshed
		daysGridPanel.validate();
		daysGridPanel.repaint();
	}
	
	private void addDayToGrid(JComponent day) {
		day.addMouseListener(new DayClickListener());
		daysGridPanel.add(day);
	}
	
	public void refreshHeader() {
		monthHeader.refreshHeader(currentDate);
	}
	
	/**
	 * Display a CalendarFile on the month
	 * @param calFile
	 */
	public void addCalendarFile(CalendarFile calFile) {
		
		List<CalendarEvent> events = calFile.getEvents();
		
		for (CalendarEvent evt: events) {
			addCalendarEvent(evt);
		}
	}
	
	
	public void addCalendarEvents() {
		 
		if (eventsDisplayed == null) {
			eventsDisplayed = new ArrayList<EventLabel>();
		}
		
		eventsDisplayed.clear();
		
		List <CalendarEvent> events = controller.getEventsForDates(firstDisplayDate, lastDisplayDate);
		
		for (CalendarEvent evt: events) {
			addCalendarEvent(evt);
		}
		
		// required if the view has been refreshed
		daysGridPanel.validate();
		daysGridPanel.repaint();
	}
	
	
	/**
	 * Adds an event to the calendar. This is only displayed if it falls on a day of this month.
	 * 
	 * @param evt
	 */
	public void addCalendarEvent(CalendarEvent evt) {
		Calendar eventDateTime = evt.getStartCalendar();

		EventLabel eventLabel = new EventLabel(evt);
		if (controller instanceof IEventListener) {
			eventLabel.setEventController((IEventListener)controller);
		}
		
		int thisMonth = currentDate.get(Calendar.MONTH);
		int eventMonth = eventDateTime.get(Calendar.MONTH);
		// eg dayOfMonth = 1st. Add to array index 0;
		int dayOfMonth = eventDateTime.get(Calendar.DAY_OF_MONTH) - 1;
		
		/* if in the current month, need to add to the array of 
		 * days[displayIndex]
		 */
		if (eventMonth == thisMonth) {
			// System.out.println("Adding event to this month");
			int displayIndex = dayOfMonth + daysRemainingLastMonth;
			days[displayIndex].addEvent(eventLabel);
			
		} else 
			// if in the previous month...
		if (eventMonth == thisMonth - 1) {
			int dateOfFirstDisplayDate = firstDisplayDate.get(Calendar.DAY_OF_MONTH);
			//System.out.println("Adding event to last month. dayOfMonth = " + dayOfMonth +
			//		" dateOfFirstDisplayDate = " + dateOfFirstDisplayDate);
			int displayIndex = dayOfMonth - dateOfFirstDisplayDate + 1;
			
			if (displayIndex >= 0)
				days[displayIndex].addEvent(eventLabel);
			
		} else 
			// if event is in next month...
			
		if (eventMonth == thisMonth + 1) {
			// System.out.println("Adding event to next month");
			int displayIndex = dayOfMonth + daysRemainingLastMonth + daysThisMonth;
			days[displayIndex].addEvent(eventLabel);
			
		} else {
			System.out.println("EVENT NOT DISPLAYED!!!");
		}
		
		// this list is used to notify eventLabels that eg selection has changed.
		eventsDisplayed.add(eventLabel);
	}
	
	/**
	 * Sets the current date. 
	 * The month view will switch to the month that contains this date. 
	 * 
	 * @param date	The new date. 
	 */
	public void setDate(Date date) {
		currentDate.setTime(date);
		
		addDaysToGrid();
		addCalendarEvents();
		
		refreshHeader();
	}
	
	/**
	 * Increments the currently displayed month. 
	 * 
	 * @param increment		The number of months to increment
	 */
	public void incrementMonth(int increment) {
		
		currentDate.add(Calendar.MONTH, increment);
		
		addDaysToGrid();
		addCalendarEvents();
		
		refreshHeader();
	}
	
	
	public void calendarEventChanged(CalendarEvent calendarEvent, String propertyChanged, Object newProperty) {
		
		int calendarID = calendarEvent.getCalendarID();
		
		// System.out.println("Controller calendarEventChanged() ID: " + calendarID + " " + propertyChanged + " " + newProperty);
	
		
		if (propertyChanged.equals(EventLabel.SELECTION_PROPERTY)) {
			for (EventLabel eventLabel: eventsDisplayed) {
				eventLabel.calendarSelected(calendarEvent.getCalendarID(), Boolean.parseBoolean(newProperty.toString()));
			}
		}
	}
	
	/**
	 * The data has changed, so need to refresh view
	 */
	public void update(Observable o, Object arg) {
		
		/*
		 * Re-populate the month grid with new days (removes existing events from view)
		 */
		addDaysToGrid();
		
		/*
		 * Get new events from database and add them to days on the grid. 
		 */
		addCalendarEvents();
	}

	/**
	 * Respond to requests to change month etc. 
	 */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		if (NEXT_MONTH_CMD.equals(command)) {
			incrementMonth(1);
		} else if (PREV_MONTH_CMD.equals(command)) {
			incrementMonth(-1);
		}
	}

	/**
	 * A MouseListener for Days.
	 * 
	 * @author will
	 *
	 */
	public class DayClickListener implements MouseListener {
		public void mouseClicked(MouseEvent e) {
			if (e.getSource() instanceof IDayDisplay) {
				System.out.println("MonthView DayClicked: " + 
						e.getSource().toString());
				/*
				 * Let propertyChangeListeners know that a day was clicked..
				 */
				MonthView.this.firePropertyChange(
					MonthView.DAY_CLICKED_PROPERTY,
					null, e.getSource());
			}
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		
	}
}
