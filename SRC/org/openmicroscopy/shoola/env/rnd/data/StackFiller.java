/*
 * org.openmicroscopy.shoola.env.rnd.data.StackFiller
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

//Third-party libraries

//Application-internal dependencies
import java.io.IOException;

import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.is.ImageServerException;
import org.openmicroscopy.shoola.env.data.PixelsService;
import org.openmicroscopy.shoola.util.concur.BufferWriteException;
import org.openmicroscopy.shoola.util.concur.ByteBufferFiller;

/** 
 * Retrieves a whole pixels stack (all wavelengths) at a given timepoint.
 * Implements {@link ByteBufferFiller} (so to retrieve the stack data in 
 * incremental steps) by composing single {@link WStackFiller}s &#151; one
 * for each wavelength stack. 
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
class StackFiller
    implements ByteBufferFiller
{

    /** Number of wavelengths in the pixels set. */
    private final int             sizeW;
    
    /** Size of a wavelength stack within the pixels set. */
    private final int             wStackSize;
    
    /** 
     * The components of this composite &#151; <code>fillers[i]</code> fetches
     * the stack of wavelength <code>i</code>.
     */
    private final WStackFiller[]  fillers;
    
    /** The wavelength whose data is currently being retrieved. */
    private int     curW;
    
    
    /**
     * Creates a new instance to retrieve the stack at timepoint <code>t</code>.
     * 
     * @param source    The proxy to <i>OMEIS</i>. Mustn't be <code>null</code>.
     * @param pixelsID  Identifies the pixels set. Mustn't be <code>null</code>.
     * @param sizeW     Number of wavelengths.
     * @param t         Timepoint.
     * @param wStackSize The size of a single wavelength stack.
     * @param bigEndian  Tells whether we should retrieve data in big-endian 
     *                      order (<code>true</code>) or little-endian 
     *                      (<code>false</code>).
     */
    StackFiller(PixelsService source, Pixels pixelsID, 
            int sizeW, int t, int wStackSize, boolean bigEndian)
    {
        this.sizeW = sizeW;
        this.wStackSize = wStackSize;
        fillers = new WStackFiller[sizeW];
        for (int w = 0; w < sizeW; ++w)
            fillers[w] = new WStackFiller(source, pixelsID, w, 
                                            t, wStackSize, bigEndian);
        curW = 0;
    }
    
    /**
     * Retrieves the specified data segment within the pixels stack and
     * writes it into <code>buffer</code>.
     * Sticks to the contract specified by {@link ByteBufferFiller}.
     * 
     * @param buffer    The buffer to write to.
     * @param offset    Start of the data segment.
     * @param length    Maximum length of the data segment.
     * @return  The number of bytes actually written or <code>-1</code> to 
     *          indicate the end of the stream.
     * @throws BufferWriteException If a stack overflow or underflow occurs.
     *          Also thrown in the case of an {@link ImageServerException} 
     *          or {@link IOException}.  The original exception is set in 
     *          the cause field.
     * @see ByteBufferFiller#write(byte[], int, int)
     */
    public int write(byte[] buffer, int offset, int length)
        throws BufferWriteException
    {
        int writeLength = -1;
        if (curW < sizeW) {
            writeLength = fillers[curW].write(buffer, offset, length);
            if (writeLength == -1) {
                ++curW;
                writeLength = 0;
            }
        }
        return writeLength;
    }
    
    /**
     * Returns the size of a whole stack (all wavelengths) within the
     * pixels set.
     * 
     * @return See above.
     * @see ByteBufferFiller#getTotalLength()
     */
    public int getTotalLength() 
    {
        return (wStackSize * sizeW);
    }
    
}
