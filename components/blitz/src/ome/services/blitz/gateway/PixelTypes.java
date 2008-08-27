/*
 * blitzgateway.util.PixelSizeMap 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package ome.services.blitz.gateway;


//Java imports
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class PixelTypes
{	
	/** Identifies the type used to store pixel values. */
	public static final String INT_8 = "int8";

	/** Identifies the type used to store pixel values. */
	public static final String UINT_8 = "uint8";

	/** Identifies the type used to store pixel values. */
	public static final String INT_16 = "int16";

	/** Identifies the type used to store pixel values. */
	public static final String UINT_16 = "uint16";

	/** Identifies the type used to store pixel values. */
	public static final String INT_32 = "int32";

	/** Identifies the type used to store pixel values. */
	public static final String UINT_32 = "uint32";

	/** Identifies the type used to store pixel values. */
	public static final String FLOAT = "float";

	/** Identifies the type used to store pixel values. */
	public static final String DOUBLE = "double";
	
	/** Set the pixels Map containing the byte size of the map. */
	public static Map<String,Integer> pixelMap = new HashMap<String, Integer>();
	static {
		pixelMap.put(INT_8, 1);
		pixelMap.put(UINT_8, 1);
		pixelMap.put(INT_16, 2);
		pixelMap.put(UINT_16, 2);
		pixelMap.put(INT_32, 4);
		pixelMap.put(UINT_32, 4);
		pixelMap.put(FLOAT, 4);
		pixelMap.put(DOUBLE, 8);
	};
}


