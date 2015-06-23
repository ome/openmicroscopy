/*
 * org.openmicroscopy.shoola.agents.util.finder.TagsLoader 
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util.finder;


//Java imports
import java.util.Collection;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.TagAnnotationData;

/** 
 * Loads the existing tags.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class TagsLoader
	extends FinderLoader
{

	 /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     *  @param ctx The security context.
     */
    public TagsLoader(Finder viewer, List<SecurityContext> ctx)
    {
    	super(viewer, ctx);
    }
    
    /**
     * Searches for values.
     * @see FinderLoader#load()
     */
    public void load()
    {
		handle = mhView.loadExistingAnnotations(ctx, TagAnnotationData.class,
				-1, this);
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see FinderLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see FinderLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
    	if (viewer.getState() == Finder.DISCARDED) return;  //Async cancel.
        viewer.setExistingTags((Collection) result);
    }
    
}
