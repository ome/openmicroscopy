/*
* org.openmicroscopy.shoola.agents.measurement.ServerSideROILoader
*
 *------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import omero.log.LogMessage;

/**
 * Loads the server side ROIs into the measurement tool. Also if there are no
 * ROI it will check to see if there is an ROI.xml file and try and load from
 * it.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ServerSideROILoader 
	extends MeasurementViewerLoader
{

	/** The id of the image the ROIs are related to. */
	private long		imageID;
	
	/** The id of the user. */
	private long		userID;
		
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;

    /**
     * This loader will check that there are ROI's on the server and if so
     * load those ROIs. If there are no ROI, the measurement tool will check
     * to see if there is an XML ROI file to load roi's from. 
     * 
     * @param viewer	The viewer this data loader is for.
     *                  Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param imageID	The id of the image the ROIs are related to.
     * @param userID	The id of the user.
     */
	public ServerSideROILoader(MeasurementViewer viewer, SecurityContext ctx,
			long imageID, long userID)
	{
		super(viewer, ctx);
		if (imageID < 0) 
			throw new IllegalArgumentException("No image specified.");
		this.imageID = imageID;
		this.userID = userID;
	}
    
	/**
     * Loads the ROI.
     * @see MeasurementViewerLoader#load()
     */
    public void load()
    {
    	handle = idView.loadROIFromServer(ctx, imageID, userID, this);
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
    	viewer.setLoadingFromServerClient((Collection) result);
    }
    
    /**
     * Notifies the user that an error has occurred and discards the 
     * {@link #viewer}.
     * @see DSCallAdapter#handleException(Throwable) 
     */
    public void handleException(Throwable exc) 
    {
    	int state = viewer.getState();
        String s = "Data Retrieval Failure: ";
        LogMessage msg = new LogMessage();
        msg.print("State: "+state);
        msg.print(s);
        msg.print(exc);
        registry.getLogger().error(this, msg);
        if (state != MeasurementViewer.DISCARDED)
        	registry.getUserNotifier().notifyError("Data Retrieval Failure", 
                                               s, exc);
        //viewer.setStatus(true);
        viewer.setLoadingFromServerClient(null);
    }

}


