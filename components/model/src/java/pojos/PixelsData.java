/*
 * pojos.PixelsData
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

package pojos;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * The data that makes up an <i>OME</i> Pixels object along with a back pointer
 * to the Image that owns this Pixels.
 * A Pixels object represents a 5D raw data array that stores the Image pixels.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class PixelsData
    implements DataObject
{
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"BIT"</code> string identifier. 
     */
    public static final int     BIT_TYPE = 0;
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"INT8"</code> string identifier. 
     */
    public static final int     INT8_TYPE = 1;
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"INT16"</code> string identifier. 
     */
    public static final int     INT16_TYPE = 2;
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"INT32"</code> string identifier. 
     */
    public static final int     INT32_TYPE = 3;
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"UINT8"</code> string identifier. 
     */
    public static final int     UINT8_TYPE = 4;
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"UINT16"</code> string identifier. 
     */
    public static final int     UINT16_TYPE = 5;
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"UINT32"</code> string identifier. 
     */
    public static final int     UINT32_TYPE = 6;
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"FLOAT"</code> string identifier. 
     */
    public static final int     FLOAT_TYPE = 7;
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"DOUBLE"</code> string identifier. 
     */
    public static final int     DOUBLE_TYPE = 8;
    
    
    /** The Pixels ID. */
    public int     id;
    
    /** The ID used by <i>OMEIS</i> to identify these Pixels. */
    public long    imageServerID;
    
    /** The URL of the <i>OMEIS</i> instance that manages these Pixels. */
    public String  imageServerURL;
    
    /** 
     * The X dimension of the 5D data array.
     * That is, the number of pixels along the X-axis in a 2D plane. 
     */
    public int     sizeX;
    
    /** 
     * The Y dimension of the 5D data array.
     * That is, the number of pixels along the Y-axis in a 2D plane. 
     */
    public int     sizeY;
    
    /** 
     * The Z dimension of the 5D data array.
     * That is, the number of focal planes in the 3D stack. 
     */
    public int     sizeZ;
    
    /** 
     * The C dimension of the 5D data array.
     * That is, the number of wavelengths.
     */
    public int     sizeC;
    
    /** 
     * The T dimension of the 5D data array.
     * That is, the number of timepoints. 
     */ 
    public int     sizeT;
    
    /** The X-size of a pixel in microns. */
    public double  pixelSizeX;
    
    /** The Y-size of a pixel in microns. */
    public double  pixelSizeY;
    
    /** The Z-size of a pixel in microns. */
    public double  pixelSizeZ;
    
    /** One of the Pixels type identifiers defined by this class. */
    public int     pixelType;
    
    /** The Image these Pixels belong to. */
    public ImageData image;
    
}
