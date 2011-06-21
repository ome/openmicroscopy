/*
 * org.openmicroscopy.shoola.env.rnd.Tile 
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
package org.openmicroscopy.shoola.env.rnd.data;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Hosts information about a tile.
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
public class Tile 
{

	/** The row index. */
	private int row;
	
	/** The column index. */
	private int column;
	
	/** The image to display if loaded. */
	private Object image;
	
	/** The index associated to the tile.*/
	private int index;
	
	/** The region covered by the tile.*/
	private Region region;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param row The row index.
	 * @param column The column index;
	 */
	public Tile(int index, int row, int column)
	{
		this.row = row;
		this.column = column;
		this.index = index;
	}
	/** 
	 * Sets the region covered by the tile.
	 * 
	 * @param region The region covered.
	 */
	public void setRegion(Region region)
	{
		this.region = region;
	}
	
	/**
	 * Returns the region covered by the tile.
	 * 
	 * @return See above.
	 */
	public Region getRegion() { return region; }
	
	/**
	 * Returns the index.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
	/**
	 * Returns the row.
	 * 
	 * @return See above.
	 */
	public int getRow() { return row; }
	
	/**
	 * Returns the column.
	 * 
	 * @return See above.
	 */
	public int getColumn() { return column; }
	
	/**
	 * Sets the image to display.
	 * 
	 * @param image The image to display
	 */
	public void setImage(Object image)
	{
		this.image = image;
	}
	
	/**
	 * Returns the image to display.
	 * 
	 * @return See above.
	 */
	public Object getImage() { return image; }
	
	/**
	 * Returns <code>true</code> if the image has been loaded,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isImageLoaded() { return image != null; }
	
}
