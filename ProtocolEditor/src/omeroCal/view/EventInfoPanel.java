
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import omeroCal.model.CalendarEvent;
import ui.components.AlignedComponent;



public class EventInfoPanel 
	extends JPanel 
	implements Observer {
	
	/**
	 * This controller should be notified of changes to this event (eg date changed)
	 * so that the UI and Model can be updated appropriately.
	 */
	IEventListener eventController;

	TimeDisplay startTime;
	TimeDisplay endTime;
	
	
	public EventInfoPanel() {
		this(null);
	}
	
	public EventInfoPanel(CalendarEvent calendarEvent) {
		
		this.setLayout(new BorderLayout());
		
		Box verticalBox = Box.createVerticalBox();
		
		String title = "";
		boolean isAllDayEvent = true;
		Date startDateTime = new Date();
		Date endDateTime = new Date();
		
		
		// set variables
		if (calendarEvent != null) {
			title = calendarEvent.getName();
			isAllDayEvent = calendarEvent.isAllDayEvent();
			startDateTime = calendarEvent.getStartTime();
			endDateTime = calendarEvent.getEndTime();
		}
		
		
		// title
		JLabel titleLabel = new CalendarLabel("<html>" + title + "</html>", 15);
		titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		verticalBox.add(titleLabel);
		
		// separator
		verticalBox.add(Box.createVerticalStrut(10));
		verticalBox.add(new JSeparator(JSeparator.HORIZONTAL));
		verticalBox.add(Box.createVerticalStrut(10));
		
		
		// Need 2 columns, where rows are all the same height
		// Use 2 vertical boxes 
		JPanel leftColumn = new JPanel(new GridLayout(0, 1));
		leftColumn.setBackground(null);
		JPanel rightColumn = new JPanel(new GridLayout(0, 1));
		rightColumn.setBackground(null);
		JPanel columnContainer = new JPanel(new BorderLayout());
		columnContainer.setBackground(null);
		columnContainer.add(leftColumn, BorderLayout.WEST);
		columnContainer.add(rightColumn, BorderLayout.CENTER);
		columnContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
		verticalBox.add(columnContainer);
		
		
		// display a checkBox for allDayEvent
		leftColumn.add(new CalendarLabel("all day : "));
		JCheckBox allDayEvent = new JCheckBox();
		allDayEvent.setBackground(null);
		allDayEvent.setSelected(isAllDayEvent);
		allDayEvent.addActionListener(new AllDayListener());
		rightColumn.add(allDayEvent);
		
		
		// display the start date in a date-picker
		leftColumn.add(new CalendarLabel("from: "));
		DatePicker datePicker = new DatePicker();
		datePicker.setDate(startDateTime);
		datePicker.setAlignmentX(Component.LEFT_ALIGNMENT);
		//datePicker.setEnabled(false);
		
		// with a time display (visible if AllDayEvent == false)
		startTime = new TimeDisplay(startDateTime, " at ");
		startTime.setVisible(!isAllDayEvent);
		Box dateTimeContainer = Box.createHorizontalBox();
		dateTimeContainer.add(datePicker);
		dateTimeContainer.add(startTime);
		rightColumn.add(new AlignedComponent(dateTimeContainer, Component.LEFT_ALIGNMENT));
		
		
		// display the end date in a date-picker
		leftColumn.add(new CalendarLabel("to: "));
		DatePicker endDatePicker = new DatePicker();
		endDatePicker.setDate(endDateTime);
		endDatePicker.setAlignmentX(Component.LEFT_ALIGNMENT);
		//endDatePicker.setEnabled(false);
		
		// with a time display (visible if AllDayEvent == false)
		endTime = new TimeDisplay(endDateTime, " at ");
		endTime.setVisible(!isAllDayEvent);
		Box endDateTimeContainer = Box.createHorizontalBox();
		endDateTimeContainer.add(endDatePicker);
		endDateTimeContainer.add(endTime);
		rightColumn.add(new AlignedComponent(endDateTimeContainer, Component.LEFT_ALIGNMENT));
		
		
		// show the Calendar that this Event belongs to
		leftColumn.add(new CalendarLabel("calendar: "));
		// Need to get Calendar info from DB
		rightColumn.add(new CalendarLabel(" Work "));
		
		
		// display alarm time in an AlarmSetter
		leftColumn.add(new CalendarLabel("alarm: "));
		rightColumn.add(new AlarmEditor(calendarEvent));
		
		
		
		// separator
		verticalBox.add(Box.createVerticalStrut(10));
		verticalBox.add(new JSeparator(JSeparator.HORIZONTAL));
		verticalBox.add(Box.createVerticalStrut(10));
		
		
		// notes 
		verticalBox.add(new CalendarLabel("Notes"));
		verticalBox.add(new CalendarLabel("Some notes need to be retrieved from the CalendarEvent to go here"));
		
		//verticalBox.setPreferredSize(new Dimension(230, 100));
		int borderWidth = 15;
		verticalBox.setBorder(new EmptyBorder(borderWidth,borderWidth,borderWidth,borderWidth));
		
		
		this.add(verticalBox, BorderLayout.NORTH);
		this.setPreferredSize(new Dimension(280, 400));
		this.setBackground(Color.WHITE);
		
	}
	
	public class AllDayListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			boolean allDay = ((JCheckBox)e.getSource()).isSelected();
			startTime.setVisible(!allDay);
			endTime.setVisible(!allDay);
		}
		
	}

	public void update(Observable o, Object arg) {
		
	}
	
public static void main(String[] args) {
		
		JFrame frame = new JFrame("MonthView");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		CalendarEvent ce = new CalendarEvent("Title Here", new Date());
		ce.setAllDayEvent(false);
		
		Calendar alarm = new GregorianCalendar();
		alarm.setTime(new Date());
		alarm.add(Calendar.MINUTE, -3);
		ce.setAlarmTime(alarm.getTime());
		
		frame.getContentPane().add(new EventInfoPanel(ce));
		
		frame.pack();
		frame.setVisible(true);
	}
	
}
