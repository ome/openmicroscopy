/*
 * org.openmicroscopy.shoola.env.ui.ActivityProcessEvent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.ui;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Event indicating that an activity has just finished.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ActivityProcessEvent 
	extends RequestEvent
{

	/** Reference to the activity that has terminated. */
	private ActivityComponent activity;
	
	/** Flag indicating that the activity has finished. */
	private boolean finished;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param activity Reference to the activity that has terminated.
	 * @param finished Pass <code>true</code> to indicate that the current 
	 *                 activity has finished, <code>false</code> otherwise.
	 */
	public ActivityProcessEvent(ActivityComponent activity, boolean finished)
	{
		this.activity = activity;
		this.finished = finished;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param activity Reference to the activity that has terminated.
	 */
	public ActivityProcessEvent(ActivityComponent activity)
	{
		this(activity, true);
	}
	
	/**
	 * Returns the activity.
	 * 
	 * @return See above.
	 */
	public ActivityComponent getActivity() { return activity; }
	
	/**
	 * Returns <code>true</code> if the activity is finished,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isFinished() { return finished; }
	
}
