/*
 * omeis.providers.re.RGBBuffer
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re;

import java.util.Arrays;

/**
 * Holds the data of an <i>RGBA</i> image. The image data is stored as a 
 * packed integer in the format RGBA. Note that Java color objects are 
 * stored in ARGB.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class RGBAIntBuffer extends RGBBuffer {

    /** The serial number. */
    private static final long serialVersionUID = 5319594152383817324L;

    /** The data buffer storing pixel values. */
    private int[] dataBuf;

    /**
     * Number of pixels on the <i>X1</i>-axis. This is the <i>X</i>-axis in
     * the case of an <i>XY</i> or <i>XZ</i> plane. Otherwise it is the <i>Z</i>-axis
     * &#151; <i>ZY</i> plane.
     */
    private int sizeX1;

    /**
     * Number of pixels on the X2-axis. This is the <i>Y</i>-axis in the case
     * of an <i>XY</i> or <i>ZY</i> plane. Otherwise it is the <i>Z</i>-axis
     * &#151; <i>XZ</i> plane.
     */
    private int sizeX2;

    /**
     * Creates a new 4-band packed integer buffer.
     * 
     * @param sizeX1
     *            The number of pixels on the <i>X1</i>-axis. This is the <i>X</i>-axis
     *            in the case of an <i>XY</i>-plane or <i>XZ</i>-plane.
     *            Otherwise it is the <i>Z</i>-axis &#151; <i>ZY</i>-plane.
     * @param sizeX2
     *            The number of pixels on the <i>X2</i>-axis. This is the <i>Y</i>-axis
     *            in the case of an <i>XY</i>-plane or <i>ZY</i>-plane.
     *            Otherwise it is the <i>Z</i>-axis &#151; <i>XZ</i>-plane.
     * @see #bands
     */
    public RGBAIntBuffer(int sizeX1, int sizeX2) {
        this.sizeX1 = sizeX1;
        this.sizeX2 = sizeX2;
        dataBuf = new int[sizeX1 * sizeX2];
    }

    /**
     * Sets the Red value for a particular pixel index.
     * 
     * @param index
     *            The index in the band array.
     * @param value
     *            The pixel value to set.
     */
    @Override
    public void setRedValue(int index, int value) {
        dataBuf[index] = dataBuf[index] | value << 24;
    }

    /**
     * Sets the Green value for a particular pixel index.
     * 
     * @param index
     *            The index in the band array.
     * @param value
     *            The pixel value to set.
     */
    @Override
    public void setGreenValue(int index, int value) {
        dataBuf[index] = dataBuf[index] | value << 16;
    }

    /**
     * Sets the Blue value for a particular pixel index.
     * 
     * @param index
     *            The index in the band array.
     * @param value
     *            The pixel value to set.
     */
    @Override
    public void setBlueValue(int index, int value) {
        dataBuf[index] = dataBuf[index] | value << 8;
    }

    /**
     * Retrieves the Red value for a particular pixel index.
     * 
     * @param index
     *            The index in the band array.
     * @return The pixel value at the index.
     */
    @Override
    public byte getRedValue(int index) {
        return (byte) ((dataBuf[index] & 0xFF000000) >> 24);
    }

    /**
     * Retrieves the Green value for a particular pixel index.
     * 
     * @param index
     *            The index in the band array.
     * @return The pixel value at the index.
     */
    @Override
    public byte getGreenValue(int index) {
        return (byte) ((dataBuf[index] & 0x00FF0000) >> 16);
    }

    /**
     * Retrieves the Blue value for a particular pixel index.
     * 
     * @param index
     *            The index in the band array.
     * @return The pixel value at the index.
     */
    @Override
    public byte getBlueValue(int index) {
        return (byte) ((dataBuf[index] & 0x0000FF00)>>8);
    }

    /**
     * Retrieves the data buffer that contains the pixel values.
     * 
     * @return an integer array containing pixel values.
     */
    public int[] getDataBuffer() {
        return dataBuf;
    }
    
    @Override
    public void zero() {
    	Arrays.fill(dataBuf, 0);
    }
}
