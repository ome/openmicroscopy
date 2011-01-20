 /*
 * uiComponents.HrsMinsField 
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
package org.openmicroscopy.shoola.agents.editor.uiComponents;

//Java imports

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a UI component for displaying and editing Hrs and Mins.
 * Uses a DoubleDigitField for the Hrs and for Mins. 
 * Fires a propertyChange TIME_IN_SECONDS when the DoubleDigitFields are
 * edited. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class HrsMinsField extends JPanel 
	implements PropertyChangeListener {
	
	/**
	 * Displays the hours
	 */
	protected DoubleDigitField hrsField;
	
	/**
	 * Displays the minutes
	 */
	protected DoubleDigitField minsField;
	
	/**
	 * Bound property for this component. 
	 * Refers to the value of the HrsMins display in seconds. 
	 */
	public static final String TIME_IN_SECONDS = "timeInSeconds";
	
	/**
	 * Keeps track of the time in seconds. 
	 */
	protected int currentTimeInSecs = 0;
	
	/**
	 * Creates an instance of this class. 
	 * Builds the UI, adding this class as a propertyChangeListener to the
	 * hrs and mins fields. 
	 */
	public HrsMinsField() {
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBackground(null);
		
		hrsField = new DoubleDigitField(0, 23);
		hrsField.addPropertyChangeListener(DoubleDigitField.DIGIT_VALUE_PROPERTY, this);
		
		minsField = new DoubleDigitField(0, 59);
		minsField.addPropertyChangeListener(DoubleDigitField.DIGIT_VALUE_PROPERTY, this);
		
		add(hrsField);
		add(new JLabel(":"));
		add(minsField);
		
		setTimeInSecs(0);
	}
	
	/**
	 * Sets the current time and updates the UI.
	 * Any seconds that do not form a whole minute are ignored. 
	 * 
	 * @param timeInSecs	The time in seconds. 
	 */
	public void setTimeInSecs(int timeInSecs) {
		
		int hours = 0;
		int mins = 0;
		
		hours = timeInSecs / 3600;
		mins = (timeInSecs - hours*3600) / 60;
		
		hrsField.setText(hours + "");
		minsField.setText(mins + "");
		
		currentTimeInSecs = timeInSecs;
	}
	
	/**
	 * Sets the time in hours and mins. 
	 * 
	 * @param hours
	 * @param mins
	 */
	public void setTime(int hours, int mins) 
	{
		hrsField.setText(hours + "");
		minsField.setText(mins + "");
		
		currentTimeInSecs = hours * 3600 + mins * 60;
	}

	/**
	 * Fired when the Hrs or Mins fields are updated. 
	 * Fires a propertyChangeEvent(TIME_IN_SECONDS)
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		
		if (DoubleDigitField.DIGIT_VALUE_PROPERTY.equals(evt.getPropertyName())) {
			int newTimeInSecs = getTimeInSecs();
			
			this.firePropertyChange(TIME_IN_SECONDS, currentTimeInSecs, newTimeInSecs);
			
			currentTimeInSecs = newTimeInSecs;
		}
 		
	}
	
	/**
	 * Gets the currently displayed time in seconds.
	 * 
	 * @return	Time in seconds
	 */
	public int getTimeInSecs() {
		int hrs = new Integer(hrsField.getText().toString());
		int mins = new Integer(minsField.getText().toString());
		
		return (hrs * 3600) + (mins * 60);
	}
	
	/**
	 * Sets the enabled status of the Hrs field and Mins field. 
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		hrsField.setEnabled(enabled);
		minsField.setEnabled(enabled);
	}

}
