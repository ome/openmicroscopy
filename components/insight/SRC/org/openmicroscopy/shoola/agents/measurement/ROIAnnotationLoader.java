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

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
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
public class ROIAnnotationLoader
    extends MeasurementViewerLoader
{

    /** The shape to handle */
    private List<DataObject> shapes;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;

    /**
     * Creates a new instance. 
     * 
     * @param viewer The viewer this data loader is for.
     *                Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param shapes The shapes.
     */
    public ROIAnnotationLoader(MeasurementViewer viewer, SecurityContext ctx,
            List<DataObject> shapes)
    {
        super(viewer, ctx);
        if (CollectionUtils.isEmpty(shapes)) 
            throw new IllegalArgumentException("No shapes specified.");
        this.shapes = shapes;
    }


    /**
     * Loads the ROI's annotations.
     * @see MeasurementViewerLoader#load()
     */
    public void load()
    {
        handle = mhView.loadStructuredData(ctx, shapes, -1, false, this);
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
        viewer.setROIAnnotations((Map<DataObject, StructuredDataResults>) result);
    }
}
