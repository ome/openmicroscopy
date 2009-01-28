/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserFactory 
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


//Java imports
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;
import pojos.TagAnnotationData;
import pojos.WellData;

/** 
 * Factory to create {@link DataBrowser} components.
 * This class keeps track of all {@link DataBrowser} instances that have been
 * created and are not yet {@link DataBrowser#DISCARDED discarded}. A new
 * component is only created if none of the <i>tracked</i> ones is already
 * displaying the given hierarchy.  Otherwise, the existing component is
 * recycled.
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
public class DataBrowserFactory
	implements ChangeListener
{

	/** The sole instance. */
	private static final DataBrowserFactory  
						singleton = new DataBrowserFactory();
	
	/** Discards all the tracked {@link DataBrowser}s. */
	public static final void discardAll()
	{
		Iterator<String> i = singleton.browsers.keySet().iterator();
		while (i.hasNext()) 
			singleton.discardedBrowsers.add(i.next());
		singleton.browsers.clear();
	}
	
	/**
	 * Creates a browser to display the results.
	 * 
	 * @param result The value to set.
	 * @return See above.
	 */
	public static final DataBrowser getSearchBrowser(Collection<DataObject> 
													result)
	{
		return singleton.createSearchDataBrowser(result);
	}

	/**
	 * Returns the {@link SearchBrowser} if any.
	 * 
	 * @return See above.
	 */
	public static final DataBrowser getSearchBrowser()
	{
		return singleton.searchBrowser;
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of images.
	 * 
	 * @param grandParent	The grandparent of the node.
	 * @param parent		The parent's node.
	 * @param wells			The collection to set.
	 * @return See above.
	 */
	public static final DataBrowser getWellsDataBrowser(Object grandParent, 
										Object parent, Set<WellData> wells)
	{
		return singleton.createWellsDataBrowser(grandParent, parent, wells);
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of images.
	 * 
	 * @param grandParent	The grandparent of the node.
	 * @param parent		The parent's node.
	 * @param images		The collection to set.
	 * @return See above.
	 */
	public static final DataBrowser getDataBrowser(Object grandParent, 
										Object parent, Set<ImageData> images)
	{
		return singleton.createImagesDataBrowser(grandParent, parent, images);
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of images.
	 * 
	 * @param parent	 The parent's node.
	 * @param nodes		 The collection to set.
	 * @param withImages Pass <code>true</code> to indicate that the images
	 * 					 are loaded, <code>false</code> otherwise.
	 * @return See above.
	 */
	public static final DataBrowser getTagsBrowser(TagAnnotationData parent, 
												Set<DataObject> nodes, 
												boolean withImages)
	{
		return singleton.createTagsDataBrowser(parent, nodes, withImages);
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of images.
	 * 
	 * @param parent	The parent's node.
	 * @param nodes		The collection to set.
	 * @return See above.
	 */
	public static final DataBrowser getDataBrowser(ProjectData parent, 
													Set<DatasetData> nodes)
	{
		return singleton.createDatasetsDataBrowser(parent, nodes);
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed node.
	 * 
	 * @param parent	The node.
	 * @return See above.
	 */
	public static final DataBrowser getDataBrowser(Object parent)
	{
		if (parent == null) return null;
		String key = parent.toString();
		if (parent instanceof DataObject) 
			key += ((DataObject) parent).getId();
		return singleton.browsers.get(key);
	}

	/**
	 * Returns <code>true</code> if a {@link DataBrowser} has been discarded,
	 * <code>false</code> otherwise.
	 * 
	 * @param parent The node.
	 * @return See above.
	 */
	public static final boolean hasBeenDiscarded(Object parent)
	{
		if (parent == null) return false;
		String key = parent.toString();
		if (parent instanceof DataObject) 
			key += ((DataObject) parent).getId();
		Iterator<String> i = singleton.discardedBrowsers.iterator();
		String value;
		while (i.hasNext()) {
			value = i.next();
			if (value.equals(key)) return true;
		}
		return false;
	}
	
	/** 
	 * Refrehes the thumbnails corresponding to the passed image ids.
	 * 
	 * @param ids The collection of images to handle.
	 */
	public static final void refreshThumbnails(Collection ids)
	{
		if (ids != null && ids.size() > 0) {
			if (singleton.searchBrowser != null)
				singleton.searchBrowser.reloadThumbnails(ids);
			Iterator i = singleton.browsers.keySet().iterator();
			while (i.hasNext())
				singleton.browsers.get(i.next()).reloadThumbnails(ids);
		}
	}
	
	/**
	 * Sets to <code>true</code> if some rendering settings have to be copied.
	 * <code>false</code> otherwise.
	 * 
	 * @param rndSettingsToCopy The value to set.
	 */
	public static final void setRndSettingsToCopy(boolean rndSettingsToCopy)
	{
		singleton.rndSettingsToCopy = rndSettingsToCopy; 
		Iterator v = singleton.browsers.keySet().iterator();
		DataBrowserComponent comp;
		while (v.hasNext()) {
			comp = (DataBrowserComponent) singleton.browsers.get(v.next());
			comp.notifyRndSettingsToCopy();
		}
		if (singleton.searchBrowser != null)
			((DataBrowserComponent) 
					singleton.searchBrowser).notifyRndSettingsToCopy();
	}
	
	/**
	 * Sets the type of object to copy or <code>null</code> if no objects to 
	 * copy.
	 * 
	 * @param dataToCopy The type of objects to copy.
	 */
	public static final void setDataToCopy(Class dataToCopy)
	{
		singleton.dataToCopy = dataToCopy;
		Iterator v = singleton.browsers.keySet().iterator();
		DataBrowserComponent comp;
		while (v.hasNext()) {
			comp = (DataBrowserComponent) singleton.browsers.get(v.next());
			comp.notifyDataToCopy();
		}
		if (singleton.searchBrowser != null)
			((DataBrowserComponent) singleton.searchBrowser).notifyDataToCopy();
	}
	
	/**
	 * Returns <code>true</code> if there are rendering settings to copy,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	static boolean hasRndSettingsToCopy()
	{
		return singleton.rndSettingsToCopy;
	}
	
	/**
	 * Returns the type of objects to copy or <code>null</code> if no objects
	 * selected.
	 * 
	 * @return See above.
	 */
	static Class hasDataToCopy()
	{
		return singleton.dataToCopy;
	}
	
	/** The collection of discarded browsers. */
	private Set<String>					discardedBrowsers;
	
	/** Map used to keep track of the browsers. */
	private Map<String, DataBrowser> 	browsers;
	
	/** The {@link DataBrowser} displaying the result of a search. */
	private DataBrowser					searchBrowser;
	
	/** Flag indicating if some rendering settings have been copied. */
	private boolean						rndSettingsToCopy;
	
	/** The type identifying the object to copy. */
	private Class						dataToCopy;
	
	/** Creates a new instance. */
	private DataBrowserFactory()
	{
		browsers = new HashMap<String, DataBrowser>();
		discardedBrowsers = new HashSet<String>();
		searchBrowser = null;
		rndSettingsToCopy = false;
		dataToCopy = null;
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of wells.
	 * 
	 * @param grandParent	The grandParent of the node.
	 * @param parent		The parent's node.
	 * @param wells			The collection to set.
	 * @return See above.
	 */
	private DataBrowser createWellsDataBrowser(Object grandParent, 
												Object parent, 
										Set<WellData> wells)
	{
		DataBrowserModel model = new WellsModel(parent, wells);
		model.setGrandParent(grandParent);
		DataBrowserComponent comp = new DataBrowserComponent(model);
		model.initialize(comp);
		comp.initialize();
		String key = parent.toString();
		if (parent instanceof DataObject) 
			key += ((DataObject) parent).getId();
		browsers.put(key, comp);
		return comp;
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of images.
	 * 
	 * @param grandParent	The grandParent of the node.
	 * @param parent		The parent's node.
	 * @param images		The collection to set.
	 * @return See above.
	 */
	private DataBrowser createImagesDataBrowser(Object grandParent, 
												Object parent, 
										Set<ImageData> images)
	{
		DataBrowserModel model = new ImagesModel(parent, images);
		model.setGrandParent(grandParent);
		DataBrowserComponent comp = new DataBrowserComponent(model);
		model.initialize(comp);
		comp.initialize();
		String key = parent.toString();
		if (parent instanceof DataObject) 
			key += ((DataObject) parent).getId();
		browsers.put(key, comp);
		return comp;
	}

	/**
	 * Creates a new {@link DataBrowser} for the passed collection of datasets.
	 * 
	 * @param parent	The parent's node.
	 * @param datasets	The collection to set.
	 * @return See above.
	 */
	private DataBrowser createDatasetsDataBrowser(DataObject parent, 
											Set<DatasetData> datasets)
	{
		DataBrowserModel model = new DatasetsModel(parent, datasets);
		DataBrowserComponent comp = new DataBrowserComponent(model);
		model.initialize(comp);
		comp.initialize();
		String key = parent.toString()+parent.getId();
		browsers.put(key, comp);
		return comp;
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of tags.
	 * 
	 * @param parent 		The parent's node.
	 * @param dataObjects	The collection to set.
	 * @param withImages    Pass <code>true</code> to indicate that the images
	 * 					    are loaded, <code>false</code> otherwise.
	 * @return See above.
	 */
	private DataBrowser createTagsDataBrowser(DataObject parent, 
			Set<DataObject> dataObjects, boolean withImages)
	{
		DataBrowserModel model = new TagsModel(parent, dataObjects, withImages);
		DataBrowserComponent comp = new DataBrowserComponent(model);
		model.initialize(comp);
		comp.initialize();
		String key = parent.toString()+parent.getId();
		browsers.put(key, comp);
		return comp;
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of tags.
	 * 
	 * @param parent The parent's node.
	 * @param tags	 The collection to set.
	 * @return See above.
	 */
	private DataBrowser createTagSetsDataBrowser(DataObject parent, 
											Set<TagAnnotationData> tags)
	{
		DataBrowserModel model = new TagSetsModel(parent, tags);
		DataBrowserComponent comp = new DataBrowserComponent(model);
		model.initialize(comp);
		comp.initialize();
		String key = parent.toString()+parent.getId();
		browsers.put(key, comp);
		return comp;
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed result.
	 * 
	 * @param result	The result of the search.
	 * @return See above.
	 */
	private DataBrowser createSearchDataBrowser(Collection<DataObject> 
												result)
	{
		DataBrowserModel model = new SearchModel(result);
		DataBrowserComponent comp = new DataBrowserComponent(model);
		model.initialize(comp);
		comp.initialize();
		searchBrowser = comp;
		return comp;
	}

	/**
	 * Removes a browser from the {@link #browsers} set when it is
	 * {@link DataBrowser#DISCARDED discarded}. 
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	
}
