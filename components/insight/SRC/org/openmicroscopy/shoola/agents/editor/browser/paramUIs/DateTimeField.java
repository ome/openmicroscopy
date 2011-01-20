 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.DateTimeField 
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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs;

//Java imports

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.Box;

//Third-party libraries

import org.jdesktop.swingx.JXDatePicker;

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;
import org.openmicroscopy.shoola.agents.editor.uiComponents.HrsMinsField;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * This is the UI component for editing a DateTime experimental value. 
 * It includes a Date-Picker for picking a specific date.
 * A checkBox allows you to "Set Time", in which case, a 
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
	implements PropertyChangeListener,
	ActionListener
{	
	
	/**
	 * A date picker, for picking an "Absolute" date.
	 */
	private JXDatePicker 		datePicker;
	
	/**
	 * Display text for the date-picker if no date is chosen. 
	 */
	public static final String PICK_DATE = "Pick Date";
	
	/**
	 * A UI component for editing Hrs and Mins
	 */
	private HrsMinsField 		hrsMinsEditor;
	
	
	/**
	 * Initialises the UI components, and sets their display values 
	 * according to the values of the Parameter we're editing. 
	 */
	private void initialise() 
	{
		IParam param = (IParam)getParameter();
		
		long UTCMillisecs;
		// display a date-picker 
		datePicker = UIUtilities.createDatePicker();
		datePicker.getEditor().setEditable(false);
		
		int hrs = 0;
		int mins = 0;
	
		// set the date according to the 'param value'
		String millisecs = param.getParamValue();
		if (millisecs != null) {
			UTCMillisecs = new Long(millisecs);
			Date date = new Date(UTCMillisecs);
			datePicker.setDate(date);
			
			Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis(UTCMillisecs);
			hrs = cal.get(Calendar.HOUR_OF_DAY);
			mins = cal.get(Calendar.MINUTE);
		} else {
			datePicker.setDate(null);
			datePicker.getEditor().setText(PICK_DATE);
		}
		datePicker.addActionListener(this);
		
		/* HrsMins editor is only visible if timeChosen is selected*/
		hrsMinsEditor = new HrsMinsField();
		hrsMinsEditor.setTime(hrs, mins);
		if (millisecs == null) {
			hrsMinsEditor.setVisible(false);
		}
		
		hrsMinsEditor.addPropertyChangeListener(
				HrsMinsField.TIME_IN_SECONDS, this);
	}
	
	/**
	 * Builds the UI
	 */
	private void buildUI() 
	{
		this.add(datePicker);
		
		this.add(Box.createHorizontalStrut(10));
		this.add(new CustomLabel( "Time: "));
		
		this.add(hrsMinsEditor);
	}
	
	/**
	 * Call this when either the date or time is edited. 
	 * Combines the values from these UI components and saves to model. 
	 */
	private void dateTimeEdited() {
		
		String newDateTime = null;
		long timeInMillis;
		
		Date date = datePicker.getDate();
		if (date != null) {
			timeInMillis = date.getTime();
			
			int secs = hrsMinsEditor.getTimeInSecs();
			timeInMillis += secs * 1000;
			
			newDateTime = timeInMillis + "";
		} 
		
		attributeEdited(TextParam.PARAM_VALUE, newDateTime);
	}

	/**
	 * Creates an instance.
	 * 
	 * @param param		The parameter being edited. 
	 */
	public DateTimeField (IParam param) 
	{	
		super(param);
		
		initialise();
		
		buildUI();
	}

	
	/**
	 * Used if a relative time is stored in millisecs. 
	 * 
	 * @param millisecs
	 * @return
	 */
	public static int convertMillisecsToDays(long millisecs) 
	{	
		int milliseconds = (int)millisecs;
		
		int millisecsPerDay = 24 * 60 * 60 * 1000;
	
		int days = milliseconds / millisecsPerDay;
		
		return days;
	}

	/**
	 * A propertyChange listener for the HrsMins editor. 
	 * If the TIME_IN_SECONDS property has changed. 
	 */
	public void propertyChange(PropertyChangeEvent evt) 
	{	
		dateTimeEdited();
		
		/*
		if (HrsMinsField.TIME_IN_SECONDS.equals(evt.getPropertyName())) {
			
			String newVal = evt.getNewValue().toString();
			
			attributeEdited(DateTimeParam.TIME_ATTRIBUTE, newVal);
		}
		*/
	}
	
	/**
	 * Gets the new value from the datePicker (or daySelector) and
	 * calls {#link {@link #attributeEdited(String, Object)}
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{
		dateTimeEdited();
	}
	
	/**
	 * @see ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() { return "Edit Date-Time"; }
}
