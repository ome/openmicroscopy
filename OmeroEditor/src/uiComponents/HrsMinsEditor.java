 /*
 * treeEditingComponents.HrsMinsEditor 
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
package uiComponents;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;



//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class HrsMinsEditor 
	extends JPanel 
	implements PropertyChangeListener {
	
	protected JSpinner hrsSpinner;
	
	protected JSpinner minsSpinner;
	
	/**
	 * Bound property for this component. 
	 * Refers to the value of the HrsMins display in seconds. 
	 */
	public static final String TIME_IN_SECONDS = "timeInSeconds";
	
	protected int currentTimeInSecs = 0;
	
	public HrsMinsEditor() {
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBackground(null);
		
		hrsSpinner = new DoubleDigitSpinner(0, 0, 23, 1);
		hrsSpinner.addPropertyChangeListener(DoubleDigitSpinner.SPINNER_VALUE, this);
		
		minsSpinner = new DoubleDigitSpinner(0, 0, 59, 1);
		minsSpinner.addPropertyChangeListener(DoubleDigitSpinner.SPINNER_VALUE, this);
		
		add(hrsSpinner);
		add(new JLabel(":"));
		add(minsSpinner);
	}
	
	public void setHrsMins(int timeInSecs) {
		
		int hours = 0;
		int mins = 0;
		
		hours = timeInSecs / 3600;
		mins = (timeInSecs - hours*3600) / 60;
		
		hrsSpinner.setValue(hours);
		minsSpinner.setValue(mins);
		
		currentTimeInSecs = timeInSecs;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		
		if (DoubleDigitSpinner.SPINNER_VALUE.equals(evt.getPropertyName())) {
			int newTimeInSecs = getDisplayedTimeInSecs();
			
			this.firePropertyChange(TIME_IN_SECONDS, currentTimeInSecs, newTimeInSecs);
			
			currentTimeInSecs = newTimeInSecs;
		}
 		
	}
	
	public int getDisplayedTimeInSecs() {
		int hrs = new Integer(hrsSpinner.getValue().toString());
		int mins = new Integer(minsSpinner.getValue().toString());
		
		return (hrs * 3600) + (mins * 60);
	}
	
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		hrsSpinner.setEnabled(enabled);
		minsSpinner.setEnabled(enabled);
	}

}
