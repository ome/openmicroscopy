/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2017 University of Dundee. All rights reserved.
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
package omero.gateway.rnd;

import java.util.Map;

import omero.ServerError;
import omero.api.RawPixelsStorePrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.DataSourceException;
import omero.romio.PlaneDef;
import omero.util.ReadOnlyByteArray;
import omero.gateway.model.PixelsData;

/** 
* Encapsulates access to the image raw data.
* Contains the logic to interpret a linear byte array as a 5D array.
* Knows how to extract a 2D-plane from the 5D array, but delegates to the
* specified 2D-Plane the retrieval of pixel values.
*
* @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* @since OME3.0
*/
public class DataSink implements AutoCloseable
{

    /** Identifies the type used to store pixel values. */
    public static final String INT_8 = PixelsData.INT8_TYPE;

    /** Identifies the type used to store pixel values. */
    public static final String UINT_8 = PixelsData.UINT8_TYPE;

    /** Identifies the type used to store pixel values. */
    public static final String INT_16 = PixelsData.INT16_TYPE;

    /** Identifies the type used to store pixel values. */
    public static final String UINT_16 = PixelsData.UINT16_TYPE;

    /** Identifies the type used to store pixel values. */
    public static final String INT_32 = PixelsData.INT32_TYPE;

    /** Identifies the type used to store pixel values. */
    public static final String UINT_32 = PixelsData.UINT32_TYPE;

    /** Identifies the type used to store pixel values. */
    public static final String FLOAT = PixelsData.FLOAT_TYPE;

    /** Identifies the type used to store pixel values. */
    public static final String DOUBLE = PixelsData.DOUBLE_TYPE;

    /** The data source. */
    private PixelsData source;

    /** The number of bytes per pixel. */
    private int bytesPerPixels;

    /** Strategy used to transform the raw data. */
    private BytesConverter strategy;

    /** The pixels store for that pixels set.*/
    private RawPixelsStorePrx store;

    /**Reference to the gateway.*/
    private Gateway gw;

    /**Reference to the SecurityContext.*/
    private SecurityContext ctx;
    
    /**
     * Creates a new instance.
     *
     * @param ctx
     *            The SecurityContext
     * @param source
     *            The pixels set.
     * @param gw
     *            Reference to the gateway.
     * @throws DSOutOfServiceException
     *             If the PixelsStore can't be accessed
     */
    public DataSink(SecurityContext ctx, PixelsData source, Gateway gw)
            throws DSOutOfServiceException {
        this.ctx = ctx;
        this.gw = gw;
        this.source = source;
        store = gw.createPixelsStore(ctx);
        try {
            store.setPixelsId(source.getId(), false);
        } catch (ServerError e) {
            throw new DSOutOfServiceException("Can't set pixels id", e);
        }
        String type = source.getPixelType();
        bytesPerPixels = getBytesPerPixels(type);
        strategy = BytesConverter.getConverter(type);
    }

    /**
     * Returns the number of bytes per pixel depending on the pixel type.
     *
     * @param v The pixels Type.
     * @return See above.
     */
    private int getBytesPerPixels(String v)
    {
        if (INT_8.equals(v) || UINT_8.equals(v)) return 1;
        if (INT_16.equals(v) || UINT_16.equals(v)) return 2;
        if (INT_32.equals(v) || UINT_32.equals(v) || FLOAT.equals(v)) 
            return 4;
        if (DOUBLE.equals(v)) return 8;
        return -1;
    }

    /**
     * Factory method to fetch plane data and create an object to access it.
     * @param z The z-section at which data is to be fetched.
     * @param t The timepoint at which data is to be fetched.
     * @param c The channel at which data is to be fetched.
     * @param strategy To transform bytes into pixels values.
     * @return A plane 2D object that encapsulates the actual plane pixels.
     * @throws DataSourceException If an error occurs while retrieving the
     *                              plane data from the pixels source.
     */
    private Plane2D createPlane(int z, int t, int c,
            BytesConverter strategy)
                    throws DataSourceException
    {
        //Retrieve data
        Plane2D plane = null;
        byte[] data = null; 
        try {
            data = store.getPlane(z, c, t);
        } catch (Exception e) {
            String p = "("+z+", "+c+", "+t+")";
            throw new DataSourceException("Cannot retrieve the plane "+p, e);
        }
        ReadOnlyByteArray array = new ReadOnlyByteArray(data, 0, data.length);
        plane = new Plane2D(array, source.getSizeX(), source.getSizeY(), 
                bytesPerPixels, strategy);
        return plane;
    }

    /**
     * Extracts a 2D tile from the pixels set this object is working for.
     *
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
     * @return A plane 2D object that encapsulates the actual plane pixels.
     * @throws DataSourceException
     *             If an error occurs while retrieving the plane data from the
     *             pixels source.
     */
    public Plane2D getTile(int z, int t, int c, int x,
            int y, int w, int h) throws DataSourceException {
        byte[] data = null;
        try {
            data = store.getTile(z, c, t, x, y, w, h);
        } catch (Exception e) {
            String p = "(" + z + ", " + c + ", " + t + ", " + x + ", " + y
                    + ", " + w + ", " + h + ")";
            throw new DataSourceException("Cannot retrieve the plane " + p, e);
        }
        ReadOnlyByteArray array = new ReadOnlyByteArray(data, 0, data.length);
        return new Plane2D(array, w, h, bytesPerPixels, strategy);
    }

    /**
     * Extracts a 2D plane from the pixels set this object is working for.
     *
     * @param z The z-section at which data is to be fetched.
     * @param t The timepoint at which data is to be fetched.
     * @param c The channel at which data is to be fetched.
     * @return A plane 2D object that encapsulates the actual plane pixels.
     * @throws DataSourceException If an error occurs while retrieving the
     *                              plane data from the pixels source.
     */
    public Plane2D getPlane(int z, int t, int c)
            throws DataSourceException
    {
        return createPlane(z, t, c, strategy);
    }

    /**
     * Get the histogram data for the given image. Currently only non-tiled
     * images are supported.
     *
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
     * @throws DataSourceException  If an error occurred 
     */
    public Map<Integer, int[]> getHistogram(int[] channels, int binCount,
            boolean globalRange, PlaneDef plane)
            throws DataSourceException {
        try {
            if (plane == null)
                plane = new PlaneDef(omeis.providers.re.data.PlaneDef.XY, 0, 0,
                        0, 0, null, -1);
            return store.getHistogram(channels, binCount, globalRange, plane);
        } catch (Exception e) {
            throw new DataSourceException("Couldn't get histogram data", e);
        }
    }
    
    /**
     * Returns <code>true</code> if a data source has already been created
     * for the specified pixels set, <code>false</code> otherwise.
     *
     * @param pixelsID The id of the pixels set.
     * @return See above.
     */
    public boolean isSame(long pixelsID)
    { 
        return pixelsID == source.getId();
    }

    @Override
    public void close() {
        gw.closeService(ctx, store);
    }
}
