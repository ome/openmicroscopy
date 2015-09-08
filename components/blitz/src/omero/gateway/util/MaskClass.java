/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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

package omero.gateway.util;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import omero.gateway.model.MaskData;

/** 
 * The mask.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @since OME3.0
 */
class MaskClass
{
	
	/** The points in the mask. */
	private Set<Point> points;
	
	/** The color of the mask. */
	private int colour;
	
	/** The minimum and maximum (x,y) coordinates of the mask. */
	private Point min, max;
	
	/** The mask Width. */
	private int width;
	
	/** The mask Height. */
	private int height;

	/**
	 * Creates a new instance. 
	 * 
	 * @param value The color value.
	 */
	MaskClass(int value)
	{
		points = new HashSet<Point>();
		colour = value;
	}

	/**
	 * Returns the color of the mask.
	 * 
	 * @return See above.
	 */
	public Color getColour() { return new Color(colour); }
	
	/**
	 * Converts the mask data to a byte array.
	 * @return See above.
	 * @throws IOException
	 */
	byte[] asBytes() 
		throws IOException
	{
		byte[] data = new byte[(int) Math.ceil(
				(double) width*(double) height/8.0)];
		int offset = 0;
		for (int y = min.y ; y < max.y + 1 ; y++)
		{
			for (int x = min.x ; x < max.x + 1 ; x++)
			{
				if(points.contains(new Point(x,y)))
					setBit(data, offset, 1);
				else
					setBit(data, offset, 0);
				offset++;
			}
		}
		return data;
	}

	/**
	 * Adds the point to the Mask.
	 * @param p See above.
	 */
	void add(Point p)
	{
		if (points.size() == 0)
		{
			min = new Point(p);
			max = new Point(p);
		}
		else
		{
			min.x = Math.min(p.x, min.x);
			min.y = Math.min(p.y, min.y);
			max.x = Math.max(p.x, max.x);
			max.y = Math.max(p.y, max.y);
		}
		width = max.x-min.x+1;
		height = max.y-min.y+1;
		points.add(p);
	}

	/**
	 * Returns the MaskClass as a MaskData object, and assign it to 
	 * the coordinates provided.
	 * 
	 * @param z The selected z-section.
	 * @param t The selected time point.
	 * @param c The selected z-channel.
	 * @return See above.
	 * @throws IOException Thrown if an error occurred while creating the mask.
	 */
	MaskData asMaskData(int z, int t, int c) 
		throws IOException
	{
		MaskData mask = new MaskData();
		mask.setX((double) min.x);
		mask.setY((double) min.y);
		mask.setWidth((double) width);
		mask.setHeight((double) height);
		mask.setReadOnly(true);
		mask.setT(t);
		mask.setZ(z);
		mask.setC(c);
		mask.getShapeSettings().setFill(new Color(colour));
		mask.setMask(asBytes());
		return mask;
	}
	
	/** 
	 * Sets the bit value in a byte array at position bit to be the value
	 * value.
	 * @param data See above.
	 * @param bit See above.
	 * @param val See above.
	 */
	void setBit(byte[] data, int bit, int val) 
	{
		int bytePosition = bit/8;
		int bitPosition = 7-bit%8;
		data[bytePosition] = (byte) ((byte)(data[bytePosition]&
									(~(byte)(0x1<<bitPosition)))|
									(byte)(val<<bitPosition));
	}

	/** 
	 * Returns the bit value in a byte array at position bit to be the value
	 * value.
	 * @param data See above.
	 * @param bit See above.
	 */
	byte getBit(byte[] data, int bit) 
	{
		int bytePosition = bit/8;
		int bitPosition = 7-bit%8;
		return (byte) ((byte)(data[bytePosition] & (0x1<<bitPosition)) !=0 ? 
				(byte) 1 : (byte)0);
	} 

}
