/*
 * org.openmicroscopy.shoola.agents.treeviewer.ParentLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola.agents.treeviewer;

//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.FileAnnotationData;

/** 
 * Loads the parents of the specified annotation.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class ParentLoader 
	extends DataTreeViewerLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle     	 handle;

    /** The annotation to handle.*/
    private FileAnnotationData data;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer	The Editor this data loader is for.
     * 					Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param values The groups or experimenters to delete.
     */
    public ParentLoader(TreeViewer viewer, SecurityContext ctx,
    		FileAnnotationData data)
    {
        super(viewer, ctx);
        if (data == null)
            throw new IllegalArgumentException("No object to handle");
        this.data = data;
    }
    
    /** 
     * Loads the parent associated to the annotation.
     * @see DataTreeViewerLoader#load()
     */
    public void load()
    {
    	 handle = dmView.loadParentsOfAnnotation(ctx, data.getId(), this);
    }

    /**
     * Cancels the data loading.
     * @see DataTreeViewerLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /** 
     * Feeds the result back to the viewer.
     * @see DataTreeViewerLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == TreeViewer.DISCARDED) return;  //Async cancel.
        List<Object> nodes = (List<Object>) result;
        if (nodes.size() == 0) return;
        viewer.browseContainer(nodes.get(0), null);
    }

}
