/*
 * omeis.providers.re.data.RegionDef 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
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
package omeis.providers.re.data;


//Java imports
import java.io.Serializable;

//Third-party libraries

//Application-internal dependencies

/** 
 * Identifies a rectangular region.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class RegionDef 
	implements Serializable
{

	 /** The generated serial number */
	private static final long serialVersionUID = 2681169086599580818L;

	/** The x-coordinate of the top-left corner. */
	private int x;
	
	/** The y-coordinate of the top-left corner. */
	private int y;
	
	/** The width of the region. */
	private int width;
	
	/** The height of the region. */
	private int height;

	/** Creates a default instance. */
	public RegionDef()
	{
		this(0, 0, 0, 0);
	}
	
	/**
	 * Creates a rectangular region.
	 * 
	 * @param x The x-coordinate of the top-left corner.
	 * @param y The y-coordinate of the top-left corner.
	 * @param width The width of the region.
	 * @param height The height of the region.
	 */
	public RegionDef(int x, int y, int width, int height)
	{
		setHeight(height);
		setWidth(width);
		setX(x);
		setY(y);
	}
	
	/**
	 * Returns the x-coordinate of the top-left corner.
	 * 
	 * @return See above.
	 */
	public int getX() { return x; }
	
	/**
	 * Returns the y-coordinate of the top-left corner.
	 * 
	 * @return See above.
	 */
	public int getY() { return y; }
	
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
	 * Sets the x-coordinate of the top-left corner.
	 * 
	 * @param x The value to set.
	 */
	public void setX(int x)
	{
		if (x < 0) x = 0;
		this.x = x;
	}
	
	/**
	 * Sets the y-coordinate of the top-left corner.
	 * 
	 * @param y The value to set.
	 */
	public void setY(int y)
	{
		if (y < 0) y = 0;
		this.y = y;
	}
	
	/**
	 * Sets the width of the region.
	 * 
	 * @param width The value to set.
	 */
	public void setWidth(int width)
	{
		if (width < 0) width = 0;
		this.width = width;
	}
	
	/**
	 * Sets the height of the region.
	 * 
	 * @param height The value to set.
	 */
	public void setHeight(int height)
	{
		if (height < 0) height = 0;
		this.height = height;
	}
	
	/**
	 * Overridden to return the region as a string.
	 * @see Object#toString()
	 */
	public String toString()
	{
		return "x="+x+" y="+y+" width="+width+" height="+height;
	}
	
}
