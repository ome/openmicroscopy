/*
 * org.openmicroscopy.shoola.env.rnd.data.WStackFiller
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
import java.io.IOException;
import java.io.InputStream;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.is.ImageServerException;
import org.openmicroscopy.shoola.env.data.PixelsService;
import org.openmicroscopy.shoola.env.data.PixelsServiceAdapter;
import org.openmicroscopy.shoola.util.concur.BufferWriteException;
import org.openmicroscopy.shoola.util.concur.ByteBufferFiller;

/** 
 * Retrieves a pixels stack at a given wavelength and timepoint.
 * Implements {@link ByteBufferFiller} so to retrieve the stack data in 
 * incremental steps.
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
class WStackFiller
    implements ByteBufferFiller
{
    
    /** Proxy to <i>OMEIS</i>. */
    private final PixelsServiceAdapter    omeis;  
    //TODO: just tmp hack, breaks I/F.
    
    /** Identifies the pixels set. */
    private final Pixels  pixelsID;
    
    /** The wavelength index that identifies the stack we want to retrieve. */
    private final int     w;
    
    /** The timepoint that identifies the stack we want to retrieve. */
    private final int     t;
    
    /** Size of a wavelength stack within the pixels set. */
    private final int     wStackSize;
    
    /** Endianness of the data we retrieve. */
    private final boolean bigEndian;
    
    /** The socket stream the write method will be working on. */
    private InputStream   wStream;
     
    /**
     * The amount of bytes that the {@link #write(byte[], int, int) write}
     * method has so far written into the buffer.
     */
    private int           bytesWritten;
    

    /**
     * Increases {@link #bytesWritten} by the amount of bytes lastly written
     * by the {@link #write(byte[], int, int) write} method.
     * 
     * @param writeLength Amount of bytes lastly written.
     * @throws BufferWriteException If stack overflow or underflow.
     */
    private void updateBytesWritten(int writeLength) 
        throws BufferWriteException
    {
        if (writeLength != -1) bytesWritten += writeLength;
        else  //Last call, check for underflow (should never happen).
            if (bytesWritten < wStackSize)
                throw new BufferWriteException(
                        "Pixels stack underflow [w="+w+", t="+t+"]: expected "+
                        wStackSize+" bytes, but retrieved "+bytesWritten+"."); 
        
        //Each time check for overflow (should never happen).
        if (wStackSize < bytesWritten)
            throw new BufferWriteException(
                    "Pixels stack overflow [w="+w+", t="+t+"]: expected "+
                    wStackSize+" bytes, but retrieved "+bytesWritten+".");
    }
    
    /**
     * Closes the {@link #wStream} so that the associated <i>HTTP</i>
     * connection can be released.
     */
    private void releaseWStream()
    {
        bytesWritten = 0;
        if (wStream == null) return;
        try {  //Release http connection.
            wStream.close();
        } catch (IOException ioe) {}
        wStream = null;
    }
    
    /**
     * Creates a new instance to retrieve the wavelength <code>w</code>
     * stack at timepoint <code>t</code>.
     * 
     * @param source    The proxy to <i>OMEIS</i>. Mustn't be <code>null</code>.
     * @param pixelsID  Identifies the pixels set. Mustn't be <code>null</code>.
     * @param w         Index of the wavelength.
     * @param t         Timepoint.
     * @param wStackSize The size of the wavelength stack.
     * @param bigEndian  Tells whether we should retrieve data in big-endian 
     *                      order (<code>true</code>) or little-endian 
     *                      (<code>false</code>).
     */
    WStackFiller(PixelsService source, Pixels pixelsID, 
            int w, int t, int wStackSize, boolean bigEndian)
    {
        if (source == null) throw new NullPointerException("No source.");
        if (pixelsID == null) throw new NullPointerException("No pixelsID.");
        omeis = (PixelsServiceAdapter) source;  //TODO: just tmp hack.
        this.pixelsID = pixelsID;
        this.w = w;
        this.t = t;
        this.wStackSize = wStackSize;
        this.bigEndian = bigEndian;
        bytesWritten = 0;
    }
    
    /**
     * Retrieves the specified data segment within the wavelength stack and
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
        int writeLength = -1;  //Assume an exc or this is last call.
        try {
            if (wStream == null)  //First time.
                wStream = omeis.getStackStream(pixelsID, w, t, bigEndian);
            int len = wStream.read(buffer, offset, length);
            updateBytesWritten(len);  //If exc, writeLength will stay -1.
            writeLength = len;  //Allows finally clause to work correctly.
        } catch (ImageServerException ise) {
            throw new BufferWriteException(
                    "Can't retrieve wavelength "+w+" stack at timepoint "+
                    t+".", ise);
        } catch (IOException ioe) {         
            throw new BufferWriteException(
                    "Can't retrieve wavelength "+w+" stack at timepoint "+
                    t+".", ioe);
        } catch (ArrayIndexOutOfBoundsException aiobe) {  
            //buffer overflow.  Should never happen b/c DataFetcher checks
            //whether the whole stack fits into buffer.
            throw new BufferWriteException(
                    "Pixels stack overflow [w="+w+", t="+t+"]: more than "+
                    (w*wStackSize+bytesWritten)+" bytes have been retrieved.");
        } finally {
            if (writeLength == -1) releaseWStream();
        }
        return writeLength;
    }
    
    /**
     * Returns the size of the wavelength stack within the pixels set.
     * 
     * @return See above.
     * @see ByteBufferFiller#getTotalLength()
     */
    public int getTotalLength() 
    {
        return wStackSize;
    }

}
