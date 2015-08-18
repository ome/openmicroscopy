/*
 * org.openmicroscopy.shoola.agents.metadata.AcquisitionDataLoader 
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import pojos.ChannelAcquisitionData;
import pojos.ChannelData;
import pojos.ImageAcquisitionData;
import pojos.ImageData;

/** 
 * Loads the acquisition metadata for an image or a given channel.
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
public class AcquisitionDataLoader 
	extends EditorLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
    
    /** Either an image or a channel. */
    private Object refObject;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer Reference to the viewer. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param refObject Either an image or a channel.
     */
	public AcquisitionDataLoader(Editor viewer, SecurityContext ctx,
			Object refObject)
	{
		super(viewer, ctx);
		if (refObject == null)
			throw new IllegalArgumentException("Ref Object cannot be null.");
		this.refObject = refObject;
	}
	
    /** 
     * Loads the acquisition metadata for an image or a given channel.
     * @see EditorLoader#load()
     */
    public void load()
    {
    	handle = imView.loadAcquisitionData(ctx, refObject, this);
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
  		if (refObject instanceof ImageData) {
  			viewer.setImageAcquisitionData((ImageAcquisitionData) result);
  		} else if (refObject instanceof ChannelData) {
  			ChannelData data = (ChannelData) refObject;
  			viewer.setChannelAcquisitionData(data.getIndex(),
  					(ChannelAcquisitionData) result);
  		}
    }
    
}
