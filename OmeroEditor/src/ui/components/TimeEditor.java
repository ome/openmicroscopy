
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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tree.DataFieldConstants;
import tree.DataFieldObserver;
import tree.IAttributeSaver;
import tree.IDataFieldObservable;


public class TimeEditor 
	extends Box 
	implements DataFieldObserver {
	
	IAttributeSaver dataField;
	String attributeName;
	
	TimeChangedListener timeChangedListener;
	
	SpinnerModel hoursModel;
	JSpinner hoursSpinner;
	SpinnerModel minsModel;
	JSpinner minsSpinner;
	SpinnerModel secsModel;
	JSpinner secsSpinner;
	
	String hoursLabel = "hrs ";
	String minsLabel = "mins ";
	String secsLabel = "secs";

	public TimeEditor(IAttributeSaver dataField, String attributeName, String nameLabel) {
		
		super(BoxLayout.X_AXIS);
		
		this.dataField = dataField;
		this.attributeName = attributeName;
		
		if (dataField instanceof IDataFieldObservable) {
			((IDataFieldObservable)dataField).addDataFieldObserver(this);
		}
		
		timeChangedListener = new TimeChangedListener();
		
		Dimension spinnerSize = new Dimension(38, 25);

		
		hoursModel = new SpinnerNumberModel(0, 0, 99, 1); // value, min, max, interval
		hoursSpinner = new JSpinner(hoursModel);
		hoursSpinner.setPreferredSize(spinnerSize);
		hoursSpinner.addChangeListener(timeChangedListener);
		
		minsModel = new SpinnerNumberModel(0, 0, 59, 1);
		minsSpinner = new JSpinner(minsModel);
		minsSpinner.setPreferredSize(spinnerSize);
		minsSpinner.addChangeListener(timeChangedListener);
		
		secsModel = new SpinnerNumberModel(0, 0, 59, 1);
		secsSpinner = new JSpinner(secsModel);
		secsSpinner.setPreferredSize(spinnerSize);
		secsSpinner.addChangeListener(timeChangedListener);
		
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(new JLabel(nameLabel));
		horizontalBox.add(hoursSpinner);
		horizontalBox.add(new JLabel(hoursLabel));
		horizontalBox.add(minsSpinner);
		horizontalBox.add(new JLabel(minsLabel));
		horizontalBox.add(secsSpinner);		
		horizontalBox.add(new JLabel(secsLabel));
		
		add(horizontalBox);
		
		// set the time value according to dataField
		dataFieldUpdated();
	}
	
	
	// called when dataField changes attributes
	public void dataFieldUpdated() {
		String timeValue = dataField.getAttribute(attributeName);
		
		int timeInSecs = getSecondsFromTimeValue(timeValue);
		
		updateTimeSpinners(timeInSecs);
	}

	
	/**
	 * This method is used to deal with the fact that time values used to be stored in "hh:mm:ss" format,
	 * but are now stored in seconds (as a string). 
	 * This method takes a string that may be in either format, and returns an integer of seconds. 
	 * 
	 * @param timeString		A String representation of time: either "hh:mm:ss" or seconds. 
	 * 
	 * @return		integer of time in seconds. 
	 */
	public static int getSecondsFromTimeValue(String timeString) {
		if ((timeString == null) || (timeString.length() == 0)) {
			return 0;
		}
		
		int timeInSecs;
		
		// this is the old way of storing time value "hh:mm:ss"
		String[] hrsMinsSecs = timeString.split(":");
		
		// if split into 3, use old system to get seconds from "hh:mm:ss"
		if (hrsMinsSecs.length == 3) {
			
			try {
				
				
				int hours = Integer.parseInt(hrsMinsSecs[0]);
				int mins = Integer.parseInt(hrsMinsSecs[1]);
				int secs = Integer.parseInt(hrsMinsSecs[2]);
					
				timeInSecs = hours*3600 + mins*60 + secs;
				
				return timeInSecs;
				
			} catch (Exception ex) {
				return 0;
			}
		} else {
			
			timeInSecs = Integer.parseInt(timeString);
			
			return timeInSecs;
		}
	}
	
	/**
	 * Listens for changes to any of the Time Spinners. 
	 * @author will
	 *
	 */
	public class TimeChangedListener implements ChangeListener {
		public void stateChanged(ChangeEvent arg0) {
			timeChanged();
		}
	}
	
	public void timeChanged() {
		
		int hours = Integer.parseInt(hoursModel.getValue().toString());
		int mins = Integer.parseInt(minsModel.getValue().toString());
		int secs = Integer.parseInt(secsModel.getValue().toString());
		
		int timeInSeconds = hours*3600 + mins*60 + secs;
		
		dataField.setAttribute(attributeName, timeInSeconds + "", true);
	}
	
	/**
	 * Updates the display with the current value of defaultTimeInSecs.
	 */
	private void updateTimeSpinners(int totalTimeInSeconds) {
		
		int seconds = totalTimeInSeconds;
		
		int hours = (seconds)/3600;
		int mins = (seconds = seconds - hours*3600)/60;
		int secs = (seconds - mins*60);
		
		hoursSpinner.removeChangeListener(timeChangedListener);
		hoursModel.setValue(hours);
		hoursSpinner.addChangeListener(timeChangedListener);
		minsSpinner.removeChangeListener(timeChangedListener);
		minsModel.setValue(mins);
		minsSpinner.addChangeListener(timeChangedListener);
		secsSpinner.removeChangeListener(timeChangedListener);
		secsModel.setValue(secs);
		secsSpinner.addChangeListener(timeChangedListener);
		
	}
	
	/**
	 * set enabled for all the components of TimeEditor. 
	 */
	public void setEnabled(boolean enabled) {
		hoursSpinner.setEnabled(enabled);
		minsSpinner.setEnabled(enabled);
		secsSpinner.setEnabled(enabled);
	}
	
	

}
