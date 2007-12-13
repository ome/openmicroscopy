/*
 * org.openmicroscopy.shoola.agents.util.tagging.view.TaggerModel 
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
package org.openmicroscopy.shoola.agents.util.tagging.view;

//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.tagging.TaggerLoader;
import org.openmicroscopy.shoola.agents.util.tagging.TagsLoader;
import org.openmicroscopy.shoola.agents.util.tagging.TagsSaver;
import org.openmicroscopy.shoola.agents.util.tagging.util.TagSaverDef;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;

import pojos.CategoryData;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
 * The Model component in the <code>Tagger</code> MVC triad.
 * This class tracks the <code>Tagger</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. This class  provide  a suitable data loader. 
 * The {@link TaggerComponent} intercepts the 
 * results of data loadings, feeds them back to this class and fires state
 * transitions as appropriate.
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
class TaggerModel
{

	 /** Holds one of the state flags defined by {@link Tagger}. */
    private int					state;
    
    private TimeRefObject		timeReference;
    
    /** The collection of objects' id this model is for. */
    private Set<Long>			ids;
    
    /** Collection of tags linked to the object. */
	private List				tags;

	/** Collection of available tags.*/
	private List				availableTags;

	/** Collection of tags i.e. the available and the ones linked to objects. */
	private List				allTags;
	
	/** Collection of tag sets. */
	private List				tagSets;
	
	/** Reference to the current loader. */
	private TaggerLoader		currentLoader;
	
	/** Helper reference used to sort tags. */
	private ViewerSorter		sorter;

	/** The type of data objects to tag. */
	private Class				rootType;
	
	/** One of the tagging constants defined by {@link Tagger} I/F. */
	private int					taggingMode;
	
    /** Reference to the component that embeds this model. */
    protected TaggerComponent 	component;
    
    /**
     * Creates a new instance.
     * 
     * @param timeReference	Collection of objects' id the tagger is for.
     * @param rootType		The type of data objects to tag.
     */
    TaggerModel(TimeRefObject timeReference, Class rootType)
    {
    	this.timeReference = timeReference;
    	this.rootType = rootType;
    	state = Tagger.NEW;
    	taggingMode = Tagger.BULK_TAGGING_MODE;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param ids 		Collection of objects' id the tagger is for.
     * @param rootType	The type of data objects to tag.
     */
    TaggerModel(Set<Long> ids, Class rootType)
    {
    	this(ids, rootType, Tagger.TAGGING_MODE);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param ids 			Collection of images' id the tagger is for.
     * @param rootType		The type of data objects to tag.
     * @param taggingMode	One of the tagging constants defined by 
     * 						{@link Tagger} I/F.
     */
    TaggerModel(Set<Long> ids, Class rootType, int taggingMode)
    {
    	this.ids = ids;
    	this.rootType = rootType;
    	state = Tagger.NEW;
    	this.taggingMode = taggingMode;
    }
    
    /**
	 * Called by the <code>Tagger</code> after creation to allow this
	 * object to store a back reference to the embedding component.
	 * 
	 * @param component The embedding component.
	 */
    void initialize(TaggerComponent component)
    {
    	this.component = component;
    }
    
    /**
     * Returns the current state.
     * 
     * @return See above.
     */
	int getState() { return state; }

	/**
	 * Returns the collection of tags.
	 * 
	 * @return See above.
	 */
	List getTags() { return tags; }

	/**
	 * Returns the collection of tags.
	 * 
	 * @return See above.
	 */
	List getAvailableTags() { return availableTags; }
	
	/**
	 * Returns the collection of tag sets.
	 * 
	 * @return See above.
	 */
	List getTagSets() { return tagSets; }

	/**
	 * Returns all the tags i.e. the ones available and the ones linked to 
	 * the objects.
	 * 
	 * @return See above.
	 */
	List getAllTags() { return allTags; }
	
	/**
	 * Returns the <code>CategoryData</code> if the passed value is a name of 
	 * a tag the image is tagged with, <code>null</code> otherwise.
	 * 
	 * @param n	The value to handle.
	 * @return See above.
	 */
	CategoryData checkUsedTag(String n)
	{
		Iterator i = tags.iterator();
		CategoryData item;
		while (i.hasNext()) {
			item = (CategoryData) i.next();
			if (item.getName().equals(n)) return item;
		}
		return null;
	}

	/**
	 * Returns the <code>CategoryData</code> if the passed value is a name of 
	 * a tag the image is tagged with, <code>null</code> otherwise.
	 * 
	 * @param n	The value to handle.
	 * @return See above.
	 */
	CategoryData checkAvailableTag(String n)
	{
		Iterator i = availableTags.iterator();
		CategoryData item;
		while (i.hasNext()) {
			item = (CategoryData) i.next();
			if (item.getName().equals(n)) return item;
		}
		return null;
	}
	
	/**
	 * Sets the tags, the tags not used and the colllection of tag sets.
	 * 
	 * @param tags			The tags linked to the image.
	 * @param availableTags	The available tag.
	 * @param tagSets		The collection of tag sets.
	 */
	void setTags(List tags, List availableTags, List tagSets)
	{
		if (sorter == null) sorter = new ViewerSorter();
		List l, available, groups;
		if (tags == null || tags.size() == 0)
			l = new ArrayList();
		else l = sorter.sort(tags);
		if (availableTags == null || availableTags.size() == 0)
			available = new ArrayList();
		else available = sorter.sort(availableTags);
		if (tagSets == null || tagSets.size() == 0)
			groups = new ArrayList();
		else groups = sorter.sort(tagSets);
		this.tags = l;
		this.availableTags = available;
		this.tagSets = groups;
		List all = new ArrayList();
		List<Long> ids = new ArrayList<Long>();
		
		Iterator i = l.iterator();
		CategoryData tag;
		while (i.hasNext()) {
			tag = (CategoryData) i.next();
			if (!ids.contains(tag.getId())) {
				all.add(tag);
				ids.add(tag.getId());
			}
		}
		i = available.iterator();
		while (i.hasNext()) {
			tag = (CategoryData) i.next();
			if (!ids.contains(tag.getId())) {
				all.add(tag);
				ids.add(tag.getId());
			}
		}
		
		allTags = sorter.sort(all);
		state = Tagger.READY;
	}
	
	/**
	 * Returns the currently selected experimenter.
	 * 
	 * @return See above.
	 */
	ExperimenterData getExperimenter()
	{
		return (ExperimenterData) TaggerFactory.getRegistry().lookup(
					LookupNames.CURRENT_USER_DETAILS);
	}
	
	/**
	 * Fires an asynchronous call to retrieve the tags related to the 
	 * image.
	 */
    void fireTagsRetrieval()
    {
    	state = Tagger.LOADING;
    	switch (taggingMode) {
			case Tagger.TAGGING_MODE:
				currentLoader = new TagsLoader(component, ids, 
						getExperimenter().getId());
				break;
			case Tagger.BULK_TAGGING_MODE:
				currentLoader = new TagsLoader(component,  
						getExperimenter().getId());
				break;
		}
    	
    	currentLoader.load();
    }
    
    private int getTaggingLevel()
    {
    	switch (taggingMode) {
			case Tagger.BULK_TAGGING_MODE:
				return TagsSaver.LEVEL_ONE;
	
			default:
				return TagsSaver.LEVEL_ZERO;
		}
    }
    
    /**
     * Fires and asynchronous call to save the tags related to the image.
     * 
     * @param def The object hosting the tags to save.
     */
    void fireTagsSaving(TagSaverDef def)
    {
    	state = Tagger.SAVING;
    	Set<CategoryData> toCreate = def.getTagsToCreate();
		Set<CategoryData> toUpdate = def.getTagsToUpdate();
		if (timeReference == null)
			currentLoader = new TagsSaver(component, ids, rootType, toCreate,
										toUpdate, getTaggingLevel());
		else 
			currentLoader = new TagsSaver(component, timeReference, 
										ImageData.class, toCreate,
										toUpdate, TagsSaver.LEVEL_ONE);
    	currentLoader.load();
    }
    
}
