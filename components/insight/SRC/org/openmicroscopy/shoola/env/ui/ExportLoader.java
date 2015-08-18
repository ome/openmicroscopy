/*
 * org.openmicroscopy.shoola.env.ui.ExportLoader
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.ui;

//Java imports
import java.io.File;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.util.Target;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.ImageData;

/**
 * Exports the passed image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ExportLoader 
	extends UserNotifierLoader
{

	/** Indicates to export the image as OMETiff. */
	public static final int	EXPORT_AS_OME_TIFF = 0;
	
	/** Indicates to export the image as OME-XML. */
	public static final int	EXPORT_AS_OME_XML = 1;
	
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  			handle;
    
    /** The image to create a movie from. */
    private ImageData 				image;

    /** The file where to export the image. */
    private File					file;

    /** One of the constants defined by this class. */
    private int						index;
    
    /** The selected schema.*/
    private Target target;
    
    /** Flag indicating that the export has been marked to be cancel.*/
    private boolean cancelled;
    
    /**
     * Notifies that an error occurred.
     * @see UserNotifierLoader#onException(String, Throwable)
     */
    protected void onException(String message, Throwable ex)
    { 
    	switch (index) {
			case EXPORT_AS_OME_TIFF:
				activity.notifyError("Unable to export as OME-TIFF", 
						message, ex);
				break;
			case EXPORT_AS_OME_XML:
				activity.notifyError("Unable to export as OME-XML", message, 
						ex);
    	}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param registry	Convenience reference for subclasses.
     * @param ctx The security context.
     * @param image		The image to export.
     * @param file		The file where to export the image.
     * @param index	 	One of the constants defined by this class.
     * @param target The selected schema.
     * @param activity 	The activity associated to this loader.
     */
	public ExportLoader(UserNotifier viewer,  Registry registry,
			SecurityContext ctx, ImageData image, File file, int index,
			Target target, ActivityComponent activity)
	{
		super(viewer, registry, ctx, activity);
		if (image == null)
			throw new IllegalArgumentException("Image not valid.");
		this.target = target;
		this.image = image;
		this.file = file;
		this.index = index;
	}
	
	/**
     * Exports the image.
     * @see UserNotifierLoader#load()
     */
    public void load()
    {
    	handle = ivView.exportImageAsOMETiff(ctx, image.getId(), file, target,
    			this);
    }
    
    /**
     * Cancels the ongoing data retrieval.
     * @see UserNotifierLoader#cancel()
     */
    public void cancel()
    {
    	cancelled = true;
    	if (handle != null) handle.cancel();
    }
 
    /** 
     * Feeds the result back to the viewer. 
     * @see UserNotifierLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
    	if (cancelled) {
    		file.delete();
    	} else {
        	if (result == null) onException(MESSAGE_RESULT, null);
        	else activity.endActivity(result);
    	}
    }
    
}
