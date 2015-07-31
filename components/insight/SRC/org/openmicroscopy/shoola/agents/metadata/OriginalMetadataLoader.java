/*
 * org.openmicroscopy.shoola.agents.metadata.OriginalMetadataLoader
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
package org.openmicroscopy.shoola.agents.metadata;


//Java imports
import java.io.File;

//Third-party libraries

//Application-internal dependencies
import omero.cmd.OriginalMetadataResponse;
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import org.openmicroscopy.shoola.env.data.RequestCallback;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.util.OriginalMetadataParser;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.MetadataHandlerView;
import org.openmicroscopy.shoola.util.filter.file.TEXTFilter;

/**
 * Loads the original metadata read directly from the file.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class OriginalMetadataLoader
	extends EditorLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
    
    /** The component where to feed the results back to. */
    private Object uiView;
    
    /** The id of the image to handle.*/
    private long imageID;

    /** The absolute of the new file. */
	private File file;

    /**
     * Notifies the user that it was not possible to retrieve the data.
     */
    private void onNullResult()
    {
    	super.handleNullResult();
    	file.delete();
    	viewer.setLoadedFile(null, null, uiView);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer Reference to the viewer. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param imageID The id of the image to load the data for.
     * @param uiView The object to handle.
     */
	public OriginalMetadataLoader(Editor viewer, SecurityContext ctx,
		long imageID, Object uiView)
	{
		super(viewer, ctx);
		this.uiView = uiView;
		this.imageID = imageID;
		file = new File(MetadataViewerAgent.getTmpDir(), "image_"+imageID+
				"."+TEXTFilter.TEXT);
		file.deleteOnExit();
	}
	
    /** 
     * Loads the acquisition metadata for an image or a given channel.
     * @see EditorLoader#load()
     */
    public void load()
    {
    	handle = mhView.loadFile(ctx, file, imageID,
    			MetadataHandlerView.METADATA_FROM_IMAGE, this);
    }
    
    /** 
     * Cancels the data loading. 
     * @see EditorLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Notifies the user that it was not possible to retrieve the data.
     * Does nothing.
     */
    public void handleNullResult() {}

    /** 
     * Sets the adapter.
     * @see UserNotifierLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe)
    {
    	Object o = fe.getPartialResult();
        if (o != null) {
        	if (o instanceof Boolean) {
        		Boolean b = (Boolean) o;
        		if (!b) onNullResult();
        	} else {
        		RequestCallback callBack = (RequestCallback) o;
            	callBack.setAdapter(this);
        	}
        }
    }
    
    /**
     * Feeds the result back to the viewer.
     * @see EditorLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
    	if (result instanceof Boolean) {
    		boolean b = ((Boolean) result).booleanValue();
    		if (!b) onNullResult();
    	} else if (result instanceof OriginalMetadataResponse) {
    		OriginalMetadataParser parser = new OriginalMetadataParser(file);
        	try {
        		parser.read((OriginalMetadataResponse) result, "=");
        		viewer.setLoadedFile(null, file, uiView);
    		} catch (Exception e) {
    			onNullResult();
    		}
    	}
    }
}
