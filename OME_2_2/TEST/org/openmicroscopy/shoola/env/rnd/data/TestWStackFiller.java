/*
 * org.openmicroscopy.shoola.env.rnd.data.TestWStackFiller
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
import java.io.IOException;

import junit.framework.TestCase;

//Application-internal dependencies
import org.openmicroscopy.ds.st.NullPixels;
import org.openmicroscopy.is.ImageServerException;
import org.openmicroscopy.shoola.util.concur.BufferWriteException;
import org.openmicroscopy.shoola.util.tests.common.MockInputStream;

/** 
 * Regular unit test for {@link WStackFiller}.
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
public class TestWStackFiller
    extends TestCase
{

    private static final int    W_STACK_SZ = 3;  //Size of the w-stack.
    
    private WStackFiller    target;  //Object under test.
    private MockPixelsServiceAdapter source;  //Linked to target.
    private MockInputStream stackStream;  //Returned by source.
    private NullPixels      pixelsID;  //Fake ID of the pixels set.
    private byte[]          buffer;  //Where all stackStreams reads go.
    
    
    //Sets up source to return stackStream or to throw e if != null.
    private void setUpSource(Exception e)
    {
        source.getStackStream(pixelsID, 0, 0, true, stackStream, e);
    }
    
    //nWrites: how many times stackStream.read() will be invoked.  Every
    //          write will be 1-byte long, except for the nWrite call if
    //          setLastWrite is true.  In this case, this last call will
    //          return -1 instead.  The value written to buffer is
    //          always 1.
    //e: if not null, it will be thrown by the last write.
    private void setUpWrites(int nWrites, boolean setLastWrite, Exception e)
    {
        //Do nWrites-1 calls that write 1 byte each, nWrites call has to 
        //return -1 if setLastWrite.  Do nothing if nWrites is 0.
        for (int i = 0; i < nWrites-1; ++i)
            stackStream.read(buffer, i, 1, 1, null);  //Also sets buf[i]=1.
        if (0 < nWrites) {
            if (setLastWrite) 
                stackStream.read(buffer, nWrites-1, 1, -1, e);
            else 
                stackStream.read(buffer, nWrites-1, 1, 1, e);
        }
    }
    
    //Makes sure buffer[0..expected-1]=1 and buffer[expected..W_STACK_SZ-1]=0.
    private void checkBytesWritten(int expected)
    {
        int i = 0;
        for (; i < expected; ++i)
            assertEquals("No write at buffer["+i+"].", 1, buffer[i]);
        for (; i < W_STACK_SZ; ++i)
            assertEquals("Write at buffer["+i+"].", 0, buffer[i]);
    }
    
    private void transitionMocksToVerificationMode()
    {
        source.activate();
        stackStream.activate();
    }
    
    private void ensureAllExpectedCallsWerePerformed()
    {
        source.verify();
        stackStream.verify();
    }
    
    public void setUp()
    {
        //Create mocks and other fixture.
        source = new MockPixelsServiceAdapter();
        stackStream = new MockInputStream();
        pixelsID = new NullPixels();
        buffer = new byte[W_STACK_SZ];
        
        //Create target -- w, t, and bigEndian are irrelevant.
        target = new WStackFiller(source, pixelsID, 
                                    0, 0, W_STACK_SZ, true);
    }
    
    public void testWStackFiller()
    {
        try {
            new WStackFiller(null, new NullPixels(), 0, 0, 10, true);
            fail("Constructor shouldn't accept null source.");
        } catch (NullPointerException npe) {
            //OK, expected.
        }
        try {
            new WStackFiller(source, null, 0, 0, 0, true);
            fail("Constructor shouldn't accept null pixelsID.");
        } catch (NullPointerException npe) {
            //OK, expected.
        }
    }
    
    public void testGetTotalLength()
    {
        assertEquals("Stack size passed to constructor is read only.", 
                W_STACK_SZ, target.getTotalLength());
    }
    
    public void testWriteWithImageServerException()
    {
        ImageServerException exc = new ImageServerException();
        setUpSource(exc);
        transitionMocksToVerificationMode();
        
        //Test.
        try {
            target.write(buffer, 0, 0);
            fail("Should wrap an ImageServerException into a "+
                    "BufferWriteException and rethrow.");
        } catch (BufferWriteException bwe) {
            //OK, expected but double-check we got the right exception:
            assertSame("Wrapped wrong exception.", exc, bwe.getCause());
        }
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void testWriteWithIOException()
    {
        IOException exc = new IOException();
        setUpSource(null);
        setUpWrites(1, false, exc);
        stackStream.close(exc);
        transitionMocksToVerificationMode();
        
        //Test.
        try {
            target.write(buffer, 0, 1);
            fail("Should wrap an IOException into a "+
                    "BufferWriteException and rethrow.");
        } catch (BufferWriteException bwe) {
            //OK, expected but double-check we got the right exception:
            assertSame("Wrapped wrong exception.", exc, bwe.getCause());
        }
        checkBytesWritten(0);
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void testWritePartial()
        throws BufferWriteException
    {
        setUpSource(null);
        setUpWrites(W_STACK_SZ-1, false, null);
        transitionMocksToVerificationMode();
        
        //Test.
        for (int i = 0; i < W_STACK_SZ-1; ++i)
            assertEquals("Returned wrong write length.", 
                    1, target.write(buffer, i, 1));
        checkBytesWritten(W_STACK_SZ-1);
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void testWriteComplete()
        throws BufferWriteException
    {
        setUpSource(null);
        setUpWrites(W_STACK_SZ+1, true, null);
        stackStream.close(null);
        transitionMocksToVerificationMode();
        
        //Test.
        for (int i = 0; i < W_STACK_SZ; ++i)
            assertEquals("Returned wrong write length.", 
                    1, target.write(buffer, i, 1));
        assertEquals("Failed to signal end of stream.", 
                -1, target.write(buffer, W_STACK_SZ, 1));
        checkBytesWritten(W_STACK_SZ);
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void testWriteStackUnderflow()
        throws BufferWriteException
    {
        setUpSource(null);
        setUpWrites(W_STACK_SZ-1, true, null);
        stackStream.close(null);
        transitionMocksToVerificationMode();
        
        //Test.
        for (int i = 0; i < W_STACK_SZ-2; ++i)
            assertEquals("Returned wrong write length.", 
                    1, target.write(buffer, i, 1));
        try {
            target.write(buffer, W_STACK_SZ-2, 1);
            fail("Failed to signal stack underflow.");
        } catch (BufferWriteException bwe) {
            //OK, expected.
        }
        checkBytesWritten(W_STACK_SZ-2);
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void testWriteStackOverflow()
        throws BufferWriteException
    {
        setUpSource(null);
        setUpWrites(W_STACK_SZ+1, false, null);
        stackStream.close(null);
        transitionMocksToVerificationMode();
        
        //Test.
        for (int i = 0; i < W_STACK_SZ; ++i)
            assertEquals("Returned wrong write length.", 
                    1, target.write(buffer, i, 1));
        try {
            target.write(buffer, W_STACK_SZ, 1);  //ArrayIndexOutOfBounds.
            fail("Failed to signal stack overflow.");
        } catch (BufferWriteException bwe) {
            //OK, expected.
        }
        checkBytesWritten(W_STACK_SZ);
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void testWriteStackOverflow2()
        throws BufferWriteException
    {
        setUpSource(null);
        buffer = new byte[W_STACK_SZ*2];  //Avoid ArrayIndexOutOfBounds.
        setUpWrites(W_STACK_SZ+1, false, null);
        stackStream.close(null);
        transitionMocksToVerificationMode();
        
        //Test.
        for (int i = 0; i < W_STACK_SZ; ++i)
            assertEquals("Returned wrong write length.", 
                    1, target.write(buffer, i, 1));
        try {
            target.write(buffer, W_STACK_SZ, 1);  //No ArrayIndexOutOfBounds.
            fail("Failed to signal stack overflow.");
        } catch (BufferWriteException bwe) {
            //OK, expected.
        }
        checkBytesWritten(W_STACK_SZ+1);
        
        ensureAllExpectedCallsWerePerformed();
    }
    
}
