
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
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

import calendar.CalendarFile;


import ui.components.CenteredComponent;


public class MonthView 
	extends JPanel 
	implements Observer {

	/**
	 * A Model of this month.
	 */
	IMonthModel monthModel;
	
	/**
	 * A reference to the Year and Month represented by this class
	 */
	GregorianCalendar thisMonth;
	
	/**
	 * The current moment in time. Used to highlight "Today" in the month view.
	 */
	GregorianCalendar now = new GregorianCalendar();
	
	/**
	 * Each week starts on Monday!
	 */ 
	int firstDayOfWeek = GregorianCalendar.MONDAY;
	
	/**
	 * A label at the top, displaying month and year, eg "March 2008"
	 */
	JLabel monthYearLabel;
	
	/**
	 * An array of the days this month.
	 */
	DayOfMonth[] days;
	
	/**
	 * A highlight color to indicate TODAY, if the current month is displayed
	 */
	protected static Color todayBackground = new Color(255, 225, 225);
	
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
		
		thisMonth = new GregorianCalendar();
		thisMonth.setTime(date);
		
	}
	
	public MonthView(IMonthModel monthModel) {
		
		this();
		
		if (monthModel instanceof Observable) {
			((Observable)monthModel).addObserver(this);
		}
		
		this.monthModel = monthModel;
		
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
		
		if (thisMonth == null)
			thisMonth = new GregorianCalendar();
		
		
		// GridLayout to hold the days, one week = 7 columns
		daysGridPanel = new JPanel(new GridLayout(0, 7));
		
		// add the days to the grid 
		addDaysToGrid();
		
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
		
		headerBox.add(new CenteredComponent(titleButtonsBox, 5));
		headerBox.add(daysOfWeekHeader);
		
		this.add(headerBox, BorderLayout.NORTH);
	}
	
	
	public void addDaysToGrid() {
		
		// if refreshing, need to remove all old days from grid
		daysGridPanel.removeAll();
		
		// Get the first day of the month
		thisMonth.set(Calendar.DAY_OF_MONTH, 1);
		int firstDayOfMonth = thisMonth.get(GregorianCalendar.DAY_OF_WEEK);
		// Calculate the number of days to ignore from the previous month
		int daysRemainingLastMonth = firstDayOfMonth - firstDayOfWeek;
		if (daysRemainingLastMonth < 0)
			daysRemainingLastMonth = daysRemainingLastMonth + 7;
		
		
		// Fill in blanks for the remaining days of last month
		for (int i=0; i< daysRemainingLastMonth; i++) {
			daysGridPanel.add(new DayOfMonth());
		}
		
		// create an array to hold the days of the month, for easy reference
		int daysThisMonth = thisMonth.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
		days = new DayOfMonth[daysThisMonth + 1];
		
		// Add the days of this month to the grid.
		for (int i=1; i< days.length; i++) {
			days[i] = new DayOfMonth(i);
			daysGridPanel.add(days[i]);
		}
		
		// if the current month is being displayed, highlight Today
		if ((now.get(Calendar.YEAR) == thisMonth.get(Calendar.YEAR)) && 
				(now.get(Calendar.MONTH) == thisMonth.get(Calendar.MONTH))) {
			int today = now.get(Calendar.DAY_OF_MONTH);
			days[today].setBackground(todayBackground);
		}
		
		// required if the view has been refreshed
		daysGridPanel.validate();
		daysGridPanel.repaint();
	}
	
	public void refreshHeader() {
		SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMMMM yyyy");
		String monthYear = monthYearFormat.format(thisMonth.getTime());
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
		
		System.out.println("MonthView addCalendarEvents()");
		
		List <CalendarEvent> events = monthModel.getEventsForMonth();
		
		for (CalendarEvent evt: events) {
			evt.addObserver(this);
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
	
	
	public void incrementMonth(int increment) {
		
		thisMonth.add(Calendar.MONTH, increment);
		
		monthModel.incrementMonth(increment);
		
		addDaysToGrid();
		addCalendarEvents();
		
		refreshHeader();
	}
	
	
	public static void main(String[] args) {
		
		JFrame frame = new JFrame("MonthView");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.getContentPane().add(new MonthView());
		
		frame.pack();
		frame.setVisible(true);
	}

	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}
}
