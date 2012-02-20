/*
 * org.openmicroscopy.shoola.agents.measurement.Analyser 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement;

//Java imports
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

import pojos.PixelsData;

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
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class Analyser 
	extends MeasurementViewerLoader
{

	/** Indicates to analyze a collection of shapes. */
	public static final int	SHAPE = 0;
	
	/** Indicates to analyze a collection of ROIs. */ 
	public static final int	ROI = 1;
	
	/** The pixels set to analyze. */
	private PixelsData 	pixels;
	
	/** One of the constants defined by this class. */
	private int			index;
	
	/** Collection of active channels. */
	private List		channels;
	
	/** Collection of shapes to analyze. */
	private List		shapes;
	
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;
   
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
			PixelsData pixels, List channels, List shapes)
	{
		super(viewer, ctx);
		if (channels == null || channels.size() == 0)
			throw new IllegalArgumentException("No channels specified.");
		if (shapes == null || shapes.size() == 0)
			throw new IllegalArgumentException("No shapes specified.");
		this.pixels = pixels;
		this.channels = channels;
		this.shapes = shapes;
		index = SHAPE;
	}
	
	/**
     * Retrieves the data.
     * @see MeasurementViewerLoader#load()
     */
    public void load()
    {
    	switch (index) {
			case SHAPE:
				handle = idView.analyseShapes(ctx, pixels, channels, shapes,
						this);
				break;
			case ROI:
				handle = idView.analyseShapes(ctx, pixels, channels, shapes,
						this);
				break;
		}
    }
    
    /**
     * Indicates that an error occurred while analyzing the data.
     * @see MeasurementViewerLoader#handleNullResult()
     */
    public void handleNullResult() 
    {
    	UserNotifier un = registry.getUserNotifier();
    	un.notifyInfo("Analysing data", "An error occurred while analysing " +
    			"the data.");
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
    	if (viewer.getState() == MeasurementViewer.DISCARDED) return;  //Async cancel.
    	viewer.setStatsShapes((Map) result);
    }
    
}
