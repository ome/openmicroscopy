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

import omero.gateway.SecurityContext;
import omero.gateway.model.DataObject;

import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/**
 * Retrieves the ROI measurements
 */
public class MeasurementLoader extends BatchCallTree {
    /** The result of the call. */
    private Object result;

    /** Loads the specified experimenter groups. */
    private BatchCall loadCall;

    /** The security context. */
    private SecurityContext ctx;

    /**
     * Creates a {@link BatchCall} to load the measurement related to the object
     * identified by the class and the id.
     * 
     * @param type
     *            The type of the object.
     * @param id
     *            The id of the object.
     * @param userID
     *            The id of the user who tagged the object or <code>-1</code> if
     *            the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadROIMeasurements(final Class type, final long id,
            final long userID) {
        return new BatchCall("Loading Measurements") {
            public void doCall() throws Exception {
                OmeroImageService os = context.getImageService();
                result = os.loadROIMeasurements(ctx, type, id, userID);
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
     * Returns, in a <code>Set</code>, the root nodes of the found trees.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() {
        return result;
    }

    /**
     * Creates a new instance. Builds the call corresponding to the passed
     * index, throws an {@link IllegalArgumentException} if the index is not
     * supported.
     * 
     * @param ctx
     *            The security context.
     * @param object
     *            The object to handle.
     * @param userID
     *            The id of the user or <code>-1</code> if the id is not
     *            specified.
     */
    public MeasurementLoader(SecurityContext ctx, Object object, long userID) {
        if (object == null)
            throw new IllegalArgumentException("Object not defined.");

        DataObject ho = (DataObject) object;
        loadCall = loadROIMeasurements(object.getClass(), ho.getId(), userID);
    }

}
