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
package org.openmicroscopy.shoola.agents.measurement;

import java.util.Collection;

import omero.gateway.SecurityContext;
import omero.gateway.model.FolderData;
import omero.gateway.model.ROIData;
import omero.log.LogMessage;

import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.calls.ROIFolderSaver.ROIFolderAction;

/**
 * Saves modifications to ROI Folders
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ROIFolderSaver extends MeasurementViewerLoader {
    
    /** The id of the image the ROIs are related to. */
    private long imageID;

    /** The id of the user. */
    private long userID;

    /** The ROI data to save. */
    private Collection<ROIData> roiList;
    
    /** All ROIs */
    private Collection<ROIData> allROIs;

    /** The ROI folders */
    private Collection<FolderData> folders;

    /** The action to perform */
    private ROIFolderAction action;

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
     * @param imageID
     *            The id of the image the ROIs are related to.
     * @param userID
     *            The id of the user.
     * @param roiList
     *            The list of the roi id's to load.
     *            @param folders The collection of folders
     *            @param action The action to perform
     */
    public ROIFolderSaver(MeasurementViewer viewer, SecurityContext ctx,
            long imageID, long userID, Collection<ROIData> roiList,
            Collection<FolderData> folders, ROIFolderAction action) {
        this(viewer, ctx, imageID, userID, null, roiList, folders, action);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer
     *            The viewer this data loader is for. Mustn't be
     *            <code>null</code>.
     * @param ctx
     *            The security context.
     * @param imageID
     *            The id of the image the ROIs are related to.
     * @param userID
     *            The id of the user.
     * @param allROIs
     *            All ROIs
     * @param roiList
     *            The list of the roi id's to load.
     * @param folders
     *            The collection of folders
     * @param action
     *            The action to perform
     */
    public ROIFolderSaver(MeasurementViewer viewer, SecurityContext ctx,
            long imageID, long userID, Collection<ROIData> allROIs,
            Collection<ROIData> roiList, Collection<FolderData> folders,
            ROIFolderAction action) {
        super(viewer, ctx);
        if (imageID < 0)
            throw new IllegalArgumentException("No image specified.");
        this.imageID = imageID;
        this.userID = userID;
        this.allROIs = allROIs;
        this.roiList = roiList;
        this.folders = folders;
        this.action = action;
    }

    /**
     * Performs the ROI Folder action
     * 
     * @see MeasurementViewerLoader#load()
     */
    public void load() {
        handle = idView.saveROIFolders(ctx, imageID, userID, allROIs, roiList,
                folders, action, this);
    }

    /**
     * Cancels the data loading.
     * 
     * @see MeasurementViewerLoader#cancel()
     */
    public void cancel() {
        handle.cancel();
    }

    /**
     * Notifies the user that an error has occurred and discards the
     * {@link #viewer}.
     * 
     * @see DSCallAdapter#handleException(Throwable)
     */
    public void handleException(Throwable exc) {
        int state = viewer.getState();
        exc.printStackTrace();
        String s = "An error occurred while saving the ROI ";
        LogMessage msg = new LogMessage();
        msg.print("State: " + state);
        msg.print(s);
        msg.print(exc);
        registry.getLogger().error(this, msg);
        registry.getUserNotifier().notifyInfo("Saving ROI", s);
        viewer.discard();
    }

    /**
     * Feeds the result back to the viewer.
     * 
     * @see MeasurementViewerLoader#handleResult(Object)
     */
    public void handleResult(Object result) {
        if (viewer.getState() == MeasurementViewer.DISCARDED)
            return; 
        viewer.setUpdateROIComponent((Collection) result, action);
    }

}