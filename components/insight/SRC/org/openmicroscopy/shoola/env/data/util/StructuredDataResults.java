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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.RatingAnnotationData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TermAnnotationData;
import omero.gateway.model.TextualAnnotationData;
import omero.gateway.model.XMLAnnotationData;

import org.openmicroscopy.shoola.env.data.model.AnnotationLinkData;
import org.openmicroscopy.shoola.util.PojosUtil;

/**
 * Helper class storing the various data related to a given object.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class StructuredDataResults {

    /** The tags related to the object. */
    private Collection<TagAnnotationData> tags = new ArrayList<TagAnnotationData>();

    /** The attachments related to the object. */
    private Collection<FileAnnotationData> attachments = new ArrayList<FileAnnotationData>();

    /** The terms related to the object. */
    private Collection<TermAnnotationData> terms = new ArrayList<TermAnnotationData>();

    /** The textual annotations. */
    private Collection<TextualAnnotationData> texts = new ArrayList<TextualAnnotationData>();

    /** The ratings of the objects. */
    private Collection<RatingAnnotationData> ratings = new ArrayList<RatingAnnotationData>();

    /** The XML type of the objects. */
    private Collection<XMLAnnotationData> xmlAnnotations = new ArrayList<XMLAnnotationData>();

    /** Collection of annotations not already stored. */
    private Collection<AnnotationData> otherAnnotation = new ArrayList<AnnotationData>();

    /** The MapAnnotations. */
    private Collection<MapAnnotationData> mapAnnotations = new ArrayList<MapAnnotationData>();

    /** The object the results are for. */
    private DataObject relatedObject;

    /** The collection of links for in-place imports. */
    private Collection<AnnotationData> transferlinks = new ArrayList<AnnotationData>();

    /**
     * Collection of parents. Filled when the related object is an
     * <code>image</code> or <code>dataset</code>.
     */
    private Collection parents;

    /** The tags and documents links. */
    private Map<DataObject, ExperimenterData> links = new HashMap<DataObject, ExperimenterData>();

    /** The concrete links. */
    private Collection<AnnotationLinkData> annotationLinks = new ArrayList<AnnotationLinkData>();

    /** Flag indicating if the annotations have been loaded or not. */
    private boolean loaded;

    /**
     * Creates a new instance.
     * 
     * @param relatedObject
     *            The object the results are for. Mustn't be <code>null</code>.
     */
    public StructuredDataResults(DataObject relatedObject) {
        this(relatedObject, true);
    }

    /**
     * Creates a new instance.
     * 
     * @param relatedObject
     *            The object the results are for. Mustn't be <code>null</code>.
     * @param loaded
     *            Flag indicating if the annotations have been loaded or not.
     *            The default value is <code>true</code>
     */
    public StructuredDataResults(DataObject relatedObject, boolean loaded) {
        if (relatedObject == null)
            throw new IllegalArgumentException("No object related.");
        this.relatedObject = relatedObject;
        this.loaded = loaded;
    }

    /**
     * Merges the specified {@link StructuredDataResults} into this one. Throws
     * an {@link IllegalArgumentException} if they are not compatible (i.e refer
     * to different objects)
     * 
     * @param other
     *            The {@link StructuredDataResults} to merge
     */
    public void merge(StructuredDataResults other) {
        DataObject o1 = (DataObject) getRelatedObject();
        DataObject o2 = (DataObject) other.getRelatedObject();
        if (!o1.getUniqueId().equals(o2.getUniqueId()))
            throw new IllegalArgumentException(
                    "Can't merge results for two different objects!");

        // merge annotations
        Collection<AnnotationData> ownAnnotations = getAllAnnotations();

        Collection<AnnotationData> toAdd = other.getAllAnnotations();
        Iterator<AnnotationData> it = toAdd.iterator();
        while (it.hasNext()) {
            AnnotationData a = it.next();
            if (PojosUtil.contains(ownAnnotations, a))
                it.remove();
        }

        addAnnotations(toAdd);

        // merge links
        for (AnnotationData d : other.transferlinks) {
            if (!PojosUtil.contains(transferlinks, d))
                transferlinks.add(d);
        }
        
        Set<String> ids = new HashSet<String>();
        for (AnnotationLinkData d : annotationLinks) {
            String s = "" + d.getLink().getId().getValue();
            ids.add(s);
        }
        for (AnnotationLinkData d : other.annotationLinks) {
            String s = "" + d.getLink().getId().getValue();
            if (!ids.contains(s))
                annotationLinks.add(d);
        }
        
        ids.clear();
        for (Entry<DataObject, ExperimenterData> e : links.entrySet()) {
            String s = e.getKey().getUniqueId() + "_"
                    + e.getValue().getUniqueId();
            ids.add(s);
        }
        for (Entry<DataObject, ExperimenterData> e : other.links.entrySet()) {
            String s = e.getKey().getUniqueId() + "_"
                    + e.getValue().getUniqueId();
            if (!ids.contains(s))
                links.put(e.getKey(), e.getValue());
        }
    }

    /**
     * Returns <code>true</code> if the annotations are loaded,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Returns the object the results are for.
     * 
     * @return See above.
     */
    public Object getRelatedObject() {
        return relatedObject;
    }

    /**
     * Returns the identifier of the data object.
     *
     * @return See above.
     */
    public long getObjectId() {
        return relatedObject.getId();
    }

    /**
     * Returns the collection of parents.
     * 
     * @return See above.
     */
    public Collection getParents() {
        return parents;
    }

    /**
     * Sets the collection of parents.
     * 
     * @param parents
     *            The value to set.
     */
    public void setParents(Collection parents) {
        this.parents = parents;
    }

    /**
     * Returns the annotations.
     * 
     * @return See above.
     */
    public Collection<TextualAnnotationData> getTextualAnnotations() {
        return texts;
    }

    /**
     * Sets the collection of annotations.
     * 
     * @param texts
     *            The value to set.
     */
    public void setTextualAnnotations(Collection<TextualAnnotationData> texts) {
        this.texts = texts;
    }

    /**
     * Returns the collection of attachments.
     * 
     * @return See above.
     */
    public Collection<FileAnnotationData> getAttachments() {
        return attachments;
    }

    /**
     * Sets the collections of attachments.
     * 
     * @param attachments
     *            The value to set.
     */
    public void setAttachments(Collection<FileAnnotationData> attachments) {
        this.attachments = attachments;
    }

    /**
     * Returns the collection of <code>XML</code> annotations.
     * 
     * @return See above.
     */
    public Collection<XMLAnnotationData> getXMLAnnotations() {
        return xmlAnnotations;
    }

    /**
     * Sets the collections of <code>XML</code> annotations.
     * 
     * @param xmlAnnotations
     *            The value to set.
     */
    public void setXMLAnnotations(Collection<XMLAnnotationData> xmlAnnotations) {
        this.xmlAnnotations = xmlAnnotations;
    }

    /**
     * Returns the ratings.
     * 
     * @return See above.
     */
    public Collection<RatingAnnotationData> getRatings() {
        return ratings;
    }

    /**
     * Sets the ratings.
     * 
     * @param ratings
     *            The value to set.
     */
    public void setRatings(Collection<RatingAnnotationData> ratings) {
        this.ratings = ratings;
    }

    /**
     * Returns the collection of tags.
     * 
     * @return See above.
     */
    public Collection<TagAnnotationData> getTags() {
        return tags;
    }

    /**
     * Sets the collections of tags.
     * 
     * @param tags
     *            The value to set.
     */
    public void setTags(Collection<TagAnnotationData> tags) {
        this.tags = tags;
    }

    /**
     * Returns the collection of terms.
     * 
     * @return See above.
     */
    public Collection<TermAnnotationData> getTerms() {
        return terms;
    }

    /**
     * Sets the collections of terms.
     * 
     * @param terms
     *            The value to set.
     */
    public void setTerms(Collection<TermAnnotationData> terms) {
        this.terms = terms;
    }

    /**
     * Returns the collection of annotations.
     * 
     * @return See above.
     */
    public Collection<AnnotationData> getOtherAnnotations() {
        return otherAnnotation;
    }

    /**
     * Sets the collections of annotations.
     * 
     * @param otherAnnotation
     *            The value to set.
     */
    public void setOtherAnnotation(Collection<AnnotationData> otherAnnotation) {
        this.otherAnnotation = otherAnnotation;
    }

    /**
     * Returns the collection of links.
     * 
     * @return See above.
     */
    public Map<DataObject, ExperimenterData> getLinks() {
        return links;
    }

    /**
     * Sets the collection.
     * 
     * @param links
     *            The collection to set.
     */
    public void setLinks(Map<DataObject, ExperimenterData> links) {
        this.links = links;
    }

    /**
     * Returns the collection of links.
     * 
     * @return See above.
     */
    public Collection<AnnotationLinkData> getAnnotationLinks() {
        return annotationLinks;
    }

    /**
     * Sets the collection.
     * 
     * @param annotationLinks
     *            The collection to set.
     */
    public void setAnnotationLinks(
            Collection<AnnotationLinkData> annotationLinks) {
        this.annotationLinks = annotationLinks;
    }

    /**
     * Sets the collection of transferlink annotations (in-place imports)
     * 
     * @param transferlinks
     *            Transferlink annotations to set
     */
    public void setTransferlinks(Collection<AnnotationData> transferlinks) {
        this.transferlinks = transferlinks;
    }

    /**
     * Returns the collection of links (in-place imports).
     * 
     * @return See above.
     */
    public Collection<AnnotationData> getTransferLinks() {
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
     * @param mapAnnotations
     *            The value to set.
     */
    public void setMapAnnotations(Collection<MapAnnotationData> mapAnnotations) {
        this.mapAnnotations = mapAnnotations;
    }

    /**
     * Add Annotations
     * 
     * @param annos
     *            The Annotations to add
     */
    public void addAnnotations(Collection<AnnotationData> annos) {
        for (AnnotationData data : annos) {
            if (data instanceof TermAnnotationData) {
                terms.add((TermAnnotationData) data);
            } else if (data instanceof TextualAnnotationData)
                texts.add((TextualAnnotationData) data);
            else if (data instanceof TagAnnotationData) {
                tags.add((TagAnnotationData) data);
            } else if (data instanceof RatingAnnotationData)
                ratings.add((RatingAnnotationData) data);
            else if (data instanceof FileAnnotationData) {
                attachments.add((FileAnnotationData) data);
            } else if (data instanceof XMLAnnotationData) {
                xmlAnnotations.add((XMLAnnotationData) data);
            } else if (data instanceof MapAnnotationData) {
                mapAnnotations.add((MapAnnotationData) data);
            } else {
                otherAnnotation.add(data);
            }
        }
    }

    /**
     * Get all Annotations
     * 
     * @return All Annotations
     */
    public Collection<AnnotationData> getAllAnnotations() {
        Collection<AnnotationData> result = new ArrayList<AnnotationData>();
        result.addAll(attachments);
        result.addAll(mapAnnotations);
        result.addAll(otherAnnotation);
        result.addAll(ratings);
        result.addAll(tags);
        result.addAll(terms);
        result.addAll(texts);
        result.addAll(xmlAnnotations);
        return result;
    }

}
