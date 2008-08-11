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
package treeEditingComponents;

//Java imports

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import tree.DataFieldConstants;
import treeModel.fields.FieldPanel;
import treeModel.fields.IParam;
import treeModel.fields.TimeParam;
import ui.components.TimeEditor;
import uiComponents.CustomButton;
import uiComponents.CustomTimer;
import uiComponents.HrsMinsSecsField;
import util.ImageFactory;

//Third-party libraries

//Application-internal dependencies

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
	extends Box
	implements ITreeEditComp,
	ActionListener,
	PropertyChangeListener {

	private IParam param;
	
	private String attributeName = TimeParam.SECONDS;
	
	HrsMinsSecsField hrsMinsSecs;
	
	CustomTimer timer;
	
	JButton startTimerButton;
	
	int timeInSeconds;
	
	Icon startTimerIcon;
	
	Icon stopTimerIcon;
	
	public TimerField(IParam param) {
		super(BoxLayout.X_AXIS);
		
		this.param = param;
		
		/*
		 * Gets data and sets the value of timeInSeconds
		 */
		convertTimeStringToInts();
		
		hrsMinsSecs = new HrsMinsSecsField();
		hrsMinsSecs.addPropertyChangeListener(
				HrsMinsSecsField.TIME_IN_SECONDS, this);
		
		this.add(hrsMinsSecs);
		this.add(Box.createHorizontalStrut(4));
		
		Object t = ((TimeParam)param).getTimer();
		if ((t != null) && (t instanceof CustomTimer)) {
			timer = (CustomTimer)t;
			timer.removeActionListeners();	// make sure no other listeners
			timeInSeconds = timer.getCurrentSecs();
		} else {
			timer = new CustomTimer();
			timer.setCurrentSecs(timeInSeconds);
			((TimeParam)param).setTimer(timer);
		}
		timer.addActionListener(new TimeElapsedListener());
		
		ImageFactory imF = ImageFactory.getInstance();
		startTimerIcon = imF.getIcon(ImageFactory.TIMER_START_ICON);
		stopTimerIcon = imF.getIcon(ImageFactory.TIMER_STOP_ICON);
		startTimerButton = new CustomButton(startTimerIcon);
		startTimerButton.addActionListener(this);
		this.add(startTimerButton);
		
		updateTimeSpinners();
	}
	
	public void attributeEdited(String attributeName, String newValue) {
		this.firePropertyChange(ITreeEditComp.VALUE_CHANGED_PROPERTY , 
				null, newValue);
	}
	
	
	public class TimeElapsedListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			timeInSeconds = timer.getCurrentSecs();
			System.out.println("TimerField timeElapsed " + timeInSeconds);
			updateTimeSpinners();
			
			if ((timeInSeconds == 0) && (TimerField.this.isVisible())) {
				JOptionPane.showMessageDialog(TimerField.this, 
						"Time's Up!", "Time Up", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void updateTimeSpinners() {
		hrsMinsSecs.setTimeInSecs(timeInSeconds);
		
		boolean timerRunning = timer.isRunning();
		startTimerButton.setIcon(timerRunning ? stopTimerIcon :startTimerIcon);
		hrsMinsSecs.setEnabled(! timerRunning);
		
		this.firePropertyChange(FieldPanel.NODE_CHANGED_PROPERTY, null, "timeInSeconds");
	}

	public String getAttributeName() {
		return attributeName;
	}

	public IParam getParameter() {
		return param;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		
		if (HrsMinsSecsField.TIME_IN_SECONDS.equals(evt.getPropertyName())) {
			timeInSeconds = Integer.parseInt(evt.getNewValue().toString());
			timer.setCurrentSecs(timeInSeconds);
			attributeEdited(attributeName, timeInSeconds + "");
		}
	}

	/**
	 * Called by the startTimer button.
	 */
	public void actionPerformed(ActionEvent e) {
		if (!timer.isRunning() && hrsMinsSecs.getTimeInSecs() > 0)
			startCountDown();
		else 
			stopCountDown();
	}
	
	public void startCountDown() {
		timer.start();
		updateTimeSpinners();
	}
	public void stopCountDown() {
		timer.stop();
		updateTimeSpinners();
		attributeEdited(attributeName, hrsMinsSecs.getTimeInSecs() + "");
	}
	
	/**
	 * This method gets the value of time in seconds from the parameter object.
	 * Converts to Seconds (integer) and sets timeInSeconds. 
	 * Also takes care of old XML / data structure, where the time was stored
	 * as an hr:min:sec string in VALUE attribute. 
	 */
	private void convertTimeStringToInts() {
		
		// this is the new way of storing time value (seconds)
		String timeValue = param.getAttribute(DataFieldConstants.SECONDS);

		if (timeValue != null) {
			timeInSeconds = TimeEditor.getSecondsFromTimeValue(timeValue);
			
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
			timeInSeconds = TimeEditor.getSecondsFromTimeValue(timeValue);
		}
	}

}
