/*
 * org.openmicroscopy.shoola.agents.metadata.DataSaver 
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
package org.openmicroscopy.shoola.agents.metadata;


//Java imports
import java.util.Collection;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.metadata.AnnotatedEvent;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.event.EventBus;

import pojos.AnnotationData;
import pojos.DataObject;

/** 
 * Saves the structured annotations.
 * This class calls one of the <code>saveData</code> methods in the
 * <code>MetadataHandlerView</code>.
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
public class DataSaver 
	extends MetadataLoader
{

	/** The objects the data are related to. */
	private Collection<DataObject>	data;

	/** The annotation to add to the data object. */
	private List<AnnotationData> 	toAdd;
	
	/** The annotation to unlink from the data object. */
	private List<Object> 	toRemove;
	
	/** The acquisition metadata. */
	private List<Object> 			acquisitionMetadata;
	
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;
    
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param data The objects the data are related to.
	 *             Mustn't be <code>null</code>.
	 * @param toAdd The collection of annotations to add.
	 * @param toRemove The collection of annotations to remove.
	 * @param acquisitionMetadata The acquisition data to save.
	 * @param loaderID The identifier of the loader.
	 */
	public DataSaver(MetadataViewer viewer, SecurityContext ctx,
		Collection<DataObject> data, List<AnnotationData> toAdd,
		List<Object> toRemove, List<Object> acquisitionMetadata, 
		int loaderID)
	{
		super(viewer, ctx, null, loaderID);
		if (data == null)
			throw new IllegalArgumentException("No object specified.");
		this.data = data;
		this.toAdd = toAdd;
		this.toRemove = toRemove;
		this.acquisitionMetadata = acquisitionMetadata;
	}
	
	/** 
	 * Loads the data.
	 * @see MetadataLoader#load()
	 */
	public void load()
	{
		long userID = MetadataViewerAgent.getUserDetails().getId();
		handle = mhView.saveData(ctx, data, toAdd, toRemove,
				acquisitionMetadata, userID, this);
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
    	viewer.onDataSave((List) data);
    	boolean post = (toAdd != null && toAdd.size() != 0) || 
				(toRemove != null && toRemove.size() != 0);
    	int count = 0;
    	if (toAdd != null) count += toAdd.size();
    	if (toRemove != null) count -= toRemove.size();
    	if (post) {
			EventBus bus = 
				MetadataViewerAgent.getRegistry().getEventBus();
			bus.post(new AnnotatedEvent((List) data, count));
		}
    }
    
}
