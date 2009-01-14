/*
 * org.openmicroscopy.shoola.env.rnd.data.Plane2D 
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
package omerojava.util;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Holds structure used to mapped the raw pixels data.
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
public class Plane2D
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
	private double[][] mappedDataAsDouble(int sizeY, boolean transpose)
	{
		double[][] mappedData;
		if(transpose)
			mappedData = new double[sizeY][sizeX];
		else
			mappedData = new double[sizeX][sizeY];
	
		int offset;
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				offset = calculateOffset(x, y);
				if(transpose)
					mappedData[y][x] = (double)strategy.pack(data, offset, bytesPerPixel);
				else
					mappedData[x][y] = (double)strategy.pack(data, offset, bytesPerPixel);
			}
		}
		return mappedData;
	}

	/**
	 * Converts the raw data.
	 * 
	 * @param sizeY The number of pixels along the y-axis.
	 */
	private long[][] mappedDataAsLong(int sizeY, boolean transpose)
	{
		long[][] mappedData;
		if(transpose)
			mappedData = new long[sizeY][sizeX];
		else
			mappedData = new long[sizeX][sizeY];
	
		int offset;
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				offset = calculateOffset(x, y);
				if(transpose)
					mappedData[y][x] = (long)strategy.pack(data, offset, bytesPerPixel);
				else
					mappedData[x][y] = (long)strategy.pack(data, offset, bytesPerPixel);
			}
		}
		return mappedData;
	}

	/**
	 * Converts the raw data.
	 * 
	 * @param sizeY The number of pixels along the y-axis.
	 */
	private int[][] mappedDataAsInt(int sizeY, boolean transpose)
	{
		int[][] mappedData;
		if(transpose)
			mappedData = new int[sizeY][sizeX];
		else
			mappedData = new int[sizeX][sizeY];
		
		int offset;
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				offset = calculateOffset(x, y);
				if(transpose)
					mappedData[y][x] = (int)strategy.pack(data, offset, bytesPerPixel);
				else
					mappedData[x][y] = (int)strategy.pack(data, offset, bytesPerPixel);
			}
		}
		return mappedData;
	}
	
	/**
	 * Converts the raw data.
	 * 
	 * @param sizeY The number of pixels along the y-axis.
	 */
	private short[][] mappedDataAsShort(int sizeY, boolean transpose)
	{
		short[][] mappedData;
		if(transpose)
			mappedData = new short[sizeY][sizeX];
		else
			mappedData = new short[sizeX][sizeY];

		int offset;
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				offset = calculateOffset(x, y);
				if(transpose)
					mappedData[y][x] = (short)strategy.pack(data, offset, bytesPerPixel);
				else
					mappedData[x][y] = (short)strategy.pack(data, offset, bytesPerPixel);
			}
		}
		return mappedData;
	}
	
	/**
	 * Converts the raw data.
	 * 
	 * @param sizeY The number of pixels along the y-axis.
	 */
	private byte[][] mappedDataAsByte(int sizeY, boolean transpose)
	{
		byte[][] mappedData;
		if(transpose)
			mappedData = new byte[sizeY][sizeX];
		else
			mappedData = new byte[sizeX][sizeY];
	
		int offset;
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				offset = calculateOffset(x, y);
				if(transpose)
					mappedData[y][x] = (byte)strategy.pack(data, offset, bytesPerPixel);
				else
					mappedData[x][y] = (byte)strategy.pack(data, offset, bytesPerPixel);
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
	public Plane2D(ReadOnlyByteArray data, int sizeX, int sizeY, 
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
	public double[][] getPixelsArrayAsDouble(boolean transpose)
	{
		return mappedDataAsDouble(sizeY, transpose);
	}

	/**
	 * Return the pixels array of the mapped data.
	 * @param transpose transpose the mapped array (used for row, col matrices 
	 * of matlab)
	 * @return see above.
	 */
	public long[][] getPixelsArrayAsLong(boolean transpose)
	{
		return mappedDataAsLong(sizeY, transpose);
	}

	/**
	 * Return the pixels array of the mapped data.
	 * @param transpose transpose the mapped array (used for row, col matrices 
	 * of matlab)
	 * @return see above.
	 */
	public int[][] getPixelsArrayAsInt(boolean transpose)
	{
		return mappedDataAsInt(sizeY, transpose);
	}

	/**
	 * Return the pixels array of the mapped data.
	 * @param transpose transpose the mapped array (used for row, col matrices 
	 * of matlab)
	 * @return see above.
	 */
	public short[][] getPixelsArrayAsShort(boolean transpose)
	{
		return mappedDataAsShort(sizeY, transpose);
	}

	/**
	 * Return the pixels array of the mapped data.
	 * @param transpose transpose the mapped array (used for row, col matrices 
	 * of matlab)
	 * @return see above.
	 */
	public byte[][] getPixelsArrayAsByte(boolean transpose)
	{
		return mappedDataAsByte(sizeY, transpose);
	}
	
}
