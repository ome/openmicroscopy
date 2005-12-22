/*
 * omeis.providers.re.RGBBuffer
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

package omeis.providers.re;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Holds the data of an <i>RGB</i> image.
 * The image data is stored in three byte arrays, one for each color band.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.2 $ $Date: 2005/06/22 17:09:48 $)
 * </small>
 * @since OME2.2
 */
public class RGBBuffer
{

    /** Index of the red band in the image's data buffer. */
    private static final int    R_BAND = 0;
    
    /** Index of the green band in the image's data buffer. */
    private static final int    G_BAND = 1;
    
    /** Index of the blue band in the image's data buffer. */
    private static final int    B_BAND = 2;
    
    
    /**
     * Holds the image's data.
     * The byte array of index {@link #R_BAND} holds the data for the red band,
     * index {@link #G_BAND} holds the data for the green band, and index 
     * {@link #B_BAND} holds the data for the blue band.
     * The size of each array is the one specified to the constructor.
     */
    private byte[][]   bands;
    
    /** 
     * Number of pixels on the <i>X1</i>-axis.
     * This is the <i>X</i>-axis in the case of an <i>XY</i> or <i>XZ</i> plane.
     * Otherwise it is the <i>Z</i>-axis &#151; <i>ZY</i> plane.
     */
    private int         sizeX1;
    
    /** 
     * Number of pixels on the X2-axis.
     * This is the <i>Y</i>-axis in the case of an <i>XY</i> or <i>ZY</i> plane.
     * Otherwise it is the <i>Z</i>-axis &#151; <i>XZ</i> plane. 
     */
    private int         sizeX2;
    
    
    /**
     * Creates a new 3-band buffer.
     * 
     * @param sizeX1 Number of pixels on the <i>X1</i>-axis.
     *               This is the <i>X</i>-axis in the case of an <i>XY</i> or 
     *               <i>XZ</i> plane.  Otherwise it is the <i>Z</i>-axis &#151;
     *               <i>ZY</i> plane.
     * @param sizeX2 Number of pixels on the X2-axis.
     *               This is the <i>Y</i>-axis in the case of an <i>XY</i> or 
     *               <i>ZY</i> plane.  Otherwise it is the <i>Z</i>-axis &#151;
     *               <i>XZ</i> plane. 
     * @see #bands
     */
    public RGBBuffer(int sizeX1, int sizeX2)
    {
        this.sizeX1 = sizeX1;
        this.sizeX2 = sizeX2;
        bands = new byte[3][];
        for (int i = 0; i < 3; ++i)
            bands[i] = new byte[sizeX1*sizeX2];
    }
    
    /**
     * Returns the data buffer for the red band.
     * 
     * @return See above.
     */
    public byte[] getRedBand() { return bands[R_BAND]; }
    
    /**
     * Returns the data buffer for the green band.
     * 
     * @return See above.
     */
    public byte[] getGreenBand() { return bands[G_BAND]; }
    
    /**
     * Returns the data buffer for the blue band.
     * 
     * @return See above.
     */
    public byte[] getBlueBand() { return bands[B_BAND]; }
    
    /**
     * Returns the number of pixels on the <i>X1</i>-axis.
     * This is the <i>X</i>-axis in the case of an <i>XY</i> or <i>XZ</i> plane.
     * Otherwise it is the <i>Z</i>-axis &#151; <i>ZY</i> plane.
     * 
     * @return The number of pixels on the <i>X1</i>-axis.
     */
    public int getSizeX1() { return sizeX1; }
    
    /**
     * Returns the number of pixels on the X2-axis.
     * This is the <i>Y</i>-axis in the case of an <i>XY</i> or <i>ZY</i> plane.
     * Otherwise it is the <i>Z</i>-axis &#151; <i>XZ</i> plane.
     *  
     * @return The number of pixels on the <i>X2</i>-axis.
     */
    public int getSizeX2() { return sizeX2; }
    
}
