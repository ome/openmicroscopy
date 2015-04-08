/*
 * org.openmicroscopy.shoola.agents.metadata.EnumerationLoader 
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
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Loads the enumerations related to images or channels.
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
public class EnumerationLoader 
	extends EditorLoader
{

	/** Indicates to load the enumeration related to the image metadata. */
	public static final int IMAGE = 0;
	
	/** Indicates to load the enumeration related to the channel metadata. */
	public static final int CHANNEL = 1;
	
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;
    
    /** One of the constants defined by this class. */
    private int index;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer Reference to the viewer. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param index  One of the constants defined by this class.
     */
    public EnumerationLoader(Editor viewer, SecurityContext ctx, int index)
    {
    	super(viewer, ctx);
    	this.index = index;
    }
    
    /** 
     * Loads the acquisition metadata for an image or a given channel.
     * @see EditorLoader#load()
     */
    public void load()
    {
    	if (index == IMAGE)
    		handle = imView.loadImageMetadataEnumerations(ctx, this);
    	else handle = imView.loadChannelMetadataEnumerations(ctx, this);
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
    	if (index == IMAGE)
  			viewer.setImageEnumerations((Map) result);
    	else viewer.setChannelEnumerations((Map) result);
    }
    
}
