/*
 * org.openmicroscopy.shoola.env.rnd.data.TestStackFiller
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
import junit.framework.TestCase;

//Application-internal dependencies
import org.openmicroscopy.ds.st.NullPixels;
import org.openmicroscopy.shoola.util.concur.BufferWriteException;
import org.openmicroscopy.shoola.util.tests.common.MockInputStream;

/** 
 * Regular unit test for {@link StackFiller}.
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
public class TestStackFiller
    extends TestCase
{
    
    private StackFiller     target;  //Object under test.
    private MockPixelsServiceAdapter source;  //Linked to target.
    private MockInputStream stackStream;  //Returned by source.
    private NullPixels      pixelsID;  //Fake ID of the pixels set.
    private byte[]          buffer;  //Where all stackStreams reads go.
    
    
    //Call this method at the beginning to each test method to:
    //+ Create a target with sizeW wavelengths and a payload of 
    //   wStackSize*sizeW.
    //+ Create a buffer of wStackSize*sizeW bytes.
    //+ Set up sizeW calls to source that return stackStream.
    private void setUpBaseEnv(int sizeW, int wStackSize)
    {
        //Create target -- t, and bigEndian are irrelevant.
        target = new StackFiller(source, pixelsID, sizeW,
                                    0, wStackSize, true);
        buffer = new byte[wStackSize*sizeW];
        for (int w = 0; w < sizeW; ++w)
            source.getStackStream(pixelsID, w, 0, true, stackStream, null);
    }
    
    //Makes sure buffer[0..expected-1]=1 and buffer[expected..W_STACK_SZ-1]=0.
    private void checkBytesWritten(int expected)
    {
        int i = 0;
        for (; i < expected; ++i)
            assertEquals("No write at buffer["+i+"].", 1, buffer[i]);
        for (; i < buffer.length; ++i)
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
        
        //target and buffer depend on the number of wavelengths a test is
        //simulating, so will be created within the test method by calling
        //setUpBaseEnv(sizeW, wStackSize).
        buffer = null;
        target = null;
    }
    
    public void testGetTotalLength1W()
    {
        setUpBaseEnv(1, 15);  //1 wave with a 15-byte stack.
        assertEquals("Calculated wrong stack size.", 
                15, target.getTotalLength());
    }
    
    public void testGetTotalLength2W()
    {
        setUpBaseEnv(2, 15);  //2 waves with a 15-byte stack each.
        assertEquals("Calculated wrong stack size.", 
                15*2, target.getTotalLength());
    }
    
    public void testWrite1W()
        throws BufferWriteException
    {
        setUpBaseEnv(1, 4);  //1 wave with a 4-byte stack.
        
        //Set up expected writes made by WStackFiller component and close.
        stackStream.read(buffer, 0, 1, 1, null);  //Also sets buf[0]=1.
        stackStream.read(buffer, 1, 2, 2, null);  //buf[1]=1, buf[2]=1.
        stackStream.read(buffer, 3, 1, 1, null);  //buf[3]=1.
        stackStream.read(buffer, 4, 15, -1, null);
        stackStream.close(null);
        
        transitionMocksToVerificationMode();
        
        //Test.
        assertEquals("Returned wrong write length.", 
                target.write(buffer, 0, 1), 1);
        assertEquals("Returned wrong write length.", 
                target.write(buffer, 1, 2), 2);
        assertEquals("Returned wrong write length.", 
                target.write(buffer, 3, 1), 1);
        assertEquals("Failed to signal end of w-stream.", 
                0, target.write(buffer, 4, 15));
        assertEquals("Failed to signal end of stream.", 
                -1, target.write(buffer, 4, 15));
        checkBytesWritten(4);
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void testWrite2W()
        throws BufferWriteException
    {
        setUpBaseEnv(2, 2);  //2 waves with a 2-byte stack each.
        
        //Set up expected writes made by WStackFiller components and close.
        stackStream.read(buffer, 0, 1, 1, null);  //Also sets buf[0]=1.
        stackStream.read(buffer, 1, 2, 1, null);  //buf[1]=1, buf[2]=1.
        stackStream.read(buffer, 2, 2, -1, null); 
        stackStream.close(null);
        stackStream.read(buffer, 2, 2, 2, null);  //buf[2]=1, buf[3]=1.
        stackStream.read(buffer, 0, 15, -1, null);
        stackStream.close(null);
        
        transitionMocksToVerificationMode();
        
        //Test.
        assertEquals("Returned wrong write length.", 
                target.write(buffer, 0, 1), 1);
        assertEquals("Returned wrong write length.", 
                target.write(buffer, 1, 2), 1);
        assertEquals("Failed to signal end of w1-stream.", 
                target.write(buffer, 2, 2), 0);
        assertEquals("Returned wrong write length.", 
                target.write(buffer, 2, 2), 2);
        assertEquals("Failed to signal end of w2-stream.", 
                target.write(buffer, 0, 15), 0);
        assertEquals("Failed to signal end of stream.", 
                -1, target.write(buffer, 0, 15));
        checkBytesWritten(4);
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    
}
