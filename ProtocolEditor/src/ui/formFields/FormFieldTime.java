package ui.formFields;

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
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.components.TimeEditor;
import ui.fieldEditors.FieldEditorTime;

public class FormFieldTime extends FormField {
	
	// Up till 7th March 08, this string was hrs:mins:secs (eg "02:12:05" )
	// But better to store seconds. 
	String timeValue;
	
	// New way of storing time (after 7th March 08)
	int timeInSeconds;
	
	// for display purposes
	SpinnerModel hoursModel;
	JSpinner hoursSpinner;
	SpinnerModel minsModel;
	JSpinner minsSpinner;
	SpinnerModel secsModel;
	JSpinner secsSpinner;
	
	Timer timer;

	private TimeChangedListener timeChangedListener;

	private JButton startTimerButton;
	
	public FormFieldTime(IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		convertTimeStringToInts();
		
		Dimension spinnerSize = new Dimension(45, 25);
		
		timeChangedListener = new TimeChangedListener();

		
		hoursModel = new SpinnerNumberModel(0, 0, 99, 1);
		hoursSpinner = new JSpinner(hoursModel);
		((DefaultEditor)hoursSpinner.getEditor()).getTextField().addFocusListener(componentFocusListener);
		hoursSpinner.setMaximumSize(spinnerSize);
		hoursSpinner.setPreferredSize(spinnerSize);
		hoursSpinner.addChangeListener(timeChangedListener);
		
		minsModel = new SpinnerNumberModel(0, 0, 59, 1);
		minsSpinner = new JSpinner(minsModel);
		((DefaultEditor)minsSpinner.getEditor()).getTextField().addFocusListener(componentFocusListener);
		minsSpinner.setMaximumSize(spinnerSize);
		minsSpinner.setPreferredSize(spinnerSize);
		minsSpinner.addChangeListener(timeChangedListener);
		
		secsModel = new SpinnerNumberModel(0, 0, 59, 1);
		secsSpinner = new JSpinner(secsModel);
		((DefaultEditor)secsSpinner.getEditor()).getTextField().addFocusListener(componentFocusListener);
		secsSpinner.setMaximumSize(spinnerSize);
		secsSpinner.setPreferredSize(spinnerSize);
		secsSpinner.addChangeListener(timeChangedListener);
		
		timer = new Timer(1000, new TimeElapsedListener());
		startTimerButton = new JButton("Start Countdown");
		startTimerButton.addFocusListener(componentFocusListener);
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
		
		updateTimeSpinners();
	
		// enable or disable components based on the locked status of this field
		refreshLockedStatus();
	}
	
	/**
	 * This simply enables or disables all the editable components of the 
	 * FormField.
	 * Gets called (via refreshLockedStatus() ) from dataFieldUpdated()
	 * 
	 * @param enabled
	 */
	public void enableEditing(boolean enabled) {
		super.enableEditing(enabled);	
		
		enableTimeSpinners(enabled);
		
		startTimerButton.setEnabled(enabled);
		
		if (enabled) {
			startTimerButton.setEnabled((getTimeInSecs() > 0));
		}
	}
	
	
	public class TimeChangedListener implements ChangeListener {
		public void stateChanged(ChangeEvent arg0) {
			timeChanged();
		}
	}
	
	public void timeChanged() {
		int hours = Integer.parseInt(hoursModel.getValue().toString());
		int mins = Integer.parseInt(minsModel.getValue().toString());
		int secs = Integer.parseInt(secsModel.getValue().toString());
		
		timeInSeconds = hours*3600 + mins*60 + secs;
		
		timeValue = timeInSeconds + "";
		
		dataField.setAttribute(DataFieldConstants.SECONDS, timeValue, true);
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
		timeChanged();
	}
	
	private void enableTimeSpinners(boolean enabled) {
		hoursSpinner.setEnabled(enabled);
		minsSpinner.setEnabled(enabled);
		secsSpinner.setEnabled(enabled);
	}
	
	public class TimeElapsedListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			timeInSeconds--;
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
		
		convertTimeStringToInts();
		updateTimeSpinners();
	}
	
	/**
	 * See <code>FormField.getValueAttribute()</code>
	 * 
	 * Gets the name of the attribute where this field stores its "value".
	 * This is used eg. as the destination to copy the default value when defaults are loaded.
	 * Mostly this is DataFieldConstants.VALUE, but this method should be over-ridden by 
	 * subclasses if they want to store their value under a different attribute (eg "seconds" for TimeField)
	 * 
	 * @return	the name of the attribute that holds the "value" of this field
	 */
	public String getValueAttribute() {
		return DataFieldConstants.SECONDS;
	}
	
	
	private void updateTimeSpinners() {
		
		int seconds = timeInSeconds;
		int hours = seconds/3600;
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
		
		//startTimerButton.setEnabled((getTimeInSecs() > 0));
	}
	
	private int getTimeInSecs() {
		return timeInSeconds;
	}

	
	private void convertTimeStringToInts() {
		
		// this is the new way of storing time value (seconds)
		timeValue = dataField.getAttribute(DataFieldConstants.SECONDS);

		if (timeValue != null) {
			timeInSeconds = TimeEditor.getSecondsFromTimeValue(timeValue);
			
			// if the dataField has old VALUE attribute - delete this! 
			if (dataField.getAttribute(DataFieldConstants.VALUE) != null)
				dataField.setAttribute(DataFieldConstants.VALUE, null, false);
			
			return;
			
			/*
			 * For older files (pre 7th March 08) use the old value "hh:mm:ss"
			 */	
		} else {
			timeValue = dataField.getAttribute(DataFieldConstants.VALUE);
			
			// if this is still null, seconds = 0;
			if ((timeValue == null) || (timeValue.length() == 0)) {
				timeInSeconds = 0;
				return;
			}
			
			// otherwise, use the old system to get seconds from "hh:mm:ss"
			timeInSeconds = TimeEditor.getSecondsFromTimeValue(timeValue);
		}
	}

	
	public void setHighlighted(boolean highlight) {
		super.setHighlighted(highlight);
		// if the user highlighted this field by clicking the field (not the textBox itself) 
		// need to get focus, otherwise focus will remain elsewhere. 
		boolean hasFocus = (((DefaultEditor)hoursSpinner.getEditor()).getTextField().hasFocus() || 
				((DefaultEditor)minsSpinner.getEditor()).getTextField().hasFocus() || 
				((DefaultEditor)secsSpinner.getEditor()).getTextField().hasFocus() || 
				startTimerButton.hasFocus());
		if (highlight && !hasFocus)
			((DefaultEditor)hoursSpinner.getEditor()).getTextField().requestFocusInWindow();
	}
}
