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


import omeroCal.view.DatePicker;
import tree.DataFieldConstants;
import treeModel.fields.DateTimeParam;
import treeModel.fields.FieldPanel;
import treeModel.fields.IParam;
import uiComponents.CustomComboBox;
import uiComponents.CustomLabel;
import uiComponents.HrsMinsEditor;
import uiComponents.HrsMinsField;


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
	extends AbstractParamEditor 
	implements PropertyChangeListener {
	
	/**
	 * Is this date parameter a "Relative" date (defined as a number of days
	 * after a previous date)
	 */
	private boolean relativeDate;
	
	/**
	 * A date picker, for picking an "Absolute" date.
	 */
	private DatePicker datePicker;
	
	/**
	 * Display text for the date-picker if no date is chosen. 
	 */
	public static final String PICK_DATE = "Pick Date";
	
	/**
	 * A comboBox for picking a "Relative" date (number of days)
	 */
	private CustomComboBox daySelector;
	
	/**
	 * CheckBox for choosing whether this date parameter also has a time 
	 */
	private JCheckBox timeChosen;
	
	/**
	 * A UI component for editing Hrs and Mins
	 */
	private HrsMinsField hrsMinsEditor;
	
	/**
	 * An ActionListener for the date picker. 
	 */
	private ActionListener calendarListener = new CalendarListener();
	
	/**
	 * An ActionListener for the comboBox for picking relative date (days)
	 */
	private ActionListener daySelectedListener = new DaySelectedListener();
	
	/**
	 * Creates an instance.
	 * 
	 * @param param		The parameter being edited. 
	 */
	public DateTimeField (IParam param) {
		
		super(param);
		
		relativeDate = param.isAttributeTrue(
				DateTimeParam.IS_RELATIVE_DATE);
		
		/* if not a relative date, display a date-picker */
		if (! relativeDate) {
			datePicker = new DatePicker();
			datePicker.addActionListener(calendarListener);
		
			this.add(datePicker);
		}
		else {
		
			// A combo-box for picking a relative date in days
			String[] dayOptions = {"Pick Day", "0 days", "1 day", "2 days","3 days","4 days",
					"5 days","6 days","7 days","8 days","9 days","10 days"};
			daySelector = new CustomComboBox(dayOptions);
			daySelector.setMaximumWidth(110);
			daySelector.setMaximumRowCount(dayOptions.length);
			daySelector.addActionListener(daySelectedListener);
			
			this.add(daySelector);
		}
		
		this.add(Box.createHorizontalStrut(10));
		this.add(new CustomLabel( " Set Time?"));
		
		String timeInSecs = param.getAttribute(DateTimeParam.TIME_ATTRIBUTE);
		
		/* A checkBox for saying that you want to choose a time*/
		timeChosen = new JCheckBox();
		timeChosen.setBackground(null);
		timeChosen.setSelected(timeInSecs != null);
		timeChosen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timeSelected();
			}
		});
		this.add(timeChosen);
		
		/* HrsMins editor is only visible if timeChosen is selected*/
		hrsMinsEditor = new HrsMinsField();
		if (timeInSecs != null) {
			hrsMinsEditor.setTimeInSecs(new Integer(timeInSecs));
		}
		hrsMinsEditor.setVisible(timeInSecs != null);
		hrsMinsEditor.addPropertyChangeListener(HrsMinsField.TIME_IN_SECONDS, this);
		this.add(hrsMinsEditor);
		
		
		updateDateFromDataField();
	}

	
	/**
	 * A propertyChange listener for the HrsMins editor. 
	 * If the TIME_IN_SECONDS property has changed. 
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (HrsMinsEditor.TIME_IN_SECONDS.equals(evt.getPropertyName())) {
			
			String newVal = evt.getNewValue().toString();
			
			attributeEdited(DateTimeParam.TIME_ATTRIBUTE, newVal);
		}
	}
	
	/**
	 * Called when the showTime checkBox is checked/unchecked.
	 * Toggles the display of the time, setting the TIME_ATTRIBUTE
	 * to null if it is hidden and 0:00 if shown. 
	 */
	public void timeSelected() {
		boolean showTime = timeChosen.isSelected();
		hrsMinsEditor.setVisible(showTime);
		
		System.out.println("DateTimeField timeSelected " + showTime);
		
		if (showTime) {
			hrsMinsEditor.firePropertyChange(
					HrsMinsEditor.TIME_IN_SECONDS, 
					-1, 	// has to be an artificial "oldValue" so it's not 
							// the same as newValue (won't fire). Can't use null
					hrsMinsEditor.getTimeInSecs());
		} else {
			attributeEdited(DateTimeParam.TIME_ATTRIBUTE, null);
		}
		/*
		 * Need to resize...
		 */
		this.firePropertyChange(FieldPanel.UPDATE_EDITING_PROPERTY, null, null);	
	}

	
	/**
	 * get the millisecs as a String from dataField, convert to Long, 
	 * use to set time;
	 */
	public void updateDateFromDataField() {
		if (! relativeDate) {
			String millisecs = getParameter()
				.getAttribute(DateTimeParam.DATE_ATTRIBUTE);
			if (millisecs != null) {
				long UTCMillisecs = new Long(millisecs);
				Date date = new Date(UTCMillisecs);
				
				datePicker.removeActionListener(calendarListener);
				datePicker.setDate(date);
				datePicker.addActionListener(calendarListener);
			} else {
				datePicker.removeActionListener(calendarListener);
				datePicker.setDate(null);
				datePicker.getEditor().setText(PICK_DATE);
				datePicker.addActionListener(calendarListener);
			}
		}
			
		else {
			String daysInMillis = getParameter().
				getAttribute(DateTimeParam.REL_DATE_ATTRIBUTE);
			if (daysInMillis != null) {
				try {
					long UTCMillisecs = new Long(daysInMillis);
					int days = convertMillisecsToDays(UTCMillisecs);
					
					daySelector.removeActionListener(daySelectedListener);
					daySelector.setSelectedIndex(days + 1);
					daySelector.addActionListener(daySelectedListener);
					
				} catch (Exception e) {
					// Either the millisecs value was too high for integer,
					// or number of days was off the scale of the daySelector
				}
			}
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

	}	
	

	public class CalendarListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			
			Date date = datePicker.getDate();
			if (date != null) {
				long timeInMillis = date.getTime();
				attributeEdited(DateTimeParam.DATE_ATTRIBUTE, timeInMillis + "");
			} else {
				
			}
		}
		
	}
	
	public class DaySelectedListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (daySelector.getSelectedIndex() > 0){
				int daysChosen = daySelector.getSelectedIndex() - 1;
				
				int millisecsPerDay = 24 * 60 * 60 * 1000;
				
				int daysInMillis = daysChosen * millisecsPerDay;
				
				attributeEdited(DateTimeParam.REL_DATE_ATTRIBUTE, daysInMillis + "");
			} else {
				attributeEdited(DateTimeParam.REL_DATE_ATTRIBUTE, null);
			}
		}
	}
	
	public String getEditDisplayName() {
		return "Edit Date-Time";
	}
}
