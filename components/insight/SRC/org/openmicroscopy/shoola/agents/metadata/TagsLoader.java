/*
 * org.openmicroscopy.shoola.agents.metadata.TagsLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Loads the existing tags.
 * This class calls one of the <code>loadExistingAnnotations</code> methods 
 * in the <code>MetadataHandlerView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class TagsLoader
    extends EditorLoader
{

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /**
     * Flag indicating to load all annotations available or 
     * to only load the user's annotation.
     */
    private boolean loadAll;

    /**	
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param loadAll Pass <code>true</code> indicating to load all
     *                annotations available if the user can annotate,
     *                <code>false</code> to only load the user's annotation.
     */
    public TagsLoader(Editor viewer, SecurityContext ctx, boolean loadAll)
    {
        super(viewer, ctx);
        this.loadAll = loadAll;
    }

    /** 
     * Loads the tags. 
     * @see EditorLoader#cancel()
     */
    public void load()
    {
        long userID = getCurrentUser();
        if (loadAll) userID = -1;
        handle = dmView.loadTags(ctx, -1L, false, true, userID,
                ctx.getGroupID(), this);
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
        //if (viewer.getState() == MetadataViewer.DISCARDED) return;  //Async cancel.
        viewer.setExistingTags((Collection) result);
    } 

}
