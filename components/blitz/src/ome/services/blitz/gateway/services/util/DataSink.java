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
package ome.services.blitz.gateway.services.util;

import ome.services.blitz.gateway.services.ImageService;
import omero.model.Pixels;


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

	/** link to the image service. */
	ImageService service;
	
	/** The data source. */
	private Pixels			source;

	/** The number of bytes per pixel. */
	private int				bytesPerPixels;

	/** Strategy used to transform the raw data. */
	private BytesConverter	strategy;

	/** Cache the raw data. */
	private PixelsCache		cache;

	/**
	 * Factory method to create a new <code>DataSink</code> to handle
	 * access to the metadata associated with the specified pixels set.
	 * 
	 * @param source	The pixels set. Mustn't be <code>null</code>.
	 * @param context	The container's registry.  Mustn't be <code>null</code>.
	 * @param size		The size of the cache.
	 * @return See above.
	 */
	public static DataSink makeNew(Pixels source, ImageService service)
	{
		if (source == null)
			throw new NullPointerException("No pixels.");
		if (service == null)
			throw new NullPointerException("No Image service.");
		return new DataSink(source, service);	
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source	The pixels set.
	 * @param service 	The Image service.
	 */
	private DataSink(Pixels source, ImageService service)
	{
		if (service == null)
			throw new NullPointerException("No Image service.");
		this.source = source;
		this.service = service;
		this.source = source;
		String type = source.getPixelsType().getValue().getValue();
		bytesPerPixels = getBytesPerPixels(type);
//		cache = CachingService.createPixelsCache(source.id.getValue(), 
//				source.sizeX.getValue()*source.sizeY.getValue()*bytesPerPixels);
		strategy = BytesConverter.getConverter(type);
	}

	/**
	 * Returns the number of bytes per pixel depending on the pixel type.
	 * 
	 * @param v The pixels Type.
	 * @return See above.
	 */
	static private int getBytesPerPixels(String v)
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
		int sizeZ = source.getSizeZ().getValue();
		int sizeC = source.getSizeC().getValue();
		if (z < 0 || sizeZ <= z) 
			throw new IllegalArgumentException(
					"z out of range [0, "+sizeZ+"): "+z+".");
		if (w < 0 || sizeC <= w) 
			throw new IllegalArgumentException(
					"w out of range [0, "+sizeC+"): "+w+".");
		if (t < 0 || source.getSizeT().getValue() <= t) 
			throw new IllegalArgumentException(
					"t out of range [0, "+source.getSizeT().getValue()+"): "+t+".");
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
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 * @throws DataSourceException If an error occurs while retrieving the
	 *                              plane data from the pixels source.
	 */
	private Plane2D createPlane(int z, int t, int w, BytesConverter strategy) 
		throws omero.ServerError
	{
		//Retrieve data
		Integer planeIndex = linearize(z, w, t);
		Plane2D plane = null;
		if (plane != null) return plane;
		byte[] data = null; 
		data = service.getRawPlane(source.getId().getValue(), z, w, t);
		ReadOnlyByteArray array = new ReadOnlyByteArray(data, 0, data.length);
		plane = new Plane2D(array, source.getSizeX().getValue(), 
							source.getSizeY().getValue(), bytesPerPixels, 
							strategy);
		return plane;
	}

	/**
	 * Extracts a 2D plane from the pixels set this object is working for.
	 * 
	 * @param z			The z-section at which data is to be fetched.
	 * @param t			The timepoint at which data is to be fetched.
	 * @param w			The wavelength at which data is to be fetched.
	 * @return A plane 2D object that encapsulates the actual plane pixels.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 */
	public Plane2D getPlane(int z, int t, int w)
		throws omero.ServerError
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
		return (pixelsID == source.getId().getValue());
	}
	
	static public double[][] mapServerToClient(byte[] data, int x, int y, String pixelType)
	{		
		Plane2D thisPlane;
		ReadOnlyByteArray array = new ReadOnlyByteArray(data, 0, data.length);
		int bpp = getBytesPerPixels(pixelType);
		BytesConverter thisStrategy = BytesConverter.getConverter(pixelType);
		thisPlane = new Plane2D(array, x, y, bpp, thisStrategy);
		return thisPlane.getPixelsArrayAsDouble();
	}
}
