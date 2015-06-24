/*
 * org.openmicroscopy.shoola.agents.imviewer.ProjectionSaver 
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
package org.openmicroscopy.shoola.agents.imviewer;


//Java imports
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.log.LogMessage;
import pojos.DataObject;
import pojos.ImageData;

/** 
 * Creates a projected image for preview or a projection of the whole image.
 * For a preview projected image, the currently selected timepoint and 
 * the active channels are taken into account.
 * For an image's creation, the user selects the active channels or the
 * whole image, the name of the projected image and where to save the 
 * the image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class ProjectionSaver 
	extends DataLoader
{

	/** Indicates to create a preview image. */
	public static final int PREVIEW = 0;
	
	/** Indicates to create a preview image. */
	public static final int PROJECTION = 1;
	
	/** 
	 * Flag indicating to apply the rendering settings of the original image 
	 * to the projected one. 
	 */
	private boolean			applySettings;
	
	/** One of the constants defined by this class. */
	private int 			index;
	
	/** The object hosting the projection's parameters. */
	private ProjectionParam	ref;

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  	handle;
    
    /**
     * Controls if the passed index is supported.
     * 
     * @param value The value to set.
     */
    private void checkIndex(int value)
    {
    	switch (value) {
			case PREVIEW:
			case PROJECTION:
				break;
			default:
				throw new IllegalArgumentException("Projection index " +
						"not supported.");
		}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param ref The object hosting the projection's parameters.
     * @param index One of the constants defined by this class.
     */
	public ProjectionSaver(ImViewer model, SecurityContext ctx,
			ProjectionParam ref, int index)
	{
		this(model, ctx, ref, index, false);
	}
	
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param ref The object hosting the projection's parameters.
     * @param index One of the constants defined by this class.
     * @param applySettings Pass <code>true</code> to set the rendering settings
	 * 						of the original image to the new pixels set,
	 * 						<code>false</code> otherwise.
     */
	public ProjectionSaver(ImViewer model, SecurityContext ctx,
		ProjectionParam ref, int index, boolean applySettings)
	{
		super(model, ctx);
		if (ref == null)
			throw new IllegalArgumentException("Parameters not specified.");
		checkIndex(index);
		this.index = index;
		this.ref = ref;
		this.applySettings = applySettings;
	}
	
	/**
     * Creates a preview image or creates a projected image.
     * @see DataLoader#load()
     */
    public void load()
    {
        switch (index) {
			case PREVIEW:
				handle = ivView.renderProjected(ctx, ref.getPixelsID(),
						ref.getStartZ(), ref.getEndZ(), ref.getStepping(),
						ref.getAlgorithm(), ref.getChannels(), this);
				break;
			case PROJECTION:
				handle = ivView.projectImage(ctx, ref, this);
		}
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Notifies the user that an error has occurred and discards the 
     * {@link #viewer}.
     * @see DSCallAdapter#handleException(Throwable)
     */
    public void handleException(Throwable exc) 
    {
        String s = "Data Retrieval Failure: ";
        LogMessage msg = new LogMessage();
        msg.print(s);
        msg.print(exc);
        registry.getLogger().error(this, msg);
        switch (index) {
			case PREVIEW:
				viewer.setProjectionPreview(null);
				break;
			case PROJECTION:
				viewer.setProjectedImage(null, null, null, false);
		}
    }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see DataLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == ImViewer.DISCARDED) return;  //Async cancel.
        switch (index) {
	        case PREVIEW:
	        	viewer.setProjectionPreview(result);
	        	break;
	        case PROJECTION:
	        	List<DataObject> l = new ArrayList<DataObject>();
	        	if (ref.getDatasets() != null) {
	        		l.addAll(ref.getDatasets());
	        	}
	        	viewer.setProjectedImage((ImageData) result, ref.getChannels(), 
	        			l, applySettings);
        }
    }
    
}
