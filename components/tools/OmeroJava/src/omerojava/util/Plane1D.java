/*
* omerojava.util.Plane1D
*
 *------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package omerojava.util;

//Java imports

//Third-party libraries

//Application-internal dependencies

/**
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class Plane1D 
{
	
	/** The number of bytes per pixel. */
	private int 				bytesPerPixel;
	
	/** The number of timepoint along the x-axis. */
	private int					sizeX;
	
	/** The number of timepoint along the x-axis. */
	private int					sizeY;
	
	/** The original array. */
	private ReadOnlyByteArray	data;
	
	/** Strategy used to transform original data. */
	private BytesConverter		strategy;
	
	/** 
	 * Determines the offset value.
	 * 
	 * @param x	The x-coordinate.
	 * @param y	The y-coordinate.
	 * @return See above.
	 */
	private int calculateOffset(int x, int y)
	{
		return bytesPerPixel*(sizeX*y+x);
	}
	
	/**
	 * Converts the raw data.
	 * 
	 * @param sizeY The number of pixels along the y-axis.
	 */
	private double[] mappedDataAsDouble(int sizeY)
	{
		double[] mappedData;
		int	index;
		mappedData = new double[sizeX*sizeY];
	
		int offset;
		for (int x = 0; x < sizeX; x++) 
		{
			for (int y = 0; y < sizeY; y++) 
			{
				offset = calculateOffset(x, y);
				index = sizeX*y+x;
				mappedData[index] = (double)strategy.pack(data, offset, bytesPerPixel);
			}
		}
		return mappedData;
	}

	/**
	 * Converts the raw data.
	 * 
	 * @param sizeY The number of pixels along the y-axis.
	 */
	private long[] mappedDataAsLong(int sizeY)
	{
		long[] mappedData;
		int	index;
		mappedData = new long[sizeX*sizeY];
	
		int offset;
		for (int x = 0; x < sizeX; x++) 
		{
			for (int y = 0; y < sizeY; y++) 
			{
				offset = calculateOffset(x, y);
				index = sizeX*y+x;
				mappedData[index] = (long)strategy.pack(data, offset, bytesPerPixel);
			}
		}
		return mappedData;
	}

	/**
	 * Converts the raw data.
	 * 
	 * @param sizeY The number of pixels along the y-axis.
	 */
	private int[] mappedDataAsInt(int sizeY)
	{
		int[] mappedData;
		int index;
		mappedData = new int[sizeX*sizeY];
		
		int offset;
		for (int x = 0; x < sizeX; x++) 
		{
			for (int y = 0; y < sizeY; y++) 
			{
				offset = calculateOffset(x, y);
				index = sizeX*y+x;
				mappedData[index] = (int)strategy.pack(data, offset, bytesPerPixel);
			
			}
		}
		return mappedData;
	}
	
	/**
	 * Converts the raw data.
	 * 
	 * @param sizeY The number of pixels along the y-axis.
	 */
	private short[] mappedDataAsShort(int sizeY)
	{
		short[] mappedData;
		int	index;
		mappedData = new short[sizeX*sizeY];

		int offset;
		for (int x = 0; x < sizeX; x++) 
		{
			for (int y = 0; y < sizeY; y++) 
			{
				offset = calculateOffset(x, y);
				index = sizeX*y+x;
				mappedData[index] = (short)strategy.pack(data, offset, bytesPerPixel);
			}
		}
		return mappedData;
	}
	
	/**
	 * Converts the raw data.
	 * 
	 * @param sizeY The number of pixels along the y-axis.
	 */
	private byte[] mappedDataAsByte(int sizeY)
	{
		byte[] mappedData;
		int	index;
		mappedData = new byte[sizeX*sizeY];
	
		int offset;
		for (int x = 0; x < sizeX; x++) 
		{
			for (int y = 0; y < sizeY; y++) 
			{
				offset = calculateOffset(x, y);
				index = sizeX*y+x;
				mappedData[index] = (byte)strategy.pack(data, offset, bytesPerPixel);
			}
		}
		return mappedData;
	}
	
	/**
	 * Creates a new intance.
	 * 
	 * @param data			The array of byte.
	 * @param sizeX			The number of pixels along the x-axis.
	 * @param sizeY			The number of pixels along the y-axis.
	 * @param bytesPerPixel	The number of bytes per pixel.
	 * @param strategy		Strategy to transform pixel.
	 */
	public Plane1D(ReadOnlyByteArray data, int sizeX, int sizeY, 
						int bytesPerPixel,
						BytesConverter strategy)
	{
		this.bytesPerPixel = bytesPerPixel;
		this.data = data;
		this.strategy = strategy;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}

	/**
	 * Return the pixels array of the mapped data.
	 * @param transpose transpose the mapped array (used for row, col matrices 
	 * of matlab)
	 * @return see above.
	 */
	public double[] getPixelsArrayAsDouble()
	{
		return mappedDataAsDouble(sizeY);
	}

	/**
	 * Return the pixels array of the mapped data.
	 * @param transpose transpose the mapped array (used for row, col matrices 
	 * of matlab)
	 * @return see above.
	 */
	public long[] getPixelsArrayAsLong()
	{
		return mappedDataAsLong(sizeY);
	}

	/**
	 * Return the pixels array of the mapped data.
	 * @param transpose transpose the mapped array (used for row, col matrices 
	 * of matlab)
	 * @return see above.
	 */
	public int[] getPixelsArrayAsInt()
	{
		return mappedDataAsInt(sizeY);
	}

	/**
	 * Return the pixels array of the mapped data.
	 * @param transpose transpose the mapped array (used for row, col matrices 
	 * of matlab)
	 * @return see above.
	 */
	public short[] getPixelsArrayAsShort()
	{
		return mappedDataAsShort(sizeY);
	}

	/**
	 * Return the pixels array of the mapped data.
	 * @param transpose transpose the mapped array (used for row, col matrices 
	 * of matlab)
	 * @return see above.
	 */
	public byte[] getPixelsArrayAsByte()
	{
		return mappedDataAsByte(sizeY);
	}
	
}
