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

	/** The power of 2 along the X-axis.*/
	private int powerAlongX;
	
	/** The power of 2 along the Y-axis.*/
	private int powerAlongY;
	
	/** The resolution level.*/
	private int level;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param level The resolution level.
	 * @param powerAlongX The power of 2 along the X-axis.
	 * @param powerAlongY The power of 2 along the Y-axis.
	 */
	public ResolutionLevel(int level, int powerAlongX, int powerAlongY)
	{
		this.level = level;
		this.powerAlongX = powerAlongX;
		this.powerAlongY = powerAlongY;
	}
	
	/**
	 * Returns the resolution level.
	 * 
	 * @return See above.
	 */
	public int getLevel() { return level; }
	
	/**
	 * Returns power of 2 along the X-axis.
	 * 
	 * @return See above.
	 */
	public int getPowerAlongX() { return powerAlongX; }
	
	/**
	 * Returns power of 2 along the Y-axis.
	 * 
	 * @return See above.
	 */
	public int getPowerAlongY() { return powerAlongY; }
	
}
