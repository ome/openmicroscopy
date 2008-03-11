package ui.fieldEditors;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.components.AlarmSetter;

public class FieldEditorTime extends FieldEditor {
	
	String timeValue;
	
	int defaultTimeInSecs;
	
	TimeChangedListener timeChangedListener;
	
	SpinnerModel daysModel;
	JSpinner daysSpinner;
	SpinnerModel hoursModel;
	JSpinner hoursSpinner;
	SpinnerModel minsModel;
	JSpinner minsSpinner;
	SpinnerModel secsModel;
	JSpinner secsSpinner;
	
	AlarmSetter alarmSetter;
	
	public FieldEditorTime(IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		// time String is in the form HHMMSS
		
		timeChangedListener = new TimeChangedListener();
		
		Dimension spinnerSize = new Dimension(38, 25);
		
		daysModel = new SpinnerNumberModel(0, 0, 99, 1);
		daysSpinner = new JSpinner(daysModel);
		daysSpinner.setPreferredSize(spinnerSize);
		daysSpinner.addChangeListener(timeChangedListener);
		
		hoursModel = new SpinnerNumberModel(0, 0, 23, 1); // value, min, max, interval
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
		horizontalBox.add(new JLabel("Default: "));
		horizontalBox.add(daysSpinner);
		horizontalBox.add(new JLabel("d "));
		horizontalBox.add(hoursSpinner);
		horizontalBox.add(new JLabel("h "));
		horizontalBox.add(minsSpinner);
		horizontalBox.add(new JLabel("m "));
		horizontalBox.add(secsSpinner);		
		horizontalBox.add(new JLabel("s "));
		
		attributeFieldsPanel.add(horizontalBox);
		
		alarmSetter = new AlarmSetter(dataField);
		attributeFieldsPanel.add(alarmSetter);
		
		// get the value from dataField
		convertTimeStringToInts();
		// display
		updateTimeSpinners();
	}
	
	
	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		timeValue = dataField.getAttribute(DataFieldConstants.DEFAULT);
		convertTimeStringToInts();
		
		updateTimeSpinners();
	}

	
	/**
	 * Updates the display with the current value of defaultTimeInSecs.
	 */
	private void updateTimeSpinners() {
		
		int seconds = defaultTimeInSecs;
		int days = seconds/(24*3600);
		int hours = (seconds = seconds - days*24*3600)/3600;
		int mins = (seconds = seconds - hours*3600)/60;
		int secs = (seconds - mins*60);
		
		daysSpinner.removeChangeListener(timeChangedListener);
		daysModel.setValue(days);
		daysSpinner.addChangeListener(timeChangedListener);
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
	 * Listens for changes to any of the Time Spinners. 
	 * @author will
	 *
	 */
	public class TimeChangedListener implements ChangeListener {
		public void stateChanged(ChangeEvent arg0) {
			timeChanged();
		}
	}
	
	/**
	 * Called when a user changes the time. 
	 * Updates the local defaultTimeInSecs integer, then saves the new value to dataField (DEFAULT attribute)
	 */
	public void timeChanged() {
		int days = Integer.parseInt(daysModel.getValue().toString());
		int hours = Integer.parseInt(hoursModel.getValue().toString());
		int mins = Integer.parseInt(minsModel.getValue().toString());
		int secs = Integer.parseInt(secsModel.getValue().toString());
		
		defaultTimeInSecs = days*24*3600 + hours*3600 + mins*60 + secs;
		
		timeValue = defaultTimeInSecs + "";
		
		dataField.setAttribute(DataFieldConstants.DEFAULT, timeValue, true);
	}

	/**
	 * Gets the time-value from dataField (DEFAULT attribute) and converts it into seconds and saves it.
	 * Originally, time was saved as "hh:mm:ss" but now it is saved as seconds.
	 * Try both formats (to allow for old files and new ones).
	 * If the old format is used, calculate seconds and save "seconds" in 
	 * the DEFAULT attribute (overwrite "hh:mm:ss")
	 */
	private void convertTimeStringToInts() {
		
		timeValue = dataField.getAttribute(DataFieldConstants.DEFAULT);
		// if this is null, seconds = 0;
		
		defaultTimeInSecs = getSecondsFromTimeValue(timeValue);
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
				
				// update the default with seconds - false- don't add to undo/redo
				return timeInSecs;
				
			} catch (Exception ex) {
				return 0;
			}
		} else {
			
			// otherwise, time is stored in seconds. 
			timeInSecs = Integer.parseInt(timeString);
			
			return timeInSecs;
		}
	}
}
