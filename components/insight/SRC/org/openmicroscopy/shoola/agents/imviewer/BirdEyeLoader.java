/*
 * org.openmicroscopy.shoola.agents.imviewer.BirdEyeLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.imviewer;

//Java imports
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.romio.PlaneDef;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import pojos.DataObject;
import pojos.ImageData;

/** 
 * Loads the image for the bird eye view.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class BirdEyeLoader 
	extends DataLoader
{

	/** The minimum ration value.*/
	public static final double MIN_RATIO = 0.1;
	
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
    
    /** The object the image is for. */
    private ImageData image;
    
    /** Reference to the plane.*/
    private PlaneDef plane;
    
    /** The ratio by which to scale the image down.*/
    private double ratio;
	
    /** Flag indicating that this loader has been cancelled.*/
    private boolean cancelled;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The view this loader is for. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param image The image to handle.
     * @param ratio The ratio by with to scale the image.
     */
	public BirdEyeLoader(ImViewer viewer, SecurityContext ctx, ImageData image,
			PlaneDef plane, double ratio)
	{
		super(viewer, ctx);
		if (image == null)
			throw new IllegalArgumentException("No image to load.");
		this.image = image;
		if (plane == null)
			throw new IllegalArgumentException("No plane to load.");
		this.plane = plane;
		this.ratio = ratio;
	}
	
	/**
     * Loads the image.
     * @see DataLoader#load()
     */
    public void load()
    {
    	//Load the thumbnail
    	List<DataObject> objects = new ArrayList<DataObject>();
    	objects.add(image);
    	handle = hiBrwView.loadThumbnails(ctx, objects,
    			Factory.THUMB_DEFAULT_WIDTH, Factory.THUMB_DEFAULT_HEIGHT,
                getCurrentUserID(), HierarchyBrowsingView.IMAGE, this);
    }
    
    /**
     * Cancels the ongoing data retrieval.
     * @see DataLoader#cancel()
     */
    public void cancel()
    {
    	cancelled = true;
    	handle.cancel();
    }
    
    /**
     * Notifies the user that the data retrieval has been cancelled.
     */
    public void handleCancellation() {}
    
    /**
     * Notifies the user that an error has occurred.
     * @see DataBrowserLoader#handleException(Throwable)
     */
    public void handleException(Throwable exc)
    {
        String s = "Bird Eye Retrieval Failure: ";
        if (viewer.getState() == ImViewer.DISCARDED) return;
        registry.getLogger().error(this, s+exc);
        if (viewer.getState() == ImViewer.CANCELLED)
        	if (cancelled) viewer.discard();
        else registry.getUserNotifier().notifyError(s, s, exc);
    }
    
    /** 
     * Feeds the image back to the bird eye viewer, as they arrive.
     * @see DataLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe)
    {
        if (viewer.getState() == ImViewer.DISCARDED) return;  //Async cancel.
        List l = (List) fe.getPartialResult();
        if (l != null && l.size()  > 0) {
        	ThumbnailData data = (ThumbnailData) l.get(0);
        	BufferedImage image = (BufferedImage) data.getThumbnail();
        	if (image != null && ratio != 1)
        		image = Factory.magnifyImage(ratio, image);
        	viewer.setBirdEyeView(image);
        }
    }

}
