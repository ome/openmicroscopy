/*
 * omeis.io.PixelsHeader
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

package tmp;

import java.util.HashMap;
import java.util.Map;

import ome.model.enums.PixelsType;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Holds metadata about a given pixels set.
 * An <i>OMEIS</i> repository file contains both the 5D pixels array and some 
 * metadata about this pixels set that was associated to an <i>OME</i> Image.
 * This class has fields to hold that metadata. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.2 $ $Date: 2005/06/17 17:47:57 $)
 * </small>
 * @since OME2.2
 */
public class PixelsConstants
{
    
    /** 
     * Identifies the <i>BIT</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    public static final int     BIT = 0;
    
    /** 
     * Identifies the <i>INT8</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    public static final int     INT8 = 1;
    
    /** 
     * Identifies the <i>INT16</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    public static final int     INT16 = 2;
    
    /** 
     * Identifies the <i>INT32</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    public static final int     INT32 = 3;
    
    /** 
     * Identifies the <i>UINT8</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    public static final int     UINT8 = 4;
    
    /** 
     * Identifies the <i>UINT16</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    public static final int     UINT16 = 5;
    
    /** 
     * Identifies the <i>UINT32</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    public static final int     UINT32 = 6;
    
    /** 
     * Identifies the <i>FLOAT</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    public static final int     FLOAT = 7;
    
    /** 
     * Identifies the <i>DOUBLE</i> data type used to store pixel values,
     * as per <i>OME</i> spec. 
     */
    public static final int     DOUBLE = 8;
    
    /** Human readable pixel type. */
    private static String[] pixelsTypes;
    static {
        pixelsTypes = new String[9];
        pixelsTypes[BIT] = "BIT";
        pixelsTypes[INT8] = "INT8";
        pixelsTypes[INT16] = "INT16";
        pixelsTypes[INT32] = "INT32";
        pixelsTypes[UINT8] = "UINT8";
        pixelsTypes[UINT16] = "UINT16";
        pixelsTypes[UINT32] = "UINT32";
        pixelsTypes[FLOAT] = "FLOAT";
        pixelsTypes[DOUBLE] = "DOUBLE";
    }
    
    private static Map pixelMap = new HashMap();
    static { // TODO candidate for enum
        pixelMap.put("BIT",Integer.valueOf(BIT));
        pixelMap.put("INT8",Integer.valueOf(INT8));
        pixelMap.put("INT16",Integer.valueOf(INT16));
        pixelMap.put("INT32",Integer.valueOf(INT32));
        pixelMap.put("UINT8",Integer.valueOf(UINT8));
        pixelMap.put("UINT16",Integer.valueOf(UINT16));
        pixelMap.put("UINT32",Integer.valueOf(UINT32));
        pixelMap.put("FLOAT",Integer.valueOf(FLOAT));
        pixelMap.put("DOUBLE",Integer.valueOf(DOUBLE));
    }
    
    /**
     * Returns the human readable pixel type.
     * 
     * @param typeID    The id of the pixel type. One of constants defined 
     *                  by this class.
     * @return See above.
     */
    public static String getPixelType(int typeID)
    {
        return pixelsTypes[typeID];
    }
    
    public static int getPixelTypeFromString(String typeString){
        return ((Integer) pixelMap.get(typeString)).intValue();
    }
    
    public static int convertPixelType(PixelsType type)
    {
        return ((Integer)pixelMap.get(type.getValue())).intValue();
    }
}
