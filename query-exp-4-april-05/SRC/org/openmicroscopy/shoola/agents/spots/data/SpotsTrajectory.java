/*
 * org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory
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
import java.util.ArrayList;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.st.Extent;
import org.openmicroscopy.ds.st.Location;
import org.openmicroscopy.ds.st.Trajectory;
import org.openmicroscopy.ds.st.TrajectoryEntry;
import org.openmicroscopy.shoola.agents.spots.events.TrajectoryEvent;
import org.openmicroscopy.shoola.agents.spots.events.TrajectoryEventManager;
/** 
 * A trajectory object
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

public class SpotsTrajectory {

	private static SpotsTrajectory previousSelection =null;
	
	
	private Trajectory trajectory;
	public static final int X=0;
	public static final int Y=1;
	public static final int Z=2;
	
	
	private static int MAX_FILTERS=6;
	public static final float LABEL_FACTOR=.2f;
	
	
	private ArrayList entries = new ArrayList();
	
	// non-statics are particular to this trajectory
	private float maxX =  -Float.MAX_VALUE;
	private float maxY =  -Float.MAX_VALUE;
	private float maxZ =  -Float.MAX_VALUE;
	private float minX =  Float.MAX_VALUE;
	private float minY =  Float.MAX_VALUE;
	private float minZ =  Float.MAX_VALUE;


	private int maxSz = Integer.MIN_VALUE;
	
	private float curMin=0.0f;
	private float curMax=0.0f;
	
	
	private TrajectoryEventManager manager = new TrajectoryEventManager();
	private int filterCount=MAX_FILTERS;
	
	public SpotsTrajectory() {
		
	}
	
	
	public SpotsTrajectory(TrajectoryEntry entry,Location location,Extent extent) {
		addPoint(entry,location,extent);
	}
	
	public void addPoint(TrajectoryEntry entry,Location location,Extent extent) {
		trajectory = entry.getTrajectory();
		SpotsTrajectoryEntry ste = new SpotsTrajectoryEntry(entry,location,extent);
		// where is this entry in the list
		int order = entry.getOrder().intValue();
		if (order > entries.size()) {
			entries.ensureCapacity(order+1);
			// pad out so i have enough things in array.
			for (int i = entries.size(); i < order; i++) {
				entries.add(null);
			}
		}
		entries.set(order-1,ste);
		
		if (ste.getX() > maxX)
			maxX=ste.getX();
	
		if (ste.getY() > maxY) 
			maxY = ste.getY();
		
		
		if (ste.getZ() > maxZ) 
			maxZ=ste.getZ();
		
			
			
		if (ste.getX() < minX) 
			minX =ste.getX();
					
		if (ste.getY() < minY) 		
			minY=ste.getY();
	
		if (ste.getZ() < minZ) 
			minZ=ste.getZ();
		
		if (ste.getSize() > maxSz)
			maxSz = ste.getSize();
	
	}
	

	public Trajectory getTrajectory() {
		return trajectory;
	}
		

	public void dump() {
		System.err.println("===========");
		System.err.println("trajectory..."+trajectory.getID());
		System.err.println("average velocity.."+trajectory.getAverageVelocity());
		System.err.println("total distance..."+trajectory.getTotalDistance());
		System.err.println(" # of entries..."+getLength());
		for (int i = 0; i < getLength(); i++) {
			SpotsTrajectoryEntry ste = getPoint(i);
			ste.dump();
		}
		System.err.println("x...min "+getMinX()+", max "+getMaxX());
		System.err.println("y...min "+getMinY()+", max "+getMaxY());
		System.err.println("z...min "+getMinZ()+", max "+getMaxZ());
	}
	public int getLength() {
		return entries.size();
	}
	
	
	public SpotsTrajectoryEntry getPoint(int i) {
		return (SpotsTrajectoryEntry) entries.get(i);
	}

		
	public float getMaxX() {
		return maxX;
	}

	public float getMaxY() {
		return maxY;
	}

	public float getMaxZ() {
		return maxZ;
	}

	public float getMinX() {
		return minX;
	}

	public float getMinY() {
		return minY;
	}

	public float getMinZ() {
		return minZ;
	}
	
	public float getMin(int which) {
		switch(which) {
			case X:
				return minX;
			case Y:
				return minY;
			case Z:
				return minZ;
			default:
				return -1;
		}
	}

	public float getMax(int which) {
		switch(which) {
			case X:
				return maxX;
			case Y:
				return maxY;
			case Z:
				return maxZ;
			default:
				return -1;
		}
	}
	
	public int getMaxSz() {
		return maxSz;
	}


	public static String getLabel (int which) {
		switch(which) {
			case X:
					return new String("X");
			case Y:
					return new String("Y");
			case Z:
					return new String("Z");
			default:
					return null;
		}
	}
	
	
	public boolean isVisible() {
		return filterCount == MAX_FILTERS;
	}
	  	
  	public TrajectoryEventManager getManager() {
		return manager;
	}
	
	public void setScaled(boolean v) {
		manager.fireTrajectoryEvent(
			new TrajectoryEvent(this,TrajectoryEvent.SCALE,v));
	}
	
	public void adjustFilterCount(int delta) {
		filterCount += delta;
		if (filterCount < 0)
			filterCount = 0;
		else if (filterCount > MAX_FILTERS)
			filterCount = MAX_FILTERS;
		boolean vis = filterCount == MAX_FILTERS;
		manager.fireTrajectoryEvent(new TrajectoryEvent(this,TrajectoryEvent.VISIBLE,vis));
	}
	

	
	public void setSelected(boolean v) {
		if (v == true) {
			TrajectoryEventManager.clearSelection();
			TrajectoryEventManager.setSelection(this);
		}
		manager.fireTrajectoryEvent(new 
				TrajectoryEvent(this,TrajectoryEvent.SELECT,v));
	}
	
}


