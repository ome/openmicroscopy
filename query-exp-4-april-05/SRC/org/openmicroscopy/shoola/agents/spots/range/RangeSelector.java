/*
 * org.openmicroscopy.shoola.agents.spots.range.RangeSelector
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
package org.openmicroscopy.shoola.agents.spots.range;

//Java imports
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectorySet;

/** 
 * Code for managing selection of a subset of an axis range.
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



public class RangeSelector {
	
	private int axis;
	
	private SpotsTrajectory[] mins;
	private SpotsTrajectory[] maxs;
	
	// indices pointing to left-most and right-most items included in current
	// range. initially, min (left-most) pos is zero and max is n-1,
	// where n is the number of items in the list.
	// curMaxPos will be initialized in constructor.
	private int curMinPos=0;
	private int curMaxPos;
	
	
	// flags for when we hit boundaries
	private boolean minHitMaxEnd = false;
	private boolean maxHitMinEnd = false;
	
	private int prevLow=0;
	private int prevHigh;
	
	public static RangeSelector getXRangeSelector(SpotsTrajectorySet tSet) {
		return new RangeSelector(SpotsTrajectory.X,tSet,new TrajMinXComparator(),new TrajMaxXComparator());
	}

	public static RangeSelector getYRangeSelector(SpotsTrajectorySet tSet) {
		return new RangeSelector(SpotsTrajectory.Y,tSet,new TrajMinYComparator(),new TrajMaxYComparator());
	}
	
	public static RangeSelector getZRangeSelector(SpotsTrajectorySet tSet) {
		return new RangeSelector(SpotsTrajectory.Z,tSet,new TrajMinZComparator(),new TrajMaxZComparator());
	}
	
	public static RangeSelector getRangeSelector(int traj,SpotsTrajectorySet tSet) {
		if (traj == SpotsTrajectory.X)
			return getXRangeSelector(tSet);
		else if (traj == SpotsTrajectory.Y)
			return getYRangeSelector(tSet);
		else 
			return getZRangeSelector(tSet);
	}
	public RangeSelector(int axis,SpotsTrajectorySet tSet,Comparator minComp,
			Comparator maxComp) {
		List items = tSet.getTrajectories();
		this.axis = axis;
		mins = getIndexArray(minComp,items);
		maxs = getIndexArray(maxComp,items);
	
		curMaxPos = items.size()-1;
		prevHigh = (int) tSet.getExtent(axis);
	 }
	
	private SpotsTrajectory[] getIndexArray(Comparator comp,List items) {
		
		Collections.sort(items,comp);
		
		Object[] objs = items.toArray();
		SpotsTrajectory[] res = new SpotsTrajectory[objs.length];
		for (int i = 0; i < objs.length; i++) {
			res[i]=(SpotsTrajectory) objs[i];
		}
		return res;
	}
	
	public void setRange(int low,int high) {

		// first do low
		if (low < prevLow) {
				// if I've moved the pointer to the left
			curMinPos =addMin(low,curMinPos,mins); // add things in
		}
		else if (low > prevLow) {
			curMinPos = removeMin(low,curMinPos,mins); // take things out.
		}
				
		prevLow = low;
		//	then do high
		if (high > prevHigh) {
			// moved to the right. add in
			curMaxPos = addMax(high,curMaxPos,maxs);
		}
		else if (high < prevHigh) {  // moved to the left, remove
			curMaxPos = removeMax(high,curMaxPos,maxs);
		} 		
		
		prevHigh = high;
		
	}
	
	public int addMin(int low,int curPos,SpotsTrajectory[] objs) {
		int pos = curPos;
		if (minHitMaxEnd == false)
			pos =pos-1;
		else
			minHitMaxEnd = false;
		while (pos >=0 && objs[pos].getMin(axis) >= low) {
			objs[pos].adjustFilterCount(1); //add it in
			pos--;
		}
		pos++;
		return pos;
	}
	
	public int removeMin(int low,int curPos,SpotsTrajectory[] objs) {
		int pos = curPos;
		while (pos < objs.length && 
				objs[pos].getMin(axis) < low
				&& minHitMaxEnd == false)  {
			objs[pos].adjustFilterCount(-1);
			pos++;
		}
		if (pos == objs.length) {
			pos=objs.length-1;
			minHitMaxEnd = true;
		}
		return pos;
	}
	
	public int addMax(int high,int curPos,SpotsTrajectory[] objs) {
		int pos = curPos;
		if (maxHitMinEnd == false)
			pos++;
		else 
			maxHitMinEnd = false;
		while (pos < objs.length 
				&& objs[pos].getMax(axis) <= high) {
			objs[pos].adjustFilterCount(1);
			pos++;
		} 
		pos--;
		return pos;
	}
	
	public int removeMax(int high,int curPos,SpotsTrajectory[] objs) {
		int pos =curPos;
		while (pos >= 0 && 
				objs[pos].getMax(axis) > high &&
				maxHitMinEnd == false) {
			objs[pos].adjustFilterCount(-1);
			pos--;
		}
		if (pos <0) {
			pos = 0;
			maxHitMinEnd = true;
		}
		return pos; // so final pos is past last one that's included.
	}
	
	
}