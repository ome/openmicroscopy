/*
 * org.openmicroscopy.shoola.agents.spots.ui.java2d.XYTrajectory2D;
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

package org.openmicroscopy.shoola.agents.spots.ui.java2d;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectoryEntry;
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;

/** 
 * 2D trajectory in the XY plane 
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

public class XYTrajectory2D extends Trajectory2D { 
	
	
	public XYTrajectory2D(TrajectoryCanvas canvas,SpotsTrajectory trajectory) {
		super(canvas,trajectory);		 
	}
	
	public float getHorizCoord(SpotsTrajectoryEntry pt,GridDimensions dimensions) {
		return (float) dimensions.getHorizCoord(pt.getX());	
	}

	public float getVertCoord(SpotsTrajectoryEntry pt,GridDimensions dimensions) {
		return (float) dimensions.getVertCoord(pt.getY());	
	}

	
	public double getVertMin() { 
		return trajectory.getMinY();
	}
	
	public double getVertMax() {
		return trajectory.getMaxY();
	}
	
	public double getHorizMin() {
		return trajectory.getMinX();
	}

	public double getHorizMax() {
		return trajectory.getMaxX();
	}

}
	
