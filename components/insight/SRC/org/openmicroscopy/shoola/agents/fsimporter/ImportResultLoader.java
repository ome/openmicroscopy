/*
 * org.openmicroscopy.shoola.agents.fsimporter.ImportResultLoader
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.fsimporter;



//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries


import org.apache.commons.collections.CollectionUtils;
//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailProvider;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;
import pojos.DataObject;
import pojos.PixelsData;
import pojos.PlateData;

/**
 * Loads the result of the import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class ImportResultLoader
	extends DataImporterLoader
{

	/** Handle to the asynchronous call so that we can cancel it.*/
	private CallHandle handle;
	
	/** The identifier of the objects to load.*/
	private final Collection<DataObject> ids;
	
	/** 
	 * The type of object to load: supported ImageData, PlateData,
	 * ThumbnailData.*/
	private final Class<?> nodeType;
	
	/** The component hosting the result of the upload.*/
	private Object comp;
	
	/** The result when loading the thumbnails.*/
	private List<Object> result;

	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The Importer this data loader is for.
	 * Mustn't be <code>null</code>.
	 * * @param ctx The security context.
	 * @param ids The collection of objects to load.
	 * @param nodeType The type of node.
	 * @param comp The component hosting the result.
	 */
	public ImportResultLoader(Importer viewer, SecurityContext ctx,
			Collection<DataObject> ids, Class<?> nodeType, Object comp)
	{
		super(viewer, ctx);
		if (CollectionUtils.isEmpty(ids) || nodeType == null)
			throw new IllegalArgumentException("No data to load");
		//Check supported type
		if (!(PlateData.class.equals(nodeType) ||
			ThumbnailData.class.equals(nodeType)))
			throw new IllegalArgumentException("Type not supported");
		this.ids = ids;
		this.nodeType = nodeType;
		this.comp = comp;
	}
	
	/** 
	 * Loads the images or plate.
	 * @see DataImporterLoader#load()
	 */
	public void load()
	{
		if (nodeType.equals(PlateData.class)) {
			List<Long> objects = new ArrayList<Long>();
			Iterator<DataObject> i = ids.iterator();
			PixelsData pxd;
			while (i.hasNext()) {
				pxd = (PixelsData) i.next();
				objects.add(pxd.getImage().getId());
			}
			handle = dmView.loadPlateFromImage(ctx, objects, this);
		} else if (nodeType.equals(ThumbnailData.class)) {
			handle = hiBrwView.loadThumbnails(ctx, ids,
                    ThumbnailProvider.THUMB_MAX_WIDTH,
                    ThumbnailProvider.THUMB_MAX_HEIGHT,
                    -1, HierarchyBrowsingView.IMAGE, this);
		}
	}
	
	/** 
	 * Cancels the data loading.
	 * @see DataImporterLoader#load()
	 */
	public void cancel() { handle.cancel(); }

	 /** 
     * Feeds the thumbnails back to the viewer, as they arrive. 
     * @see DataBrowserLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
    	if (viewer.getState() == DataBrowser.DISCARDED ||
    			!ThumbnailData.class.equals(nodeType)) return;
    	ThumbnailData td = (ThumbnailData) fe.getPartialResult();
    	if (td != null) {
    		if (result == null) result = new ArrayList<Object>();
        	result.add(td);
    		if (result.size() == ids.size())
    			viewer.setImportResult(result, comp);
    	}
    }

    /**
     * Feeds the result back to the viewer.
     * @see DataImporterLoader#handleResult(Object)
     */
	public void handleResult(Object result)
	{
		if (viewer.getState() == DataBrowser.DISCARDED ||
				!PlateData.class.equals(nodeType)) return;
		//Handle the plate.
		Map<Long, Object> m = (Map<Long, Object>) result;
		if (m.size() == 1) {
			Iterator<Object> i = m.values().iterator();
			while (i.hasNext()) {
				viewer.setImportResult(i.next(), comp);
			}
		}
	}
}
