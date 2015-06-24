/*
 * org.openmicroscopy.shoola.env.data.util.StructuredDataResults 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

import org.openmicroscopy.shoola.env.data.model.AnnotationLinkData;

//Third-party libraries

//Application-internal dependencies
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.FileAnnotationData;
import pojos.MapAnnotationData;
import pojos.RatingAnnotationData;
import pojos.TagAnnotationData;
import pojos.TermAnnotationData;
import pojos.TextualAnnotationData;
import pojos.XMLAnnotationData;

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
	
	/** The tags related to the object. */
	private Collection<TagAnnotationData>	tags;
	
	/** The attachments related to the object. */
	private Collection<FileAnnotationData>	attachments;
	
	/** The terms related to the object. */
	private Collection<TermAnnotationData>	terms;
	
	/** The textual annotations. */
	private Collection<TextualAnnotationData> texts;

	/** The ratings of the objects. */
	private Collection<RatingAnnotationData>  ratings;
	
	/** The XML type of the objects. */
	private Collection<XMLAnnotationData>  xmlAnnotations;
	
	/** Collection of annotations not already stored. */
	private Collection<AnnotationData>     otherAnnotation;
	
	/** The MapAnnotations. */
	private Collection<MapAnnotationData>     mapAnnotations;
	
	/** The object the results are for. */
	private DataObject					relatedObject;
	
	/** The collection of links  for in-place imports.*/
	private Collection<AnnotationData> transferlinks;

	/** 
	 * Collection of parents. 
	 * Filled when the related object is an <code>image</code> or
	 * <code>dataset</code>.
	 */
	private Collection					parents;

	/** The tags and documents links. */
	private Map							links;
	
	/** The concrete links.*/
	private Collection<AnnotationLinkData> annotationLinks;
	
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
	 * Sets the collection of annotations.
	 * 
	 * @param texts The value to set.
	 */
	public void setTextualAnnotations(Collection<TextualAnnotationData> texts)
	{
		this.texts = texts;
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
	 * Sets the collections of attachments.
	 * 
	 * @param attachments The value to set.
	 */
	public void setAttachments(Collection<FileAnnotationData> attachments)
	{
		this.attachments = attachments;
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
	 * Sets the collections of <code>XML</code> annotations.
	 * 
	 * @param xmlAnnotations The value to set.
	 */
	public void setXMLAnnotations(Collection<XMLAnnotationData> xmlAnnotations)
	{
		this.xmlAnnotations = xmlAnnotations;
	}
	
	/**
	 * Returns the ratings.
	 * 
	 * @return See above.
	 */
	public Collection<RatingAnnotationData> getRatings() { return ratings; }

	/**
	 * Sets the ratings.
	 * 
	 * @param ratings The value to set.
	 */
	public void setRatings(Collection<RatingAnnotationData> ratings)
	{ 
		this.ratings = ratings; 
	}

	/**
	 * Returns the collection of tags.
	 * 
	 * @return See above.
	 */
	public Collection<TagAnnotationData> getTags() { return tags; }

	/**
	 * Sets the collections of tags.
	 * 
	 * @param tags The value to set.
	 */
	public void setTags(Collection<TagAnnotationData> tags)
	{ 
		this.tags = tags;
	}

	/**
	 * Returns the collection of terms.
	 * 
	 * @return See above.
	 */
	public Collection<TermAnnotationData> getTerms() { return terms; }

	/**
	 * Sets the collections of terms.
	 * 
	 * @param terms The value to set.
	 */
	public void setTerms(Collection<TermAnnotationData> terms)
	{ 
		this.terms = terms; 
	}
	
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
	 * Sets the collections of annotations.
	 * 
	 * @param terms The value to set.
	 */
	public void setOtherAnnotation(Collection<AnnotationData> otherAnnotation)
	{ 
		this.otherAnnotation = otherAnnotation; 
	}
	
	/**
	 * Returns the collection of links.
	 * 
	 * @return See above.
	 */
	public Map getLinks() { return links; }
	
	/**
	 * Sets the collection.
	 * 
	 * @param links The collection to set.
	 */
	public void setLinks(Map links) { this.links = links; }
	
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
	 * Sets the collection.
	 * 
	 * @param links The collection to set.
	 */
	public void setAnnotationLinks(Collection<AnnotationLinkData> annotationLinks)
	{
		this.annotationLinks = annotationLinks;
	}
	
	/**
	 * Sets the collection of transferlink annotations (in-place imports)
	 * @param transferlinks Transferlink annotations to set
	 */
	public void setTransferlinks(Collection<AnnotationData> transferlinks)
	{
		this.transferlinks = transferlinks;
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

	/**
	 * Sets the collection of {@link MapAnnotationData}.
	 * 
	 * @return See above.
	 */
	public void setMapAnnotations(Collection<MapAnnotationData> mapAnnotations) {
		this.mapAnnotations = mapAnnotations;
	}
	
	
}
