/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
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

import java.util.Collection;
import java.util.Map;

import omero.gateway.SecurityContext;
import omero.gateway.model.DataObject;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.model.AnnotationType;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/**
 * Loads the number of annotations related to the given objects. This class
 * calls the <code>loadAnnotationCount</code> method in the
 * <code>MetadataHandlerView</code>.
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class AnnotationCountLoader extends MetadataLoader {

    /** The objects the data are related to. */
    private Collection<DataObject> dataObjects;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /**
     * Creates a new instance.
     * 
     * @param viewer
     *            The viewer this data loader is for. Mustn't be
     *            <code>null</code>.
     * @param ctx
     *            The security context.
     * @param dataObjects
     *            The objects the data are related to. Mustn't be
     *            <code>null</code>.
     * @param loaderID
     *            The identifier of the loader.
     */
    public AnnotationCountLoader(MetadataViewer viewer, SecurityContext ctx,
            Collection<DataObject> dataObjects, int loaderID) {
        super(viewer, ctx, null, loaderID);
        if (CollectionUtils.isEmpty(dataObjects))
            throw new IllegalArgumentException("No object specified.");
        this.dataObjects = dataObjects;
    }

    /**
     * Loads the data.
     * 
     * @see MetadataLoader#cancel()
     */
    public void load() {
        handle = mhView.loadAnnotationCount(ctx, dataObjects, -1, this);
    }

    /**
     * Cancels the data loading.
     * 
     * @see MetadataLoader#cancel()
     */
    public void cancel() {
        handle.cancel();
    }

    /**
     * Feeds the result back to the viewer.
     * 
     * @see MetadataLoader#handleResult(Object)
     */
    public void handleResult(Object result) {
        if (viewer.getState() == MetadataViewer.DISCARDED)
            return; // Async cancel.
        viewer.setAnnotationCount((Map<AnnotationType, Long>) result, loaderID);
    }

    /**
     * Notifies the user that an error has occurred and discards the
     * {@link #viewer}.
     * 
     * @see DSCallAdapter#handleException(Throwable)
     */
    public void handleException(Throwable exc) {
        handleException(exc, false);
    }
}
