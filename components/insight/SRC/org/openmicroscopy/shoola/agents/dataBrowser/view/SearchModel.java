/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.SearchModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserTranslator;
import org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import omero.gateway.SecurityContext;

import pojos.DataObject;
import pojos.GroupData;
import pojos.ImageData;

/** 
 * Concrete model displaying the result of a search.
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
class SearchModel 
	extends DataBrowserModel
{

	/** The result to display. */
	private Map<SecurityContext, Collection<DataObject>> results;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param results The results to display.
	 */
	SearchModel(Map<SecurityContext, Collection<DataObject>> results)
	{
		super(null);
		if (results  == null) 
			throw new IllegalArgumentException("No results.");
		this.results = results;
		numberOfImages = 0;
		Set<ImageDisplay> vis = new HashSet<ImageDisplay>();
		Iterator<Entry<SecurityContext, Collection<DataObject>>> 
		i = results.entrySet().iterator();
		Entry<SecurityContext, Collection<DataObject>> e;
		SecurityContext ctx;
		GroupData g;
		Collection<DataObject> objects;
		boolean singleGroup = isSingleGroup();
		while (i.hasNext()) {
			e = i.next();
			ctx = e.getKey();
			this.ctx = ctx;
			objects = e.getValue();
			numberOfImages += objects.size();
			if (singleGroup) {
				 vis.addAll(DataBrowserTranslator.transformObjects(objects));
			} else {
				g = getGroup(ctx.getGroupID());
				if (g != null && objects != null && objects.size() > 0)
				    vis.add(DataBrowserTranslator.transformObjects(objects, g));
			}
		}
		browser = BrowserFactory.createBrowser(vis);
	}
	
	/**
	 * Overridden to start several loaders.
	 */
	void loadData(boolean refresh, Collection ids)
	{
		if (refresh) imagesLoaded = 0;
		if (imagesLoaded != 0 && ids != null)
			imagesLoaded = imagesLoaded-ids.size();
		if (imagesLoaded == numberOfImages) return;
		Map<Long, List<ImageData>> map = new HashMap<Long, List<ImageData>>();
		List<ImageNode> nodes = browser.getVisibleImageNodes();
		if (nodes == null || nodes.size() == 0) return;
		Iterator<ImageNode> i = nodes.iterator();
		ImageNode node;
		ImageData image;
		long groupId;
		List<ImageData> imgs;
		if (ids != null) {
			ImageData img;
			while (i.hasNext()) {
				node = i.next();
				img = (ImageData) node.getHierarchyObject();
				if (ids.contains(img.getId())) {
					if (node.getThumbnail().getFullScaleThumb() == null) {
						image = (ImageData) node.getHierarchyObject();
						groupId = image.getGroupId();
						if (!map.containsKey(groupId)) {
							map.put(groupId, new ArrayList<ImageData>());
						}
						imgs = map.get(groupId);
						imgs.add(image);
						imagesLoaded++;
					}
				}
			}
		} else {
			while (i.hasNext()) {
				node = i.next();
				if (node.getThumbnail().getFullScaleThumb() == null) {
					image = (ImageData) node.getHierarchyObject();
					groupId = image.getGroupId();
					if (!map.containsKey(groupId)) {
						map.put(groupId, new ArrayList<ImageData>());
					}
					imgs = map.get(groupId);
					imgs.add(image);
					imagesLoaded++;
				}
			}
		}
		if (map.size() == 0) return;
		Entry<Long, List<ImageData>> e;
		Iterator<Entry<Long, List<ImageData>>> j = map.entrySet().iterator();
		DataBrowserLoader loader;
		Collection<DataObject> l;
		while (j.hasNext()) {
			e = j.next();
			l = sorter.sort(e.getValue());
			loader = new ThumbnailLoader(component, new SecurityContext(
					e.getKey()), l, l.size());
			loader.load();
		}
		state = DataBrowser.LOADING;
	}

	/**
	 * Creates a concrete loader.
	 * @see DataBrowserModel#createDataLoader(boolean, Collection)
	 */
	protected  List<DataBrowserLoader> createDataLoader(boolean refresh, 
			Collection ids)
	{
		return null;
	}

	/**
	 * Returns the type of this model.
	 * @see DataBrowserModel#getType()
	 */
	protected int getType() { return DataBrowserModel.SEARCH; }
	
	/**
	 * No-operation implementation in our case.
	 * @see DataBrowserModel#getNodes()
	 */
	protected List<ImageDisplay> getNodes() { return null; }

}
