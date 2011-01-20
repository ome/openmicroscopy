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

//Third-party libraries

//Application-internal dependencies
import pojos.DataObject;

/** 
 * Helper class storing the various data related to a given object.
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

	/** 
	 * The collection of objects hosting info about the experimenters
	 * who viewed a given image.
	 */
	private Collection<ViewedByDef>		viewedBy;
	
	/** The tags related to the object. */
	private Collection					tags;
	
	/** The attachments related to the object. */
	private Collection					attachments;
	
	/** The urls related to the object. */
	private Collection					urls;
	
	/** The textual annotations. */
	private Collection					textualAnnotations;

	/** The object the results are for. */
	private DataObject					relatedObject;
	
	/** 
	 * Collection of parents. 
	 * Filled when the related object is an <code>image</code> or
	 * <code>dataset</code>.
	 */
	private Collection					parents;
	
	/** The ratings of the objects. */
	private Collection					ratings;

	/** Flag indicating if tha object has been published e.g. image. */
	private Collection					published;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param relatedObject The object the results are for. 
	 * 						Mustn't be <code>null</code>.
	 */
	public StructuredDataResults(DataObject	relatedObject)
	{
		if (relatedObject == null)
			throw new IllegalArgumentException("No object related.");
		this.relatedObject = relatedObject;
	}
	
	/**
	 * Returns the object the results are for.
	 * 
	 * @return See above.
	 */
	public Object getRelatedObject() { return relatedObject; }
	
	/**
	 * Returns the collection of parents.
	 * 
	 * @return See above.
	 */
	public Collection getParents() { return parents; }
	
	/** 
	 * Sets the collection of parents.
	 * 
	 * @param parents The value to set.
	 */
	public void setParents(Collection parents) { this.parents = parents; }
	
	/**
	 * Returns the annotations.
	 * 
	 * @return See above.
	 */
	public Collection getTextualAnnotations() { return textualAnnotations; }

	/**
	 * Sets the collection of annotations.
	 * 
	 * @param annotations The value to set.
	 */
	public void setTextualAnnotations(Collection annotations)
	{
		this.textualAnnotations = annotations;
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
	public Collection getRatings() { return ratings; }

	/**
	 * Sets the ratings.
	 * 
	 * @param ratings The value to set.
	 */
	public void setRatings(Collection ratings) { this.ratings = ratings; }

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
	
	/**
	 * Sets the collection of objects.
	 * 
	 * @param viewedBy The value to set.
	 */
	public void setViewedBy(Collection<ViewedByDef> viewedBy)
	{ 
		this.viewedBy = viewedBy;
	}
	
	/**
	 * Sets the collection of objects.
	 * 
	 * @param published The value to set.
	 */
	public void setPublished(Collection published)
	{ 
		this.published = published;
	}
	
	/**
	 * Returns the collection of {@link ViewedByDef} objects.
	 * 
	 * @return See above.
	 */
	public Collection getViewedBy() { return viewedBy; }
	
	/**
	 * Returns the collection of published annotations.
	 * 
	 * @return See above.
	 */
	public Collection getPublished() { return published; }
	
}
