 /*
 * treeEditingComponents.TimerField 
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

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.FieldPanel;
import org.openmicroscopy.shoola.agents.editor.model.DataFieldConstants;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TimeParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomTimer;
import org.openmicroscopy.shoola.agents.editor.uiComponents.HrsMinsSecsField;


/** 
 * A timer field. Displays Hrs:Mins:Secs using a HrsMinsSecsField class.
 * Also has a start-stop button to run a timer. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TimerField 
	extends AbstractParamEditor
	implements
	ActionListener,
	PropertyChangeListener {
	
	/**
	 * The attribute used to store the value of this parameter (time in seconds)
	 */
	private String attributeName = TimeParam.SECONDS;
	
	/**
	 * The Hours, Minuutes and Seconds editor. Fires property change events 
	 * when edited, and returns time in seconds. 
	 */
	HrsMinsSecsField hrsMinsSecs;
	
	/**
	 * An extension of the Swing timer. Counts down seconds. Keeps track of 
	 * seconds. A timer object may be attached to the parameter object itself,
	 * so that the counting down is not dependent on the UI.
	 */
	CustomTimer timer;
	
	/**
	 * A button for starting and stopping the timer. 
	 */
	JButton startTimerButton;
	
	/**
	 * The current time in seconds. 
	 */
	int timeInSeconds;
	
	/**
	 * Icon for start timer button
	 */
	Icon startTimerIcon;
	
	/**
	 * Icon displayed while the timer is running. 
	 */
	Icon stopTimerIcon;
	
	/**
	 * Creates an instance. 
	 * 
	 * @param param		The parameter we're editing.
	 */
	public TimerField(IParam param) {
		
		super(param);
		
		/*
		 * Get data and sets the value of timeInSeconds
		 */
		convertTimeStringToInts();
		
		/*
		 * Create and add an hrsMinsSecs field. 
		 * Add this as propertyChangeListener
		 */
		hrsMinsSecs = new HrsMinsSecsField();
		hrsMinsSecs.addPropertyChangeListener(
				HrsMinsSecsField.TIME_IN_SECONDS, this);
		this.add(hrsMinsSecs);
		
		this.add(Box.createHorizontalStrut(4));
		
		/*
		 * Check whether a timer object has been set for the Time parameter
		 * If the timer is running, get the current time.
		 * If the timer is not running, the time
		 * will be set when the start button is pressed. 
		 */
		Object t = ((TimeParam)param).getTimer();
		if ((t != null) && (t instanceof CustomTimer)) {
			timer = (CustomTimer)t;
			timer.removeActionListeners();	// make sure no other listeners
			if (timer.isRunning())		// need to display current time.
				timeInSeconds = timer.getCurrentSecs();
		} else {
			// if no timer exists, create one. 
			timer = new CustomTimer();
			((TimeParam)param).setTimer(timer);
		}
		timer.addActionListener(new TimeElapsedListener());
		
		/*
		 * A start-stop button for the timer. 
		 */
		IconManager iM = IconManager.getInstance();
		startTimerIcon = iM.getIcon(IconManager.TIMER_START_ICON);
		stopTimerIcon = iM.getIcon(IconManager.TIMER_STOP_ICON);
		startTimerButton = new CustomButton(startTimerIcon);
		startTimerButton.addActionListener(this);
		this.add(startTimerButton);
		
		/*
		 * update the view with this value. 
		 */
		updateTimerUI();
	}
	
	/**
	 * An ActionListener for the timer. 
	 * When the timer fires (each second), the value of timeInSeconds is 
	 * updated from the timer, and the Timer UI is updated. 
	 * If timer reaches 0 seconds, a message dialog is displayed, and 
	 * the parameter is updated with the new time. 
	 * 
	 * @author will
	 *
	 */
	public class TimeElapsedListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			timeInSeconds = timer.getCurrentSecs();
			updateTimerUI();
			
			if ((timeInSeconds == 0) && (TimerField.this.isVisible())) {
				JOptionPane.showMessageDialog(TimerField.this, 
						"Time's Up!", "Time Up", JOptionPane.ERROR_MESSAGE);
				
				attributeEdited(attributeName, timeInSeconds + "");
			}
		}
	}
	
	/**
	 * Updates the hrsMinsSecs fields with the new time value.
	 * If the timer is running, this editor will be disabled.
	 * Also updates the Start-stop button, with the start or stop icon.
	 * In addition, this method fires a FieldPanel.NODE_CHANGED_PROPERTY
	 * so that the JTree will refresh this node, even if this field is
	 * not current being edited (selected). 
	 */
	private void updateTimerUI() {
		hrsMinsSecs.setTimeInSecs(timeInSeconds);
		
		boolean timerRunning = timer.isRunning();
		startTimerButton.setIcon(timerRunning ? stopTimerIcon :startTimerIcon);
		hrsMinsSecs.setEnabled(! timerRunning);
		
		this.firePropertyChange(FieldPanel.NODE_CHANGED_PROPERTY, null, "timer");
	}

	/**
	 * Listens for changes to the HrsMinsSecsField.TIME_IN_SECONDS property.
	 * Updates the timeInSeconds value, and calls attributeEdited() to 
	 * save the new value to the parameter.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (HrsMinsSecsField.TIME_IN_SECONDS.equals(evt.getPropertyName())) {
			timeInSeconds = Integer.parseInt(evt.getNewValue().toString());
			//timer.setCurrentSecs(timeInSeconds);
			attributeEdited(attributeName, timeInSeconds + "");
		}
	}

	/**
	 * Called by the startTimer button.
	 * If the timer is not running, it starts. Otherwise, stops! 
	 */
	public void actionPerformed(ActionEvent e) {
		if (!timer.isRunning() && hrsMinsSecs.getTimeInSecs() > 0)
			startCountDown();
		else 
			stopCountDown();
	}
	
	/**
	 * Starts the timer.
	 */
	public void startCountDown() {
		timer.setCurrentSecs(timeInSeconds);
		timer.start();
		updateTimerUI();
	}
	
	/**
	 * Stops the timer. 
	 */
	public void stopCountDown() {
		timer.stop();
		updateTimerUI();
		attributeEdited(attributeName, hrsMinsSecs.getTimeInSecs() + "");
	}
	
	/**
	 * This method gets the value of time in seconds from the parameter object.
	 * Converts to Seconds (integer) and sets timeInSeconds. 
	 * Also takes care of old XML / data structure, where the time was stored
	 * as an hr:min:sec string in VALUE attribute. 
	 */
	private void convertTimeStringToInts() {
		
		IAttributes param = getParameter();
		// this is the new way of storing time value (seconds)
		String timeValue = param.getAttribute(TimeParam.SECONDS);

		if (timeValue != null) {
			timeInSeconds = getSecondsFromTimeValue(timeValue);
			
			// if the dataField has old VALUE attribute - delete this! 
			if (param.getAttribute(DataFieldConstants.VALUE) != null)
				param.setAttribute(DataFieldConstants.VALUE, null);
			
			return;
			
			/*
			 * For older files (pre 7th March 08) use the old value "hh:mm:ss"
			 */	
		} else {
			timeValue = param.getAttribute(DataFieldConstants.VALUE);
			
			// if this is still null, seconds = 0;
			if ((timeValue == null) || (timeValue.length() == 0)) {
				timeInSeconds = 0;
				return;
			}
			
			// otherwise, use the old system to get seconds from "hh:mm:ss"
			timeInSeconds = getSecondsFromTimeValue(timeValue);
		}
	}
	
	public String getEditDisplayName() {
		return "Edit Time";
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

}
