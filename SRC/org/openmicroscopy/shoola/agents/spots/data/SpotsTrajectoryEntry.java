/*
 * org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectoryEntry
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

package org.openmicroscopy.shoola.agents.spots.data;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.st.Location;
import org.openmicroscopy.ds.st.TrajectoryEntry;
/** 
 * An entry in a trajectory
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
public class SpotsTrajectoryEntry {
		
	
	private Location location;
	private TrajectoryEntry entry;
	
	public SpotsTrajectoryEntry(TrajectoryEntry entry,Location location) {
		this.entry = entry;
		this.location = location;		
	}
	
	public float getX() {
		return location.getTheX().floatValue();
	}
	
	public float getY() {
		return location.getTheY().floatValue();
	}
	
	public float getZ() {
		return location.getTheZ().floatValue();
	}
	
	public float getVal(int axis) {
		switch (axis) {
			case SpotsTrajectory.X:
				return getX();
			case SpotsTrajectory.Y:
				return getY();
			case SpotsTrajectory.Z:
				return getZ();
			default: // should never get here
				return Float.MIN_VALUE;
		}
	}
	
	public void dump() {
		System.err.println("trajectory entry.."+entry.getID()+", order .."+entry.getOrder());
		System.err.println("...feature.."+entry.getFeature().getID());
		System.err.println("pos: "+getX()+","+getY()+","+getZ());
	}
}