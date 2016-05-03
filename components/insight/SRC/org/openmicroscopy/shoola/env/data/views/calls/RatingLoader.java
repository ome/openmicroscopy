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

import java.util.List;

import omero.gateway.SecurityContext;

import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/**
 * Retrieves the ratings
 */
public class RatingLoader extends BatchCallTree {
    /** The result of the call. */
    private Object result;

    /** Loads the specified experimenter groups. */
    private BatchCall loadCall;

    /** The security context. */
    private SecurityContext ctx;

    /**
     * Creates a {@link BatchCall} to load the ratings related to the object
     * identified by the class and the id.
     * 
     * @param type
     *            The type of the object.
     * @param ids
     *            The collection of id of the object.
     * @param userID
     *            The id of the user who tagged the object or <code>-1</code> if
     *            the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadRatings(final Class type, final List<Long> ids,
            final long userID) {
        return new BatchCall("Loading Ratings") {
            public void doCall() throws Exception {
                OmeroMetadataService os = context.getMetadataService();
                result = os.loadRatings(ctx, type, ids, userID);
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
     * @param type
     *            The type of node the annotations are related to.
     * @param ids
     *            Collection of the id of the object.
     * @param userID
     *            The id of the user or <code>-1</code> if the id is not
     *            specified.
     */
    public RatingLoader(SecurityContext ctx, Class type, List<Long> ids,
            long userID) {
        this.ctx = ctx;
        loadCall = loadRatings(type, ids, userID);
    }

}
