/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.TagSetsModel 
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserTranslator;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import omero.gateway.SecurityContext;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;
import pojos.TagAnnotationData;

/** 
 * Model for the tags sets.
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
class TagSetsModel 	
	extends DataBrowserModel
{

	/** The collection of objects this model is for. */
	private Set<TagAnnotationData> tagSets;

	/**
	 * Creates a new instance.
	 * 
	 * @param ctx The security context.
	 * @param parent	The parent of the datasets.
	 * @param tagSets 	The collection to datasets the model is for.
	 */
	TagSetsModel(SecurityContext ctx, Object parent,
			Set<TagAnnotationData> tagSets)
	{
		super(ctx);
		if (tagSets  == null) 
			throw new IllegalArgumentException("No images.");
		this.tagSets = tagSets;
		this.parent = parent;
		Set visTrees = DataBrowserTranslator.transformHierarchy(tagSets);
        browser = BrowserFactory.createBrowser(visTrees);
        //layoutBrowser();
        Iterator<TagAnnotationData> i = tagSets.iterator();
        TagAnnotationData tag;
        Set<DataObject> objects;
        Iterator<DataObject> k;
        DataObject child;
        Set<DatasetData> datasets;
        Iterator<DatasetData> j;
		while (i.hasNext()) {
			tag = i.next();
			objects = tag.getDataObjects();
			if (objects != null) {
				k = objects.iterator();
				while (k.hasNext()) {
					child = k.next();
					if (child instanceof ImageData)
						numberOfImages++;
					else if (child instanceof DatasetData) {
						numberOfImages += 
							((DatasetData) child).getImages().size();
					} else if (child instanceof ProjectData) {
						datasets = ((ProjectData) child).getDatasets();
						j = datasets.iterator();
						while (j.hasNext()) {
							numberOfImages += (j.next()).getImages().size();
						}
					}
				}
			}
		}
	}
	
	/**
	 * Creates a concrete loader.
	 * @see DataBrowserModel#createDataLoader(boolean, Collection)
	 */
	protected  List<DataBrowserLoader> createDataLoader(boolean refresh, 
			Collection ids)
	{
		if (refresh) imagesLoaded = 0;
		if (imagesLoaded == numberOfImages) return null;
		//only load thumbnails not loaded.
		List<ImageNode> nodes = browser.getVisibleImageNodes();
		if (nodes == null || nodes.size() == 0) return null;
		Iterator<ImageNode> i = nodes.iterator();
		ImageNode node;
		List<DataObject> imgs = new ArrayList<DataObject>();
		if (ids != null) {
			ImageData img;
			while (i.hasNext()) {
				node = i.next();
				img = (ImageData) node.getHierarchyObject();
				if (ids.contains(img.getId())) {
					if (node.getThumbnail().getFullScaleThumb() == null) {
						imgs.add((ImageData) node.getHierarchyObject());
						imagesLoaded++;
					}
				}
			}
		} else {
			while (i.hasNext()) {
				node = i.next();
				if (node.getThumbnail().getFullScaleThumb() == null) {
					imgs.add((ImageData) node.getHierarchyObject());
					imagesLoaded++;
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
	protected int getType() { return DataBrowserModel.TAGSETS; }
	
	/**
	 * No-op implementation in our case.
	 * @see DataBrowserModel#getNodes()
	 */
	protected List<ImageDisplay> getNodes() { return null; }
	
}
