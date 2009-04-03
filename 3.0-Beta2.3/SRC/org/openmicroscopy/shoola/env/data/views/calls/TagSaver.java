/*
 * org.openmicroscopy.shoola.env.data.views.calls.TagSaver 
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
package org.openmicroscopy.shoola.env.data.views.calls;


//Java imports
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.CategoryData;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
 * Command to create and save the tags.
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
public class TagSaver
	extends BatchCallTree
{
	
	/** Indicates to tag the selected objects. */
	public static final int TAG_LEVEL_ZERO = 0;
	
	/** Indicates to tag the children of the selected objects. */
	public static final int TAG_LEVEL_ONE = 1;
	
	/** The batch call. */
	private BatchCall       call;

	/** The result of the save action. */
	private Object          result;
	
	/**
	 * Creates a {@link BatchCall} to tag the images.
	 * 
	 * @param ids      The images to tag.
	 * @param toCreate The collection of tags to create if any.
	 * @param toUpdate The collection of tags to update if any.
	 * @return The {@link BatchCall}.
	 */
	private BatchCall tagImages(final Set<Long> ids, 
								final Set<CategoryData> toCreate,
								final Set<CategoryData> toUpdate)
	{
		return new BatchCall("Tagging images.") {
			public void doCall() throws Exception
			{
				OmeroDataService os = context.getDataService();
				//create the tags
				Set<CategoryData> tags = new HashSet<CategoryData>();
				Set<ImageData> images = new HashSet<ImageData>(ids.size());
				Iterator i = ids.iterator();
				ImageData img;
				while (i.hasNext()) {
					img = new ImageData();
					img.setId((Long) i.next());
					images.add(img);
				}
				if (toCreate != null && toCreate.size() > 0) {
					i = toCreate.iterator();
					CategoryData tag;
					while (i.hasNext()) {
						tag = (CategoryData) 
							os.createDataObject((CategoryData) i.next(), null);
						tags.add(tag);
					}
				}
				if (toUpdate != null)
					tags.addAll(toUpdate);
				result = os.classify(images, tags);
			}
		};
	}
	
	/**
	 * Creates a {@link BatchCall} to tag the images acquired during a
	 * given period of time.
	 * 
	 * @param timeRef	the time object defi§ing the time interval.	
	 * @param toCreate The collection of tags to create if any.
	 * @param toUpdate The collection of tags to update if any.
	 * @return The {@link BatchCall}.
	 */
	private BatchCall tagImages(final TimeRefObject timeRef, 
							final Set<CategoryData> toCreate,
							final Set<CategoryData> toUpdate)
	{
		return new BatchCall("Tagging images.") {
			public void doCall() throws Exception
			{
				OmeroDataService os = context.getDataService();
				long userID = ((ExperimenterData) context.lookup(
						LookupNames.CURRENT_USER_DETAILS)).getId();
				Set images =  os.getImagesPeriod(timeRef.getStartTime(), 
							timeRef.getEndTime(), userID);
				Set<CategoryData> tags = new HashSet<CategoryData>();
				if (toCreate != null && toCreate.size() > 0) {
					Iterator i = toCreate.iterator();
					CategoryData tag;
					while (i.hasNext()) {
						tag = (CategoryData) 
							os.createDataObject((CategoryData) i.next(), null);
						tags.add(tag);
					}
				}
				if (toUpdate != null)
					tags.addAll(toUpdate);
				if (images != null) {
					result = os.classify(images, tags);
				} else result = new HashSet();
			}
		};
	}
	
	/**
	 * Creates a {@link BatchCall} to tag the objects.
	 * 
	 * @param ids      	The ids of objects containing the images to tag.
	 * @param rootType	The type of object to handle.
	 * @param toCreate 	The collection of tags to create if any.
	 * @param toUpdate 	The collection of tags to update if any.
	 * @return The {@link BatchCall}.
	 */
	private BatchCall tagImagesInContainer(final Set<Long> ids, 
							final Class rootType,
							final Set<CategoryData> toCreate,
							final Set<CategoryData> toUpdate)
	{
		return new BatchCall("Tagging images.") {
			public void doCall() throws Exception
			{
				OmeroDataService os = context.getDataService();
				//create the tags
				Set<CategoryData> tags = new HashSet<CategoryData>();
				if (toCreate != null && toCreate.size() > 0) {
					Iterator i = toCreate.iterator();
					CategoryData tag;
					while (i.hasNext()) {
						tag = (CategoryData) 
							os.createDataObject((CategoryData) i.next(), null);
						tags.add(tag);
					}
				}
				if (toUpdate != null)
					tags.addAll(toUpdate);
				result = os.tagImagesIncontainers(ids, rootType, tags);
			}
		};
	}
	
	/**
	 * Adds the {@link #call} to the computation tree.
	 * @see BatchCallTree#buildTree()
	 */
	protected void buildTree() { add(call); }

	/**
	 * Returns <code>null</code>, as the return type of the underlying call
	 * <code>void</code>.
	 * @see BatchCallTree#getResult()
	 */
	protected Object getResult() { return result; }
	
	/**
	 * Creates a new instance.
	 * 
	 * @param ids		The ids of objects containing the images to tag.
	 * @param rootType	The type of object to handle.
	 * @param tagLevel	One of the constants defined by this class.
	 * @param toCreate 	The collection of tags to create if any.
	 * @param toUpdate 	The collection of tags to update if any.
	 */
	public TagSaver(Set<Long> ids, Class rootType, int tagLevel,
							Set<CategoryData> toCreate, 
							Set<CategoryData> toUpdate)
	{
		if ((toCreate == null || toCreate.size() == 0) &&
				(toUpdate == null || toUpdate.size() == 0))
			throw new IllegalArgumentException("No tags to create or update.");
		if (ImageData.class.equals(rootType))
			call = tagImages(ids, toCreate, toUpdate);
		else if ((DatasetData.class.equals(rootType) ||
				CategoryData.class.equals(rootType)) && 
				tagLevel == TAG_LEVEL_ONE)
			call = tagImagesInContainer(ids, rootType, toCreate, toUpdate);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param timeRef	The time object defining the time interval.
	 * @param rootType	The type of object to handle.
	 * @param tagLevel	One of the constants defined by this class.
	 * @param toCreate 	The collection of tags to create if any.
	 * @param toUpdate 	The collection of tags to update if any.
	 */
	public TagSaver(TimeRefObject timeRef, Class rootType, int tagLevel,
			Set<CategoryData> toCreate, 
			Set<CategoryData> toUpdate)
	{
		if ((toCreate == null || toCreate.size() == 0) &&
				(toUpdate == null || toUpdate.size() == 0))
			throw new IllegalArgumentException("No tags to create or update.");
		if (ImageData.class.equals(rootType))
			call = tagImages(timeRef, toCreate, toUpdate);
	}

}
