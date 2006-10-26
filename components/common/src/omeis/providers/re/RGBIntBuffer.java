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
public class RGBIntBuffer extends RGBBuffer
{

    /** The serial number. */
	private static final long serialVersionUID = 5319594152389817324L;

	/** The data buffer storing pixel values. */
	private int[] dataBuf;
    
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
     * Creates a new 3-band packed integer buffer.
     * 
     * @param sizeX1 The number of pixels on the <i>X1</i>-axis.
     *               This is the <i>X</i>-axis in the case of an <i>XY</i>-plane
     *               or <i>XZ</i>-plane. Otherwise it is the 
     *               <i>Z</i>-axis &#151; <i>ZY</i>-plane.
     * @param sizeX2 The number of pixels on the <i>X2</i>-axis.
     *               This is the <i>Y</i>-axis in the case of an <i>XY</i>-plane
     *               or <i>ZY</i>-plane. Otherwise it is the <i>Z</i>-axis
     *               &#151; <i>XZ</i>-plane. 
     * @see #bands
     */
    public RGBIntBuffer(int sizeX1, int sizeX2)
    {
        this.sizeX1 = sizeX1;
        this.sizeX2 = sizeX2;
        dataBuf = new int[sizeX1*sizeX2];
    }
    
    /**
     * Sets the Red value for a particular pixel index.
     * @param index The index in the band array.
     * @param value The pixel value to set.
     */
    public void setRedValue(int index, int value)
    {
    	dataBuf[index] = dataBuf[index] | (value << 16);
    }
    
    /**
     * Sets the Green value for a particular pixel index.
     * @param index The index in the band array.
     * @param value The pixel value to set.
     */
    public void setGreenValue(int index, int value)
    {
    	dataBuf[index] = dataBuf[index] | (value << 8);
    }
    
    /**
     * Sets the Blue value for a particular pixel index.
     * @param index The index in the band array.
     * @param value The pixel value to set.
     */
    public void setBlueValue(int index, int value)
    {
    	dataBuf[index] = dataBuf[index] | value;
    }
    
    /**
     * Retrieves the Red value for a particular pixel index.
     * @param index The index in the band array.
     * @return The pixel value at the index.
     */
    public byte getRedValue(int index)
    {
    	return (byte) ((dataBuf[index] & 0x00FF0000) >> 16);
    }
    
    /**
     * Retrieves the Green value for a particular pixel index.
     * @param index The index in the band array.
     * @return The pixel value at the index.
     */
    public byte getGreenValue(int index)
    {
    	return (byte) ((dataBuf[index] & 0x0000FF00) >> 8);
    }
    
    /**
     * Retrieves the Blue value for a particular pixel index.
     * @param index The index in the band array.
     * @return The pixel value at the index.
     */
    public byte getBlueValue(int index)
    {
    	return (byte) (dataBuf[index] & 0x000000FF);
    }
    
    /**
     * Retrieves the data buffer that contains the pixel values.
     * @return an integer array containing pixel values.
     */
    public int[] getDataBuffer()
    {
    	return dataBuf;
    }
}
