/*
 * org.openmicroscopy.shoola.agents.spots.ui.java3d.LineGeometry
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
import com.sun.j3d.utils.geometry.Primitive;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineStripArray;
import javax.vecmath.Point3d;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectoryEntry;


/** 
 * Geometry for the line connecting points of a trajectory 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */



public class LineGeometry extends LineStripArray {

	  private SpotsTrajectory traj;
	  Point3d pts[];
	  private Extents extents;
 	
	  public LineGeometry(SpotsTrajectory t,Extents extents)	  {
 	  	super(t.getLength(),GeometryArray.COORDINATES,new int[] {t.getLength()});
 	    this.traj=t;
 	    this.extents = extents;
 	    
 	    // set coordinates
 	    pts = new Point3d[t.getLength()];
 	    for (int i = 0; i < t.getLength(); i++) {
 	    	SpotsTrajectoryEntry pt = traj.getPoint(i);
 	    	pts[i] = extents.calculatePoint(pt);
 	    }
 	    
 	    //
 	    setCapability(GeometryArray.ALLOW_COORDINATE_READ);
 	    setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
 	    setCapability(GeometryArray.ALLOW_REF_DATA_READ);
	    setCapability(GeometryArray.ALLOW_REF_DATA_WRITE);
	    setCapability(GeometryArray.ALLOW_INTERSECT);
	    setCapability(Primitive.ENABLE_GEOMETRY_PICKING);
 	    setCoordinates(0,pts);
 	    
	  }
 	  
 	  public void adjustToFit(int axis,float low,float high) {
 	  
 	  	for (int i = 0; i < traj.getLength(); i++) {
 	  		adjustToFit(axis,low,high,i);
 	  	}
 	  	setCoordinates(0,pts);
 	  }
 	  
 	  
 	  private void adjustToFit(int axis,float low,float high,int i) {
 	  	SpotsTrajectoryEntry pt = traj.getPoint(i);
 	  	extents.adjustToFit(axis,pts[i],pt,low,high);
 	  }
}
