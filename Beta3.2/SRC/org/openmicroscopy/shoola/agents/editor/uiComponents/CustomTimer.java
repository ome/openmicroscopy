 /*
 * uiComponents.CustomTimer 
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a wrapper around the javax.swing.Timer. 
 * It extends this class to allow the timer to keep a record of seconds
 * for count-down. 
 * This allows UI classes to attach a timer to the data, so that other 
 * observers of the data can use the same timer. 
 * Needed for JTree because browsing the tree creates multiple renderings of 
 * the data, so the timer cannot be associated with the UI.
 * 
 * Start() begins counting down in seconds. When reaches 0, timer stops. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CustomTimer 
	extends Timer {

	/**
	 * The number of seconds on the clock. 
	 * Decremented every second while the timer is running.
	 */
	private int currentSecs;
	
	/**
	 * Creates an instance of this class.
	 * Specifies a Timer that will call actionListeners every second 
	 * (1000 milliseconds)
	 */
	public CustomTimer() {
		
		super(1000, null);
	}
	
	/**
	 * Overrides the superclass fireActionPerformed() method, to 
	 * decrement the seconds before calling
	 * super.fireActionPerformed(.
	 * This ensures that ActionListeners will get notified of the 
	 * new value of currentSeconds.
	 */
	protected void fireActionPerformed(ActionEvent e) {
		currentSecs--;
		if (currentSecs == 0) {
			stop();
		}
		super.fireActionPerformed(e);
	}
	
	/**
	 * Allows the setting of the number of seconds on the clock.
	 * 
	 * @param secs
	 */
	public void setCurrentSecs(int secs) {
		currentSecs = secs;
	}
	
	/**
	 * Get the current number of seconds on the clock. 
	 * 
	 * @return
	 */
	public int getCurrentSecs() {
		return currentSecs;
	}
	
	/**
	 * This allows a UI class that is using this timer to remove 
	 * other UI classes that may have been listening to it before.
	 * Eg. When browsing JTree, numerous views (renderings) of the 
	 * timer are created, and must become ActionListeners of this 
	 * class. 
	 * No way to remove themselves as Listeners when they become 
	 * invisible? 
	 */
	public void removeActionListeners() {
		ActionListener[] aLs = getActionListeners();
		
		for (int i=0; i<aLs.length; i++) {
			removeActionListener(aLs[i]);
		}
	}

}
