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
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.is.ImageServerException;
import org.openmicroscopy.shoola.env.data.PixelsService;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;

/** 
 * Encapsulates access to the image raw data. 
 * Contains the logic to interpret a linear byte array as a 5D array. 
 * Knows how to extract a 2D-plane from the 5D array, but delegates to the 
 * specified 2D-Plane the retrieval of pixel values. 
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
	
	/**
	 * Maps a pixel type string identifier to the corresponding constant
	 * defined by this class.
	 */
	private static Map			pixelTypesMap;
	static {
		pixelTypesMap = new HashMap();
		pixelTypesMap.put("BIT", new Integer(BIT));
		pixelTypesMap.put("INT8", new Integer(INT8));
		pixelTypesMap.put("INT16", new Integer(INT16));
		pixelTypesMap.put("INT32", new Integer(INT32));
		pixelTypesMap.put("UINT8", new Integer(UINT8));
		pixelTypesMap.put("UINT16", new Integer(UINT16));
		pixelTypesMap.put("UINT32", new Integer(UINT32));
		pixelTypesMap.put("FLOAT", new Integer(FLOAT));
		pixelTypesMap.put("DOUBLE", new Integer(DOUBLE));
	}
	
	private static final boolean	BIG_ENDIAN = true;
	
	/**
	 * Utility method to convert a pixel type string-identifier into the
	 * corresponding constant defined by this class.
	 * 
	 * @param type	The pixel type.  Must be a valid identifier (one out of
	 * 				"BIT", "INT8", etc.) defined by the <i>OME</i> spec.
	 * @return
	 */
	public static int getPixelTypeID(String type)
	{
		if (type == null)	throw new NullPointerException("No type.");
		Integer id = (Integer) pixelTypesMap.get(type.toUpperCase());
		if (id == null)
			throw new IllegalArgumentException("Unsupported pixel type: "+type);
		return id.intValue();
	}
	

	/** The id of the pixels set. */
	private Pixels      			pixelsID;
	//TODO: this should be turned into int!
	
	/**
	 * The type used to store pixel values. One of the constants defined by
	 * this class.
	 */
	private int         		pixelType;
    
    private PixelsDimensions 	dims;
    private PlaneDef			curPlaneDef;
    private PixelsService 		source;
    
    private byte[][]			stack;
    
    
    
    private void fillStack(int t)
		throws DataSourceException
    {
    	int stackSize = dims.sizeX * dims.sizeY * 
    					dims.sizeZ * BYTES_PER_PIXEL[pixelType];
    	stack = new byte[dims.sizeW][];
    	try {
			for (int w = 0; w < dims.sizeW; ++w) {
				stack[w] = source.getStack(pixelsID, w, t, BIG_ENDIAN);
				if (stack[w].length != stackSize)
					throw new DataSourceException(
						"Wrong stack size: "+stackSize+"..."+stack[w].length);
			}	
		} catch (ImageServerException ise) {
			throw new DataSourceException("Can't retrieve pixels data.", ise);
		}
    }
    
	/**
	 * Creates a new object to deal with the image data.
	 * 
	 * @param pixelsID		The id of the pixels set.
	 * @param pixelType		The type used to store pixel values. 
	 * 						Must be one of the constants defined by this class.
	 * @param dims			The pixels dimensions.
	 * @param source		The gateway to the raw data.
	 */
	public DataSink(Pixels pixelsID, int pixelType, PixelsDimensions dims, 
													PixelsService source)
	{
		if (pixelType < MIN_PIX_TYPE  ||  MAX_PIX_TYPE < pixelType)
			throw new IllegalArgumentException("Invalid pixel type");
		if (dims == null)	throw new NullPointerException("No dimensions.");
		if (source == null)	throw new NullPointerException("No source.");
		this.pixelsID = pixelsID;
		this.pixelType = pixelType;
		this.dims = dims;
		this.curPlaneDef = null;
		this.source = source;
		stack = new byte[dims.sizeW][];
	}

	/** Retrieves the Pixels type */
	public int getType()
	{
		return pixelType;
	}
	
	/** Builds a plane2D. */
	public Plane2D getPlane2D(PlaneDef pDef, int w)
		throws DataSourceException
	{
		if (curPlaneDef == null || curPlaneDef.getT() != pDef.getT())
			fillStack(pDef.getT());
		curPlaneDef = pDef;
		BytesConverter strategy = 
							BytesConverter.getConverter(pixelType, BIG_ENDIAN);
		return createPlane(stack[w], strategy);
	}
	
	private Plane2D createPlane(byte[] wavelengthStack, BytesConverter strategy)
	{
		Plane2D plane = null;
		switch (curPlaneDef.getSlice()) {
			case PlaneDef.XY:
				plane = new XYPlane(curPlaneDef, dims, 
									BYTES_PER_PIXEL[pixelType],
									wavelengthStack, strategy);
				break;
			case PlaneDef.XZ:
				plane = new XZPlane(curPlaneDef, dims, 
									BYTES_PER_PIXEL[pixelType],
									wavelengthStack, strategy);
				break;
			case PlaneDef.YZ:
				plane = new YZPlane(curPlaneDef, dims, 
									BYTES_PER_PIXEL[pixelType],
									wavelengthStack, strategy);
		}
		return plane;
	}

}

