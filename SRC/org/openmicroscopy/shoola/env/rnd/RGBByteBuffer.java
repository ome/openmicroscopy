/*
 * org.openmicroscopy.shoola.env.rnd.RGBByteBuffer
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.rnd;


//Java imports
import java.awt.image.DataBuffer;

//Third-party libraries

//Application-internal dependencies
import omero.romio.RGBBuffer;

/** 
 * Creates a data buffer with three banks of type byte.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class RGBByteBuffer
    extends DataBuffer
{

    /** The number of banks in this DataBuffer. */
    private byte[][]    banks;
    
    /**
     * Creates a new instance.
     * 
     * @param buf   The remote DataBuffer. Mustn't be <code>null</code>.
     */
    RGBByteBuffer(RGBBuffer buf)
    {
        super(TYPE_BYTE, buf.sizeX1, buf.sizeX2, 3);
        banks = new byte[3][];
        banks[0] = buf.bands[omero.romio.RedBand.value];
        banks[1] = buf.bands[omero.romio.GreenBand.value];
        banks[2] = buf.bands[omero.romio.BlueBand.value];
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
