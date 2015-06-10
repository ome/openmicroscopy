/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package omero.gateway.facility;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DataSourceException;
import omero.gateway.rnd.DataSink;
import omero.gateway.rnd.Plane2D;
import pojos.PixelsData;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

//Java imports

/**
 *  A {@link Facility}  for accessing raw data
 *  
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class RawDataFacility extends Facility {

    /** Cache for holding/reusing {@link DataSink}s */
    private static final Cache<Long, DataSink> cache = CacheBuilder
            .newBuilder().build();

    /**
     * Creates a new instance
     * @param gateway Reference to the {@link Gateway}
     */
    RawDataFacility(Gateway gateway) {
        super(gateway);
    }

    /**
     * Extracts a 2D plane from the pixels set. Connection to the PixelsStore
     * will be closed automatically.
     * 
     * @param ctx
     *            The security context.
     * @param pixels
     *            The {@link PixelsData} object to fetch the data from.
     * @param z
     *            The z-section at which data is to be fetched.
     * @param t
     *            The timepoint at which data is to be fetched.
     * @param w
     *            The wavelength at which data is to be fetched.
     * @return A plane 2D object that encapsulates the actual plane pixels.
     * @throws DataSourceException
     *             If an error occurs while retrieving the plane data from the
     *             pixels source.
     */
    public Plane2D getPlane(SecurityContext ctx, PixelsData pixels, int z,
            int t, int w) throws DataSourceException {
        Plane2D data = null;
        try {
            DataSink ds = RawDataFacility.getDataSink(pixels, gateway);
            data = ds.getPlane(ctx, z, t, w, true);
        } catch (ExecutionException e) {
            throw new DataSourceException("Can't initiate DataSink", e);
        }
        return data;
    }

    /**
     * Extracts a 2D plane from the pixels set.
     *
     * @param ctx
     *            The security context.
     * @param pixels
     *            The {@link PixelsData} object to fetch the data from.
     * @param z
     *            The z-section at which data is to be fetched.
     * @param t
     *            The timepoint at which data is to be fetched.
     * @param w
     *            The wavelength at which data is to be fetched.
     * @param close
     *            Pass <code>true></code> to close the connection to the
     *            Pixelstore, <code>false</code> to leave it open.
     * @return A plane 2D object that encapsulates the actual plane pixels.
     * @throws DataSourceException
     *             If an error occurs while retrieving the plane data from the
     *             pixels source.
     */
    public Plane2D getPlane(SecurityContext ctx, PixelsData pixels, int z,
            int t, int w, boolean close) throws DataSourceException {
        Plane2D data = null;
        try {
            DataSink ds = RawDataFacility.getDataSink(pixels, gateway);
            data = ds.getPlane(ctx, z, t, w, close);
            if (close)
                cache.invalidate(pixels.getId());
        } catch (ExecutionException e) {
            throw new DataSourceException("Can't initiate DataSink", e);
        }
        return data;
    }

    private static DataSink getDataSink(final PixelsData pixels,
            final Gateway gateway) throws ExecutionException {
        return cache.get(pixels.getId(), new Callable<DataSink>() {
            @Override
            public DataSink call() throws Exception {
                return DataSink.makeNew(pixels, gateway);
            }
        });
    }
}
