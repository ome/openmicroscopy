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
package org.openmicroscopy.shoola.agents.fsimporter;

import java.util.ArrayList;
import java.util.List;

import omero.gateway.SecurityContext;

import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import pojos.AnnotationData;
import pojos.DataObject;
import pojos.FileAnnotationData;
import pojos.ImageData;


/**
 * Save the measurements.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class MeasurementsSaver
    extends DataImporterLoader
{

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /** The image the ROIs are related to. */
    private ImageData image;

    /** The id of the user. */
    private long userID;

    /** The data to save. */
    private FileAnnotationData data;

    /**
     * Creates a new instance.
     * 
     * @param viewer The Importer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param data The data to save.
     * @param image The image to link the roi to.
     * @param userID The owner of the rois.
     */
    public MeasurementsSaver(Importer viewer, SecurityContext ctx,
            FileAnnotationData data, ImageData image, long userID)
    {
        super(viewer, ctx);
        if (image == null) 
            throw new IllegalArgumentException("No image specified.");
        this.image = image;
        this.userID = userID;
        this.data = data;
    }

    /** 
     * Starts the import.
     * @see DataImporterLoader#load()
     */
    public void load()
    {
        List<AnnotationData> toAdd = new ArrayList<AnnotationData>();
        toAdd.add(data);
        List<DataObject> nodes = new ArrayList<DataObject>();
        nodes.add(image);
        handle = mhView.saveData(ctx, nodes, toAdd, null, null, userID, this);
    }
 
    /** 
     * Cancels the data loading.
     * @see DataImporterLoader#load()
     */
    public void cancel() { handle.cancel(); }

    /**
     * Feeds the result back to the viewer.
     * @see DataImporterLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    }

}
