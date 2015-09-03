/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import omero.log.LogMessage;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

import omero.gateway.model.PixelsData;

/** 
 * Analyses the collection of ROI shapes.
 * This class calls the <code>analyseShapes</code> method in the
 * <code>ImageDataView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class Analyser 
	extends MeasurementViewerLoader
{
	
	/** The pixels set to analyze. */
	private PixelsData pixels;

	/** Collection of active channels. */
	private Collection channels;
	
	/** Collection of shapes to analyze. */
	private List shapes;
	
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
   
    /**
     * Creates a new instance. 
     * 
     * @param viewer	The viewer this data loader is for.
     *                  Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param pixels	The pixels set to analyze.
     * @param channels	Collection of active channels. 
     * 					Mustn't be <code>null</code>.
     * @param shapes	Collection of shapes to analyze. 
     * 					Mustn't be <code>null</code>.
     */
	public Analyser(MeasurementViewer viewer, SecurityContext ctx,
			PixelsData pixels, Collection channels, List shapes)
	{
		super(viewer, ctx);
		if (CollectionUtils.isEmpty(channels))
			throw new IllegalArgumentException("No channels specified.");
		if (CollectionUtils.isEmpty(shapes))
			throw new IllegalArgumentException("No shapes specified.");
		this.pixels = pixels;
		this.channels = channels;
		this.shapes = shapes;
	}
	
	/**
     * Retrieves the data.
     * @see MeasurementViewerLoader#load()
     */
    public void load()
    {
    	handle = idView.analyseShapes(ctx, pixels, channels, shapes, this);
    }
    
    /**
     * Indicates that an error occurred while analyzing the data.
     * @see MeasurementViewerLoader#handleNullResult()
     */
    public void handleNullResult() 
    {
        handleException(null);
    }
    
    /**
     * Notifies the user that an error has occurred.
     * @see DSCallAdapter#handleException(Throwable) 
     */
    public void handleException(Throwable exc)
    {
        if (exc != null) {
            int state = viewer.getState();
            String s = "Data Retrieval Failure: ";
            LogMessage msg = new LogMessage();
            msg.print("State: "+state);
            msg.print(s);
            msg.print(exc);
            registry.getLogger().error(this, msg);
        }
        UserNotifier un = registry.getUserNotifier();
    	un.notifyInfo("Analyzing data", "An error occurred while analyzing " +
    			"the data.");
    	viewer.setStatsShapes(null);
    }
    
    /**
     * Cancels the data loading.
     * @see MeasurementViewerLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Feeds the result back to the viewer.
     * @see MeasurementViewerLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
    	if (viewer.getState() == MeasurementViewer.DISCARDED) return;
    	viewer.setStatsShapes((Map) result);
    }

}
