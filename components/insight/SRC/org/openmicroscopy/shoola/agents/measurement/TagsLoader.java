/*
 * org.openmicroscopy.shoola.agents.measurement.ROILoader
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement;


import java.util.Collection;

import omero.gateway.SecurityContext;

import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/**
 * Loads the tags.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.1
 */
public class TagsLoader
    extends MeasurementViewerLoader
{

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;

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
    public TagsLoader(MeasurementViewer viewer, SecurityContext ctx,
            boolean loadAll)
    {
        super(viewer, ctx);
        this.loadAll = loadAll;
    }

    /**
     * Loads the tags.
     * @see MeasurementViewerLoader#load()
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
     * @see MeasurementViewerLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /**
     * Feeds the result back to the viewer.
     * @see MeasurementViewerLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == MeasurementViewer.DISCARDED) return;  //Async cancel.
        viewer.setExistingTags((Collection) result);
    }

}
