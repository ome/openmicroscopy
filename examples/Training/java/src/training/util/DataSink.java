/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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
package training.util;


import omero.gateway.model.PixelsData;

/** 
 * Encapsulates access to the image raw data.
 * Contains the logic to interpret a linear byte array as a 5D array.
 * Knows how to extract a 2D-plane from the 5D array, but delegates to the
 * specified 2D-Plane the retrieval of pixel values.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
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

	/** The data source. */
	private PixelsData		source;
	
	/** The number of bytes per pixel. */
	private int				bytesPerPixels;

	/** Strategy used to transform the raw data. */
	private BytesConverter	strategy;

	/**
	 * Creates a new instance.
	 * 
	 * @param source	The pixels set.
	 * @param context	The container's registry.
	 * @param cacheSize	The size of the cache.
	 */
	public DataSink(PixelsData source)
	{
		this.source = source;
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
	 * 
	 * @param data The array to convert.
	 * @param strategy	To transform bytes into pixels values.
	 * @return A plane 2D object that encapsulates the actual plane pixels.
	 * @throws Exception If an error occurs while retrieving the
	 *                              plane data from the pixels source.
	 */
	private Plane2D createPlane(byte[] data, BytesConverter strategy)
		throws Exception
	{
		//Retrieve data
		ReadOnlyByteArray array = new ReadOnlyByteArray(data, 0, data.length);
		return new Plane2D(array, source.getSizeX(), source.getSizeY(), 
				bytesPerPixels, strategy);
	}

	/**
	 * Extracts a 2D plane from the pixels set this object is working for.
	 * 
	 * @param data The array to convert.
	 * @return A plane 2D object that encapsulates the actual plane pixels.
	 * @throws Exception If an error occurs while retrieving the
	 *                              plane data from the pixels source.
	 */
	public Plane2D getPlane(byte[] data)
		throws Exception
	{
		return createPlane(data, strategy);
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
		return (pixelsID == source.getId());
	}
	
}
