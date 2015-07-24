/*
 *  Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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
package org.openmicroscopy.shoola.env.data.views.calls;

import java.util.List;

import org.openmicroscopy.shoola.agents.metadata.FileAnnotationCheckResult;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

import pojos.DataObject;
import pojos.FileAnnotationData;

/**
 * Loads the parents ({@link DataObject}) of the specified {@link FileAnnotationData} objects,
 * wrapped in a {@link FileAnnotationCheckResult}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.0
 */
public class FileAnnotationCheckLoader extends BatchCallTree {

    /** The result of the call. */
    private FileAnnotationCheckResult result;

    /** The call */
    private BatchCall loadCall;

    /**
     * Creates a {@link BatchCall} to load the parents of the annotations.
     * 
     * @param ctx The security context.
     * @param annotations The @link FileAnnotationData} objects
     * @param referenceObjects The DataObjects from which the FileAnnotation should be removed
     * @return The {@link BatchCall}.
     */
    private BatchCall makeCall(final SecurityContext ctx,
            final List<FileAnnotationData> annotations, final List<DataObject> referenceObjects) {
        return new BatchCall("Load Parents of annotations") {
            public void doCall() throws Exception {
                
                result = new FileAnnotationCheckResult(referenceObjects);
                
                for(FileAnnotationData fd : annotations) {
                    OmeroMetadataService svc = context.getMetadataService();
                    List<DataObject> parents = svc.loadParentsOfAnnotations(ctx, fd.getId(), -1);
                    
                    result.addLinks(fd, parents);
                }
            }
        };
    }

    /**
     * Adds the {@link #loadCall} to the computation tree.
     * 
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() {
        add(loadCall);
    }

    /**
     * Returns {@link FileAnnotationCheckResult}, which holds a <code>Map</code> 
     * of {@link FileAnnotationData} objects mapped to {@link DataObject}s
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() {
        return result;
    }

    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param annotations The {@link FileAnnotationData} objects
     */
    public FileAnnotationCheckLoader(SecurityContext ctx, List<FileAnnotationData> annotations, List<DataObject> referenceObjects) {
        loadCall = makeCall(ctx, annotations, referenceObjects);
    }

}
