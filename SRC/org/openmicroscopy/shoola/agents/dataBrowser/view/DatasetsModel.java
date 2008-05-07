/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.DatasetsModel 
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserTranslator;
import org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * 
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
class DatasetsModel 
	extends DataBrowserModel
{

	/** The colleciton of objects this model is for. */
	private Set<DatasetData> datasets;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent	The parent of the datasets.
	 * @param datasets 	The collection to datasets the model is for.
	 */
	DatasetsModel(Object parent, Set<DatasetData> datasets)
	{
		super();
		if (datasets  == null) 
			throw new IllegalArgumentException("No images.");
		this.datasets = datasets;
		this.parent = parent;
		long userID = DataBrowserAgent.getUserDetails().getId();
		Set visTrees = DataBrowserTranslator.transformHierarchy(datasets, 
							userID, 0);
        browser = BrowserFactory.createBrowser(visTrees);
        layoutBrowser();
        Iterator<DatasetData> i = datasets.iterator();
		DatasetData data;
		while (i.hasNext()) {
			data = i.next();
			numberOfImages += data.getImages().size();
		}
	}
	
	/**
	 * Creates a concrete loader.
	 * @see DataBrowserModel#createDataLoader(boolean)
	 */
	protected DataBrowserLoader createDataLoader(boolean refresh)
	{
		/*
		if (refresh) {
			Iterator<DatasetData> i = datasets.iterator();
			Set<ImageData> images = new HashSet<ImageData>();
			DatasetData data;
			while (i.hasNext()) {
				data = i.next();
				images.addAll(data.getImages());
			}
			return new ThumbnailLoader(component, images);
		}
		*/
		if (imagesLoaded == numberOfImages) return null;
		//only load thumbnails not loaded.
		List<ImageNode> nodes = browser.getVisibleImageNodes();
		if (nodes == null || nodes.size() == 0) return null;
		Iterator<ImageNode> i = nodes.iterator();
		ImageNode node;
		Set<ImageData> imgs = new HashSet<ImageData>();
		while (i.hasNext()) {
			node = i.next();
			if (node.getThumbnail().getFullScaleThumb() == null) {
				imgs.add((ImageData) node.getHierarchyObject());
				imagesLoaded++;
			}
		}
		return new ThumbnailLoader(component, imgs);
	}
}
