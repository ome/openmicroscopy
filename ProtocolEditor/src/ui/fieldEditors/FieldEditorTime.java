package ui.fieldEditors;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;

public class FieldEditorTime extends FieldEditor {
	
	String timeValue;
	int hours = 0;
	int mins = 0;
	int secs = 0;
	
	TimeChangedListener timeChangedListener;
	
	SpinnerModel hoursModel;
	JSpinner hoursSpinner;
	SpinnerModel minsModel;
	JSpinner minsSpinner;
	SpinnerModel secsModel;
	JSpinner secsSpinner;
	
	public FieldEditorTime(IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		// time String is in the form HHMMSS
		
		timeValue = dataField.getAttribute(DataFieldConstants.DEFAULT);
		if (timeValue != null) {
			convertTimeStringToInts();
		}
		
		timeChangedListener = new TimeChangedListener();
		
		hoursModel = new SpinnerNumberModel(hours, 0, 99, 1); // value, min, max, interval
		hoursSpinner = new JSpinner(hoursModel);
		// ((JSpinner.DefaultEditor)hoursSpinner.getEditor()).getTextField().setColumns(2);
		hoursSpinner.addChangeListener(timeChangedListener);
		
		minsModel = new SpinnerNumberModel(mins, 0, 59, 1);
		minsSpinner = new JSpinner(minsModel);
		minsSpinner.addChangeListener(timeChangedListener);
		
		secsModel = new SpinnerNumberModel(secs, 0, 59, 1);
		secsSpinner = new JSpinner(secsModel);
		secsSpinner.addChangeListener(timeChangedListener);
		
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(new JLabel("Default time: "));
		horizontalBox.add(hoursSpinner);
		horizontalBox.add(new JLabel("h "));
		horizontalBox.add(minsSpinner);
		horizontalBox.add(new JLabel("m "));
		horizontalBox.add(secsSpinner);		
		horizontalBox.add(new JLabel("s "));
		
		attributeFieldsPanel.add(horizontalBox);
	}
	
	
	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		timeValue = dataField.getAttribute(DataFieldConstants.DEFAULT);
		convertTimeStringToInts();
		
		updateTimeSpinners();
	}

	private void updateTimeSpinners() {
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
	
	public class TimeChangedListener implements ChangeListener {
		public void stateChanged(ChangeEvent arg0) {
			String hrs = hoursModel.getValue().toString();
			if (hrs.length() ==1)
				hrs = "0" + hrs;
			String mns = minsModel.getValue().toString();
			if (mns.length() == 1)
				mns = "0" + mns;
			String scs = secsModel.getValue().toString();
			if (scs.length() == 1)
				scs = "0" + scs;
			timeValue = hrs + ":" + mns + ":" + scs;
			convertTimeStringToInts();	// doesn't do much!
			
			dataField.setAttribute(DataFieldConstants.DEFAULT, timeValue, true);
		}
	}

	private void convertTimeStringToInts() {
		if ((timeValue == null) || (timeValue.length() == 0)) {
			hours = 0;
			mins = 0;
			secs = 0;
			return;
		}
		try {
			String[] hrsMinsSecs = timeValue.split(":");
			hours = Integer.parseInt(hrsMinsSecs[0]);
			mins = Integer.parseInt(hrsMinsSecs[1]);
			secs = Integer.parseInt(hrsMinsSecs[2]);
		} catch (Exception ex) {
			hours = 0;
			mins = 0;
			secs = 0;
		}
	}
}
