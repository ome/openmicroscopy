/*
 * org.openmicroscopy.shoola.env.rnd.DataSink
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.rnd.data;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;

/** 
 * The image data. 
 * Contains the logic to interpret a linear byte array as a 5D array. 
 * Knows how to extract a 2D-plane from the 5D array, but delegates to the 
 * specified 2D-Plane the retrieval of pixel values. 
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class DataSink
{
	/** Minimum value assigned to the dimension order constants. */
	private static int  		MIN_DIM_ORDER = 0;
	
	/** Minimum value assigned to the dimension order constants. */
	private static int			MAX_DIM_ORDER = 5;
    
	/** Identifies the type used to store pixel values. */
	public static final int    	BIT = 0;
	
	/** Identifies the type used to store pixel values. */
	public static final int    	INT8 = 1;
	
	/** Identifies the type used to store pixel values. */
	public static final int    	INT16 = 2;
	
	/** Identifies the type used to store pixel values. */
	public static final int    	INT32 = 3;
	
	/** Identifies the type used to store pixel values. */
	public static final int    	UINT8 = 4;
	
	/** Identifies the type used to store pixel values. */
	public static final int    	UINT16 = 5;
	
	/** Identifies the type used to store pixel values. */
	public static final int    	UINT32 = 6;
	
	/** Identifies the type used to store pixel values. */
	public static final int    	FLOAT = 7;
	
	/** Identifies the type used to store pixel values. */
	public static final int    	DOUBLE = 8;

	/** Minimum value assigned to the pixel type constants. */
	private static int  		MIN_PIX_TYPE = 1;
	
	/** Maximum value assigned to the pixel type constants. */
	private static int 			MAX_PIX_TYPE = 6;
	
	/** 
	 * Tells you how many bytes are used to store a pixel. 
	 * Indexed by type constants.
	 */
	private static int[]    	BYTES_PER_PIXEL = new int[MAX_PIX_TYPE]; 
	
	static {
		BYTES_PER_PIXEL[INT8] = BYTES_PER_PIXEL[UINT8] = 1;
		BYTES_PER_PIXEL[INT16] = BYTES_PER_PIXEL[UINT16] = 2;
		//TODO: add the other pixel types when we support them
	}

	/** The ID of the pixels data. */
	private int      			ID;
	
	/** 
	* The order in which pixels are stored. Must be one of the constants 
	* defined by this class.
	*/
	private int         		dimOrder;
	
	/** 
	* The type used to store pixel values. Must be one of the constants 
	* defined by this class.
	*/
	private int         		pixelType;
	
	/** 
	 * Whether or not multi-byte pixels values are stored in big endian order.
	 */
	private boolean     		isBigEndian;
    
	/** 
	* Creates a new object to deal with the image data.
 	*
 	* @param ID  			The id of the pixel data.
 	* @param dimOrder		The order in which pixels are stored. 
 	* 						Must be one of the constants defined by this class.  
 	* @param pixelType  	The type used to store pixel values. 
 	* 						Must be one of the constants defined by this class.
 	* @param isBigEndian	Whether or not multi-byte pixels values are stored 
 	* 						in big endian order.
 	*/
	DataSink(int ID, int dimOrder, int pixelType, boolean isBigEndian)
	{
		if (ID == 0) throw new IllegalArgumentException();
		if (dimOrder < MIN_DIM_ORDER  ||  MAX_DIM_ORDER < dimOrder)
			throw new IllegalArgumentException("Invalid dimension order");
		if (pixelType < MIN_PIX_TYPE  ||  MAX_PIX_TYPE < pixelType)
			throw new IllegalArgumentException("Invalid pixel type");
		this.ID = ID;
		this.dimOrder = dimOrder;
		this.pixelType = pixelType;
		this.isBigEndian = isBigEndian;
	}

	/** Retrieves the Pixels type */
	public int getType()
	{
		return pixelType;
	}
	
	/** Builds a plane2D. */
	public Plane2D getPlane2D(PlaneDef pDef, int index, int sizeX1, int sizeX2)
	{
		return new Plane2D(pDef, sizeX1, sizeX2);
	}

}

