/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openmicroscopy.shoola.agents.metadata;

import java.util.List;

import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import pojos.DataObject;
import pojos.FileAnnotationData;

/** 
 * Loads the {@link DataObject}s linked to the given {@link FileAnnotationData} objects
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.0
 */
public class FileAnnotationChecker extends EditorLoader {

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /** List of {@link FileAnnotationData} which should be checked */
    private List<FileAnnotationData> annotations;

    /** The DataObjects from which the FileAnnotations should be removed */
    private List<DataObject> toBeDeletedFrom;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer Reference to the viewer. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param annotations List of {@link FileAnnotationData} which should be checked
     * @param toBeDeletedFrom The DataObjects from which the FileAnnotations should be removed
     */
    public FileAnnotationChecker(Editor viewer, SecurityContext ctx,
            List<FileAnnotationData> annotations, List<DataObject> toBeDeletedFrom) {
        super(viewer, ctx);
        this.annotations = annotations;
        this.toBeDeletedFrom = toBeDeletedFrom;
    }

    /**
     * Feeds the result back to the viewer.
     * @see EditorLoader#handleResult(Object)
     */
    public void handleResult(Object result) {
       viewer.handleFileAnnotationRemoveCheck((FileAnnotationCheckResult) result);
    }

    /**
     * Loads the {@link DataObject}s linked to the {@link FileAnnotationData} objects
     */
    public void load() {
        handle = mhView.checkFileAnnotationDeletion(ctx, annotations, toBeDeletedFrom, this);
    }

    /** 
     * Cancels the data loading. 
     * @see EditorLoader#cancel()
     */
    public void cancel() {
        handle.cancel();
    }

}
