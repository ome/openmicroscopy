/*
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.measurement;

import java.util.List;
import java.util.Map;

import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import pojos.AnnotationData;
import pojos.DataObject;


/**
 * Loads the annotations linked to the specified shapes.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.1
 */
public class ROIAnnotationSaver
    extends MeasurementViewerLoader
{

    /** The shapes to annotate */
    private Map<DataObject, List<AnnotationData>> toAdd;

    /** The shapes to remove the annotation from.*/
    private Map<DataObject, List<AnnotationData>> toRemove;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;

    /**
     * Creates a new instance. 
     * 
     * @param viewer The viewer this data loader is for.
     *                Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param toAdd The shapes to annotate.
     * @param toRemove  The shapes to remove the annotation from.
     */
    public ROIAnnotationSaver(MeasurementViewer viewer, SecurityContext ctx,
            Map<DataObject, List<AnnotationData>> toAdd,
            Map<DataObject, List<AnnotationData>> toRemove)
    {
        super(viewer, ctx);
        this.toAdd = toAdd;
        this.toRemove = toRemove;
    }

    /**
     * Saves the annotations.
     * @see MeasurementViewerLoader#load()
     */
    public void load()
    {
        handle = mhView.annotateData(ctx, toAdd, toRemove, -1, this);
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
        //
    }
}
