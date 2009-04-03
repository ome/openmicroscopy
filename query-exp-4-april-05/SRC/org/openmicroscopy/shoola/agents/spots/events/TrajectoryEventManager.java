/*
 * org.openmicroscopy.shoola.agents.spots.events.TrajectoryEventManager
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.spots.events;

//Java imports
import javax.swing.event.EventListenerList;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;

/** 
* A manager for trajectory events
* 
*
* @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
*
* @version 2.2
* <small>
* (<b>Internal version:</b> $Revision$ $Date$)
* </small>
* @since OME2.2
*/



public class TrajectoryEventManager {

	private EventListenerList listeners = new EventListenerList();

	private static long time = 0;
	private static int count = 0;
	
	private static SpotsTrajectory selectedTrajectory;
	
	
	
	public TrajectoryEventManager() {
		
	}
	
	//list management
	
	 public void addTrajectoryEventListener(TrajectoryEventListener
			 listener) {
	 	listeners.add(TrajectoryEventListener.class,
				 	listener);
 	}
	
	 public void removeTrajectoryEventListener(TrajectoryEventListener
	 	listener) {
			 listeners.remove(TrajectoryEventListener.class,
				 listener);
 	}
	
 	public void fireTrajectoryEvent(TrajectoryEvent event) {
 		
 		long start = System.currentTimeMillis();
 		
 		
		Object[] ls=listeners.getListenerList();
	 	for (int i = ls.length-2; i >=0; i-=2) {
		 	if (ls[i] == TrajectoryEventListener.class) {
			 	((TrajectoryEventListener) ls[i+1]).
				 	trajectoryChanged(event);
		 	}
	 	}

 		long end = System.currentTimeMillis();
 		time += end -start;
 		count++;
 	}
 	
 	public static void dumpTimes() {
 		double ave = (double)time/(double) count;
 		System.err.println("TrajectoryEventManager. "+count+" actions, time "+time+", ave "+ave);
 	}
 	
 	public static void clearSelection() {
 		if (selectedTrajectory != null)
 			selectedTrajectory.setSelected(false);
 		selectedTrajectory = null;
 	}
 	
 	public static void setSelection(SpotsTrajectory traj) {
 		selectedTrajectory = traj;
 	}
 	
}