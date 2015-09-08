/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserTranslator;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.DecoratorVisitor;
import omero.gateway.SecurityContext;

import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;

/** 
 * A concrete Model for a collection of D/D hierarchy consisting of a single 
 * tree rooted by given Datasets.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class DatasetsModel 
	extends DataBrowserModel
{

	/**
	 * Creates a new instance.
	 * 
	 * @param ctx The security context.
	 * @param parent	The parent of the datasets.
	 * @param datasets 	The collection to datasets the model is for.
	 */
	DatasetsModel(SecurityContext ctx, Object parent, Set<DatasetData> datasets)
	{
		super(ctx);
		if (datasets  == null) 
			throw new IllegalArgumentException("No datasets.");
		this.parent = parent;
		Set visTrees = DataBrowserTranslator.transformHierarchy(datasets);
        browser = BrowserFactory.createBrowser(visTrees);
        browser.accept(new DecoratorVisitor(getCurrentUser().getId()));
        
        //Visit the node to set the 
        //layoutBrowser();
        Iterator<DatasetData> i = datasets.iterator();
		DatasetData data;
		List<Long> ids = new ArrayList<Long>();
		Set images;
		Iterator j;
		ImageData img;
		while (i.hasNext()) {
			data = i.next();
			images = data.getImages();
			if (images != null) {
				j = images.iterator();
				while (j.hasNext()) {
					img = (ImageData) j.next();
					if (!ids.contains(img.getId())) {
						try {
							img.getDefaultPixels();
							ids.add(img.getId());
							numberOfImages++;
						} catch (Exception e) {}
					}
				}
			}
		}
	}
	
	/**
	 * Creates a concrete loader.
	 * @see DataBrowserModel#createDataLoader(boolean, Collection)
	 */
	protected List<DataBrowserLoader> createDataLoader(boolean refresh,
			Collection ids)
	{
		if (refresh) imagesLoaded = 0;
		if (imagesLoaded != 0 && ids != null)
			imagesLoaded = imagesLoaded-ids.size();
		if (imagesLoaded == numberOfImages) return null;
		//only load thumbnails not loaded.
		List<ImageNode> nodes = browser.getVisibleImageNodes();
		if (nodes == null || nodes.size() == 0) return null;
		Iterator<ImageNode> i = nodes.iterator();
		ImageNode node;
		List<DataObject> imgs = new ArrayList<DataObject>();
		ImageData img;
		List<Long> loaded = new ArrayList<Long>();
		if (ids != null) {
			while (i.hasNext()) {
				node = i.next();
				if (node.getThumbnail().getFullScaleThumb() == null) {
					img = (ImageData) node.getHierarchyObject();
					if (ids.contains(img.getId())) {
						if (!loaded.contains(img.getId())) {
							try {
								img.getDefaultPixels();
								imgs.add(img);
								loaded.add(img.getId());
								imagesLoaded++;
							} catch (Exception e) {}
						}
					}
				}
			}
		} else {
			while (i.hasNext()) {
				node = i.next();
				if (node.getThumbnail().getFullScaleThumb() == null) {
					img = (ImageData) node.getHierarchyObject();
					if (!loaded.contains(img.getId())) {
						try {
							img.getDefaultPixels();
							imgs.add(img);
							loaded.add(img.getId());
							imagesLoaded++;
						} catch (Exception e) {}
					}
				}
			}
		}
		if (imgs.size() == 0) return null;
		return createThumbnailsLoader(sorter.sort(imgs));
	}
	
	/**
	 * Returns the type of this model.
	 * @see DataBrowserModel#getType()
	 */
	protected int getType() { return DataBrowserModel.DATASETS; }

	/**
	 * No-operation implementation in our case.
	 * @see DataBrowserModel#getNodes()
	 */
	protected List<ImageDisplay> getNodes() { return null; }
	
}
