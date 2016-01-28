/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package omero.gateway.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static omero.rtypes.rstring;
import omero.RString;
import omero.model.Annotation;
import omero.model.CommentAnnotation;
import omero.model.DatasetAnnotationLink;
import omero.model.ImageAnnotationLink;
import omero.model.ProjectAnnotationLink;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;

/**
 * A tag annotation can either be related to an image or a tag but not to both
 * at the same time.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class TagAnnotationData extends AnnotationData {


    /**
     * The name space used to indicate that the tag is used a tag set.
     */
    public static final String INSIGHT_TAGSET_NS = 
            omero.constants.metadata.NSINSIGHTTAGSET.value;

    /** The descriptions of the tag. */
    private List<TextualAnnotationData> descriptions;

    /** The collection of data object related to the tag. */
    private Set<DataObject> dataObjects;

    /** The collection of tags related to the tags. */
    private Set<TagAnnotationData> tags;

    /**
     * Creates a new instance.
     *
     * @param tag
     *            The text of the tag.
     * @param asTagSet Pass <code>true</code> to create the tag as a tag set,
     *                 <code>false</code> otherwise.
     */
    public TagAnnotationData(String tag, boolean asTagSet) {
        this(tag, null, asTagSet);
    }

    /**
     * Creates a new instance.
     *
     * @param tag
     *            The text of the tag.
     */
    public TagAnnotationData(String tag) {
        this(tag, null, false);
    }

    /**
     * Creates a new instance.
     *
     * @param tag
     *            The text of the tag.
     * @param description
     *            The description of the tag.
     */
    public TagAnnotationData(String tag, String description) {
        this(tag, description, false);
    }

    /**
     * Creates a new instance.
     *
     * @param tag
     *            The text of the tag.
     * @param description
     *            The description of the tag.
     * @param asTagSet
     *                Pass <code>true</code> to create the tag as a tag set,
     *                <code>false</code> otherwise.
     */
    public TagAnnotationData(String tag, String description, boolean asTagSet) {
        super(TagAnnotationI.class);
        setTagValue(tag);
        setTagDescription(description);
        if (asTagSet) setNameSpace(INSIGHT_TAGSET_NS);
    }

    /**
     * Creates a new instance.
     *
     * @param tag
     *            The tag to wrap.
     */
    public TagAnnotationData(TagAnnotation tag) {
        super(tag);
    }

    /**
     * Creates a new instance.
     *
     * @param tag
     *            The tag to wrap.
     * @param value
     *            The descriptions of the tag.
     */
    public TagAnnotationData(TagAnnotation tag, List<CommentAnnotation> value) {
        super(tag);
        if (value == null) {
            return;
        }
        descriptions = new ArrayList<TextualAnnotationData>();
        Iterator<CommentAnnotation> i = value.iterator();
        while (i.hasNext()) {
            descriptions.add(new TextualAnnotationData(i.next()));
        }
    }

    /**
     * Sets the collection of data objects related to that tag.
     *
     * @param dataObjects
     *            The value to set.
     */
    public void setDataObjects(Set<DataObject> dataObjects) {
        String ns = getNameSpace();
        if (INSIGHT_TAGSET_NS.equals(ns)) 
            throw new IllegalArgumentException("Cannot add dataObject to "
                    + "a Tag Set.");
        this.dataObjects = dataObjects;
    }

    /**
     * Returns the collection of tags related to this tag.
     *
     * @return See above.
     */
    public Set<TagAnnotationData> getTags() {
        String ns = getNameSpace();
        if (!INSIGHT_TAGSET_NS.equals(ns)) return null;
        TagAnnotation tagSet = (TagAnnotation) asIObject();
        if (tags == null && tagSet.sizeOfAnnotationLinks() >= 0) {
            tags = new HashSet<TagAnnotationData>();
            List l = tagSet.linkedAnnotationList();
            Iterator i = l.iterator();
            Annotation data;
            while (i.hasNext()) {
                data = (Annotation) i.next();
                if (data instanceof TagAnnotation)
                    tags.add(new TagAnnotationData((TagAnnotation) data));
            }
        }
        return tags;
    }

    /**
     * Returns the collection of data objects related to this tag.
     * FIXME 
     * @return See above.
     */
    public Set<DataObject> getDataObjects()
    { 
        if (dataObjects == null && asAnnotation().sizeOfAnnotationLinks() >= 0)
        {
            List l = asAnnotation().copyAnnotationLinks();
            dataObjects = new HashSet<DataObject>();
            ImageAnnotationLink iaLink;
            DatasetAnnotationLink daLink;
            ProjectAnnotationLink paLink;
            for (Object object : l) {
                if (object instanceof ImageAnnotationLink) {
                    iaLink = (ImageAnnotationLink) object;
                    dataObjects.add(new ImageData(iaLink.getParent()));
                } else if (object instanceof DatasetAnnotationLink) {
                    daLink = (DatasetAnnotationLink) object;
                    dataObjects.add(new DatasetData(daLink.getParent()));
                } else if (object instanceof ProjectAnnotationLink) {
                    paLink = (ProjectAnnotationLink) object;
                    dataObjects.add(new ProjectData(paLink.getParent()));
                }
            }
        }
        return dataObjects == null ? null : new HashSet<DataObject>(dataObjects);
    }  

    /**
     * Sets the tag's descriptions.
     *
     * @param value
     *            The collection to set.
     */
    public void setTagDescriptions(List<TextualAnnotationData> value) {
        descriptions = value;
    }

    /**
     * Returns the descriptions linked to that tag.
     *
     * @return See above.
     */
    public List getTagDescriptions() {
        return descriptions;
    }

    /**
     * Sets the description of the tag.
     *
     * @param value
     *            The value to set.
     */
    public void setTagDescription(String value) {
        if (value == null || value.trim().length() == 0) {
            return;
        }
        setDirty(true);
        asAnnotation().setDescription(rstring(value));
    }

    /**
     * Returns the description of the tag.
     *
     * @return See above.
     */
    public String getTagDescription() {
        RString value = asAnnotation().getDescription();
        if (value == null) return "";
        return value.getValue();
    }

    /**
     * Sets the value of the tag.
     *
     * @param tag
     *            The value to set.
     */
    public void setTagValue(String tag) {
        setContent(tag);
    }

    /**
     * Returns the text of the tag.
     *
     * @return See above.
     */
    public String getTagValue() {
        return getContentAsString();
    }

    /**
     * Returns the textual content of the annotation.
     *
     * @see AnnotationData#getContent()
     */
    @Override
    public Object getContent() {
        omero.RString s = ((TagAnnotation) asAnnotation()).getTextValue();
        return s == null ? null : s.getValue();
    }

    /**
     * Returns the textual content of the annotation.
     *
     * @see AnnotationData#getContentAsString()
     */
    @Override
    public String getContentAsString() {
        Object o = getContent();
        if (o == null) return "";
        return (String) o;
    }

    /**
     * Sets the text annotation.
     *
     * @see AnnotationData#setContent(Object)
     */
    @Override
    public void setContent(Object content) {
        if (content == null) {
            throw new IllegalArgumentException("Tag value cannot be null.");
        }

        if (!(content instanceof String)) {
            throw new IllegalArgumentException("Object must be of type String");
        }
        String tag = (String) content;
        if (tag.trim().length() == 0) {
            throw new IllegalArgumentException("Tag value cannot be null.");
        }
        setDirty(true);
        ((TagAnnotation) asAnnotation()).setTextValue(rstring(tag));
    }

}
