/*
 * org.openmicroscopy.shoola.agents.metadata.UserPhotoUploader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
import java.awt.image.BufferedImage;
import java.io.File;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.ExperimenterData;

/** 
 * Uploads a photo for the specified experimenter.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class UserPhotoUploader 
	extends EditorLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
    
    /** The experimenter to handle. */
    private ExperimenterData experimenter;
    
    /** The photo to upload. */
    private File photo;
    
    /** The format of the photo to upload. */
    private String format; 
    
    /**	
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param experimenter The experimenter to handle.
     * @param photo The photo to upload.
     */
    public UserPhotoUploader(Editor viewer, SecurityContext ctx,
    		ExperimenterData experimenter, File photo, String format)
    {
    	 super(viewer, ctx);
    	 if (experimenter == null)
    		 throw new IllegalArgumentException("No experimenter specified.");
    	 if (photo == null)
    		 throw new IllegalArgumentException("No photo to upload.");
    	 this.experimenter = experimenter;
    	 this.photo = photo;
    	 this.format = format;
    }
    
    /** 
	 * Loads the latest user photo. 
	 * @see EditorLoader#cancel()
	 */
	public void load()
	{
		handle = adminView.uploadExperimenterPhoto(ctx, experimenter, photo,
				format, this);
	}
	
	/** 
	 * Cancels the data loading. 
	 * @see EditorLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }
	
	/**
     * Feeds the result back to the viewer.
     * @see EditorLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	viewer.setUserPhoto((BufferedImage) result, experimenter.getId());
    } 
    
}
