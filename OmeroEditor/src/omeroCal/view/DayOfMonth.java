
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
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import omeroCal.model.CalendarEvent;

/** 
 * A Panel to display the day of the month.
 * Contains the date in the top-left corner, and will display any events
 * in a vertical box. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DayOfMonth 
	extends JPanel 
	implements IDayDisplay {
	
	Box eventBox;
	
	JLabel dayLabel;
	
	private Calendar date;
	
	public static final Color CAL_GREY = new Color(200, 200, 200);
	
	/**
	 * A highlight color to indicate TODAY, if the current month is displayed
	 */
	public static final Color TODAY_BACKGROUND = new Color(255, 225, 225);
	
	public DayOfMonth(Calendar dayOfMonth) {
		
		date = new GregorianCalendar();
		date.setTime(dayOfMonth.getTime());
		
		int day = dayOfMonth.get(Calendar.DAY_OF_MONTH);
		
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createMatteBorder(1,0,0,1, CAL_GREY));
		
		Dimension daySize = new Dimension(115, 105);
		setMinimumSize(daySize);
		setPreferredSize(daySize);
		
		this.setBackground(Color.WHITE);
		
		dayLabel = new JLabel(day + "");
		add(dayLabel , BorderLayout.NORTH);
		
		eventBox = Box.createVerticalBox();
		add(eventBox, BorderLayout.CENTER);
	}
	
	public DayOfMonth() {
		
	}
	
	/**
	 * Adds an event component, for display in this panel.
	 */
	public void addEvent(JComponent event) {

		eventBox.add(event);
	}
	
	/**
	 * The month display includes days that are from the previous and next
	 * months. 
	 * These should be displayed differently, using this method. 
	 * 
	 * @param dayFromOtherMonth
	 */
	public void setDayFromOtherMonth(boolean dayFromOtherMonth) {
		dayLabel.setForeground(dayFromOtherMonth ? CAL_GREY : Color.black);
		setBackground(dayFromOtherMonth ? new Color(247,247,247) : Color.white);
	}
	
	public void setToday(boolean today) {
		setBackground(today ? TODAY_BACKGROUND : Color.white);
	}
	
	/**
	 * returns the date of this day. 
	 */
	public Date getDate() {
		return date.getTime();
	}
	
	/**
	 * Returns the date, formatted as a string.
	 */
	public String toString() {
		SimpleDateFormat f = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
		return f.format(date.getTime());
	}

}
