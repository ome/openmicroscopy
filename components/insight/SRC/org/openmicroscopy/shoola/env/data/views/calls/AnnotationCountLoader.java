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

import omero.gateway.SecurityContext;
import omero.gateway.model.DataObject;

import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/**
 * Loads the number of annotations related to the given objects.
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class AnnotationCountLoader extends BatchCallTree {

    /** The result of the call. */
    private Object result;

    /** Loads the specified experimenter groups. */
    private BatchCall loadCall;

    /**
     * Adds the {@link #loadCall} to the computation tree.
     * 
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() {
        add(loadCall);
    }

    /**
     * Returns, in a <code>Set</code>, the root nodes of the found trees.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() {
        return result;
    }

    /**
     * Creates a new instance
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param data
     *            The object to load the annotations for
     * @param userID
     *            The user id
     */
    public AnnotationCountLoader(final SecurityContext ctx,
            final Collection<DataObject> data, final long userID) {
        loadCall = new BatchCall("Loading Annotation Count") {
            public void doCall() throws Exception {
                OmeroMetadataService os = context.getMetadataService();
                result = os.loadAnnotationCount(ctx, data, userID);
            }
        };
    }

}
