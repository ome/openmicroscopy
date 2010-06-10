/*
 * pojos.util.MaskClass
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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

package pojos.util;


//Java imports
import java.awt.Color;
import java.awt.Point;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import pojos.MaskData;

/** 
 * 
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


class MaskClass
{
	/** The points in the mask. */
	Set<Point> points;
	
	/** The colour of the mask. */
	int colour;
	
	/** The min and max (x,y) coords of the mask. */
	Point min, max;
	
	/** The mask Width. */
	int width;
	
	/** The mask Height. */
	int height;

	/**
	 * Instantiate the maskClass with the colour value.
	 * @param value See above.
	 */
	MaskClass(int value)
	{
		points = new HashSet<Point>();
		colour = value;
	}

	/**
	 * Return the colour of the mask.
	 * @return See above.
	 */
	public Color getColour()
	{
		return new Color(colour);
	}


	/**
	 * Convert the mask data to a byte array.
	 * @return See above.
	 * @throws IOException
	 */
	public byte[] asBytes() throws IOException
	{

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream outputStream = new DataOutputStream(byteStream);
		for(int x = min.x ; x < max.x + 1 ; x++)
		{
			for(int y = min.y ; y < max.y + 1 ; y++)
			{
				if(points.contains(new Point(x,y)))
					outputStream.writeInt(colour);
				else
					outputStream.writeInt(0);
			}
		}
		outputStream.close();
		return byteStream.toByteArray();
	}

	/**
	 * Add the point to the Mask.
	 * @param p See above.
	 */
	public void add(Point p)
	{
		if(points.size()==0)
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
	 * Return the MaskClass as a MaskData object, and assign it to 
	 * The coords provided.
	 * @param z See above.
	 * @param t See above.
	 * @param c See above.
	 * @return See above.
	 * @throws IOException
	 */
	public MaskData asMaskData(int z, int t, int c) throws IOException
	{
		MaskData mask = new MaskData();
		mask.setX((double)min.x);
		mask.setY((double)min.y);
		mask.setWidth((double)width);
		mask.setHeight((double)height);
		mask.setReadOnly(true);
		mask.setT(t);
		mask.setZ(z);
		mask.setC(c);
		mask.setMask(this.asBytes());
		return mask;
	}
}
