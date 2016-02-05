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
package org.openmicroscopy.shoola.env.data.views.calls;

import java.util.Collection;
import java.util.Collections;

import omero.gateway.SecurityContext;
import omero.gateway.facility.ROIFacility;
import omero.gateway.model.FolderData;
import omero.gateway.model.ROIData;

import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/**
 * Saves modifications to ROI Folders
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ROIFolderSaver extends BatchCallTree {
    public enum ROIFolderAction {
        ADD_TO_FOLDER, REMOVE_FROM_FOLDER, CREATE_FOLDER, DELETE_FOLDER
    }

    /** Call to save the ROIs. */
    private BatchCall saveCall;

    /** Was the save successful. */
    private Collection<ROIData> result;

    /**
     * Creates a {@link BatchCall} to load the ROIs.
     */
    private BatchCall makeSaveCall(final SecurityContext ctx,
            final long imageID, final long userID,
            final Collection<ROIData> roiList,
            final Collection<FolderData> folders, final ROIFolderAction action) {
        return new BatchCall("save ROI") {
            public void doCall() throws Exception {
                ROIFacility svc = context.getGateway().getFacility(
                        ROIFacility.class);
                if (action == ROIFolderAction.ADD_TO_FOLDER) {
                    svc.addRoisToFolders(ctx, imageID, roiList, folders);
                }
                if (action == ROIFolderAction.REMOVE_FROM_FOLDER) {
                    svc.removeRoisFromFolders(ctx, imageID, roiList, folders);
                }
                result = Collections.EMPTY_LIST;
            }
        };
    }

    /**
     * Adds the {@link #saveCall} to the computation tree.
     * 
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() {
        add(saveCall);
    }

    /**
     * Returns the result of the save.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() {
        return result;
    }

    /**
     * Creates a new instance.
     * 
     * @param ctx
     *            The security context.
     * @param imageID
     *            The image's ID.
     * @param userID
     *            The user's ID.
     * @param roiList
     *            The list of ROIs to save.
     */
    public ROIFolderSaver(SecurityContext ctx, long imageID, long userID,
            Collection<ROIData> roiList, Collection<FolderData> folders,
            ROIFolderAction action) {
        saveCall = makeSaveCall(ctx, imageID, userID, roiList, folders, action);
    }

}
