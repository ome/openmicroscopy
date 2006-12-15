/*
 * ome.flame.RGBByteBuffer
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

import java.awt.image.DataBuffer;

import omeis.providers.re.RGBBuffer;

/**
 * Creates a data buffer with three banks of type byte.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/28 11:08:53 $) </small>
 * @since OME2.2
 */
class RGBByteBuffer extends DataBuffer {
    private byte[][] banks;

    RGBByteBuffer(RGBBuffer buf, int sizeX, int sizeY) {
        super(TYPE_BYTE, sizeX * sizeY, 3);
        banks = new byte[3][];
        banks[0] = buf.getRedBand();
        banks[1] = buf.getGreenBand();
        banks[2] = buf.getBlueBand();
    }

    /**
     * Returns the requested data array element from the specified bank as an
     * integer.
     * 
     * @param bank
     *            The specified bank.
     * @param i
     *            The index of the requested data array element.
     * @see java.awt.image.DataBuffer#getElem(int, int)
     */
    @Override
    public int getElem(int bank, int i) {
        return banks[bank][i] & 0xFF;
    }

    /**
     * Sets the requested data array element in the specified bank from the
     * given integer.
     * 
     * @param bank
     *            The specified bank.
     * @param i
     *            The specified index into the data array.
     * @param val
     *            The data to set the element in the specified bank.
     * @see java.awt.image.DataBuffer#setElem(int, int, int)
     */
    @Override
    public void setElem(int bank, int i, int val) {
        banks[bank][i] = (byte) val;
    }

}
