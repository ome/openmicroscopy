/*
 * org.openmicroscopy.shoola.agents.spots.range.TrajComparator
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
import java.util.Comparator;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;

/** 
 * Abstract Comparator for comparing trajectory values
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
public abstract class TrajComparator implements Comparator {

	public TrajComparator() {
			
	}
	
	public int  compare(Object o1,Object o2) {
		SpotsTrajectory t1 = (SpotsTrajectory)o1;
		SpotsTrajectory t2 = (SpotsTrajectory)o2;

		double v1 = getCompValue(t1);
		double v2 = getCompValue(t2);
		/*double minz1 = t1.getMinZ();
		double minz2 = t2.getMinZ(); */
		if (v1 < v2) 
			return -1;
		else if (v1 > v2) 
			return 1;
		else 
			return 0;
		//can't just do shorter return (int) (minz1-minz2);
		// because if minz1-minz<1, converting to int will make diff. 0
		// and then the objects will look equal even if they aren't.
	}
	
	public abstract double getCompValue(SpotsTrajectory t);
}