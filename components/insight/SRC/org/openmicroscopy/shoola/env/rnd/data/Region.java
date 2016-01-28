/*
 * org.openmicroscopy.shoola.env.rnd.data.Region 
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.rnd.data;

/** 
 * Describes a region.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class Region
{

	/** The x-coordinate of the top-left corner of the region. */
	private int xLocation;
	
	/** The y-coordinate of the top-left corner of the region. */
	private int yLocation;
	
	/** The width of the region. */
	private int width;
	
	/** The height of the region. */
	private int height;
	
	/**
	 * Creates a new instance.
	 *
	 * @param width The width of the region.
	 * @param height The height of the region.
	 */
	public Region(int width, int height)
	{
		this(0, 0, width, height);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param xLocation The x-coordinate of the top-left corner of the region.
	 * @param yLocation The y-coordinate of the top-left corner of the region.
	 * @param width		The width of the region.
	 * @param height	The height of the region.
	 */
	public Region(int xLocation, int yLocation, int width, int height)
	{
		if (xLocation < 0) xLocation = 0;
		if (yLocation < 0) yLocation = 0;
		if (width <= 0) 
			throw new IllegalArgumentException(
					"Region's width cannot be less than 1.");
		if (height <= 0) 
			throw new IllegalArgumentException(
					"Region's width cannot be less than 1.");
		this.xLocation = xLocation;
		this.yLocation = yLocation;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Returns the x-coordinate of the top-left corner of the region.
	 * 
	 * @return See above.
	 */
	public int getX() { return xLocation; }
	
	/**
	 * Returns the y-coordinate of the top-left corner of the region.
	 * 
	 * @return See above.
	 */
	public int getY() { return yLocation; }
	
	/**
	 * Returns the width of the region.
	 * 
	 * @return See above.
	 */
	public int getWidth() { return width; }
	
	/**
	 * Returns the height of the region.
	 * 
	 * @return See above.
	 */
	public int getHeight() { return height; }
	
	/**
	 * Overridden to return the dimension of the region and location.
	 */
	public String toString()
	{
		return "x="+xLocation+" y="+yLocation+" w="+width+" h="+height; 
	}
	
}
