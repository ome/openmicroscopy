 /*
 * treeEditingComponents.DateTimeField 
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
package treeEditingComponents;

//Java imports

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

import fields.DateTimeParam;
import fields.FieldPanel;
import fields.IParam;

import omeroCal.view.DatePicker;
import tree.DataFieldConstants;
import ui.components.CustomComboBox;
import uiComponents.HrsMinsEditor;


/** 
 * This is the UI component for editing a DateTime experimental value. 
 * It includes a Date-Picker for picking a specific date.
 * Alternatively, users can use a comboBox to choose a "relative" date 
 * (eg 3 days after a previous date in the experiment).
 * Finally, a checkBox allows you to "Set Time", in which case, a 
 * time field is shown. 
 * 
 * The Date-Picker and ComboBox are mutually exclusive: Only one can show a value.
 * All components cause the IFieldValue to be updated, by calling
 * setAttribute(name, value);
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DateTimeField 
	extends JPanel 
	implements PropertyChangeListener,
	ITreeEditComp{
	
	private IParam param;
	
	
	DatePicker datePicker;
	
	CustomComboBox daySelector;
	
	JCheckBox timeChosen;
	
	HrsMinsEditor hrsMinsEditor;
	
	String lastUpdatedAttribute;
	
	CalendarListener calendarListener = new CalendarListener();
	
	DaySelectedListener daySelectedListener = new DaySelectedListener();
	
	
	public DateTimeField (IParam param) {
		
		this.param = param;
		
		this.setBackground(null);
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		datePicker = new DatePicker();
		datePicker.addActionListener(calendarListener);
		
		this.add(datePicker);
		this.add(new JLabel(" OR "));
		
		// A combo-box for days
		String[] dayOptions = {"Pick Day", "0 days", "1 day", "2 days","3 days","4 days",
				"5 days","6 days","7 days","8 days","9 days","10 days"};
		daySelector = new CustomComboBox(dayOptions);
		daySelector.setMaximumWidth(110);
		daySelector.setMaximumRowCount(dayOptions.length);
		daySelector.addActionListener(daySelectedListener);
		
		this.add(daySelector);
		this.add(Box.createHorizontalStrut(10));
		this.add(new JLabel( " Set Time?"));
		
		String timeInSecs = param.getAttribute(DateTimeParam.TIME_ATTRIBUTE);
		
		timeChosen = new JCheckBox();
		timeChosen.setBackground(null);
		timeChosen.setSelected(timeInSecs != null);
		timeChosen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timeSelected();
			}
		});
		this.add(timeChosen);
		
		
		hrsMinsEditor = new HrsMinsEditor();
		if (timeInSecs != null) {
			hrsMinsEditor.setHrsMins(new Integer(timeInSecs));
		}
		hrsMinsEditor.setVisible(timeInSecs != null);
		hrsMinsEditor.addPropertyChangeListener(HrsMinsEditor.TIME_IN_SECONDS, this);
		this.add(hrsMinsEditor);
		
		updateDateFromDataField();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		
		System.out.println("DateTimeField propertyChange " + evt.getPropertyName());
		if (HrsMinsEditor.TIME_IN_SECONDS.equals(evt.getPropertyName())) {
			
			String newVal = evt.getNewValue().toString();
			
			attributeEdited(DateTimeParam.TIME_ATTRIBUTE, newVal);
		}
		
	}
	
	public void timeSelected() {
		boolean showTime = timeChosen.isSelected();
		hrsMinsEditor.setVisible(showTime);
		
		System.out.println("DateTimeField timeSelected " + showTime);
		
		if (showTime) {
			hrsMinsEditor.firePropertyChange(
					HrsMinsEditor.TIME_IN_SECONDS, 
					-1, 	// has to be an artificial "oldValue" so it's not 
							// the same as newValue (won't fire). Can't use null
					hrsMinsEditor.getDisplayedTimeInSecs());
		} else {
			attributeEdited(DateTimeParam.TIME_ATTRIBUTE, null);
		}
		/*
		 * Need to resize...
		 */
		this.firePropertyChange(FieldPanel.SIZE_CHANGED_PROPERTY, null, null);	
	}

	public void attributeEdited(String attributeName, String newValue) {
		/*
		 * Before calling propertyChange, need to make sure that 
		 * getAttributeName() will return the name of the newly edited property
		 */
		lastUpdatedAttribute = attributeName;
		
		this.firePropertyChange(FieldPanel.VALUE_CHANGED_PROPERTY, null, newValue);
	}

	public String getAttributeName() {
		return this.lastUpdatedAttribute;
	}

	public IParam getParameter() {
		return param;
	}
	
	
	/**
	 * get the millisecs as a String from dataField, convert to Long, use to set time;
	 */
	public void updateDateFromDataField() {
		String millisecs = param.getAttribute(DataFieldConstants.UTC_MILLISECS);
		if (millisecs != null) {
			long UTCMillisecs = new Long(millisecs);
			Date date = new Date(UTCMillisecs);
			
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			
			int year = calendar.get(Calendar.YEAR);
			
			/*
			 * If UTCMillisecs has been used to store an absolute date, the date will
			 * be eg 2008. 
			 * 
			 * However, if UTCMillisecs has been used to store a relative date (eg 2 days)
			 * then UTCMillisecs will simply equal that time in milliseconds. 
			 * This means the year in a calendar will be 1970 (Epoch). 
			 */
			
			// if date has been set, year is eg 2008.
			if (year != 1970) {
				datePicker.removeActionListener(calendarListener);
				datePicker.setDate(date);
				datePicker.addActionListener(calendarListener);
				
				daySelector.removeActionListener(daySelectedListener);
				daySelector.setSelectedIndex(0);
				daySelector.addActionListener(daySelectedListener);
			} else {
				
				try {
					int days = convertMillisecsToDays(UTCMillisecs);
					
					daySelector.removeActionListener(daySelectedListener);
					daySelector.setSelectedIndex(days + 1);
					daySelector.addActionListener(daySelectedListener);
				
					datePicker.removeActionListener(calendarListener);
					datePicker.setDate(null);
					datePicker.getEditor().setText("Pick Date");
					datePicker.addActionListener(calendarListener);
					
				} catch (Exception e) {
					// Either the millisecs value was too high for integer,
					// or number of days was off the scale of the daySelector
				}
			}
			
		} else {
			datePicker.removeActionListener(calendarListener);
			datePicker.setDate(null);
			datePicker.getEditor().setText("Pick Date");
			datePicker.addActionListener(calendarListener);
			
			daySelector.removeActionListener(daySelectedListener);
			daySelector.setSelectedIndex(0);
			daySelector.addActionListener(daySelectedListener);
		}
		
		
	}
	
	
	/**
	 * Used if a relative time is stored in millisecs. 
	 * 
	 * @param millisecs
	 * @return
	 */
	public static int convertMillisecsToDays(long millisecs) {
		
		int milliseconds = (int)millisecs;
		
		int millisecsPerDay = 24 * 60 * 60 * 1000;
	
		int days = milliseconds / millisecsPerDay;
		
		return days;
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
			
			long timeInMillis = calendar.getTimeInMillis();
			attributeEdited(DataFieldConstants.UTC_MILLISECS, timeInMillis + "");
		} else
		
		if (daySelector.getSelectedIndex() > 0){
			int daysChosen = daySelector.getSelectedIndex() - 1;
			
			int millisecsPerDay = 24 * 60 * 60 * 1000;
			
			int daysInMillis = daysChosen * millisecsPerDay;
			
			attributeEdited(DataFieldConstants.UTC_MILLISECS, daysInMillis + "");
		} else {
			attributeEdited(DataFieldConstants.UTC_MILLISECS, null);
		}
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
}
