/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openmicroscopy.shoola.env.data.model.AnnotationLinkData;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.RatingAnnotationData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TermAnnotationData;
import omero.gateway.model.TextualAnnotationData;
import omero.gateway.model.XMLAnnotationData;

/** 
 * Helper class storing the various data related to a given object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class StructuredDataResults
{
	
	/** The tags related to the object. */
	private Collection<TagAnnotationData>	tags = new ArrayList<TagAnnotationData>();
	
	/** The attachments related to the object. */
	private Collection<FileAnnotationData>	attachments = new ArrayList<FileAnnotationData>();
	
	/** The terms related to the object. */
	private Collection<TermAnnotationData>	terms = new ArrayList<TermAnnotationData>();
	
	/** The textual annotations. */
	private Collection<TextualAnnotationData> texts = new ArrayList<TextualAnnotationData>();

	/** The ratings of the objects. */
	private Collection<RatingAnnotationData>  ratings = new ArrayList<RatingAnnotationData>();
	
	/** The XML type of the objects. */
	private Collection<XMLAnnotationData>  xmlAnnotations = new ArrayList<XMLAnnotationData>();
	
	/** Collection of annotations not already stored. */
	private Collection<AnnotationData>     otherAnnotation = new ArrayList<AnnotationData>();
	
	/** The MapAnnotations. */
	private Collection<MapAnnotationData>     mapAnnotations = new ArrayList<MapAnnotationData>();
	
	/** The object the results are for. */
	private DataObject					relatedObject;
	
	/** The collection of links  for in-place imports.*/
	private Collection<AnnotationData> transferlinks = new ArrayList<AnnotationData>();

	/** 
	 * Collection of parents. 
	 * Filled when the related object is an <code>image</code> or
	 * <code>dataset</code>.
	 */
	private Collection					parents;

	/** The tags and documents links. */
	private Map							links = new HashMap();
	
	/** The concrete links.*/
	private Collection<AnnotationLinkData> annotationLinks = new ArrayList<AnnotationLinkData>();
	
	/** Flag indicating if the annotations have been loaded or not.*/
	private boolean loaded;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param relatedObject The object the results are for. 
	 * 						Mustn't be <code>null</code>.
	 */
	public StructuredDataResults(DataObject relatedObject)
	{
		this(relatedObject, true);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param relatedObject The object the results are for.
	 * 						Mustn't be <code>null</code>.
	 * @param loaded Flag indicating if the annotations have been loaded or not.
	 * The default value is <code>true</code>
	 */
	public StructuredDataResults(DataObject relatedObject, boolean loaded)
	{
		if (relatedObject == null)
			throw new IllegalArgumentException("No object related.");
		this.relatedObject = relatedObject;
		this.loaded = loaded;
	}

	/**
	 * Returns <code>true</code> if the annotations are loaded,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isLoaded() { return loaded; }
	
	/**
	 * Returns the object the results are for.
	 * 
	 * @return See above.
	 */
	public Object getRelatedObject() { return relatedObject; }

	/**
	 * Returns the identifier of the data object.
	 *
	 * @return See above.
	 */
	public long getObjectId() { return relatedObject.getId(); }

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
	public Collection<TextualAnnotationData> getTextualAnnotations()
	{ 
		return texts; 
	}

	/**
	 * Returns the collection of attachments.
	 * 
	 * @return See above.
	 */
	public Collection<FileAnnotationData> getAttachments()
	{ 
		return attachments; 
	}

	/**
	 * Returns the collection of <code>XML</code> annotations.
	 * 
	 * @return See above.
	 */
	public Collection<XMLAnnotationData> getXMLAnnotations()
	{ 
		return xmlAnnotations; 
	}
	
	/**
	 * Returns the ratings.
	 * 
	 * @return See above.
	 */
	public Collection<RatingAnnotationData> getRatings() { return ratings; }

	/**
	 * Returns the collection of tags.
	 * 
	 * @return See above.
	 */
	public Collection<TagAnnotationData> getTags() { return tags; }

	/**
	 * Returns the collection of terms.
	 * 
	 * @return See above.
	 */
	public Collection<TermAnnotationData> getTerms() { return terms; }

	
	/**
	 * Returns the collection of annotations.
	 * 
	 * @return See above.
	 */
	public Collection<AnnotationData> getOtherAnnotations()
	{ 
		return otherAnnotation; 
	}
	
	/**
	 * Returns the collection of links.
	 * 
	 * @return See above.
	 */
	public Map getLinks() { return links; }
	
	/**
	 * Returns the collection of links.
	 * 
	 * @return See above.
	 */
	public Collection<AnnotationLinkData> getAnnotationLinks()
	{
		return annotationLinks;
	}
	
	/**
	 * Returns the collection of links (in-place imports).
	 * 
	 * @return See above.
	 */
	public Collection<AnnotationData> getTransferLinks()
	{
		return transferlinks;
	}

	/**
	 * Returns the collection of {@link MapAnnotationData}.
	 * 
	 * @return See above.
	 */
	public Collection<MapAnnotationData> getMapAnnotations() {
		return mapAnnotations;
	}
}
