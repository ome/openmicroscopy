
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
import java.awt.Dimension;
import java.awt.Font;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.border.Border;

public class TimeDisplay extends Box {
	
	SpinnerModel hoursModel;
	JSpinner hoursSpinner;
	SpinnerModel minsModel;
	JSpinner minsSpinner;
	
	Border selectedBorder;
	Border unSelectedBorder;

	public static final Color BLUE_HIGHLIGHT = new Color(181,213,255);
	
	Font timeFont = new Font("SansSerif", Font.PLAIN, 10);
	protected JFormattedTextField hoursField;
	protected JFormattedTextField minsField;
	
	public TimeDisplay() {
		super(BoxLayout.X_AXIS);

		buildUI(0, 0);
	}
	
	public TimeDisplay(Calendar calendar) {
		super(BoxLayout.X_AXIS);
		
		buildUI(calendar);
	}
	
	public TimeDisplay(Date date, String label) {
		super(BoxLayout.X_AXIS);
		
		this.add(new CalendarLabel(label));
		buildUI(date);
	}
	
	public TimeDisplay(int hours, int mins) {
		super(BoxLayout.X_AXIS);
		
		buildUI(hours, mins);
	}
	

	public void buildUI(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		buildUI(calendar);
	}
	
	public void buildUI(Calendar calendar) {
		buildUI(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
	}

	public void buildUI(int hours, int mins) {
		
		Dimension fieldSize = new Dimension(19, 18);
		
		
		hoursField = new CalendarFormattedTextField();
		hoursField.setText(hours<10 ? "0" + hours : hours + "");
		
		
		minsField = new CalendarFormattedTextField();
		minsField.setText(mins<10 ? "0" + mins : mins + "");
		
		
		this.add(hoursField);
		this.add(new JLabel(":"));
		this.add(minsField);
	}
	
	/**
	 * Set the enabled state of the hours and minutes text components
	 */
	public void setEnabled(boolean enabled) {
		hoursField.setEnabled(enabled);
		minsField.setEnabled(enabled);
	}
	
}
