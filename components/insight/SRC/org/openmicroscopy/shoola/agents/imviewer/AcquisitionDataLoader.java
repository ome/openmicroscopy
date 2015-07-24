/*
 * org.openmicroscopy.shoola.agents.metadata.AcquisitionDataLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer;

//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.gateway.SecurityContext;

import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.agents.metadata.EditorLoader;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import pojos.ImageAcquisitionData;

/**
 * Loads the image acquisition data
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */
public class AcquisitionDataLoader extends DataLoader {

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /** The image to load the acquisition data for */
    private Object refObject;

    /**
     * Creates a new instance.
     * 
     * @param viewer
     *            Reference to the viewer. Mustn't be <code>null</code>.
     * @param ctx
     *            The security context.
     * @param refObject
     *            Either an image or a channel.
     */
    public AcquisitionDataLoader(ImViewer viewer, SecurityContext ctx,
            Object refObject) {
        super(viewer, ctx);
        if (refObject == null)
            throw new IllegalArgumentException("Ref Object cannot be null.");
        this.refObject = refObject;
    }

    /**
     * Loads the acquisition metadata for an image or a given channel.
     * 
     * @see EditorLoader#load()
     */
    public void load() {
        handle = ivView.loadAcquisitionData(ctx, refObject, this);
    }

    /**
     * Cancels the data loading.
     * 
     * @see EditorLoader#cancel()
     */
    public void cancel() {
        handle.cancel();
    }

    /**
     * Feeds the result back to the viewer.
     * 
     * @see EditorLoader#handleResult(Object)
     */
    public void handleResult(Object result) {
        viewer.setImageAcquisitionData((ImageAcquisitionData) result);
    }

}
