 /*
 * omeroCal.view.DayIcon 
 *
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
 */
package omeroCal.view;

//Java imports

import java.awt.Color;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * A representation of a Day, that is very small and shows the date number only.
 * If an event is added, the background color changes and toolTip updates, 
 * but the event is not displayed. 
 * 
 * Use this day UI component for small icon views. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DayIcon 
	extends JPanel
	implements IDayDisplay {
	
	/**
	 * A Label to display the day of month
	 */
	private JLabel dayLabel;
	
	private Calendar date;
	
	/**
	 * Creates an instance and builds the UI.
	 * Sets size, dayLabel, background and border.
	 * 
	 * @param dayOfMonth	The day of the month (1-31) displayed in this panel
	 */
	public DayIcon(Calendar dayOfMonth) {
		
		date = new GregorianCalendar();
		date.setTime(dayOfMonth.getTime());
		
		int day = dayOfMonth.get(Calendar.DAY_OF_MONTH);
		
		Dimension daySize = new Dimension(20, 20);
		setMinimumSize(daySize);
		setPreferredSize(daySize);
		
		setBorder(BorderFactory.createMatteBorder(2,1,2,0, Color.white));
		this.setBackground(DayOfMonth.CAL_GREY);
		
		dayLabel = new CalendarLabel(day + "");
		add(dayLabel);
	}

	/**
	 * Changes the background colour and adds the event.toString()
	 * to the toolTipText. 
	 */
	public void addEvent(JComponent event) {
		String toolTip = this.getToolTipText();
		if (toolTip == null) 
			toolTip = "<html>";
		else 
			toolTip = toolTip + "<br>";
		String eventText = event.toString();
		this.setToolTipText(toolTip + eventText);
		
		this.setBackground(TimeDisplay.BLUE_HIGHLIGHT);
	}

	/**
	 * Hides the dayLabel and sets the background to white. 
	 */
	public void setDayFromOtherMonth(boolean dayFromOtherMonth) {
		this.setBackground(Color.white);
		dayLabel.setVisible(false);
	}

	/**
	 * If this day is today, change the background colour. 
	 */
	public void setToday(boolean today) {
		this.setBackground(DayOfMonth.TODAY_BACKGROUND);
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
