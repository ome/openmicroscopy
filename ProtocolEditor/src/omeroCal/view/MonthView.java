
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
	IEventListener {

	/**
	 * A Model of the calendar
	 */
	ICalendarModel controller;
	
	/**
	 * A reference to the Year, Month and Day currently selected.
	 * eg used to switch between MonthView and WeekView, which keeping same date highlighted
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
	DayOfMonth[] days;
	
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
	 * Creates a new instance of MonthView, with the month and year set to current time.
	 */
	public MonthView() {
		
		buildUI();	// sets thisMonth to now
	}
	
	/**
	 * Creates a new instance of MonthView, with the month and year set to date.
	 * @param date	The date (month and year) that this class will represent.
	 */
	public MonthView(Date date) {
		
		this();
		
		currentDate = new GregorianCalendar();
		currentDate.setTime(date);
		
	}
	
	public MonthView(ICalendarModel controller) {
		
		this();
		
		if (controller instanceof Observable) {
			((Observable)controller).addObserver(this);
		}
		if (controller instanceof Controller) {
			((Controller)controller).addEventListener(this);
		}
		
		this.controller = controller;
		
		addCalendarEvents();
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
		
		
		
		JPanel daysOfWeekHeader = new JPanel(new GridLayout(0, 7));
		int headerFontSize = 13;
		daysOfWeekHeader.add(new AlignedComponent(new CalendarLabel("Monday", headerFontSize)));
		daysOfWeekHeader.add(new AlignedComponent(new CalendarLabel("Tuesday", headerFontSize)));
		daysOfWeekHeader.add(new AlignedComponent(new CalendarLabel("Wednesday", headerFontSize)));
		daysOfWeekHeader.add(new AlignedComponent(new CalendarLabel("Thursday", headerFontSize)));
		daysOfWeekHeader.add(new AlignedComponent(new CalendarLabel("Friday", headerFontSize)));
		daysOfWeekHeader.add(new AlignedComponent(new CalendarLabel("Saturday", headerFontSize)));
		daysOfWeekHeader.add(new AlignedComponent(new CalendarLabel("Sunday", headerFontSize)));
		
		
		Box headerBox = Box.createVerticalBox();
		
		Box titleButtonsBox = Box.createHorizontalBox();
		
		JButton prevMonthButton = new JButton("<");
		prevMonthButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				incrementMonth(-1);
			}
		});
		JButton nextMonthButton = new JButton(">");
		nextMonthButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				incrementMonth(1);
			}
		});
		
		monthYearLabel = new CalendarLabel("", 14);
		refreshHeader();
		
		titleButtonsBox.add(prevMonthButton);
		titleButtonsBox.add(monthYearLabel);
		titleButtonsBox.add(nextMonthButton);
		
		headerBox.add(new AlignedComponent(titleButtonsBox, 5));
		headerBox.add(daysOfWeekHeader);
		
		this.add(headerBox, BorderLayout.NORTH);
	}
	
	
	public void addDaysToGrid() {
		
		// if refreshing, need to remove all old days from grid
		daysGridPanel.removeAll();
		
		// Initialize display dates (if null)
		if (firstDisplayDate == null)
			firstDisplayDate = new GregorianCalendar();
		if (lastDisplayDate == null)
			lastDisplayDate = new GregorianCalendar();
		
		
		// Get the first day of the month
		firstDisplayDate.setTime(currentDate.getTime());
		firstDisplayDate.set(Calendar.DAY_OF_MONTH, 1);
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
		// what day of the week is the last day of the month?
		int lastDayOfMonth = lastDisplayDate.get(Calendar.DAY_OF_WEEK);
		// calculate number of days to display from start of next month
		int daysFromStartOfNextMonth = FIRST_DAY_OF_WEEK + 6 - lastDayOfMonth;
			if (daysFromStartOfNextMonth > 6)
				daysFromStartOfNextMonth = daysFromStartOfNextMonth - 7;
		lastDisplayDate.add(Calendar.DAY_OF_MONTH, daysFromStartOfNextMonth);
		
		
		int totalDaysDisplayed = daysRemainingLastMonth + daysThisMonth + daysFromStartOfNextMonth;
		System.out.println("MonthView addDaysToGrid  totalDaysDisplayed = " + totalDaysDisplayed);
		System.out.println("MonthView addDaysToGrid  daysThisMonth = " + daysThisMonth);
		System.out.println("MonthView addDaysToGrid  daysFromStartOfNextMonth = " + daysFromStartOfNextMonth);
		

		
		// create an array to hold the days of the month, for easy reference
	//	int daysThisMonth = currentDate.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
		days = new DayOfMonth[totalDaysDisplayed + 1];
		
		int index;	// keep track of days added to grid
		
		// Add the days from last month to the grid
		int dateOfFirstDisplayDate = firstDisplayDate.get(Calendar.DAY_OF_MONTH);
		for (index=0; index <daysRemainingLastMonth; index++) {
			days[index] = new DayOfMonth(dateOfFirstDisplayDate);
			days[index].setDayFromOtherMonth(true);
			daysGridPanel.add(days[index]);
			dateOfFirstDisplayDate++;
		}
		
		// Add the days of this month to the grid.
		int day = 1;
		for (; index < daysThisMonth+daysRemainingLastMonth; index++) {
			days[index] = new DayOfMonth(day);
			daysGridPanel.add(days[index]);
			day++;
		}
		
		// Add the days of next month to the grid
		day = 1;
		for (; index < totalDaysDisplayed; index++) {
			days[index] = new DayOfMonth(day);
			days[index].setDayFromOtherMonth(true);
			daysGridPanel.add(days[index]);
			day++;
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
	
	public void refreshHeader() {
		SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMMMM yyyy");
		String monthYear = monthYearFormat.format(currentDate.getTime());
		monthYearLabel.setText(monthYear);
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
			
			int displayIndex = dayOfMonth + daysRemainingLastMonth;
			days[displayIndex].addEventLabel(eventLabel);
			
		} else 
			// if in the previous month...
		if (eventMonth == thisMonth - 1) {
			
			days[dayOfMonth].addEventLabel(eventLabel);
			
		} else 
			// if event is in next month...
			
		if (eventMonth == thisMonth + 1) {
			
			int displayIndex = dayOfMonth + daysRemainingLastMonth + daysThisMonth;
			days[displayIndex].addEventLabel(eventLabel);
			
		}
		
		// this list is used to notify eventLabels that eg selection has changed.
		eventsDisplayed.add(eventLabel);
	}
	
	
	public void incrementMonth(int increment) {
		
		currentDate.add(Calendar.MONTH, increment);
		
		controller.incrementMonth(increment);
		
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
}
