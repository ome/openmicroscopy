/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2007-2008 University of Dundee. All rights reserved.
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

package ui.formFields;

import java.awt.Dimension;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.freixas.jcalendar.DateEvent;
import org.freixas.jcalendar.DateListener;
import org.freixas.jcalendar.JCalendarCombo;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;

/**
 * This class wraps a Gregorian Calendar, which is displayed with a Date-Picker and hrs-mins spinners.
 * It saves the time to dataField as a UTC millisecond string.
 * Changes made via the date-picker or hrs-mins spinners are first saved to the Gregorian Calendar...
 * then updated to dataField as millisecond string. 
 * The default time is NOW! 
 * 
 * @author will
 *
 */
public class FormFieldDateTime extends FormField {
	
	JCalendarCombo jCalendarCombo;
	DateListener dateListener;

	SpinnerModel hoursModel;
	JSpinner hoursSpinner;
	SpinnerModel minsModel;
	JSpinner minsSpinner;
	TimeChangedListener timeChangedListener;
	
	GregorianCalendar gc;
	
	public FormFieldDateTime(IDataFieldObservable dataFieldObs) {
		super(dataFieldObs);
		
		gc = new GregorianCalendar();
		
		jCalendarCombo = new JCalendarCombo();
		dateListener = new CalendarListener();
		jCalendarCombo.addDateListener(dateListener);
		
		horizontalBox.add(jCalendarCombo);
		
		Dimension spinnerSize = new Dimension(45, 25);
		
		timeChangedListener = new TimeChangedListener();
		hoursModel = new SpinnerNumberModel(0, 0, 23, 1);
		hoursSpinner = new JSpinner(hoursModel);
		((DefaultEditor)hoursSpinner.getEditor()).getTextField().addFocusListener(componentFocusListener);
		hoursSpinner.setMaximumSize(spinnerSize);
		hoursSpinner.setPreferredSize(spinnerSize);
		hoursSpinner.addChangeListener(timeChangedListener);
		
		minsModel = new SpinnerNumberModel(0, 0, 59, 1);
		minsSpinner = new JSpinner(minsModel);
		((DefaultEditor)minsSpinner.getEditor()).getTextField().addFocusListener(componentFocusListener);
		minsSpinner.setMaximumSize(spinnerSize);
		minsSpinner.setPreferredSize(spinnerSize);
		minsSpinner.addChangeListener(timeChangedListener);
		
		Box timeBox = Box.createHorizontalBox();
		timeBox.add(Box.createHorizontalStrut(10));
		timeBox.add(hoursSpinner);
		timeBox.add(new JLabel("hrs "));
		timeBox.add(minsSpinner);
		timeBox.add(new JLabel("mins "));
		horizontalBox.add(timeBox);
		
		updateDateTimeFromDataField();	// based on millisecs in dataField.  
		refreshDateTimeDisplay();	// updates Calendar and Time
	}
	
	public class TimeChangedListener implements ChangeListener {
		public void stateChanged(ChangeEvent arg0) {
			int hrs = new Integer(hoursModel.getValue().toString());
			int mins = new Integer(minsModel.getValue().toString());
			
			gc.set(Calendar.HOUR_OF_DAY, hrs);
			gc.set(Calendar.MINUTE, mins);
			
			saveDateTimeValues();
		}
	}
	
	public class CalendarListener implements DateListener {
		public void dateChanged(DateEvent evt) {
			Calendar newDate = evt.getSelectedDate();
			
			int year = newDate.get(Calendar.YEAR);
			int month = newDate.get(Calendar.MONTH);
			int date = newDate.get(Calendar.DATE);
			
			gc.set(year, month, date);
			
			saveDateTimeValues();
		}
		
	}
	
	public void saveDateTimeValues() {
		long timeInMillis = gc.getTimeInMillis();
		dataField.setAttribute(DataFieldConstants.UTC_MILLISECS, timeInMillis + "", true);
	}
	
	// overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		
		GregorianCalendar oldDate = new GregorianCalendar();
		oldDate.setTime(gc.getTime());
		
		// get the millisecs value from dataField, set gc
		updateDateTimeFromDataField();
		
		// if the new value is different from old, 
		// (ie the dataField has not just been updated by this class) refresh! - needed for undo/redo.
		if (gc.compareTo(oldDate) != 0) {
			refreshDateTimeDisplay();
		}
	}
	
	/**
	 * get the millisecs as a String from dataField, convert to Long, use to set time;
	 */
	public void updateDateTimeFromDataField() {
		String millisecs = dataField.getAttribute(DataFieldConstants.UTC_MILLISECS);
		if (millisecs != null) {
			long UTCMillisecs = new Long(millisecs);
			gc.setTimeInMillis(UTCMillisecs);
		} else 
			gc.setTime(new Date());
	}
	
	
	public void refreshDateTimeDisplay() {
		
		Date date = gc.getTime();
			
		jCalendarCombo.removeDateListener(dateListener);
		jCalendarCombo.setDate(date);
		jCalendarCombo.addDateListener(dateListener);
		
		refreshTimeDisplay();
	}
	
	public void refreshTimeDisplay() {
		int hours = gc.get(Calendar.HOUR_OF_DAY);
		int mins = gc.get(Calendar.MINUTE);
			
		hoursSpinner.removeChangeListener(timeChangedListener);
		hoursSpinner.setValue(hours);
		hoursSpinner.addChangeListener(timeChangedListener);
		
		minsSpinner.removeChangeListener(timeChangedListener);
		minsSpinner.setValue(mins);
		minsSpinner.addChangeListener(timeChangedListener);
	}
	
	public void setHighlighted(boolean highlight) {
		super.setHighlighted(highlight);
		// if the user highlighted this field by clicking the field (not the textBox itself) 
		// need to get focus, otherwise focus will remain elsewhere. 
		if (highlight && (!jCalendarCombo.hasFocus()))
			jCalendarCombo.requestFocusInWindow();
	}
	
}


