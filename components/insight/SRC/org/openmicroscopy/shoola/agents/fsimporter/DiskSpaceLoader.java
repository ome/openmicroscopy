/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter;


import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.agents.metadata.EditorLoader;
import org.openmicroscopy.shoola.env.data.model.DiskQuota;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import omero.gateway.model.ExperimenterData;

/** 
 * Loads the available and used disk space.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class DiskSpaceLoader 
	extends DataImporterLoader
{

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;

    /**
     * Creates a new instance.
     * 
     * @param viewer Reference to the viewer. Mustn't be <code>null</code>.
     * @param ctx The security context.
     */
	public DiskSpaceLoader(Importer viewer, SecurityContext ctx)
	{
		super(viewer, ctx);
	}

    /** 
     * Loads the used and free space.
     * @see EditorLoader#load()
     */
    public void load()
    { 
    	handle = adminView.getDiskSpace(ctx, ExperimenterData.class,
    			getCurrentUserID(), this); 
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
