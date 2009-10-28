/*
 * org.openmicroscopy.shoola.env.data.model.SplitViewFigureParam 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.model;

//Java imports
import java.util.LinkedHashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/** 
 * Hosts the parameters needed for the creation of a split view figure.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class SplitViewFigureParam
{

    /** Indicates to use the image's name as the name a row. */
	public static final int		IMAGE_NAME = 0;
    
    /** Indicates to use the dataset's name as the name a row. */
	public static final int		DATASET_NAME = 1;
    
    /** Indicates to use the tag's name as the name a row. */
	public static final int		TAG_NAME = 2;
    
	/** Identify the <code>JPEG</code> format. */
	public static final int		JPEG = 0;
	
	/** Identify the <code>PNG</code> format. */
	public static final int		PNG = 1;

	/** The supported formats. */
	public static final Map<Integer, String> 	FORMATS;
	

	static {
		FORMATS = new LinkedHashMap<Integer, String>(2);
		FORMATS.put(JPEG, "JPEG");
		FORMATS.put(PNG, "PNG");
	}
	
	/** One of the format constants defined by this class. */
	private int 	format;
	
	/** One of the projection constants defined by this class. */
	private int 	projectionType;
	
	/** The lower bound of the projection range. */
	private int 	zStart;
	
	/** The lower bound of the projection range. */
	private int 	zEnd;
	
	/** The stepping used while projecting. Default is <code>1</code>. */
	private int		stepping;
	
	/** The name of the file. */
	private String  name;
	
	/** Sets the default value. */
	private void setDefault()
	{
		format = JPEG;
		projectionType = ProjectionParam.MAXIMUM_INTENSITY;
		stepping = 1;
	}
	
}
