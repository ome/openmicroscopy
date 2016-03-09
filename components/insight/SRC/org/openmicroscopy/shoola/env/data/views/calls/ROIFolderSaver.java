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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import omero.cmd.CmdCallbackI;
import omero.gateway.SecurityContext;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.facility.ROIFacility;
import omero.gateway.model.FolderData;
import omero.gateway.model.ROIData;
import omero.model.IObject;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/**
 * Saves modifications to ROI Folders
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ROIFolderSaver extends BatchCallTree {
    
    /**
     * The actions which this {@link ROIFolderSaver} can handle
     */
    public enum ROIFolderAction {
        /** Add ROIs to Folder */
        ADD_TO_FOLDER, 
        /** Remove ROIs from Folder */
        REMOVE_FROM_FOLDER, 
        /** Create Folder */
        CREATE_FOLDER, 
        /** Delete Folder */
        DELETE_FOLDER
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
            final Collection<ROIData> allROIs,
            final Collection<ROIData> roiList,
            final Collection<FolderData> folders, final ROIFolderAction action) {
        return new BatchCall("save ROI") {
            public void doCall() throws Exception {
                ROIFacility svc = context.getGateway().getFacility(
                        ROIFacility.class);
                DataManagerFacility dm = context.getGateway().getFacility(DataManagerFacility.class);
                
                if (action == ROIFolderAction.ADD_TO_FOLDER) {
                    Collection<ROIData> notSelected = relativeComplement(
                            roiList, allROIs);
                    if (!notSelected.isEmpty())
                        svc.saveROIs(ctx, imageID, notSelected);
                    svc.addRoisToFolders(ctx, imageID, roiList, folders);
                } else if (action == ROIFolderAction.REMOVE_FROM_FOLDER) {
                    svc.removeRoisFromFolders(ctx, imageID, roiList, folders);
                } else if (action == ROIFolderAction.CREATE_FOLDER) {
                    for (FolderData folder : folders)
                        dm.saveAndReturnObject(ctx, folder);
                } else if (action == ROIFolderAction.DELETE_FOLDER) {
                    List<IObject> ifolders = new ArrayList<IObject>(
                            folders.size());
                    for (FolderData f : folders)
                        ifolders.add((IObject) f.asFolder());

                    CmdCallbackI cb = dm.delete(ctx, ifolders);
                    // wait for the delete action to be finished
                    cb.block(10000);
                } 
                result = Collections.EMPTY_LIST;
            }
        };
    }

    /**
     * Get the relative complement of coll1 in coll2
     * 
     * @param coll1
     *            The collection
     * @param coll2
     *            The other collection
     * @return The elements of coll2 which are not part of coll1
     */
    private static Collection<ROIData> relativeComplement(
            Collection<ROIData> coll1, Collection<ROIData> coll2) {
        if (CollectionUtils.isEmpty(coll1))
            return coll2;
        if (CollectionUtils.isEmpty(coll2))
            return Collections.EMPTY_LIST;

        Collection<ROIData> result = new ArrayList<ROIData>();
        for (ROIData t : coll2) {
            boolean found = false;
            for (ROIData t2 : coll1) {
                if (t.getUuid().equals(t2.getUuid())) {
                    found = true;
                    break;
                }
            }

            if (!found)
                result.add(t);
        }
        return result;
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
     * @param allROIs
     *            All ROIs
     * @param roiList
     *            The list of ROIs to save.
     */
    public ROIFolderSaver(SecurityContext ctx, long imageID, long userID,
            Collection<ROIData> allROIs, Collection<ROIData> roiList,
            Collection<FolderData> folders, ROIFolderAction action) {
        saveCall = makeSaveCall(ctx, imageID, userID, allROIs, roiList,
                folders, action);
    }

}
