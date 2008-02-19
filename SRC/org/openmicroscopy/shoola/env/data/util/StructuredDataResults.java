/*
 * org.openmicroscopy.shoola.env.data.util.StructuredDataResults 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.util;


//Java imports
import java.util.Collection;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import pojos.ExperimenterData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class StructuredDataResults
{

	/** The ratings related to the object. */
	private Map<ExperimenterData, Collection> 	ratings;
	
	/** The tags related to the object. */
	private Collection							tags;
	
	/** The attachments related to the object. */
	private Collection							attachments;
	
	/** The urls related to the object. */
	private Collection							urls;
	
	/** The annotations. */
	private Map									annotations;

	/** The object the results are for. */
	private Object								relatedObject;
	
	/**
	 * Returns the annotations.
	 * 
	 * @return See above.
	 */
	public Map getAnnotations() { return annotations; }

	/**
	 * Sets the collections of annotations.
	 * 
	 * @param annotations The value to set.
	 */
	public void setAnnotations(Map annotations)
	{
		this.annotations = annotations;
	}

	/**
	 * Returns the collection of attachments.
	 * 
	 * @return See above.
	 */
	public Collection getAttachments() { return attachments; }

	/**
	 * Sets the collections of attachments.
	 * 
	 * @param attachments The value to set.
	 */
	public void setAttachments(Collection attachments)
	{
		this.attachments = attachments;
	}

	/**
	 * Returns the ratings.
	 * 
	 * @return See above.
	 */
	public Map<ExperimenterData, Collection> getRatings() 
	{
		return ratings;
	}

	/**
	 * Sets the ratings.
	 * 
	 * @param ratings The value to set.
	 */
	public void setRatings(Map<ExperimenterData, Collection> ratings) 
	{
		this.ratings = ratings;
	}

	/**
	 * Returns the collection of tags.
	 * 
	 * @return See above.
	 */
	public Collection getTags() { return tags; }

	/**
	 * Sets the collections of tags.
	 * 
	 * @param tags The value to set.
	 */
	public void setTags(Collection tags) { this.tags = tags; }

	/**
	 * Returns the collection of urls.
	 * 
	 * @return See above.
	 */
	public Collection getUrls() { return urls; }

	/**
	 * Sets the collections of urls.
	 * 
	 * @param urls The value to set.
	 */
	public void setUrls(Collection urls) { this.urls = urls; }
	
}
