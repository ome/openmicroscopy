/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.util;

import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.shoola.agents.dataBrowser.view.QuickSearch;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.tagging.util.TagCellRenderer;
import org.openmicroscopy.shoola.agents.util.tagging.util.TagItem;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.util.ui.HistoryDialog;
import org.openmicroscopy.shoola.util.ui.search.SearchObject;
import org.openmicroscopy.shoola.util.ui.search.SearchUtil;
import omero.gateway.model.DataObject;
import omero.gateway.model.TagAnnotationData;

/** 
 * Filtering the data displaying the browser.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class QuickFiltering
	extends QuickSearch
	implements PropertyChangeListener
{

	/** Bound property indicating to load the tags. */
	public static final String TAG_LOADING_PROPERTY = "tagLoading";
	
	/** Bound property indicating to filter the data. */
	public static final String	FILTER_DATA_PROPERTY = "filterData";
	
	/** Bound property indicating to filter the data. */
	public static final String	FILTER_TAGS_PROPERTY = "filterTags";
	
	/** Bound property indicating to display all nodes. */
	public static final String	DISPLAY_ALL_NODES_PROPERTY = "displayAllNodes";	
	
	/** The collection of tags if any. */
	private Collection 		tags;
	
	 /** The dialog displaying the existing tags. */
    private HistoryDialog	tagsDialog;
   
	/** Loads or displays the existing tags. */
	private void handleTagInsert()
	{
		if (tags == null) {
    		firePropertyChange(TAG_LOADING_PROPERTY, Boolean.valueOf(false), 
    				Boolean.valueOf(true));
    		return;
    	}
		codeCompletion();
		
		if (tagsDialog == null) return;
		String name = getSearchValue();
		
		List<String> l = SearchUtil.splitTerms(name, 
				SearchUtil.COMMA_SEPARATOR);
		if (l.size() > 0) {
			if (tagsDialog.setSelectedTextValue(l.get(l.size()-1).trim())) {
        		Rectangle r = getSelectionArea().getBounds();
        		tagsDialog.setFocusable(false);
        		tagsDialog.show(getSelectionArea(), 0, r.height);
        		//setFocusOnArea();
        	} else tagsDialog.setVisible(false);
		}
	}
	
	 /** Initializes the {@link HistoryDialog} used for code completion. */
    private void codeCompletion()
    {
    	if (tagsDialog != null) return;
    	Rectangle r = getSelectionArea().getBounds();
		Object[] data = null;
		if (tags != null && tags.size() > 0) {

			data = new Object[tags.size()];
			Iterator j = tags.iterator();
			DataObject object;

			TagItem item;
			int i = 0;
			while (j.hasNext()) {
				object = (DataObject) j.next();
				item = new TagItem(object);
				data[i] = item;
				i++;
			}
			long id = MetadataViewerAgent.getUserDetails().getId();
			tagsDialog = new HistoryDialog(data, r.width);
			tagsDialog.setListCellRenderer(new TagCellRenderer(id));
			tagsDialog.addPropertyChangeListener(
					HistoryDialog.SELECTION_PROPERTY, this);
		}
    }
    
	/** 
	 * Creates a new instance. 
	 * 
	 * @param text The default context.
	 * */
	public QuickFiltering(String text)
	{
		setSingleSelection(true);
		setDefaultSearchContext(text);
		setSearchEnabled(false);
		addPropertyChangeListener(this);
	}
	
	/**
	 * Sets the collection of existing tags.
	 * 
	 * @param tags The value to set.
	 */
	public void setTags(Collection tags) 
	{
		if (tags == null) return;
		this.tags = tags;
	}
	
	/**
	 * Returns the collection of tags.
	 * 
	 * @return See above. 
	 */
	public Collection getTags() { return tags; }
	
	/**
	 * Sets the selected tags.
	 * 
	 * @param tags The tags to filter by.
	 */
	public void setSelectedTags(Collection tags)
	{
		clear();
		if (tags == null || tags.size() == 0) {
			firePropertyChange(DISPLAY_ALL_NODES_PROPERTY, 
					Boolean.valueOf(false), Boolean.valueOf(true));
			
			return;
		}
		List<String> list = new ArrayList<String>();
		Iterator i = tags.iterator();
		TagAnnotationData tag;
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			list.add(tag.getTagValue());
		}
		setSearchValue(list, false);
		FilterContext context = new FilterContext(); 
		List<String> l = SearchUtil.splitTerms(getSearchValue(),  
				SearchUtil.COMMA_SEPARATOR); 
		if (l != null && l.size() > 0) { 
			context.addAnnotationType(TagAnnotationData.class, l); 
			firePropertyChange(FILTER_TAGS_PROPERTY, null, context); 
		} 
	}
	
	/**
	 * Overridden to load the tags if the {@link QuickSearch#TAGS} is selected.
	 * @see QuickSearch#handleTextInsert()
	 */
	protected void handleTextInsert()
	{
		SearchObject node = getSelectedNode();
		if (node == null) return;
		switch (node.getIndex()) {
			case QuickSearch.TAGS:
				handleTagInsert();
				break;
		}
	}
	
	/**
	 * Overridden to load the tags if the {@link QuickSearch#TAGS} is selected.
	 * @see QuickSearch#handleKeyEnter()
	 */
	protected void handleKeyEnter()
	{
		SearchObject node = getSelectedNode();
		if (node == null) return;
		switch (node.getIndex()) {
			case QuickSearch.TAGS:
				if (tagsDialog != null && tagsDialog.isVisible()) {
					Object item = tagsDialog.getSelectedTextValue();
					if (!(item instanceof TagItem)) return;
					DataObject ho = ((TagItem) item).getDataObject();
					if (ho instanceof TagAnnotationData) {
						String v = ((TagAnnotationData) ho).getTagValue();
						setSearchValue(v, true);
						FilterContext context = new FilterContext();
						List<String> l = SearchUtil.splitTerms(getSearchValue(), 
								SearchUtil.COMMA_SEPARATOR);
						if (l != null && l.size() > 0) {
							context.addAnnotationType(TagAnnotationData.class, 
									l);
							firePropertyChange(FILTER_TAGS_PROPERTY, null, 
									context);
						}
					}
				} else  {
					onNodeSelection();
				}
				break;
			case QuickSearch.FULL_TEXT:
			case QuickSearch.COMMENTS:
				onNodeSelection();
		}
	}
	
	
	/**
	 * Reacts to the property fired by the <code>SearchContextMenu</code>
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		super.propertyChange(evt);
		String name = evt.getPropertyName();
		if (HistoryDialog.SELECTION_PROPERTY.equals(name)) { 
			Object item = evt.getNewValue(); 
			if (!(item instanceof TagItem)) return; 
			DataObject ho = ((TagItem) item).getDataObject(); 
			if (ho instanceof TagAnnotationData) { 
				String v = ((TagAnnotationData) ho).getTagValue(); 
				setSearchValue(v, true); 
				FilterContext context = new FilterContext(); 
				List<String> l = SearchUtil.splitTerms(getSearchValue(),  
						SearchUtil.COMMA_SEPARATOR); 
				if (l != null && l.size() > 0) { 
					context.addAnnotationType(TagAnnotationData.class, l); 
					firePropertyChange(FILTER_TAGS_PROPERTY, null, context); 
				} 
			} 
		} else if (VK_UP_SEARCH_PROPERTY.equals(name)) {
			if (tagsDialog != null && tagsDialog.isVisible())
				tagsDialog.setSelectedIndex(false);
		} else if (VK_DOWN_SEARCH_PROPERTY.equals(name)) {
			if (tagsDialog != null && tagsDialog.isVisible())
				tagsDialog.setSelectedIndex(true);
		} else if (QUICK_SEARCH_PROPERTY.equals(name)) {
			if (tagsDialog != null && tagsDialog.isVisible()) {
				/*
				Object item = tagsDialog.getSelectedTextValue();
				if (!(item instanceof TagItem)) return;
				DataObject ho = ((TagItem) item).getDataObject();
				if (ho instanceof TagAnnotationData) {
					String v = ((TagAnnotationData) ho).getTagValue();
					setSearchValue(v, false);
					FilterContext context = new FilterContext();
					List<String> l = SearchUtil.splitTerms(getSearchValue(), 
							SearchUtil.COMMA_SEPARATOR);
					if (l != null && l.size() > 0) {
						context.addAnnotationType(TagAnnotationData.class, l);
						firePropertyChange(FILTER_TAGS_PROPERTY, null, context);
					}
				}
				*/
			} else
				firePropertyChange(FILTER_DATA_PROPERTY, evt.getOldValue(), 
						          evt.getNewValue());
		}
	}
	
}
