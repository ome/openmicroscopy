/*
 * org.openmicroscopy.shoola.env.rnd.data.DataSink 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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



//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.api.RawPixelsStorePrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.cache.CacheService;
import omero.gateway.exception.DataSourceException;
import omero.util.ReadOnlyByteArray;
import pojos.PixelsData;

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
public class DataSink
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

    /**
     * Factory method to create a new <code>DataSink</code> to handle
     * access to the metadata associated with the specified pixels set.
     * 
     * @param source The pixels set. Mustn't be <code>null</code>.
     * @param gw Reference to the {@link Gateway} Mustn't be <code>null</code>.
     * @return See above.
     */
    public static DataSink makeNew(PixelsData source, Gateway gw)
    {
        if (source == null)
            throw new NullPointerException("No pixels.");
        if (gw == null) 
            throw new NullPointerException("No Gateway.");
        return new DataSink(source, gw);
    }
    
    /**
     * Factory method to create a new <code>DataSink</code> to handle access to
     * the metadata associated with the specified pixels set.
     * 
     * @param source
     *            The pixels set. Mustn't be <code>null</code>.
     * @param gw
     *            Reference to the {@link Gateway} Mustn't be <code>null</code>.
     * @param cacheSize
     *            The size of the cache to use. (Make sure the {@link Gateway} provides
     *            a {@link CacheService})
     * @return See above.
     */
    public static DataSink makeNew(PixelsData source, Gateway gw, int cacheSize) {
        if (source == null)
            throw new NullPointerException("No pixels.");
        if (gw == null)
            throw new NullPointerException("No Gateway.");
        return new DataSink(source, gw, cacheSize);
    }

    /** The data source. */
    private PixelsData source;

    /** The number of bytes per pixel. */
    private int bytesPerPixels;

    /** Strategy used to transform the raw data. */
    private BytesConverter strategy;

    /** The id of the cache. */
    private int cacheID = -1;

    /** The pixels store for that pixels set.*/
    private RawPixelsStorePrx store;
    
    private Gateway gw;

    /**
     * Creates a new instance without using a cache.
     *
     * @param source The pixels set.
     * @param context The container's registry.
     */
    private DataSink(PixelsData source, Gateway gw)
    {
        this(source, gw, 0);
    }
    
    /**
     * Creates a new instance.
     *
     * @param source The pixels set.
     * @param context The container's registry.
     * @param cacheSize The size of the cache.
     */
    private DataSink(PixelsData source, Gateway gw, int cacheSize)
    {
        this.gw = gw;
        this.source = source;
        String type = source.getPixelType();
        bytesPerPixels = getBytesPerPixels(type); 
        
        if(cacheSize > 0) {
            if(gw.getCacheService() == null)
                throw new IllegalArgumentException("No cache provided!");
            
            int maxEntries =
                    cacheSize/(source.getSizeX()*source.getSizeY()*bytesPerPixels);
            cacheID = gw.getCacheService().createCache(
                    CacheService.IN_MEMORY, maxEntries);
        }
        
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
     * Transforms 3D coords into linear coords.
     * The returned value <code>L</code> is calculated as follows: 
     * <nobr><code>L = sizeZ*sizeW*t + sizeZ*w + z</code></nobr>.
     *
     * @param z The z coord. Must be in the range <code>[0, sizeZ)</code>.
     * @param w The w coord. Must be in the range <code>[0, sizeW)</code>.
     * @param t The t coord. Must be in the range <code>[0, sizeT)</code>.
     * @return The linearized value corresponding to <code>(z, w, t)</code>.
     */
    private Integer linearize(int z, int w, int t)
    {
        int sizeZ = source.getSizeZ();
        int sizeC = source.getSizeC();
        if (z < 0 || sizeZ <= z)
            throw new IllegalArgumentException(
                    "z out of range [0, "+sizeZ+"): "+z+".");
        if (w < 0 || sizeC <= w)
            throw new IllegalArgumentException(
                    "w out of range [0, "+sizeC+"): "+w+".");
        if (t < 0 || source.getSizeT() <= t)
            throw new IllegalArgumentException(
                    "t out of range [0, "+source.getSizeT()+"): "+t+".");
        return Integer.valueOf(sizeZ*sizeC*t + sizeZ*w + z);
    }

    /**
     * Factory method to fetch plane data and create an object to access it.
     *
     * @param ctx The security context.
     * @param z The z-section at which data is to be fetched.
     * @param t The timepoint at which data is to be fetched.
     * @param w The wavelength at which data is to be fetched.
     * @param strategy To transform bytes into pixels values.
     * @return A plane 2D object that encapsulates the actual plane pixels.
     * @throws DataSourceException If an error occurs while retrieving the
     *                              plane data from the pixels source.
     */
    private Plane2D createPlane(SecurityContext ctx, int z, int t, int w,
            BytesConverter strategy, boolean close)
            throws DataSourceException
    {
        //Retrieve data
        Integer planeIndex = linearize(z, w, t);
        Plane2D plane = null;
        if (cacheID >= 0) {
            CacheService cache = gw.getCacheService();
            plane = (Plane2D) cache.getElement(cacheID, planeIndex);
            if (plane != null)
                return plane;
        }
        byte[] data = null; 
        try {
            //initializes if null.
            if (store == null) {
                store = gw.createPixelsStore(ctx);
                store.setPixelsId(source.getId(), false);
            }
            data = store.getPlane(z, w, t);
        } catch (Exception e) {
            String p = "("+z+", "+w+", "+t+")";
            throw new DataSourceException("Cannot retrieve the plane "+p, e);
        } finally {
            if (close) {
                gw.closeService(ctx, store);
                store = null;
            }
        }
        ReadOnlyByteArray array = new ReadOnlyByteArray(data, 0, data.length);
        plane = new Plane2D(array, source.getSizeX(), source.getSizeY(), 
                bytesPerPixels, strategy);
        if(cacheID >= 0)
            gw.getCacheService().addElement(cacheID, planeIndex, plane);
        return plane;
    }

    /**
     * Extracts a 2D plane from the pixels set this object is working for.
     *
     * @param ctx The security context.
     * @param z The z-section at which data is to be fetched.
     * @param t The timepoint at which data is to be fetched.
     * @param w The wavelength at which data is to be fetched.
     * @return A plane 2D object that encapsulates the actual plane pixels.
     * @throws DataSourceException If an error occurs while retrieving the
     *                              plane data from the pixels source.
     */
    public Plane2D getPlane(SecurityContext ctx, int z, int t, int w)
            throws DataSourceException
    {
        return createPlane(ctx, z, t, w, strategy, true);
    }

    /**
     * Extracts a 2D plane from the pixels set this object is working for.
     *
     * @param ctx The security context.
     * @param z The z-section at which data is to be fetched.
     * @param t The timepoint at which data is to be fetched.
     * @param w The wavelength at which data is to be fetched.
     * @return A plane 2D object that encapsulates the actual plane pixels.
     * @throws DataSourceException If an error occurs while retrieving the
     *                              plane data from the pixels source.
     */
    public Plane2D getPlane(SecurityContext ctx, int z, int t, int w, boolean
            close)
            throws DataSourceException
    {
        return createPlane(ctx, z, t, w, strategy, close);
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

    /** Erases the cache. */
    public void clearCache()
    {
        if(cacheID==-1)
            return;
        
        gw.getCacheService().clearCache(cacheID);
    }

    /**
     * Sets the size either to 1 or 0 depending on the passed value.
     *
     * @param cacheInMemory Passed <code>true</code> to set the size to 1,
     *                      <code>false</code> to set to 0.
     */
    public void setCacheInMemory(boolean cacheInMemory)
    {
        if(cacheID==-1)
            return;
        
        clearCache();
        if (cacheInMemory)
            gw.getCacheService().setCacheEntries(cacheID, 1);
        else gw.getCacheService().setCacheEntries(cacheID, 0);
    }

}
