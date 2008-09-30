/*
 * org.openmicroscopy.shoola.env.rnd.data.DataSink 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.rnd.data;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.cache.CacheService;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.util.mem.ReadOnlyByteArray;
import ome.model.core.Pixels;

/** 
* Encapsulates access to the image raw data. 
* Contains the logic to interpret a linear byte array as a 5D array. 
* Knows how to extract a 2D-plane from the 5D array, but delegates to the 
* specified 2D-Plane the retrieval of pixel values. 
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since OME3.0
*/
public class DataSink
{

	/** Identifies the type used to store pixel values. */
	static final String INT_8 = "int8";

	/** Identifies the type used to store pixel values. */
	static final String UINT_8 = "uint8";

	/** Identifies the type used to store pixel values. */
	static final String INT_16 = "int16";

	/** Identifies the type used to store pixel values. */
	static final String UINT_16 = "uint16";

	/** Identifies the type used to store pixel values. */
	static final String INT_32 = "int32";

	/** Identifies the type used to store pixel values. */
	static final String UINT_32 = "uint32";

	/** Identifies the type used to store pixel values. */
	static final String FLOAT = "float";

	/** Identifies the type used to store pixel values. */
	static final String DOUBLE = "double";

	/**
	 * Factory method to create a new <code>DataSink</code> to handle
	 * access to the metadata associated with the specified pixels set.
	 * 
	 * @param source	The pixels set. Mustn't be <code>null</code>.
	 * @param context	The container's registry.  Mustn't be <code>null</code>.
	 * @param size		The size of the cache.
	 * @return See above.
	 */
	public static DataSink makeNew(Pixels source, Registry context)
	{
		if (source == null)
			throw new NullPointerException("No pixels.");
		if (context == null) 
			throw new NullPointerException("No registry.");
		return new DataSink(source, context);
	}
	
	/** The data source. */
	private Pixels			source;

	/** The container's registry. */
	private Registry		context;

	/** The number of bytes per pixel. */
	private int				bytesPerPixels;

	/** Strategy used to transform the raw data. */
	private BytesConverter	strategy;

	/** Cache the raw data. */
	//private PixelsCache		cache;

	/** The id of the cache. */
	private int				cacheID;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source	The pixels set.
	 * @param context	The container's registry.
	 */
	private DataSink(Pixels source, Registry context)
	{
		this.source = source;
		this.context = context;
		String type = source.getPixelsType().getValue();
		bytesPerPixels = getBytesPerPixels(type);
		cacheID = context.getCacheService().createCache();
		/*
		cache = CachingService.createPixelsCache(source.getId(), 
				source.getSizeX()*source.getSizeY()*bytesPerPixels);
				*/
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
	 * @param z The z coord.  Must be in the range <code>[0, sizeZ)</code>.
	 * @param w The w coord.  Must be in the range <code>[0, sizeW)</code>.
	 * @param t The t coord.  Must be in the range <code>[0, sizeT)</code>.
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
		return new Integer(sizeZ*sizeC*t + sizeZ*w + z);
	}

	/**
	 * Factory method to fetch plane data and create an object to access it.
	 * 
	 * @param z			The z-section at which data is to be fetched.
	 * @param t			The timepoint at which data is to be fetched.
	 * @param w			The wavelength at which data is to be fetched.
	 * @param strategy	To transform bytes into pixels values.
	 * @return A plane 2D object that encapsulates the actual plane pixels.
	 * @throws DataSourceException If an error occurs while retrieving the
	 *                              plane data from the pixels source.
	 */
	private Plane2D createPlane(int z, int t, int w, BytesConverter strategy)
		throws DataSourceException
	{
		//Retrieve data
		Integer planeIndex = linearize(z, w, t);
		CacheService cache = context.getCacheService();
		Plane2D plane = (Plane2D) cache.getElement(cacheID, planeIndex);//cache.extract(planeIndex);
		if (plane != null) return plane;
		byte[] data = null; 
		try {
			OmeroImageService service = context.getImageService();
			data = service.getPlane(source.getId(), z, t, w);
		} catch (Exception e) {
			String p = "("+z+", "+t+", "+w+")";
			throw new DataSourceException("Cannot retrieve the plane "+p, e);
		}
		ReadOnlyByteArray array = new ReadOnlyByteArray(data, 0, data.length);
		plane = new Plane2D(array, source.getSizeX().intValue(), 
							source.getSizeY().intValue(), bytesPerPixels, 
							strategy);
		//cache.add(planeIndex, plane);
		cache.addElement(cacheID, planeIndex, plane);
		return plane;
	}

	/**
	 * Extracts a 2D plane from the pixels set this object is working for.
	 * 
	 * @param z			The z-section at which data is to be fetched.
	 * @param t			The timepoint at which data is to be fetched.
	 * @param w			The wavelength at which data is to be fetched.
	 * @return A plane 2D object that encapsulates the actual plane pixels.
	 * @throws DataSourceException If an error occurs while retrieving the
	 *                              plane data from the pixels source.
	 */
	public Plane2D getPlane(int z, int t, int w)
		throws DataSourceException
	{
		return createPlane(z, t, w, strategy);
	}

	/**
	 * Returns <code>true</code> if a data source has already been created
	 * for the specified pixels set, <code>false</code> otherwise.
	 * 
	 * @param pixelsID	The id of the pixels set.
	 * @return See above.
	 */
	public boolean isSame(long pixelsID)
	{
		return (pixelsID == source.getId().longValue());
	}
	
	/** Erases the cache. */
	public void eraseCache()
	{
		context.getCacheService().removeCache(cacheID);
	}
	
}
