/*
 * org.openmicroscopy.shoola.env.rnd.data.DataSink
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
import org.openmicroscopy.shoola.env.data.PixelsService;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.util.concur.AsyncByteBuffer;
import org.openmicroscopy.shoola.util.concur.tasks.CmdProcessor;

/** 
 * Encapsulates access to the image raw data. 
 * Contains the logic to interpret a linear byte array as a 5D array. 
 * Knows how to extract a 2D-plane from the 5D array, but delegates to the 
 * specified 2D-Plane the retrieval of pixel values. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class DataSink
{
    
    /** Identifies the type used to store pixel values. */
    public static final int     BIT = 0;
    
    /** Identifies the type used to store pixel values. */
    public static final int     INT8 = 1;
    
    /** Identifies the type used to store pixel values. */
    public static final int     INT16 = 2;
    
    /** Identifies the type used to store pixel values. */
    public static final int     INT32 = 3;
    
    /** Identifies the type used to store pixel values. */
    public static final int     UINT8 = 4;
    
    /** Identifies the type used to store pixel values. */
    public static final int     UINT16 = 5;
    
    /** Identifies the type used to store pixel values. */
    public static final int     UINT32 = 6;
    
    /** Identifies the type used to store pixel values. */
    public static final int     FLOAT = 7;
    
    /** Identifies the type used to store pixel values. */
    public static final int     DOUBLE = 8;

    /** Minimum value assigned to the pixel type constants. */
    private static int          MIN_PIX_TYPE = 1;
    
    /** Maximum value assigned to the pixel type constants. */
    private static int          MAX_PIX_TYPE = 9;
    
    /** 
     * Tells you how many bytes are used to store a pixel. 
     * Indexed by type constants.
     */
    private static int[]        BYTES_PER_PIXEL = new int[MAX_PIX_TYPE]; 
    static {
        BYTES_PER_PIXEL[INT8] = BYTES_PER_PIXEL[UINT8] = 1;
        BYTES_PER_PIXEL[INT16] = BYTES_PER_PIXEL[UINT16] = 2;
        BYTES_PER_PIXEL[UINT32] = BYTES_PER_PIXEL[FLOAT] = 4;
        BYTES_PER_PIXEL[DOUBLE] = 8;
    }
    
    /**
     * Maps a pixel type string identifier to the corresponding constant
     * defined by this class.
     */
    private static Map          pixelTypesMap;
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
    
    /** Tells the endianness of the pixels. */
    static final boolean    BIG_ENDIAN = true;
    
    
    /**
     * Utility method to convert a pixel type string-identifier into the
     * corresponding constant defined by this class.
     * 
     * @param type  The pixel type.  Must be a valid identifier (one out of
     *              "BIT", "INT8", etc.) defined by the <i>OME</i> spec.
     * @return  The corresponding integer constant defined by this class.
     */
    public static int getPixelTypeID(String type)
    {
        if (type == null)   throw new NullPointerException("No type.");
        Integer id = (Integer) pixelTypesMap.get(type.toUpperCase());
        if (id == null)
            throw new IllegalArgumentException("Unsupported pixel type: "+type);
        return id.intValue();
    }
    

    /** The id of the pixels set. */
    private Pixels              pixelsID;
    
    /**
     * The type used to store pixel values. 
     * One of the constants defined by this class.
     */
    private int                 pixelType;
    
    /** Size of the pixels set. */
    private PixelsDimensions    pixDims;
    
    /** Proxy to the remote pixels source. */
    private PixelsService       source;
    
    /** 
     * Retrieves planes and stacks.  
     * If a whole stack fits into into its internal buffer, then the
     * stack at the current timepoint is cached into that buffer. 
     */
    private DataFetcher         dataFetcher;
    
    
    /**
     * Factory method to fetch plane data and create an object to access it.
     * 
     * @param pDef Defines the plane.  Mustn't be <code>null</code>.
     * @param w The wavelength at which data is to be fetched.
     * @param strategy To transform bytes into pixels values.
     * @return A plane 2D object that encapsulates the actual plane pixels.
     * @throws DataSourceException If an error occurs while retrieving the
     *          plane data from the pixels source.  In the case of XZ/ZY planes
     *          this exception can be thrown if a pixels stack is too big to
     *          be cached locally.
     */
    private Plane2D createPlane(PlaneDef pDef, int w, BytesConverter strategy)
        throws DataSourceException
    {
        Plane2D plane = null;
        switch (pDef.getSlice()) {
            case PlaneDef.XY:
                plane = new XYPlane(pDef, pixDims, 
                                    BYTES_PER_PIXEL[pixelType],
                                    dataFetcher.getPlaneData(
                                            pDef.getZ(), w, pDef.getT()), 
                                    strategy);
                break;
            case PlaneDef.XZ:
                if (!dataFetcher.canCacheStack())
                    throw new DataSourceException(
                                    "The stack exceeds the memory capability.");
                plane = new XZPlane(pDef, pixDims, 
                                    BYTES_PER_PIXEL[pixelType],
                                    dataFetcher.getStackData(w, pDef.getT()), 
                                    strategy);
                break;
            case PlaneDef.ZY:
                if (!dataFetcher.canCacheStack())
                    throw new DataSourceException(
                                    "The stack exceeds the memory capability.");
                plane = new ZYPlane(pDef, pixDims, 
                                    BYTES_PER_PIXEL[pixelType],
                                    dataFetcher.getStackData(w, pDef.getT()), 
                                    strategy);
        }
        return plane;
    }
    
    /**
     * Creates a new object to deal with the image data.
     * All references passed in mustn't be <code>null</code>.
     * 
     * @param pixelsID      The id of the pixels set.
     * @param pixelType     The type used to store pixel values. 
     *                      Must be one of the constants defined by this class.
     * @param dims          The pixels dimensions.
     * @param source        The gateway to the raw data.
     * @param stackBuffer   The buffer to cache the stack data.
     * @param cmdProcessor  To perform asynchronous data retieval.
     */
    public DataSink(Pixels pixelsID, int pixelType, PixelsDimensions dims, 
                    PixelsService source, AsyncByteBuffer stackBuffer,
                    CmdProcessor cmdProcessor)
    {
        if (pixelType < MIN_PIX_TYPE || MAX_PIX_TYPE < pixelType)
            throw new IllegalArgumentException("Invalid pixel type");
        if (dims == null)   throw new NullPointerException("No dimensions.");
        if (source == null) throw new NullPointerException("No source.");

        this.pixelsID = pixelsID;
        this.pixelType = pixelType;
        this.pixDims = dims;
        this.source = source;
        
        dataFetcher = new DataFetcher(this, stackBuffer, cmdProcessor);
    }
    
    /**
     * Extracts a 2D plane from the pixels set this object is working for.
     * 
     * @param pDef Defines the plane.  Mustn't be <code>null</code>.
     * @param w The wavelength at which data is to be fetched.
     * @return A plane 2D object that encapsulates the actual plane pixels.
     * @throws DataSourceException If an error occurs while retrieving the
     *          plane data from the pixels source.  In the case of XZ/ZY planes
     *          this exception can be thrown if a pixels stack is too big to
     *          be cached locally.
     */
    public Plane2D getPlane2D(PlaneDef pDef, int w)
        throws DataSourceException
    {
        if (pDef == null) 
            throw new NullPointerException("No plane definition.");
        BytesConverter strategy = 
                        BytesConverter.getConverter(pixelType, BIG_ENDIAN);
        return createPlane(pDef, w, strategy);
    }

    /**
     * Returns the id of the pixels set this object is working for.
     * 
     * @return See above.
     */
    public Pixels getPixelsID() { return pixelsID; }
    
    /**
     * Returns the dimensions of the pixels set this object is working for.
     * 
     * @return See above
     */
    public PixelsDimensions getPixDims() { return pixDims; }
    
    /** 
     * Tells how many bytes are used for each pixel within the pixels set
     * this object is working for.
     * 
     * @return See above. 
     */
    public int getBytesPerPixel() { return BYTES_PER_PIXEL[pixelType]; }
    
    /**
     * Returns a proxy to the remote pixels source.
     * 
     * @return See above.
     */
    public PixelsService getSource() { return source; }
    
}

