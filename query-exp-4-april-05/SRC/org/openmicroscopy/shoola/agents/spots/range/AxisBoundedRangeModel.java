/*
 * org.openmicroscopy.shoola.agents.spots.range.AxisBoundedRangeModel
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
import javax.swing.DefaultBoundedRangeModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;

/** 
 * A {@link BoundedRangeModel} that tracks which axis it is tied to,
 * along with strings for positions on axis
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

public class AxisBoundedRangeModel extends DefaultBoundedRangeModel{
	
	private int axis;
	
	public AxisBoundedRangeModel(int axis,int min,int max, int low,int high) {
		super(low,high-low,min,max);
		this.axis =axis;
	}	
	
	public int getAxis() {
		return axis;
	}
	
	
	public int getMax() {
		return getValue()+getExtent();
	}
	
	public String getLowTickString() {
		int low = getValue();
		int high = getMax();
		int lowval =  (int) (low+SpotsTrajectory.LABEL_FACTOR*(high-low));
		return Integer.toString(lowval);
	}
	
	public String getHighTickString() {
		int low = getValue();
		int high = getMax();
		int highval =  (int) (low+(1-SpotsTrajectory.LABEL_FACTOR)*(high-low));
		return Integer.toString(highval);
	}
}

