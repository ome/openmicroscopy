/*
 * pojos.TagAnnotationData 
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
package pojos;


//Java imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.annotations.TagAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.core.Image;
import ome.util.CBlock;

/** 
 * A tag annotaion can either be related to an image or a tag
 * but not to both at the same time.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TagAnnotationData 
	extends AnnotationData
{

	/** The descriptions of the tag. */
	private List<TextualAnnotationData>	descriptions;
	
	/** The textual description of the tag. */
	private TextualAnnotationData		description;
	
	/** The collection of images related to the tag. */
	private Set<ImageData>				images;
	
	/** The collection of tags related to the tags. */
	private Set<TagAnnotationData>		tags;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param tag	The text of the tag.
	 */
	public TagAnnotationData(String tag)
	{
		this(tag, null);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param tag			The text of the tag.
	 * @param description	The description of the tag.
	 */
	public TagAnnotationData(String tag, String description)
	{
		super(TagAnnotation.class);
		setTagValue(tag);
		setTagDescription(description);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param tag	The tag to wrap.
	 */	
	public TagAnnotationData(TagAnnotation tag)
	{
		super(tag);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param tag	The tag to wrap.
	 * @param value	The descriptions of the tag.
	 */	
	public TagAnnotationData(TagAnnotation tag, List<TextAnnotation> value)
	{
		super(tag);
		if (value == null) return;
		descriptions = new ArrayList<TextualAnnotationData>();
		Iterator<TextAnnotation> i = value.iterator();
		while (i.hasNext()) 
			descriptions.add(new TextualAnnotationData(i.next()));
	}
	
	/**
	 * Sets the collection of images. 
	 * 
	 * @param images The value to set.
	 */
	public void setImages(Set<ImageData> images)
	{
		if (tags != null) 
			throw new IllegalArgumentException("Cannot add images to " +
					"a tagSet.");
		this.images = images;
	}
	
	/**
	 * Sets the collection of tags. 
	 * 
	 * @param tags The value to set.
	 */
	public void setTags(Set<TagAnnotationData> tags)
	{
		if (images != null) 
			throw new IllegalArgumentException("Cannot add tags to " +
					"a tag with images.");
		this.tags = tags;
	}
	
	/**
	 * Returns the collection of tags related to this tag.
	 * 
	 * @return See above.
	 */
	public Set<TagAnnotationData> getTags() { return tags; }
	
	/**
	 * Returns the collection of images related to this tag.
	 * 
	 * @return See above.
	 */
	public Set<ImageData> getImages()
	{ 
		if (images == null && asAnnotation().sizeOfAnnotationLinks() >= 0) {
			images = new HashSet<ImageData>(asAnnotation().eachLinkedAnnotation
					(new CBlock<ImageData>() {
                public ImageData call(IObject object) {
                    return new ImageData((Image) object);
                }
            }));
        }
        return images == null ? null : new HashSet<ImageData>(images);
	}
	
	/**
	 * Sets the tag's descriptions.
	 * 
	 * @param value The collection to set.
	 */
	public void setTagDescriptions(List<TextualAnnotationData> value)
	{
		descriptions = value;
	}
	
	/**
	 * Returns the descriptions linked to that tag.
	 * 
	 * @return See above.
	 */
	public List getTagDescriptions()
	{
		return descriptions;
	}
	
	/**
	 * Sets the description of the tag.
	 * 
	 * @param value The value to set.
	 */
	public void setTagDescription(String value)
	{
		if (value == null || value.trim().length() == 0)
			return;
		description = new TextualAnnotationData(value);
	}
	
	/**
	 * Returns the description of the tag.
	 * 
	 * @return See above.
	 */
	public TextualAnnotationData getTagDescription() { return description; }
	
	/**
	 * Sets the value of the tag.
	 * 
	 * @param tag The value to set.
	 */
	public void setTagValue(String tag) { setContent(tag); }
	
	/**
	 * Returns the text of the tag.
	 * 
	 * @return See above.
	 */
	public String getTagValue() { return getContentAsString(); }
	
	/**
	 * Returns the textual content of the annotation.
	 * @see AnnotationData#getContent()
	 */
	public Object getContent()
	{
		return ((TagAnnotation) asAnnotation()).getTextValue();
	}

	/**
	 * Returns the textual content of the annotation.
	 * @see AnnotationData#getContentAsString()
	 */
	public String getContentAsString() { return (String) getContent(); }

	/**
	 * Sets the text annotation.
	 * @see AnnotationData#setContent(Object)
	 */
	public void setContent(Object content)
	{
		if (content == null)
			throw new IllegalArgumentException("Tag value cannot be null.");
		
		if (!(content instanceof String))
			throw new IllegalArgumentException("Object must be of type String");
		String tag = (String) content;
		if (tag.trim().length() == 0)
			throw new IllegalArgumentException("Tag value cannot be null.");
		((TagAnnotation) asAnnotation()).setTextValue(tag);
	}
	
}
