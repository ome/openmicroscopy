/*
 * org.openmicroscopy.shoola.agents.metadata.EditorLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014-2015 University of Dundee. All rights reserved.
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


import java.util.Collection;

import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import omero.gateway.model.ROIResult;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.ImageDataView;
import omero.log.LogMessage;
import org.openmicroscopy.shoola.agents.events.metadata.ROICountLoaded;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;

/** 
 * An async. loader which updates the UI components with
 * the number of ROIs the image associated to the PreviewToolBar has.
 *
 * @author  Domink Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ROICountLoader
	extends MetadataLoader
{
    /** Reference to the registry */
    private final Registry registry;
    
    /** Reference to the ImageDataView */
    private final ImageDataView imView;
    
    /** The id of the image the ROIs are related to. */
    private long            imageID;
    
    /** The id of the user. */
    private long            userID;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Creates a new instance
     * 
     * @param viewer Reference to the viewer
     * @param ctx The SecurityContext
     * @param loaderID The loader ID
     * @param imageId The image id to load the ROIs for
     * @param userID The user id
     */
    public ROICountLoader(MetadataViewer viewer, SecurityContext ctx,
            int loaderID, long imageId, long userID) {
        super(viewer, ctx, loaderID);
        this.imageID = imageId;
        this.userID = userID;

        registry = MetadataViewerAgent.getRegistry();
        imView = (ImageDataView) registry
                .getDataServicesView(ImageDataView.class);
    }
    
    /**
     * Handles a null result
     */
    public void handleNullResult() 
    {
    	LogMessage msg = new LogMessage();
        msg.print("No data returned.");
        registry.getLogger().error(this, msg);
    }
    
    /** Handles the cancellation */
    public void handleCancellation() 
    {
        String info = "The data retrieval has been cancelled.";
        registry.getLogger().info(this, info);
    }
    
    /**
     * Handles exceptions
     * @see DSCallAdapter#handleException(Throwable)
     */
    public void handleException(Throwable exc) 
    {
    	String s = "Data Retrieval Failure: ";
        LogMessage msg = new LogMessage();
        msg.print(s);
        msg.print(exc);
        registry.getLogger().error(this, msg);
        registry.getUserNotifier().notifyError("Data Retrieval Failure", 
                                               s, exc);
    }
    
    /** Fires an asynchronous data loading. */
    public  void load() {
        handle = imView.loadROIFromServer(ctx, imageID, userID, this);
    }
    
    /** Cancels any ongoing data loading. */
    public void cancel() {
        handle.cancel();
    }

    @Override
    /**
     * Updates the toolbar
     */
    public void handleResult(Object result) {
        int n = 0;
        
        Collection c = (Collection)result;
        for(Object obj : c) {
            if(obj instanceof ROIResult) {
                n += ((ROIResult)obj).getROIs().size();
            }
        }
        
        ROICountLoaded evt = new ROICountLoaded(imageID, n);
        registry.getEventBus().post(evt);
    }
    
    
    
}
