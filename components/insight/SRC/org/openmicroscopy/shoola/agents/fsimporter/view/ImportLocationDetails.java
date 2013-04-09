/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.fsimporter.view;

/** 
 * Provides a transfer object for import location information
 *
 * @author Scott Littlewood, <a href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since 4.4
 */
public class ImportLocationDetails {

	public static final int UNSPECIFIED_USER = -1;
	
	/** The datatype being loaded */
	private int dataType;
	
	/** The id of the user to laod data for. */
	private long userId = UNSPECIFIED_USER;

	public ImportLocationDetails(int dataType)
	{
		this(dataType, UNSPECIFIED_USER);
		
	}
	
	public ImportLocationDetails(int dataType, long userId)
	{
		this.dataType = dataType;
		this.userId = userId;
	}

	/**
	 * Returns the data type to load.
	 * @return see above.
	 */
	public long getDataType() {
		return dataType;
	}
	
	/**
	 * Returns the user id to identify the data to load data for.
	 * @return see above.
	 */
	public long getUserId() {
		return userId;
	}
}
