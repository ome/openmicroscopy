/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.SearchThumbnailLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailProvider;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;

import omero.gateway.SecurityContext;
import omero.gateway.model.SearchResult;
import omero.gateway.model.SearchResultCollection;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;

/**
 * A DataBrowserModel for search results
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * 
 * @since 5.0
 */
public class AdvancedResultSearchModel extends DataBrowserModel {

    /** Maximum number of thumbnails to load */
    private static final int MAX_THUMBS = 100;

    /** Holds all the ImageDisplays */
    private List<ImageDisplay> displays = new ArrayList<ImageDisplay>();

    /** Holds the thumbnails */
    private Map<DataObject, Thumbnail> thumbs = new HashMap<DataObject, Thumbnail>();

    /** References to the tables to be notified when thumbs have been loaded */
    private List<SearchResultTable> tables = new ArrayList<SearchResultTable>();

    /** Reference to the search results */
    private SearchResultCollection results;
    
    /**
     * Creates a new instance.
     * 
     * @param results
     *            The results to display.
     */
    public AdvancedResultSearchModel(SearchResultCollection results) {

        super(null);
        if (results == null)
            throw new IllegalArgumentException("No results.");
        
        this.results = results;

        displays.addAll(createDisplays(results.getDataObjects(-1,
                ProjectData.class)));
        
        displays.addAll(createDisplays(results.getDataObjects(-1,
                ScreenData.class)));
        
        displays.addAll(createDisplays(results.getDataObjects(-1,
                DatasetData.class)));

        displays.addAll(createDisplays(results.getDataObjects(-1,
                PlateData.class)));
        
        displays.addAll(createDisplays(results.getDataObjects(-1,
                PlateAcquisitionData.class)));
        
        List<DataObject> imgs = results.getDataObjects(-1, ImageData.class);
        List<ImageDisplay> imgNodes = createDisplays(imgs);
        displays.addAll(imgNodes);

        browser = BrowserFactory.createBrowser(displays);
    }

    /**
     * Registers a table to be notified when thumbs have been loaded
     * 
     * @param table
     */
    public void registerTable(SearchResultTable table) {
        this.tables.add(table);
    }

    /**
     * Creates the {@link ImageDisplay}s for the given {@link DataObject}s
     * 
     * @param dataObjs
     * @return
     */
    private List<ImageDisplay> createDisplays(Collection<DataObject> dataObjs) {
        List<ImageDisplay> result = new ArrayList<ImageDisplay>();

        for (DataObject dataObj : dataObjs) {
            ImageDisplay d = null;

            if (dataObj instanceof ImageData) {
                d = new ImageNode("", dataObj, null);
            } else if (dataObj instanceof ProjectData
                    || dataObj instanceof DatasetData
                    || dataObj instanceof ScreenData
                    || dataObj instanceof PlateData
                    || dataObj instanceof PlateAcquisitionData) {
                d = new ImageSet("", dataObj);
            }

            if (d != null)
                result.add(d);
        }

        return result;
    }

    @Override
    void loadData(boolean refresh, Collection ids) {
        loadThumbs();
    }

    /**
     * Starts a loader for each group to load the thumbnails
     */
    private void loadThumbs() {

        int count = 0;
        Map<Long, List<ImageData>> map = new HashMap<Long, List<ImageData>>();
        for (ImageDisplay d : displays) {
            DataObject obj = (DataObject) d.getHierarchyObject();
            if (!(obj instanceof ImageData))
                continue;

            if (count >= MAX_THUMBS)
                break;

            List<ImageData> objs = map.get(obj.getGroupId());
            if (objs == null) {
                objs = new ArrayList<ImageData>();
                map.put(obj.getGroupId(), objs);
            }
            objs.add((ImageData) obj);
            count++;
        }

        for (Entry<Long, List<ImageData>> e : map.entrySet()) {
            List<ImageData> imgs = e.getValue();
            if (!imgs.isEmpty()) {
                SearchThumbnailLoader loader = new SearchThumbnailLoader(
                        component, new SecurityContext(e.getKey()), imgs, this);
                loader.load();
            }
        }
    }

    /**
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
     * @see DataBrowserModel#getNodes()
     */
    protected List<ImageDisplay> getNodes() {
        return displays;
    }

    /**
     * Add a thumbnail for a certain image
     * 
     * @param imgId
     * @param img
     */
    public void setThumbnail(long imgId, BufferedImage img) {
        for (ImageDisplay d : displays) {
            if (d.getHierarchyObject() instanceof ImageData
                    && ((ImageData) d.getHierarchyObject()).getId() == imgId) {
                ImageData refObj = (ImageData) d.getHierarchyObject();
                ThumbnailProvider thumb = new ThumbnailProvider(refObj);
                thumb.setFullScaleThumb(img);
                thumbs.put(refObj, thumb);
                break;
            }
        }
    }

    /**
     * Get the thumbnail for a certain image
     * 
     * @param refObj The image to handle.
     * @return See above
     */
    public Thumbnail getThumbnail(DataObject refObj) {
        return thumbs.get(refObj);
    }

    /**
     * Notifies the tables that the thumbnails have been loaded
     */
    public void notifyThumbsLoaded() {
        for (SearchResultTable table : tables) {
            table.repaint();
        }
    }

    /**
     * Checks if the search result corresponding to the provided type and id is
     * an ID match
     * 
     * @param type The type of data object
     * @param id The object to handle.
     * @return <code>true</code> if found, <code>false</code> otherwise.
     */
    public boolean isIdMatch(Class<? extends DataObject> type, long id) {
        for (SearchResult r : results) {
            if (r.isIdMatch() && r.getObjectId() == id
                    && r.getType().equals(type))
                return true;
        }
        return false;
    }
}
