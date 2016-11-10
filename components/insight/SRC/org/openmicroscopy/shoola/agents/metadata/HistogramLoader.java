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

import omero.gateway.SecurityContext;
import omero.gateway.model.ImageData;

import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.MetadataHandlerView;

/**
 * Loads the histogram data for a certain {@link ImageData}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class HistogramLoader extends DSCallAdapter {

    /** Reference to the {@link MetadataHandlerView} */
    private MetadataHandlerView view;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /** Reference to the {@link SecurityContext} */
    private SecurityContext ctx;

    /** The {@link ImageData} */
    private ImageData img;

    /** The channel indices */
    private int[] channels;

    /** The Z plane */
    private int z;

    /** The T plane */
    private int t;

    /**
     * Creates a new instance
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param img
     *            The {@link ImageData}
     * @param channels
     *            The channel indices
     * @param z
     *            The Z plane
     * @param t
     *            The T plane
     */
    public HistogramLoader(SecurityContext ctx, ImageData img, int[] channels,
            int z, int t) {
        this.ctx = ctx;
        this.img = img;
        this.channels = channels;
        this.z = z;
        this.t = t;

        Registry registry = MetadataViewerAgent.getRegistry();
        view = (MetadataHandlerView) registry
                .getDataServicesView(MetadataHandlerView.class);
    }

    /** Fires an asynchronous data loading. */
    public void load() {
        handle = view.loadHistogram(ctx, img, channels, z, t, this);
    }

    /** Cancels any ongoing data loading. */
    public void cancel() {
        handle.cancel();
    }

    @Override
    public void handleResult(Object result) {
        // TODO: Update UI
    }

}
