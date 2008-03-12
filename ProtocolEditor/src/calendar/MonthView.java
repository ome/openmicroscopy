
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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ui.components.CenteredComponent;


public class MonthView extends JPanel {

	/**
	 * A reference to the Year and Month represented by this class
	 */
	GregorianCalendar thisMonth;
	
	/**
	 * An array of the days this month.
	 */
	DayOfMonth[] days;
	
	
	JPanel daysGridPanel;
	
	/**
	 * Creates a new instance of MonthView, with the month and year set to current time.
	 */
	public MonthView() {
		
		buildUI();
	}
	
	/**
	 * Creates a new instance of MonthView, with the month and year set to date.
	 * @param date	The date (month and year) that this class will represent.
	 */
	public MonthView(Date date) {
		
		thisMonth = new GregorianCalendar();
		thisMonth.setTime(date);
		
		buildUI();
	}
	
	
	/**
	 * Build the MonthView UI.
	 * If the date of thisMonth has not been initialized, it is set to new GregorianCalendar().
	 * This is the current time!
	 * The Month is laid out in a grid of days, with 7 columns (one week) per row.
	 */
	public void buildUI() {
		this.setLayout(new BorderLayout());
		
		if (thisMonth == null)
			thisMonth = new GregorianCalendar();
		//thisMonth.set(GregorianCalendar.MONTH, GregorianCalendar.SEPTEMBER);
		
		// set the calendar to the first day of the month
		thisMonth.set(GregorianCalendar.DAY_OF_MONTH, 1);

		
		// Each week starts on Monday! 
		thisMonth.setFirstDayOfWeek(GregorianCalendar.MONDAY);
		int firstDayOfWeek = thisMonth.getFirstDayOfWeek();
		
		// Get the first day of the month
		int firstDayOfMonth = thisMonth.get(GregorianCalendar.DAY_OF_WEEK);
		// Calculate the number of days to ignore from the previous month
		int daysRemainingLastMonth = firstDayOfMonth - firstDayOfWeek;
		if (daysRemainingLastMonth < 0)
			daysRemainingLastMonth = daysRemainingLastMonth + 7;
		
		
		
		// GridLayout to hold the days, one week = 7 columns
		daysGridPanel = new JPanel(new GridLayout(0, 7));

		
		// Fill in blanks for the remaining days of last month
		for (int i=0; i< daysRemainingLastMonth; i++) {
			daysGridPanel.add(new DayOfMonth());
		}
		
		// Add the days of this month to the grid.
		int daysThisMonth = thisMonth.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
		days = new DayOfMonth[daysThisMonth + 1];
		
		for (int i=1; i< days.length; i++) {
			days[i] = new DayOfMonth(i);
			daysGridPanel.add(days[i]);
		}
		
		this.add(daysGridPanel, BorderLayout.CENTER);
		
		
		
		JPanel daysOfWeekHeader = new JPanel(new GridLayout(0, 7));
		int headerFontSize = 13;
		daysOfWeekHeader.add(new CenteredComponent(new CalendarLabel("Monday", headerFontSize)));
		daysOfWeekHeader.add(new CenteredComponent(new CalendarLabel("Tuesday", headerFontSize)));
		daysOfWeekHeader.add(new CenteredComponent(new CalendarLabel("Wednesday", headerFontSize)));
		daysOfWeekHeader.add(new CenteredComponent(new CalendarLabel("Thursday", headerFontSize)));
		daysOfWeekHeader.add(new CenteredComponent(new CalendarLabel("Friday", headerFontSize)));
		daysOfWeekHeader.add(new CenteredComponent(new CalendarLabel("Saturday", headerFontSize)));
		daysOfWeekHeader.add(new CenteredComponent(new CalendarLabel("Sunday", headerFontSize)));
		
		
		SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMMMM yyyy");
		String monthYear = monthYearFormat.format(thisMonth.getTime());
		
		Box headerBox = Box.createVerticalBox();
		headerBox.add(new CenteredComponent(new CalendarLabel(monthYear, 14), 5));
		headerBox.add(daysOfWeekHeader);
		
		this.add(headerBox, BorderLayout.NORTH);
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
	
	
	/**
	 * Adds an event to the calendar. This is only displayed if it falls on a day of this month.
	 * 
	 * @param evt
	 */
	public void addCalendarEvent(CalendarEvent evt) {
		Calendar eventDateTime = evt.getStartCalendar();
		if (eventDateTime.get(Calendar.MONTH) == thisMonth.get(Calendar.MONTH)) {
			int dayOfMonth = eventDateTime.get(Calendar.DAY_OF_MONTH);
			days[dayOfMonth].addEvent(evt);
		}
	}
	
	
	public static void main(String[] args) {
		
		JFrame frame = new JFrame("MonthView");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.getContentPane().add(new MonthView());
		
		frame.pack();
		frame.setVisible(true);
	}
}
