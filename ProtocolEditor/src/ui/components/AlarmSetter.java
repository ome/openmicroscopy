
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

package ui.components;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tree.DataFieldConstants;
import tree.DataFieldObserver;
import tree.IAttributeSaver;
import tree.IDataFieldObservable;


public class AlarmSetter 
	extends JPanel 
	implements DataFieldObserver {
	
	IAttributeSaver dataField;
	String attributeName;
	
	SpinnerModel timeModel;
	JSpinner timeSpinner;
	TimeChangedListener timeChangedListener;
	
	CustomComboBox timeUnitChooser;
	TimeActionListener timeActionListener;
	
	public static final int DAYS = 0;
	public static final int HOURS = 1;
	public static final int MINS = 2;
	public static final String[] timeUnits = {"Days", "Hours", "Minutes"};
	public static final int[] secondsInTimeUnit = {24*3600, 3600, 60};
	
	public static final int BEFORE = 0;
	public static final int AFTER = 1;
	public static final String[] beforeAfterOptions = {"Before", "After"};
	CustomComboBox beforeAfterChooser;
	
	public AlarmSetter(IAttributeSaver dataField, String attributeName) {
		
		
		this.dataField = dataField;
		this.attributeName = attributeName;
		
		if (dataField instanceof IDataFieldObservable) {
			((IDataFieldObservable)dataField).addDataFieldObserver(this);
		}
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		JLabel alarmLabel = new JLabel("Alarm: ");
		alarmLabel.setToolTipText("Choose a time Before or After the event for alarm to go off");
		this.add(alarmLabel);
		
		
		Dimension spinnerSize = new Dimension(45, 25);
		
		timeChangedListener = new TimeChangedListener();
		timeActionListener = new TimeActionListener();
		
		timeModel = new SpinnerNumberModel(0, 0, 99, 1);
		timeSpinner = new JSpinner(timeModel);
		timeSpinner.setMaximumSize(spinnerSize);
		timeSpinner.setPreferredSize(spinnerSize);
		timeSpinner.addChangeListener(timeChangedListener);
		this.add(timeSpinner);
		
		timeUnitChooser = new CustomComboBox(timeUnits);
		timeUnitChooser.addActionListener(timeActionListener);
		timeUnitChooser.setMaximumWidth(100);
		this.add(timeUnitChooser);
		
		
		beforeAfterChooser = new CustomComboBox(beforeAfterOptions);
		beforeAfterChooser.addActionListener(timeActionListener);
		this.add(beforeAfterChooser);
		
		// update the display with value from dataField
		dataFieldUpdated();
	}
	
	public class TimeChangedListener implements ChangeListener {
		public void stateChanged(ChangeEvent arg0) {
			timeChanged();
		}
	}
	
	public class TimeActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			timeChanged();
		}
	}
	
	public void timeChanged() {
		int timeValue = Integer.parseInt(timeModel.getValue().toString());
		
		int timeUnit = timeUnitChooser.getSelectedIndex();
		int secondsOfTimeUnit = secondsInTimeUnit[timeUnit];
		
		int secondsAfterEvent = timeValue * secondsOfTimeUnit;
		
		if (beforeAfterChooser.getSelectedIndex() == BEFORE) {
			secondsAfterEvent = secondsAfterEvent * -1;
		}
		
		dataField.setAttribute(attributeName, secondsAfterEvent + "", true);
	}
	
	public void dataFieldUpdated() {
		String alarmTimeInSeconds = dataField.getAttribute(attributeName);
		setAlarmTime(alarmTimeInSeconds);
	}
	
	/** 
	 * Sets the display of the timeSpinner and timeUnitChooser to reflect the time in seconds.
	 * 
	 * @param seconds	Time in seconds. This should be equal to an integer of days, hours OR minutes.
	 */
	public void setAlarmTime(String seconds) {
		
		if ((seconds == null) || (seconds.equals("0"))) {
			timeSpinner.removeChangeListener(timeChangedListener);
			timeModel.setValue(0);
			timeSpinner.addChangeListener(timeChangedListener);
			
			return;
		}
		
		int secs = Integer.parseInt(seconds);
		
		if (secs < 0) {
			setBeforeAfterChooserIndex(BEFORE);
			secs = secs * -1;
		} else {
			setBeforeAfterChooserIndex(AFTER);
		}
		
		int timeUnit;
		int timeValue;
		
		// need to work out what the units are
		for (timeUnit = 0; timeUnit<timeUnits.length; timeUnit++) {
			timeValue = secs/secondsInTimeUnit[timeUnit];
			// if timeValue > 0 then this is the units you want
			if (timeValue > 0) {
				
				// if it is an exact match 
				if (secs == timeValue * secondsInTimeUnit[timeUnit]) {
					setTimeUnitChooserIndex(timeUnit);
					setTimeSpinnerValue(timeValue);
					return;
				} else {
				
				// if any seconds are left over (eg user sets 90 minutes or 36 hours)
					// try smaller units
					if (timeUnit < timeUnits.length -1) {
						timeUnit++;
						timeValue = secs/secondsInTimeUnit[timeUnit];
						setTimeUnitChooserIndex(timeUnit);
						setTimeSpinnerValue(timeValue);
						return;
					}
				
				}
				
			}
		}
	}
	
	public void setTimeUnitChooserIndex(int index) {
		timeUnitChooser.removeActionListener(timeActionListener);
		timeUnitChooser.setSelectedIndex(index);
		timeUnitChooser.addActionListener(timeActionListener);
	}
	
	public void setTimeSpinnerValue(int value) {
		timeSpinner.removeChangeListener(timeChangedListener);
		timeModel.setValue(value);
		timeSpinner.addChangeListener(timeChangedListener);
	}
	
	public void setBeforeAfterChooserIndex(int index) {
		beforeAfterChooser.removeActionListener(timeActionListener);
		beforeAfterChooser.setSelectedIndex(index);
		beforeAfterChooser.addActionListener(timeActionListener);
	}

}
