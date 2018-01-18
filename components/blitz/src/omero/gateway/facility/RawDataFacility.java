/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2017 University of Dundee. All rights reserved.
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

import java.util.Collections;
import java.util.Map;

import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.DataSourceException;
import omero.gateway.model.PixelsData;
import omero.gateway.rnd.DataSink;
import omero.gateway.rnd.Plane2D;
import omero.romio.PlaneDef;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.MultiKeyMap;

/**
 * A {@link Facility} for accessing raw data
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class RawDataFacility extends Facility implements AutoCloseable {

    /** Cache the {@link DataSink}s for re-use (keys: ctx.groupid and pixelsId) */
    private MultiKeyMap cache = new MultiKeyMap();

    /**
     * Creates a new instance
     * 
     * @param gateway
     *            Reference to the {@link Gateway}
     */
    RawDataFacility(Gateway gateway) {
        super(gateway);
    }

    /**
     * Get the histogram data for the given image, using default 256 bins and
     * the channels global min/max for the histogram range. Currently only
     * non-tiled images are supported.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param pixels
     *            The {@link PixelsData} object
     * @param channels
     *            The channel indices
     * @param z
     *            The z plane index (optional; default: 0)
     * @param t
     *            The t plane index (optional; default: 0)
     * @return A {@link Map} of histogram data, where the key is the channel
     *         index
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public Map<Integer, int[]> getHistogram(SecurityContext ctx,
            PixelsData pixels, int[] channels, int z, int t)
            throws DSOutOfServiceException, DSAccessException {
        z = z >= 0 ? z : 0;
        t = t >= 0 ? t : 0;
        PlaneDef plane = new PlaneDef(omeis.providers.re.data.PlaneDef.XY, 0,
                0, z, t, null, -1);
        return getHistogram(ctx, pixels, channels, 256, true, plane);
    }

    /**
     * Get the histogram data for the given image. Currently only non-tiled
     * images are supported.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param pixels
     *            The {@link PixelsData} object
     * @param channels
     *            The channel indices
     * @param binCount
     *            The number of bins (optional, default: 256)
     * @param globalRange
     *            Use the global minimum/maximum to determine the histogram
     *            range, otherwise use plane minimum/maximum value
     * @param plane
     *            The plane to specify z/t and/or a certain region (optional,
     *            default: whole region of the first z/t plane)
     * @return A {@link Map} of histogram data, where the key is the channel
     *         index
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public Map<Integer, int[]> getHistogram(SecurityContext ctx,
            PixelsData pixels, int[] channels, int binCount,
            boolean globalRange, PlaneDef plane)
            throws DSOutOfServiceException, DSAccessException {
        if (pixels == null)
            return Collections.emptyMap();
        
        try {
            if (plane == null)
                plane = new PlaneDef(omeis.providers.re.data.PlaneDef.XY, 0, 0,
                        0, 0, null, -1);
            return getDataSink(ctx, pixels, gateway).getHistogram(channels,
                    binCount, globalRange, plane);
        } catch (Exception e) {
            handleException(this, e, "Couldn't get histogram data.");
        }
        return null;
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
     * @param c
     *            The channel at which data is to be fetched.
     * @return A plane 2D object that encapsulates the actual plane pixels.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public Plane2D getPlane(SecurityContext ctx, PixelsData pixels, int z,
            int t, int c) throws DSOutOfServiceException, DSAccessException {
        if (pixels == null)
            return null;
        
        try {
            return getDataSink(ctx, pixels, gateway).getPlane(z, t, c);
        } catch (Exception e) {
            handleException(this, e, "Couldn't get plane z=" + z + " t=" + t
                    + " c=" + c);
        }
        return null;
    }

    /**
     * Extracts a 2D tile from the pixels set
     * 
     * @param ctx
     *            The security context.
     * @param pixels
     *            The {@link PixelsData} object to fetch the data from.
     * @param z
     *            The z-section at which data is to be fetched.
     * @param t
     *            The timepoint at which data is to be fetched.
     * @param c
     *            The channel at which data is to be fetched.
     * @param x
     *            The x coordinate
     * @param y
     *            The y coordinate
     * @param w
     *            The width of the tile
     * @param h
     *            The height of the tile
     * @return A plane 2D object that encapsulates the actual tile pixels.
     * @throws DataSourceException
     *             If an error occurs while retrieving the plane data from the
     *             pixels source.
     */
    public Plane2D getTile(SecurityContext ctx, PixelsData pixels, int z,
            int t, int c, int x, int y, int w, int h)
            throws DataSourceException {
        if (pixels == null)
            return null;
        
        try {
            return getDataSink(ctx, pixels, gateway).getTile(z, t, c, x, y, w,
                    h);
        } catch (DSOutOfServiceException e) {
            throw new DataSourceException("Can't initiate DataSink", e);
        }
    }

    /**
     * Retrieves a data sink corresponding the pixels.
     *
     * @param ctx
     *            The SecurityContext
     * @param pixels
     *            The pixels to handle.
     * @param gateway
     *            The gateway.
     * @return See above.
     * @throws DSOutOfServiceException
     *             If an error occurs when initializing the RawPixelsStore
     */
    private DataSink getDataSink(SecurityContext ctx, PixelsData pixels,
            Gateway gateway) throws DSOutOfServiceException {
        DataSink ds = (DataSink) cache.get(ctx.getGroupID(), pixels.getId());
        if (ds == null) {
            ds = new DataSink(ctx, pixels, gateway);
            cache.put(ctx.getGroupID(), pixels.getId(), ds);
        }
        return ds;
    }

    @Override
    public void close() {
        MapIterator it = cache.mapIterator();
        while (it.hasNext()) {
            it.next();
            ((DataSink) it.getValue()).close();
        }
        firePropertyChanged(Gateway.PROP_FACILITY_CLOSED,
                null, getClass().getName());
        removePropertyChangeListener(null);
    }
}
