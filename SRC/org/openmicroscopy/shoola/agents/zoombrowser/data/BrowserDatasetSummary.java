/*
 * org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserDatasetSummary
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
import java.lang.Comparable;
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.zoombrowser.piccolo.DatasetNode;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.DataObject;


/** 
 * An extension of 
 * {@link org.openmicroscopy.shoola.env.data.model.DatasetSummary}, 
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
public class BrowserDatasetSummary  extends DatasetSummary implements Comparable
{
	
	/** the visual representation of this dataset in the browser */
	private DatasetNode node;
	
	/** images in the dataset */
	private Collection images = null;
	
	public BrowserDatasetSummary() {}
	
	public BrowserDatasetSummary(int id, String name)
	{
		super(id,name);
	}	
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { return new BrowserDatasetSummary(); }

	public void setNode(DatasetNode node) {
		this.node = node;
	}
	
	public DatasetNode getNode() { return node; }
	
	/**
	 * @return
	 */
	public Collection getImages() {
		return images;
	}

	/**
	 * @param collection
	 */
	public void setImages(Collection collection) {
		images = collection;
	}
	
	public int getImageCount() {
		if (images == null)
			return 0;
		else 
			return images.size();
		
	}
	
	
	public int compareTo(Object o) {
		if (o instanceof BrowserDatasetSummary) {
			BrowserDatasetSummary d2 = (BrowserDatasetSummary) o;
			//return getID()-d2.getID();
			int diff = getImageCount()-d2.getImageCount();
			if (diff != 0)
				return diff;
			else 
				return getID()-d2.getID();
		}
		else
			return -1;
	}
	
}
