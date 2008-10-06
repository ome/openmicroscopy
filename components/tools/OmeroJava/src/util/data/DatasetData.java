/*
 * util.data.DatasetData 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package util.data;


//Java imports
import java.util.List;


//Third-party libraries

//Application-internal dependencies
import omero.RString;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DatasetData
	extends IObjectData
{	
	/** The Dataset object that the DatasetData object wraps. */
	private Dataset 	dataset;
	
	/**
	 * Create the Dataset data object from the  
	 * @param dataset
	 */
	public DatasetData(Dataset dataset)
	{
		super(dataset);
		this.dataset = dataset;
	}
	
	/**
	 * Rturn the original wrapped dataset object.
	 * @return see above.
	 */
	public Dataset getDataset()
	{
		return dataset;
	}
	
	/**
	 * Set the name of the dataset.
	 * @param name see above.
	 */
	public void setName(String name)
	{
		dataset.setName( new RString(name) );
	}
	
	/**
	 * Get the name of the dataset.
	 * @return see above.
	 */
	public String getName()
	{
		return dataset.getName().val;
	}
	
	/**
	 * Get the dataset annotation links.
	 * @return see above.
	 */
	public List<DatasetAnnotationLink> getAnnotationLinks()
	{
		return dataset.copyAnnotationLinks(); // FIXME
	}
	
	/**
	 * Get the number of annotation links.
	 * @return see above.
	 */
	public int getAnnotationLinksCount()
	{
		return dataset.sizeOfAnnotationLinks();
	}
	
	/**
	 * Get the number of annotationLinks for owner (id)
	 * @param owner the id of the user.
	 * @return number of links.
	 */
	public long getAnnotationLinksCount(long owner)
	{
		return dataset.getAnnotationLinksCountPerOwner().get(new Long(owner));
	}
	
	/** 
	 * Are the annotation links loaded. 
	 * @return see above.
	 */
	public boolean annotationLinksLoaded()
	{
		return dataset.sizeOfAnnotationLinks() >= 0;
	}
	
}