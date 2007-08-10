/*
 * org.openmicroscopy.shoola.agents.treeviewer.DiskSpaceLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer;


//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.profile.ProfileEditor;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Retrieves the available and used disk space.
 * This class calls the <code>getDiskSpace</code> in the
 * <code>DataManagerView</code>.
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
	extends ProfileEditorLoader
{

	/** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer	The viewer this data loader is for.
     *                  Mustn't be <code>null</code>.
     */
	public DiskSpaceLoader(ProfileEditor viewer)
	{
		super(viewer);
	}
	
	/**
     * Loads the used and free space.
     * @see ProfileEditorLoader#load()
     */
	public void load()
	{
		handle = dmView.getDiskSpace(this);
	}
	
    /**
     * Cancels the ongoing data retrieval.
     * @see ProfileEditorLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see ProfileEditorLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == ProfileEditor.DISCARDED) return;  //Async cancel.
        List l = (List) result;
        if (l.size() != 2) return;
        viewer.setDiskSpace(((Long) l.get(0)).longValue(), 
        					((Long) l.get(1)).longValue());
    }
    
}
