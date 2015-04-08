/*
 * org.openmicroscopy.shoola.agents.metadata.DiskSpaceLoader 
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
import org.openmicroscopy.shoola.env.data.model.DiskQuota;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * Loads the space used and the space available on the server for the
 * specified user.
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
public class DiskSpaceLoader
	extends EditorLoader
{

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;
    
    /** The id of the user or group. */
    private long		id;

    /** Either <code>ExperimenterData</code> or <code>GroupData</code>. */
    private Class		type;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer Reference to the viewer. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param type	Either <code>ExperimenterData</code> or
     * 				<code>GroupData</code>.
     * @param id The identifier of the user or the group.
     */
	public DiskSpaceLoader(Editor viewer, SecurityContext ctx, Class type,
			long id)
	{
		super(viewer, ctx);
		if (!(ExperimenterData.class.equals(type) ||
			GroupData.class.equals(type)))
			throw new IllegalArgumentException("Type can only by " +
					"ExperimenterData or GroupData.");
		this.id = id;
		this.type = type;
	}

    /** 
     * Loads the used and free space.
     * @see EditorLoader#load()
     */
    public void load()
    { 
    	handle = adminView.getDiskSpace(ctx, type, id, this); 
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
        viewer.setDiskSpace((DiskQuota) result);
    }

}
