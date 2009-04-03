/*
 * org.openmicroscopy.shoola.util.concur.FakeByteBufferFiller
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

package org.openmicroscopy.shoola.util.concur;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Simulates a real {@link ByteBufferFiller}.
 * Simply writes {@link #totalLength} {@link #writeValue}s into the buffer.
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
public class FakeByteBufferFiller
    implements ByteBufferFiller
{
    
    private int     bytesWritten;
    private int     totalLength;
    private byte    writeValue;
    
    
    FakeByteBufferFiller(int totalLength, byte writeValue)
    {
        bytesWritten = 0;
        this.totalLength = totalLength;  //If <=0, write ret -1 from 1st call.
        this.writeValue = writeValue;
    }

    public int write(byte[] buffer, int offset, int length)
    {
        int bytesLeft = totalLength-bytesWritten, 
            bytesToWrite = Math.min(bytesLeft, length), i;
        if (bytesLeft < 1) return -1;
        for (i = 0; i < bytesToWrite; ++i)
            buffer[offset+i] = writeValue;
        bytesWritten += i;
        return i;
    }

    public int getTotalLength()
    {
        return totalLength;
    }

}
