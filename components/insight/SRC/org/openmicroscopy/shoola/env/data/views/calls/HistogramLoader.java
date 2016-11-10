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

import java.util.Map;

import omero.gateway.SecurityContext;
import omero.gateway.facility.RawDataFacility;
import omero.gateway.model.ImageData;

import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/**
 * Loads the histogram data for a certain {@link ImageData}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class HistogramLoader extends BatchCallTree {

    /** The call */
    private BatchCall loadCall;

    /** The result of the loading call */
    private Map<Integer, int[]> result;

    /**
     * Creates a new instance
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param img
     *            The {@link ImageData}
     * @param channels
     *            The channel indices
     * @param z
     *            Z plane index
     * @param t
     *            T plane index
     */
    public HistogramLoader(SecurityContext ctx, ImageData img, int[] channels,
            int z, int t) {
        loadCall = new BatchCall("Loading Histogram data") {
            public void doCall() throws Exception {
                result = context
                        .getGateway()
                        .getFacility(RawDataFacility.class)
                        .getHistogram(ctx, img.getDefaultPixels(), channels, z,
                                t);
            }
        };
    }

    @Override
    protected void buildTree() {
        add(loadCall);
    }

    @Override
    /**
     * Get the result of the call, which is a Map holding the histogram data, where its key is the channel index.
     * @return See above. 
     */
    protected Object getResult() {
        return result;
    }

}
