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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import omeroCal.view.DatePicker;

import org.freixas.jcalendar.DateEvent;
import org.freixas.jcalendar.DateListener;
import org.freixas.jcalendar.JCalendarCombo;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.components.CustomComboBox;

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
	
	DatePicker datePicker;
	ActionListener calendarListener;
	public static final String PICK_DATE = "Pick Date";

	CustomComboBox daySelector;
	ActionListener daySelectedListener;
	
	JCheckBox timeChosen;
	
	SpinnerModel hoursModel;
	JSpinner hoursSpinner;
	SpinnerModel minsModel;
	JSpinner minsSpinner;
	Box timeBox;
	TimeChangedListener timeChangedListener;
	
	/**
	 * This is chosen by the date picker, and
	 * is saved as UTCmilliseconds to the dataField.
	 * If this is chosen, relativeDays = null, and vice versa.
	 */
	// GregorianCalendar chosenDate;
	
	/**
	 * A delay from a previously set date; Saved as DAYS
	 * If this is chosen, chosenDate = null, and vice versa.
	 */
	// int relativeDays;
	
	public FormFieldDateTime(IDataFieldObservable dataFieldObs) {
		super(dataFieldObs);
		
		// A date-picker
		datePicker = new DatePicker();
		calendarListener = new CalendarListener();
		datePicker.addActionListener(calendarListener);
		horizontalBox.add(datePicker);
		
		horizontalBox.add(new JLabel(" OR "));
		
		// A combo-box for days
		String[] dayOptions = {"Pick Day", "0 days", "1 day", "2 days","3 days","4 days",
				"5 days","6 days","7 days","8 days","9 days","10 days"};
		daySelector = new CustomComboBox(dayOptions);
		daySelector.setMaximumWidth(110);
		daySelector.setMaximumRowCount(dayOptions.length);
		daySelectedListener = new DaySelectedListener(); 
		daySelector.addActionListener(daySelectedListener);
		horizontalBox.add(daySelector);
		
		
		// A checkbox for indicating that Time should be chosen
		horizontalBox.add(Box.createHorizontalStrut(10));
		horizontalBox.add(new JLabel( " Set Time?"));
		timeChosen = new JCheckBox();
		timeChosen.setSelected(false);
		timeChosen.setBackground(null);
		timeChosen.addActionListener(new TimeCheckedListener());
		horizontalBox.add(timeChosen);
		
		Dimension spinnerSize = new Dimension(45, 25);
		timeChangedListener = new TimeChangedListener();
		
		// Spinners for choosing a time (hrs & mins)
		hoursModel = new SpinnerNumberModel(0, 0, 23, 1);
		hoursSpinner = new JSpinner(hoursModel);
		((DefaultEditor)hoursSpinner.getEditor()).getTextField().addFocusListener(componentFocusListener);
		hoursSpinner.setMaximumSize(spinnerSize);
		hoursSpinner.setPreferredSize(spinnerSize);
		hoursSpinner.addChangeListener(timeChangedListener);
		// disabled unless timeChosen is selected
		
		minsModel = new SpinnerNumberModel(0, 0, 59, 1);
		minsSpinner = new JSpinner(minsModel);
		((DefaultEditor)minsSpinner.getEditor()).getTextField().addFocusListener(componentFocusListener);
		minsSpinner.setMaximumSize(spinnerSize);
		minsSpinner.setPreferredSize(spinnerSize);
		minsSpinner.addChangeListener(timeChangedListener);
		
		
		timeBox = Box.createHorizontalBox();
		timeBox.add(Box.createHorizontalStrut(2));
		timeBox.add(hoursSpinner);
		timeBox.add(new JLabel(":"));
		timeBox.add(minsSpinner);
		//timeBox.add(new JLabel("mins "));
		horizontalBox.add(timeBox);
		timeBox.setVisible(false); 	// only made visible if timeChosen is checked. 
		
		updateDateFromDataField();	// update and display date, based on UTCmillisecs  
		updateTimeFromDataField();	// updates time, based on SECONDS
	}
	
	
	public class CalendarListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			
			daySelector.removeActionListener(daySelectedListener);
			daySelector.setSelectedIndex(0);
			daySelector.addActionListener(daySelectedListener);
			
			saveDateValues();
		}
		
	}
	
	public class DaySelectedListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			datePicker.removeActionListener(calendarListener);
			datePicker.setDate(null);
			datePicker.addActionListener(calendarListener);
			
			saveDateValues();
		}
	}
	
	public class TimeCheckedListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			boolean time = timeChosen.isSelected();
			timeBox.setVisible(time);
			
			saveTimeValues();
		}
	}
	
	public class TimeChangedListener implements ChangeListener {
		public void stateChanged(ChangeEvent arg0) {
			
			saveTimeValues();
		}
	}
	
	/**
	 * Save the date-picked OR the days-chosen in the UTCmillisecond attribute.
	 * If the days-chosen is not null, date-picked IS null, Year & Month is set to 0;
	 */
	public void saveDateValues() {

		// sets time to NOW
		Calendar calendar = new GregorianCalendar();
		
		DateFormat dateFormat = DateFormat.getDateInstance( DateFormat.DEFAULT);
		DateFormat timeFormat = DateFormat.getTimeInstance (DateFormat.DEFAULT);
		
		Date date = datePicker.getDate();
		if (date != null) {
			calendar.setTime(date);
			
			System.out.println(dateFormat.format(calendar.getTime()));
			System.out.println(timeFormat.format(calendar.getTime()));
			
			long timeInMillis = calendar.getTimeInMillis();
			dataField.setAttribute(DataFieldConstants.UTC_MILLISECS, timeInMillis + "", true);
		} else
		
		if (daySelector.getSelectedIndex() > 0){
			int daysChosen = daySelector.getSelectedIndex() - 1;
			
			calendar.set(Calendar.YEAR, 0);
			calendar.set(Calendar.MONTH, 0);
			
			calendar.set(Calendar.DAY_OF_MONTH, daysChosen);
			
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			
			System.out.println(dateFormat.format(calendar.getTime()));
			System.out.println(timeFormat.format(calendar.getTime()));
			
			long timeInMillis = calendar.getTimeInMillis();
			dataField.setAttribute(DataFieldConstants.UTC_MILLISECS, timeInMillis + "", true);
		} else {
			dataField.setAttribute(DataFieldConstants.UTC_MILLISECS, null, true);
		}
	}	
	
	
	public void saveTimeValues() {
		if (timeChosen.isSelected()) {
			int hrs = new Integer(hoursModel.getValue().toString());
			int mins = new Integer(minsModel.getValue().toString());
			
			int timeInSecs = (hrs * 3600) + (60 * mins);
			dataField.setAttribute(DataFieldConstants.SECONDS, timeInSecs + "", true);
		} else {
			dataField.setAttribute(DataFieldConstants.SECONDS, null, true);
		}
	}
	
	// overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		
		// get and display the millisecs value from dataField, set gc
		updateDateFromDataField();
	
		// get and display the time
		updateTimeFromDataField();
		
	}
	
	/**
	 * get the millisecs as a String from dataField, convert to Long, use to set time;
	 */
	public void updateDateFromDataField() {
		String millisecs = dataField.getAttribute(DataFieldConstants.UTC_MILLISECS);
		if (millisecs != null) {
			long UTCMillisecs = new Long(millisecs);
			Date date = new Date(UTCMillisecs);
			
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			
			int year = calendar.get(Calendar.YEAR);
			
			// if date has been set, year is eg 2008.
			if (year >1) {
				datePicker.removeActionListener(calendarListener);
				datePicker.setDate(date);
				datePicker.addActionListener(calendarListener);
				
				daySelector.removeActionListener(daySelectedListener);
				daySelector.setSelectedIndex(0);
				daySelector.addActionListener(daySelectedListener);
			} else {
				int days = calendar.get(Calendar.DAY_OF_MONTH);
				
				daySelector.removeActionListener(daySelectedListener);
				daySelector.setSelectedIndex(days + 1);
				daySelector.addActionListener(daySelectedListener);
				
				datePicker.removeActionListener(calendarListener);
				datePicker.setDate(null);
				datePicker.getEditor().setText(PICK_DATE);
				datePicker.addActionListener(calendarListener);
			}
			
		} else {
			datePicker.removeActionListener(calendarListener);
			datePicker.setDate(null);
			datePicker.getEditor().setText(PICK_DATE);
			datePicker.addActionListener(calendarListener);
			
			daySelector.removeActionListener(daySelectedListener);
			daySelector.setSelectedIndex(0);
			daySelector.addActionListener(daySelectedListener);
		}
		
		
	}
	
	
	public void updateTimeFromDataField() {
		
		String seconds = dataField.getAttribute(DataFieldConstants.SECONDS);
		
		int hours = 12;
		int mins = 0;
		
		if (seconds != null) {
			
			int secs = Integer.parseInt(seconds);
			
			hours = secs / 3600;
			mins = (secs - hours*3600) / 60;
			
		}
		
		boolean timeIsSet = seconds != null;

		
		timeChosen.setSelected(timeIsSet);	// doesn't always hide/show spinners
		timeBox.setVisible(timeIsSet);

		
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
		if (highlight && (!datePicker.hasFocus()))
			datePicker.requestFocusInWindow();
	}
	
}


