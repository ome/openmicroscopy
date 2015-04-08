/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.ImagesModel 
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
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.DecoratorVisitor;
import omero.gateway.SecurityContext;
import pojos.DataObject;
import pojos.ImageData;

/** 
 * A concrete Model for a collection of images.
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
class ImagesModel 
	extends DataBrowserModel
{

	/** The images to lay out. */
	private Collection<ImageData> images;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param ctx The security context.
	 * @param parent The parent.
	 * @param images The collection to images the model is for.
	 */
	ImagesModel(SecurityContext ctx, Object parent,
			Collection<ImageData> images)
	{
		super(ctx);
		if (images  == null) 
			throw new IllegalArgumentException("No images.");
		this.images = images;
		this.parent = parent;
		numberOfImages = images.size();
		Set visTrees = DataBrowserTranslator.transformImages(images);
        browser = BrowserFactory.createBrowser(visTrees);
        browser.accept(new DecoratorVisitor(getCurrentUser().getId()));
        //layoutBrowser();
	}
	
	/**
	 * Creates a concrete loader.
	 * @see DataBrowserModel#createDataLoader(boolean, Collection)
	 */
	protected  List<DataBrowserLoader> createDataLoader(boolean refresh, 
			Collection ids)
	{
		if (refresh) imagesLoaded = 0;
		if (imagesLoaded != 0 && ids != null)
			imagesLoaded = imagesLoaded-ids.size();
		
		//only load thumbnails not loaded.
		if (imagesLoaded == numberOfImages) return null;
		List<ImageNode> nodes = browser.getVisibleImageNodes();
		if (nodes == null || nodes.size() == 0) return null;
		Iterator<ImageNode> i = nodes.iterator();
		ImageNode node;
		List<DataObject> imgs = new ArrayList<DataObject>();
		ImageData img;
		if (ids != null) {
			while (i.hasNext()) {
				node = i.next();
				img = (ImageData) node.getHierarchyObject();
				if (ids.contains(img.getId())) {
					if (node.getThumbnail().getFullScaleThumb() == null) {
						try {
							//valid.
							img.getDefaultPixels();
							imgs.add(img);
							imagesLoaded++;
						} catch (Exception e) {}
					}
				}
			}
		} else {
			while (i.hasNext()) {
				node = i.next();
				img = (ImageData) node.getHierarchyObject();
				if (node.getThumbnail().getFullScaleThumb() == null) {
					try {
						img.getDefaultPixels();
						imgs.add(img);
						imagesLoaded++;
					} catch (Exception e) {
						numberOfImages--;
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
	protected int getType() { return DataBrowserModel.IMAGES; }
	
	/**
	 * No-op implementation in our case.
	 * @see DataBrowserModel#getNodes()
	 */
	protected List<ImageDisplay> getNodes() { return null; }
	
}
