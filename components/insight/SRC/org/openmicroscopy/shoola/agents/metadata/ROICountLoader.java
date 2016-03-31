/*
 * org.openmicroscopy.shoola.agents.metadata.EditorLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014-2016 University of Dundee. All rights reserved.
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

import omero.gateway.SecurityContext;
import omero.log.LogMessage;

import org.openmicroscopy.shoola.agents.metadata.editor.PropertiesUI;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.ImageDataView;

/**
 * An async. loader which updates the UI components with the number of ROIs the
 * image associated to the PreviewToolBar has.
 *
 * @author Domink Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ROICountLoader extends DSCallAdapter {
    /** Reference to the registry */
    private final Registry registry;

    /** Reference to the ImageDataView */
    private final ImageDataView imView;

    /** The security context. */
    private final SecurityContext ctx;

    /** Reference to the {@link PropertiesUI} showing the number of ROIs */
    private PropertiesUI propUI;

    /** The id of the image the ROIs are related to. */
    private long imageID;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /**
     * Creates a new instance
     * 
     * @param ctx
     *            The SecurityContext
     * @param propUI
     *            Reference to the UI.
     * @param imageID
     *            The image id to load the ROIs for
     */
    public ROICountLoader(SecurityContext ctx, PropertiesUI propUI, long imageID) {
        if (ctx == null)
            throw new NullPointerException("No security context.");
        this.ctx = ctx;
        this.imageID = imageID;
        this.propUI = propUI;
        registry = MetadataViewerAgent.getRegistry();
        imView = (ImageDataView) registry
                .getDataServicesView(ImageDataView.class);
    }

    /**
     * Handles a null result
     */
    public void handleNullResult() {
        LogMessage msg = new LogMessage();
        msg.print("No data returned.");
        registry.getLogger().error(this, msg);
    }

    /** Handles the cancellation */
    public void handleCancellation() {
        String info = "The data retrieval has been cancelled.";
        registry.getLogger().info(this, info);
    }

    /**
     * Handles exceptions
     * 
     * @see DSCallAdapter#handleException(Throwable)
     */
    public void handleException(Throwable exc) {
        String s = "Data Retrieval Failure: ";
        LogMessage msg = new LogMessage();
        msg.print(s);
        msg.print(exc);
        registry.getLogger().error(this, msg);
        registry.getUserNotifier()
                .notifyError("Data Retrieval Failure", s, exc);
    }

    /** Fires an asynchronous data loading. */
    public void load() {
        handle = imView.getROICount(ctx, imageID, this);
    }

    /** Cancels any ongoing data loading. */
    public void cancel() {
        handle.cancel();
    }

    @Override
    /**
     * Updates the toolbar
     */
    public void handleResult(Object result) {
        if (result instanceof Integer)
            propUI.updateROICount((Integer) result);
    }

}