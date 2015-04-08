/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserFactory 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.env.data.util.AdvancedSearchResultCollection;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
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
{
	
	/** The sole instance. */
	private static final DataBrowserFactory  
						singleton = new DataBrowserFactory();
	
	/** Discards all the tracked {@link DataBrowser}s. */
	public static final void discardAll()
	{
		Iterator<Entry<Object, DataBrowser>> 
		v = singleton.browsers.entrySet().iterator();
		DataBrowserComponent comp;
		Entry<Object, DataBrowser> entry;
		while (v.hasNext()) {
			entry = v.next();
			comp = (DataBrowserComponent) entry.getValue();
			comp.discard();
			singleton.discardedBrowsers.add((String) entry.getKey());
		}
		System.gc();
		singleton.browsers.clear();
	}
	
	/**
	 * Creates a browser to display the results.
	 * 
	 * @param result The value to set.
	 * @return See above.
	 */
	public static final DataBrowser getSearchBrowser(
			Map<SecurityContext, Collection<DataObject>> result)
	{
		return singleton.createSearchDataBrowser(result);
	}
	
	/**
         * Creates a browser to display the results.
         * 
         * @param result The value to set.
         * @return See above.
         */
        public static final DataBrowser getSearchBrowser(AdvancedSearchResultCollection results)
        {
                return singleton.createSearchDataBrowser(results);
        }

	/**
	 * Returns the browser used for searching data.
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
	 * @param ctx The security context.
	 * @param ancestors		Map containing the ancestors of the node.
	 * @param parent		The parent's node.
	 * @param wells			The collection to set.
	 * @param withThumbnails Pass <code>true</code> to load the thumbnails,
     * 						 <code>false</code> otherwise.
	 * @return See above.
	 */
	public static final DataBrowser getWellsDataBrowser(
			SecurityContext ctx, Map<Class, Object> ancestors, Object parent,
			Set<WellData> wells, boolean withThumbnails)
	{
		return singleton.createWellsDataBrowser(ctx, ancestors, parent, wells,
				withThumbnails);
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of images.
	 * 
	 * @param ctx The security context.
	 * @param grandParent	The grandparent of the node.
	 * @param parent		The parent's node.
	 * @param images		The collection to set.
	 * @param node  		The node to handle.
	 * @return See above.
	 */
	public static final DataBrowser getDataBrowser(
			SecurityContext ctx, Object grandParent, 
					Object parent, Collection<ImageData> images, 
					TreeImageDisplay node)
	{
		return singleton.createImagesDataBrowser(ctx, grandParent, parent,
				images, node);
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of images.
	 * 
	 * @param ctx The security context.
	 * @param parent	 The parent's node.
	 * @param nodes		 The collection to set.
	 * @param withImages Pass <code>true</code> to indicate that the images
	 * 					 are loaded, <code>false</code> otherwise.
	 * @return See above.
	 */
	public static final DataBrowser getTagsBrowser(
			SecurityContext ctx, TagAnnotationData parent, 
			Collection<DataObject> nodes, boolean withImages)
	{
		return singleton.createTagsDataBrowser(ctx, parent, nodes, withImages);
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of 
	 * experimenters.
	 * 
	 * @param ctx The security context.
	 * @param parent		The parent's node.
	 * @param experimenters	The collection to set.
	 * @return See above.
	 */
	public static final DataBrowser getGroupsBrowser(
			SecurityContext ctx, GroupData parent,
			Collection<ExperimenterData> experimenters)
	{
		return singleton.createGroupsBrowser(ctx, parent, experimenters);
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of 
	 * files.
	 * 
	 *  @param ctx The security context.
	 * @param parent		The parent's node.
	 * @param experimenters	The collection to set.
	 * @return See above.
	 */
	public static final DataBrowser getFSFolderBrowser(
			SecurityContext ctx, FileData parent, Collection<DataObject> files)
	{
		return singleton.createFSFolderBrowser(ctx, parent, files);
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of datasets.
	 * 
	 * @param ctx The security context.
	 * @param parent	The parent's node.
	 * @param nodes		The collection to set.
	 * @return See above.
	 */
	public static final DataBrowser getDataBrowser(SecurityContext ctx, 
			ProjectData parent, Set<DatasetData> nodes)
	{
		return singleton.createDatasetsDataBrowser(ctx, parent, nodes);
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed node.
	 * 
	 * @param parent  The node.
	 * @param experimenter  The experimenter associated to the node.
	 * @return See above.
	 */
	public static final DataBrowser getDataBrowser(Object parent)
	{
		if (parent == null) return null;
		return singleton.browsers.get(createKey(parent));
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
		String key = createKey(parent);
		Iterator<String> i = singleton.discardedBrowsers.iterator();
		String value;
		while (i.hasNext()) {
			value = i.next();
			if (value.equals(key)) return true;
		}
		return false;
	}
	
	/** 
	 * Refreshes the thumbnails corresponding to the passed image ids.
	 * 
	 * @param ids The collection of images to handle.
	 */
	public static final void refreshThumbnails(Collection ids)
	{
		if (ids != null && ids.size() > 0) {
			if (singleton.searchBrowser != null)
				singleton.searchBrowser.reloadThumbnails(ids);
			Iterator<Entry<Object, DataBrowser>> 
			v = singleton.browsers.entrySet().iterator();
			DataBrowserComponent comp;
			Entry<Object, DataBrowser> entry;
			while (v.hasNext()) {
				entry = v.next();
				comp = (DataBrowserComponent) entry.getValue();
				comp.reloadThumbnails(ids);
			}
			System.gc();
		}
	}
	
	/**
	 * Sets the image to copy the settings from.
	 * 
	 * @param rndSettingsToCopy The reference image to copy the rendering settings from
         * @param rndDefToCopy 'Pending' rendering settings to copy (can be null)
	 */
	public static final void setRndSettingsToCopy(ImageData rndSettingsToCopy, RndProxyDef rndDefToCopy)
	{
		singleton.rndSettingsToCopy = rndSettingsToCopy; 
		singleton.rndDefToCopy = rndDefToCopy; 
		Iterator<Entry<Object, DataBrowser>> 
		v = singleton.browsers.entrySet().iterator();
		DataBrowserComponent comp;
		Entry<Object, DataBrowser> entry;
		while (v.hasNext()) {
			entry = v.next();
			comp = (DataBrowserComponent) entry.getValue();
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
		Iterator<Entry<Object, DataBrowser>> 
		v = singleton.browsers.entrySet().iterator();
		DataBrowserComponent comp;
		Entry<Object, DataBrowser> entry;
		while (v.hasNext()) {
			entry = v.next();
			comp = (DataBrowserComponent) entry.getValue();
			comp.notifyDataToCopy();
		}
		if (singleton.searchBrowser != null)
			((DataBrowserComponent) singleton.searchBrowser).notifyDataToCopy();
	}
	
	/**
	 * Notifies the model that the user's group has successfully be modified
	 * if the passed value is <code>true</code>, unsuccessfully 
	 * if <code>false</code>.
	 * 
	 * @param success 	Pass <code>true</code> if successful, <code>false</code>
	 * 					otherwise.
	 */
	public static final void onGroupSwitched(boolean success)
	{
		if (!success)  return;
		singleton.dataToCopy = null;
		Iterator<Entry<Object, DataBrowser>> 
		v = singleton.browsers.entrySet().iterator();
		DataBrowserComponent comp;
		Entry<Object, DataBrowser> entry;
		while (v.hasNext()) {
			entry = v.next();
			comp = (DataBrowserComponent) entry.getValue();
			comp.discard();
		}
		singleton.browsers.clear();
		singleton.discardedBrowsers.clear();
		singleton.searchBrowser = null;
	}
	
	/**
	 * Notifies the model that the user has annotated data.
	 * 
	 * @param containers The objects to handle.
	 * @param count A positive value if annotations are added, a negative value
	 * if annotations are removed.
	 */
	public static void onAnnotated(List<DataObject> containers, int count)
	{
		Iterator<Entry<Object, DataBrowser>> 
		v = singleton.browsers.entrySet().iterator();
		DataBrowserComponent comp;
		Entry<Object, DataBrowser> entry;
		while (v.hasNext()) {
			entry = v.next();
			comp = (DataBrowserComponent) entry.getValue();
			comp.onAnnotated(containers, count);
		}
	}
	
	/**
	 * Sets the display mode.
	 * 
	 * @param displayMode The value to set.
	 */
	public static void setDisplayMode(int displayMode)
	{
		Iterator<Entry<Object, DataBrowser>> 
		v = singleton.browsers.entrySet().iterator();
		Entry<Object, DataBrowser> entry;
		while (v.hasNext()) {
			entry = v.next();
			entry.getValue().setDisplayMode(displayMode);
		}
		onGroupSwitched(true);
	}
	
	/**
	 * Returns <code>true</code> if there are rendering settings to copy,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	static boolean hasRndSettingsToCopy()
	{
		return singleton.rndSettingsToCopy != null || singleton.rndDefToCopy != null;
	}
    
	/**
	 * Returns <code>true</code> if the image to copy the rendering settings
	 * from is in the specified group, <code>false</code> otherwise.
	 * 
	 * @param groupID The group to handle.
	 * @return See above.
	 */
	static boolean areSettingsCompatible(long groupID)
	{
		if (!hasRndSettingsToCopy()) return false;
		RndProxyDef def = singleton.rndDefToCopy;
		ImageData img = singleton.rndSettingsToCopy;
		return singleton.rndDefToCopy != null || singleton.rndSettingsToCopy.getGroupId() == groupID;
	}
	
	/**
	 * Returns the type of objects to copy or <code>null</code> if no objects
	 * selected.
	 * 
	 * @return See above.
	 */
	static Class hasDataToCopy() { return singleton.dataToCopy; }
	
	/**
	 * Creates the key identifying the browser from the specified object.
	 * 
	 * @param parent The value to handle.
	 * @return See above.
	 */
	private static final String createKey(Object parent)
	{
		String key = parent.toString();
		if (parent instanceof DataObject) 
			key += ((DataObject) parent).getId();
		else if (parent instanceof TreeImageDisplay)
			key = TreeImageTimeSet.createPath((TreeImageDisplay) parent, key);
		return key;
	}
	
	/** The collection of discarded browsers. */
	private Set<String>					discardedBrowsers;
	
	/** Map used to keep track of the browsers. */
	private Map<Object, DataBrowser> 	browsers;
	
	/** The {@link DataBrowser} displaying the result of a search. */
	private DataBrowser					searchBrowser;
	
	/** The image to copy the rendering settings from. */
	private ImageData rndSettingsToCopy;
	
	/** The copied 'pending' rendering settings */
	private RndProxyDef rndDefToCopy;
	
	/** The type identifying the object to copy. */
	private Class						dataToCopy;
	
	/** Creates a new instance. */
	private DataBrowserFactory()
	{
		browsers = new HashMap<Object, DataBrowser>();
		discardedBrowsers = new HashSet<String>();
		searchBrowser = null;
		rndSettingsToCopy = null;
		dataToCopy = null;
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of wells.
	 * 
	 * @param ctx The security context.
	 * @param ancestors		Map containing the ancestors of the node.
	 * @param parent		The parent's node.
	 * @param wells			The collection to set.
	 * @param withThumbnails Pass <code>true</code> to load the thumbnails,
     * 						 <code>false</code> otherwise.
	 * @return See above.
	 */
	private DataBrowser createWellsDataBrowser(SecurityContext ctx,
			Map<Class, Object> ancestors,
			Object parent, Set<WellData> wells, boolean withThumbnails)
	{
		Object p = parent;
		Object go = null;
		if (parent instanceof PlateAcquisitionData) {
			p = ancestors.get(PlateData.class);
			if (p == null) return null;
			ancestors.remove(PlateData.class);
		}
		if (ancestors.size() > 0) {
			Iterator<Entry<Class, Object>> i = ancestors.entrySet().iterator();
			Entry<Class, Object> entry;
			while (i.hasNext()) {
				entry = i.next();
				go = entry.getValue();
				break;
			}
		}
		DataBrowserModel model = new WellsModel(ctx, p, wells, withThumbnails);
		model.setGrandParent(go);
		DataBrowserComponent comp = new DataBrowserComponent(model);
		model.initialize(comp);
		comp.initialize();
		if (parent instanceof PlateData) {
			PlateData plate = (PlateData) parent;
			Set<PlateAcquisitionData> set = plate.getPlateAcquisitions();
			if (set != null && set.size() == 1) {
				Iterator<PlateAcquisitionData> j = set.iterator();
				while (j.hasNext()) {
					parent = j.next();
				}
			}
		}
		String key = parent.toString();
		if (parent instanceof DataObject) 
			key += ((DataObject) parent).getId();
		browsers.put(key, comp);
		return comp;
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of images.
	 * 
	 * @param ctx The security context.
	 * @param grandParent	The grandParent of the node.
	 * @param parent		The parent's node.
	 * @param images		The collection to set.
	 * @param experimenter  The experimenter associated to the node.
	 * @return See above.
	 */
	private DataBrowser createImagesDataBrowser(
			SecurityContext ctx, Object grandParent, 
					Object parent, Collection<ImageData> images, 
					TreeImageDisplay node)
	{
		DataBrowserModel model = new ImagesModel(ctx, parent, images);
		model.setGrandParent(grandParent);
		DataBrowserComponent comp = new DataBrowserComponent(model);
		model.initialize(comp);
		comp.initialize();
		String key = parent.toString();
		if (parent instanceof DataObject) 
			key += ((DataObject) parent).getId();
		else key = TreeImageTimeSet.createPath(node, key);
		browsers.put(key, comp);
		return comp;
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of datasets.
	 * 
	 * @param ctx The security context.
	 * @param parent	The parent's node.
	 * @param datasets	The collection to set.
	 * @return See above.
	 */
	private DataBrowser createDatasetsDataBrowser(SecurityContext ctx,
			DataObject parent, Set<DatasetData> datasets)
	{
		DataBrowserModel model = new DatasetsModel(ctx, parent, datasets);
		DataBrowserComponent comp = new DataBrowserComponent(model);
		model.initialize(comp);
		comp.initialize();
		StringBuffer buffer = new StringBuffer();
		if (parent == null) {
			buffer.append(DatasetData.class.toString());
			Iterator<DatasetData> i = datasets.iterator();
			List<Long> ids = new ArrayList<Long>();
			while (i.hasNext()) {
				ids.add(((DatasetData) i.next()).getId());
			}
			sortNodes(ids);
			Iterator<Long> j = ids.iterator();
			
			while (j.hasNext()) {
				buffer.append(""+(Long) j.next());
			}
		} else buffer.append(parent.toString()+parent.getId());
		browsers.put(buffer.toString(), comp);
		return comp;
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of tags.
	 * 
	 * @param ctx The security context.
	 * @param parent 		The parent's node.
	 * @param dataObjects	The collection to set.
	 * @param withImages    Pass <code>true</code> to indicate that the images
	 * 					    are loaded, <code>false</code> otherwise.
	 * @return See above.
	 */
	private DataBrowser createTagsDataBrowser(SecurityContext ctx,
		DataObject parent, Collection<DataObject> dataObjects,
		boolean withImages)
	{
		DataBrowserModel model = new TagsModel(ctx, parent, dataObjects,
				withImages);
		DataBrowserComponent comp = new DataBrowserComponent(model);
		model.initialize(comp);
		comp.initialize();
		String key = parent.toString()+parent.getId();
		browsers.put(key, comp);
		return comp;
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of 
	 * experimenters.
	 * 
	 * @param ctx The security context.
	 * @param parent 		The parent's node.
	 * @param experimenters	The collection to set.
	 * @return See above.
	 */
	private DataBrowser createGroupsBrowser(SecurityContext ctx,
			GroupData parent, Collection<ExperimenterData> experimenters)
	{
		DataBrowserModel model = new GroupModel(ctx, parent, experimenters);
		DataBrowserComponent comp = new DataBrowserComponent(model);
		model.initialize(comp);
		comp.initialize();
		String key = parent.toString()+parent.getId();
		browsers.put(key, comp);
		return comp;
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of files.
	 * 
	 * @param ctx The security context.
	 * @param parent	The parent's node.
	 * @param files		The collection to set.
	 * @return See above.
	 */
	private DataBrowser createFSFolderBrowser(SecurityContext ctx,
			FileData parent, Collection<DataObject> files)
	{
		DataBrowserModel model = new FSFolderModel(ctx, parent, files);
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
	 * @param result The result of the search.
	 * @return See above.
	 */
	private DataBrowser createSearchDataBrowser(
			Map<SecurityContext, Collection<DataObject>> result)
	{
		DataBrowserModel model = new SearchModel(result);
		DataBrowserComponent comp = new DataBrowserComponent(model);
		model.initialize(comp);
		comp.initialize();
		searchBrowser = comp;
		return comp;
	}
	
	
	/**
         * Creates a new {@link DataBrowser} for the passed result.
         * 
         * @param result The result of the search.
         * @return See above.
         */
        private DataBrowser createSearchDataBrowser(AdvancedSearchResultCollection result)
        {
                DataBrowserModel model = new AdvancedResultSearchModel(result);
                DataBrowserComponent comp = new DataBrowserComponent(model);
                model.initialize(comp);
                comp.initialize();
                searchBrowser = comp;
                return comp;
        }

    /** 
     * Sorts the passed collection of <code>DataObject</code>s by id.
     * 
     * @param nodes Collection of <code>DataObject</code>s to sort.
     */
    private void sortNodes(List nodes)
    {
        if (nodes == null || nodes.size() == 0) return;
        Comparator c = new Comparator() {
            public int compare(Object o1, Object o2)
            {
                long i1 = ((Long) o1), i2 = ((Long) o2);
                int v = 0;
                if (i1 < i2) v = -1;
                else if (i1 > i2) v = 1;
                return -v;
            }
        };
        Collections.sort(nodes, c);
    }

}
