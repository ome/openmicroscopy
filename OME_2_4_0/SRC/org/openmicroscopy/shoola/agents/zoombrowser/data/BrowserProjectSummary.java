/*
 * org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserProjectSummary
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

package org.openmicroscopy.shoola.agents.zoombrowser.data;

//Java imports
import java.util.List;
import java.util.Iterator;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;


/** 
 * An extension of 
 * {@link org.openmicroscopy.shoola.env.data.model.ProjectSummary}, 
 * adding a few extra methods for convenience
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
public class BrowserProjectSummary  extends ProjectSummary
{
	
	public BrowserProjectSummary() {}
	
	public BrowserProjectSummary(int id, String name)
	{
		super(id,name);
	}	
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { return new BrowserProjectSummary(); }
	
	public boolean hasDatasets() {
		return (getDatasets().size() >0);
	}
	
	public boolean hasDataset(DatasetData d) {
		if (d == null)
			return false;
		Iterator iter = getDatasets().iterator();
		boolean res = false;
		while (iter.hasNext()) {
			DatasetData ds = (DatasetData) iter.next();
			if (ds.getID() == d.getID())
				return true;
		}
		return res;
	}
	
	public boolean sharesDatasetsWith(ProjectSummary p) {
		List myDatasets = getDatasets();
		if (p == null)
			return false;
		if (myDatasets == null || myDatasets.size() == 0)
			return false;
		List otherDatasets = p.getDatasets();
		if (otherDatasets == null || otherDatasets.size() == 0)
			return false;
	
		// iterate to identify any matches.	
		// sorting each and matching would perhaps speed things up. 
		Iterator iter = myDatasets.iterator();
		BrowserDatasetData d1,d2;
		while (iter.hasNext()) {
			d1 = (BrowserDatasetData) iter.next();
			Iterator iter2 = otherDatasets.iterator();
			while (iter2.hasNext()) {
				d2 = (BrowserDatasetData) iter2.next();
				if (d1.getID() == d2.getID())
					return true;
			}
		}
		return false;
	}
}
