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
import java.util.Map.Entry;
import java.util.Set;

import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserTranslator;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import org.openmicroscopy.shoola.env.data.util.AdvancedSearchResult;
import org.openmicroscopy.shoola.env.data.util.AdvancedSearchResultCollection;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;

import pojos.DataObject;
import pojos.ImageData;

//Third-party libraries
//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailLoader;


class AdvancedResultSearchModel extends DataBrowserModel {

    List<AdvancedSearchResult> data = new ArrayList<AdvancedSearchResult>();

    Set<ImageDisplay> displays = new HashSet<ImageDisplay>();

    /**
     * Creates a new instance.
     * 
     * @param results
     *            The results to display.
     */
    AdvancedResultSearchModel(AdvancedSearchResultCollection results) {

        super(null);
        if (results == null)
            throw new IllegalArgumentException("No results.");

        Iterator<AdvancedSearchResult> it = results.iterator();
        while (it.hasNext()) {
            data.add(it.next());
        }

        Set set = DataBrowserTranslator.transformHierarchy(results
                .getDataObjects(-1, null));
        displays.addAll(set);
        
        browser = BrowserFactory.createBrowser(displays);
    }

    private Set<ImageNode> getImageNodes() {
        Set<ImageNode> nodes = new HashSet<ImageNode>();

        Iterator<ImageDisplay> it = displays.iterator();
        while (it.hasNext()) {
            ImageDisplay d = it.next();
            if (d instanceof ImageNode)
                nodes.add((ImageNode) d);
        }
        return nodes;
    }

    /**
     * Overridden to start several loaders.
     */
    void loadData(boolean refresh, Collection ids) {
        Map<Long, List<ImageData>> map = new HashMap<Long, List<ImageData>>();
        Set<ImageNode> nodes = getImageNodes();
        if (nodes.size() == 0)
            return;
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
        if (map.size() == 0)
            return;
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
     * 
     * @see DataBrowserModel#createDataLoader(boolean, Collection)
     */
    protected List<DataBrowserLoader> createDataLoader(boolean refresh,
            Collection ids) {
        return null;
    }

    /**
     * Returns the type of this model.
     * 
     * @see DataBrowserModel#getType()
     */
    protected int getType() {
        return DataBrowserModel.SEARCH;
    }

    /**
     * No-operation implementation in our case.
     * 
     * @see DataBrowserModel#getNodes()
     */
    protected List<ImageDisplay> getNodes() {
        return null;
    }

    public List<AdvancedSearchResult> getData() {
        return data;
    }

//    public Thumbnail getThumbnail(long imgId) {
//        Iterator<ImageNode> it = getImageNodes().iterator();
//        while (it.hasNext()) {
//            ImageNode node = it.next();
//            if (node.getHierarchyObject() instanceof ImageData) {
//                ImageData img = (ImageData) node.getHierarchyObject();
//                if (img.getId() == imgId) {
//                    return node.getThumbnail();
//                }
//            }
//        }
//        return null;
//    }
}
