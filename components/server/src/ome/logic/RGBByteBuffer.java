/*
 * ome.flame.RGBByteBuffer
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

package ome.logic;

import java.awt.image.DataBuffer;

import omeis.providers.re.RGBBuffer;


/** 
 * Creates a data buffer with three banks of type byte.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.3 $ $Date: 2005/06/28 11:08:53 $)
 * </small>
 * @since OME2.2
 */
class RGBByteBuffer
    extends DataBuffer
{
    private byte[][]    banks;
    
    RGBByteBuffer(RGBBuffer buf, int sizeX, int sizeY)
    {
        super(TYPE_BYTE, sizeX * sizeY, 3);
        banks = new byte[3][];
        banks[0] = buf.getRedBand();
        banks[1] = buf.getGreenBand();
        banks[2] = buf.getBlueBand();
    }
    
    /** 
     * Returns the requested data array element from the specified bank
     * as an integer.
     * 
     * @param bank The specified bank.
     * @param i The index of the requested data array element.
     * @see java.awt.image.DataBuffer#getElem(int, int)
     */
    public int getElem(int bank, int i) { return (banks[bank][i] & 0xFF); }

    /**
     * Sets the requested data array element in the specified bank
     * from the given integer.
     * @param bank The specified bank.
     * @param i The specified index into the data array.
     * @param val The data to set the element in the specified bank.
     * @see java.awt.image.DataBuffer#setElem(int, int, int)
     */
    public void setElem(int bank, int i, int val) 
    {
        banks[bank][i] = (byte) val;
    }

}
