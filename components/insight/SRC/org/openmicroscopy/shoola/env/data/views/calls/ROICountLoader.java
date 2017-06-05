/*
 * org.openmicroscopy.shoola.env.data.views.calls.ServerSideROILoader
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
import omero.gateway.facility.ROIFacility;

import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/**
 * Loads the number of ROIs for a specific image
 *
 * @author Domink Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ROICountLoader extends BatchCallTree {

    /** The result of the query. */
    private Object results;

    /** Call to load the ROIs. */
    private BatchCall loadCall;

    private BatchCall makeLoadCalls(final SecurityContext ctx,
            final long imageID) {
        return new BatchCall("Load ROI count from Server") {
            public void doCall() throws Exception {
                results = context.getGateway().getFacility(ROIFacility.class)
                        .getROICount(ctx, imageID);
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
     * Returns the root node of the requested tree.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() {
        return results;
    }

    /**
     * Creates a new instance.
     * 
     * @param ctx
     *            The security context.
     * @param imageID
     *            The image's ID.
     */
    public ROICountLoader(SecurityContext ctx, long imageID) {
        loadCall = makeLoadCalls(ctx, imageID);
    }

}
