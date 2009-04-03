/*
 * org.openmicroscopy.shoola.agents.spots.ui.java3d.Extents
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

package org.openmicroscopy.shoola.agents.spots.ui.java3d;

//Java imports
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectoryEntry;
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectorySet;


/** 
 * Extents of the space in various dimensions, and related calculations. 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */





public class Extents {
	
	public static final float CUBE_SIDE=.8f;
	public static final float CUBE_HALF=CUBE_SIDE/2;
	
	// SpotsTrajectory.X is 0, TrajectoryY is 1, TrajectoryZ is 2
	private double aspects[] = {1.0,1.0,1.0};
	
	private SpotsTrajectorySet tSet;
	
	public Extents(SpotsTrajectorySet tSet) {
		this.tSet = tSet;
	}
	
	public void calculateTranslation(Vector3d v,SpotsTrajectoryEntry p) {
		double x = calculateOffset(SpotsTrajectory.X,p.getX());
		double y = calculateOffset(SpotsTrajectory.Y,p.getY());
		double z = calculateOffset(SpotsTrajectory.Z,p.getZ());
		
		v.set(x,y,z);
	}
	
	public Point3d calculatePoint(SpotsTrajectoryEntry p) {
		double x = calculateOffset(SpotsTrajectory.X,p.getX());
		double y = calculateOffset(SpotsTrajectory.Y,p.getY());
		double z = calculateOffset(SpotsTrajectory.Z,p.getZ());
		
		return new Point3d(x,y,z);
	}

	
	private double calculateOffset(int axis,double point) {
	
		return 
			newPointVal(point,axis,0f,
					(float)tSet.getExtent(axis));
	}
	
	public void adjustPointToFit(int axis,Vector3d v,SpotsTrajectoryEntry pt,
			float low,float high) {
		double newVal = newPointVal(pt,axis,low,high);
		//	System.err.println("new x is "+newX);
		if (axis == SpotsTrajectory.X) 
			v.set(newVal,v.y,v.z);
		else if (axis == SpotsTrajectory.Y)
			v.set(v.x,newVal,v.z);
		else 
			v .set(v.x,v.y,newVal);
	}
	
	public void adjustToFit(int axis,Point3d pt,SpotsTrajectoryEntry tp,float low,
			float high) {
		double newVal = newPointVal(tp,axis,low,high);
	
		if (axis == SpotsTrajectory.X)
			pt.set(newVal,pt.y,pt.z); 
		else if (axis == SpotsTrajectory.Y)
			pt.set(pt.x,newVal,pt.z);
		else 
			pt.set(pt.x,pt.y,newVal);
	}
	
	private double newPointVal(SpotsTrajectoryEntry pt,int axis,
			float low,float high) {
		double val = pt.getVal(axis);
		return newPointVal(val,axis,low,high);
	}
	
	private  double newPointVal(double val,int axis,
				float low,float high) {
		double side = getExtent(axis);
		if (low == high) { // single plane
			return 1.0;
		}
		else {
			double ratio = (val-low)/(high-low);
			return ratio*side-side/2;
		}
	}
	
	private  double getAspect(int axis) {
		return aspects[axis];
	}
	
	public  void setAspect(int axis,double aspect) {
		aspects[axis]=aspect;
	}
	
	
	public  double getExtent(int axis) {
		return getAspect(axis)*CUBE_SIDE;
	}
	
	public  void setScaledAspects(boolean v) {
	
		aspects[SpotsTrajectory.X] =1.0;
		if (v == false) {
			aspects[SpotsTrajectory.Y] =1.0;
			aspects[SpotsTrajectory.Z] = 1.0;
		}
		else {
			aspects[SpotsTrajectory.Y] = tSet.getExtentY()/tSet.getExtentX();
			aspects[SpotsTrajectory.Z] = 4*tSet.getExtentZ()/tSet.getExtentX();
		}
	}
	
	public double getFrontOffset(int axis) {
		return -getExtent(axis)/2;
	}
}