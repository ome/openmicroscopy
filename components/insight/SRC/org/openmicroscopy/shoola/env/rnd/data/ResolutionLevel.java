/*
 * org.openmicroscopy.shoola.agents.imviewer.util.ResolutionLevel 
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

import java.awt.Dimension;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Store information about the level and the power of 2 along the axis.
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
public class ResolutionLevel
{

	/** The resolution level.*/
	private int level;
	
	/** The tile size.*/
	private Dimension tileSize;
	
	/** The image size.*/
	private Dimension imageSize;
	
	/** The ratio along the X-axis.*/
	private double ratioX;
	
	/** The ratio along the Y-axis.*/
	private double ratioY;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param level The resolution level.
	 * @param tileSize The dimension of the tile.
	 * @param imageSize The size of the image.
	 */
	public ResolutionLevel(int level, Dimension tileSize, Dimension imageSize)
	{
		this.level = level;
		this.tileSize = tileSize;
		this.imageSize = imageSize;
		setRatio(1, 1);
	}
	
	/**
	 * Sets the ratio along the X and Y-axis.
	 * 
	 * @param ratioX The ratio along the X-axis.
	 * @param ratioY The ratio along the Y-axis.
	 */
	public void setRatio(double ratioX, double ratioY)
	{
		this.ratioX = ratioX;
		this.ratioY = ratioY;
	}
	
	/**
	 * Returns the lowest of the ratio.
	 * 
	 * @return See above.
	 */
	public double getRatio()
	{
		if (ratioX < ratioY) return ratioX;
		return ratioY;
	}
	
	/**
	 * Returns the image's size.
	 * 
	 * @return See above.
	 */
	public Dimension getImageSize() { return imageSize; }
	
	/**
	 * Returns the resolution level.
	 * 
	 * @return See above.
	 */
	public int getLevel() { return level; }

	/**
	 * Returns the tile size.
	 * 
	 * @return See above.
	 */
	public Dimension getTileSize() { return tileSize; }

}
