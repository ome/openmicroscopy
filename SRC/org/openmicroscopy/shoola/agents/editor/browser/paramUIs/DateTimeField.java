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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs;

//Java imports

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import javax.swing.Box;
import javax.swing.JCheckBox;

//Third-party libraries

import org.jdesktop.swingx.JXDatePicker;

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.FieldPanel;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.params.DateTimeParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomComboBox;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;
import org.openmicroscopy.shoola.agents.editor.uiComponents.HrsMinsField;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


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
	implements PropertyChangeListener,
	ActionListener
{	
	
	/**
	 * An array of Strings to populate the day-chooser comboBox. 
	 * Allows users to pick a "relative" date, that is a number of days
	 * after a date defined by another field. 
	 */
	private String[] dayOptions = {"Pick Day", 
			"0 days", "1 day", "2 days","3 days","4 days",
			"5 days","6 days","7 days","8 days","9 days","10 days"};
	
	/**
	 * Is this date parameter a "Relative" date (defined as a number of days
	 * after a previous date)
	 */
	private boolean 			relativeDate;
	
	/**
	 * A date picker, for picking an "Absolute" date.
	 */
	private JXDatePicker 		datePicker;
	
	/**
	 * Display text for the date-picker if no date is chosen. 
	 */
	public static final String PICK_DATE = "Pick Date";
	
	/**
	 * A comboBox for picking a "Relative" date (number of days)
	 */
	private CustomComboBox 		daySelector;
	
	/**
	 * CheckBox for choosing whether this date parameter also has a time 
	 */
	private JCheckBox 			timeChosen;
	
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
		IAttributes param = getParameter();
		relativeDate = param.isAttributeTrue(
				DateTimeParam.IS_RELATIVE_DATE);
		
		/* if not a relative date, display a date-picker */
		if (! relativeDate) {
			datePicker = UIUtilities.createDatePicker();
		
			// set the date according to the value of DATE_ATTRIBUTE
			String millisecs = param.getAttribute(DateTimeParam.DATE_ATTRIBUTE);
			if (millisecs != null) {
				long UTCMillisecs = new Long(millisecs);
				Date date = new Date(UTCMillisecs);
				datePicker.setDate(date);
			} else {
				datePicker.setDate(null);
				datePicker.getEditor().setText(PICK_DATE);
			}
			datePicker.addActionListener(this);
		}
		else {
			// A combo-box for picking a relative date in days
			daySelector = new CustomComboBox(dayOptions);
			daySelector.setMaximumWidth(110);
			daySelector.setMaximumRowCount(dayOptions.length);
						
			// Set the combo-box selection according to the value of
			// REL_DATE_ATTRIBUTE
			String daysInMillis = param.getAttribute(
					DateTimeParam.REL_DATE_ATTRIBUTE);
			if (daysInMillis != null) {
				try {
					long UTCMillisecs = new Long(daysInMillis);
					int days = convertMillisecsToDays(UTCMillisecs);
					daySelector.setSelectedIndex(days + 1);
				} catch (Exception e) {
					// Either the millisecs value was too high for integer,
					// or number of days was off the scale of the daySelector
				}
			}
			daySelector.addActionListener(this);
		}
		
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
		
		/* HrsMins editor is only visible if timeChosen is selected*/
		hrsMinsEditor = new HrsMinsField();
		if (timeInSecs != null) {
			hrsMinsEditor.setTimeInSecs(new Integer(timeInSecs));
		}
		hrsMinsEditor.setVisible(timeInSecs != null);
		hrsMinsEditor.addPropertyChangeListener(
				HrsMinsField.TIME_IN_SECONDS, this);
	}
	
	/**
	 * Builds the UI
	 */
	private void buildUI() 
	{
		/* if this param is an absolute date, display a date-picker */
		if (datePicker != null) {
			this.add(datePicker);
		}
		
		if (daySelector != null) {
			this.add(daySelector);
		}
		
		this.add(Box.createHorizontalStrut(10));
		this.add(new CustomLabel( " Set Time?"));
		
		this.add(timeChosen);
		
		this.add(hrsMinsEditor);
	}
	
	/**
	 * Called when the showTime checkBox is checked/unchecked.
	 * Toggles the display of the time, setting the 
	 * {@link DateTimeParam#TIME_ATTRIBUTE}
	 * to null if it is hidden and 0:00 if shown. 
	 */
	private void timeSelected() 
	{
		boolean showTime = timeChosen.isSelected();
		hrsMinsEditor.setVisible(showTime);
		
		if (showTime) {
			hrsMinsEditor.firePropertyChange(
					HrsMinsField.TIME_IN_SECONDS, 
					-1, 	// has to be an artificial "oldValue" so it's not 
							// the same as newValue (won't fire). Can't use null
					hrsMinsEditor.getTimeInSecs());
		} else {
			attributeEdited(DateTimeParam.TIME_ATTRIBUTE, null);
		}
		// Need to resize, and stay in editing mode...
		firePropertyChange(FieldPanel.UPDATE_EDITING_PROPERTY, null, null);	
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
		if (HrsMinsField.TIME_IN_SECONDS.equals(evt.getPropertyName())) {
			
			String newVal = evt.getNewValue().toString();
			
			attributeEdited(DateTimeParam.TIME_ATTRIBUTE, newVal);
		}
	}
	
	/**
	 * Gets the new value from the datePicker (or daySelector) and
	 * calls {#link {@link #attributeEdited(String, Object)}
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource().equals(datePicker)) {
			Date date = datePicker.getDate();
			if (date != null) {
				long timeInMillis = date.getTime();
				attributeEdited(DateTimeParam.DATE_ATTRIBUTE, timeInMillis + "");
			} else {
				
			}
		}
		
		if (e.getSource().equals(daySelector)) {
	
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
	
	/**
	 * @see ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() { return "Edit Date-Time"; }
}
