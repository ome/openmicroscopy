package ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tree.DataField;
import ui.FieldEditorTime.TimeChangedListener;

public class FormFieldTime extends FormField {
	
	String timeValue;
	
	int hours = 0;
	int mins = 0;
	int secs = 0;
	
	SpinnerModel hoursModel;
	JSpinner hoursSpinner;
	SpinnerModel minsModel;
	JSpinner minsSpinner;
	SpinnerModel secsModel;
	JSpinner secsSpinner;
	
	Timer timer;

	private TimeChangedListener timeChangedListener;

	private JButton startTimerButton;
	
	public FormFieldTime(DataField dataField) {
		
		super(dataField);
		
		timeValue = dataField.getAttribute(DataField.VALUE);
		
		convertTimeStringToInts();
		
		Dimension spinnerSize = new Dimension(45, 25);
		
		timeChangedListener = new TimeChangedListener();
		hoursModel = new SpinnerNumberModel(hours, 0, 99, 1);
		hoursSpinner = new JSpinner(hoursModel);
		hoursSpinner.setMaximumSize(spinnerSize);
		hoursSpinner.setPreferredSize(spinnerSize);
		hoursSpinner.addChangeListener(timeChangedListener);
		
		minsModel = new SpinnerNumberModel(mins, 0, 59, 1);
		minsSpinner = new JSpinner(minsModel);
		minsSpinner.setMaximumSize(spinnerSize);
		minsSpinner.setPreferredSize(spinnerSize);
		minsSpinner.addChangeListener(timeChangedListener);
		
		secsModel = new SpinnerNumberModel(secs, 0, 59, 1);
		secsSpinner = new JSpinner(secsModel);
		secsSpinner.setMaximumSize(spinnerSize);
		secsSpinner.setPreferredSize(spinnerSize);
		secsSpinner.addChangeListener(timeChangedListener);
		
		timer = new Timer(1000, new TimeElapsedListener());
		startTimerButton = new JButton("Start Countdown");
		startTimerButton.setEnabled(getTimeInSecs() > 0);
		startTimerButton.setBackground(null);
		startTimerButton.addActionListener(new StartTimerListener());
		
		Box timeBox = Box.createHorizontalBox();
		timeBox.add(hoursSpinner);
		timeBox.add(new JLabel("hrs "));
		timeBox.add(minsSpinner);
		timeBox.add(new JLabel("mins "));
		timeBox.add(secsSpinner);
		timeBox.add(new JLabel("secs "));
		timeBox.add(startTimerButton);
		
		horizontalBox.add(timeBox);
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
			
			dataField.setAttribute(DataField.VALUE, timeValue, true);
		}
	}
	
	public class StartTimerListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (!timer.isRunning() && getTimeInSecs() > 0)
				startCountDown();
			else 
				stopCountDown();
		}
	}
	
	public void startCountDown() {
		timer.start();
		startTimerButton.setText("Stop Countdown");
		enableTimeSpinners(false);
	}
	public void stopCountDown() {
		timer.stop();
		startTimerButton.setText("Start Countdown");
		enableTimeSpinners(true);
	}
	
	private void enableTimeSpinners(boolean enabled) {
		hoursSpinner.setEnabled(enabled);
		minsSpinner.setEnabled(enabled);
		secsSpinner.setEnabled(enabled);
	}
	
	public class TimeElapsedListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			updateHrsMinsSecs(getTimeInSecs() -1);
			updateTimeSpinners();
			
			if (getTimeInSecs() == 0) {
				JOptionPane.showMessageDialog(FormFieldTime.this, "Time's Up!", "Time Up", JOptionPane.ERROR_MESSAGE);
				stopCountDown();
			}
		}
	}
	
	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		timeValue = dataField.getAttribute(DataField.VALUE);
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
		
		startTimerButton.setEnabled((getTimeInSecs() > 0));
	}
	
	private int getTimeInSecs() {
		return hours*3600 + mins*60 + secs;
	}
	private void updateHrsMinsSecs(int seconds) {
		hours = seconds/3600;
		mins = (seconds - hours*3600)/60;
		secs = (seconds - hours*3600 - mins*60);
		
		System.out.println("FormFieldTime updateHrsMinsSecs = " + hours + " " + mins + " " + secs);
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
