/*
 * org.openmicroscopy.shoola.agents.metadata.StructuredDataLoader 
 *
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
package org.openmicroscopy.shoola.agents.metadata;


//Java imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries


import org.apache.commons.collections.CollectionUtils;
//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;

/** 
 * Loads the structured annotations related to a given object.
 * This class calls the <code>loadThumbnails</code> method in the
 * <code>MetadataHandlerView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class StructuredDataLoader 
	extends MetadataLoader
{

	/** The objects the data are related to. */
	private List<DataObject> dataObjects;

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;

	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
	 * @param dataObjects The objects the data are related to.
	 *                   Mustn't be <code>null</code>.
	 * @param loaderID The identifier of the loader.
	 */
	public StructuredDataLoader(MetadataViewer viewer, SecurityContext ctx,
			List<DataObject> dataObjects, int loaderID)
	{
		super(viewer, ctx, null, loaderID);
		if (CollectionUtils.isEmpty(dataObjects))
			throw new IllegalArgumentException("No object specified.");
		this.dataObjects = dataObjects;
	}

	/** 
	 * Loads the data.
	 * @see MetadataLoader#cancel()
	 */
	public void load()
	{
	    handle = mhView.loadStructuredData(ctx, dataObjects, -1, false, this);
	}

	/** 
	 * Cancels the data loading. 
	 * @see MetadataLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }

	/**
     * Feeds the result back to the viewer.
     * @see MetadataLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	if (viewer.getState() == MetadataViewer.DISCARDED) return;  //Async cancel.
    	viewer.setMetadata((Map<DataObject, StructuredDataResults>) result,
    			loaderID);
    }

    /**
     * Notifies the user that an error has occurred and discards the 
     * {@link #viewer}.
     * @see DSCallAdapter#handleException(Throwable)
     */
    public void handleException(Throwable exc)
    {
        handleException(exc, false);
        Map<DataObject, StructuredDataResults> m =
                new HashMap<DataObject, StructuredDataResults>();
        Iterator<DataObject> i = dataObjects.iterator();
        DataObject data;
        while (i.hasNext()) {
            data = i.next();
            m.put(data, new StructuredDataResults(data, false));
        }
        viewer.setMetadata(m, loaderID);
    }
}
