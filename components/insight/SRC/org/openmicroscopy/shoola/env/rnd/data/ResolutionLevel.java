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
	
	/**
	 * Creates a new instance.
	 * 
	 * @param level The resolution level.
	 * @param tileSize The dimension of the tile.
	 */
	public ResolutionLevel(int level, Dimension tileSize)
	{
		this.level = level;
		this.tileSize = tileSize;
	}
	
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
