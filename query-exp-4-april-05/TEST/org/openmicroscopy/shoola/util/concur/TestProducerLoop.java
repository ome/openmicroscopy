/*
 * org.openmicroscopy.shoola.util.concur.TestProducerLoop
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

package org.openmicroscopy.shoola.util.concur;


//Java imports

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.concur.tasks.ExecHandle;
import org.openmicroscopy.shoola.util.concur.tasks.FakeCmdProcessor;

/** 
 * Tests the operation of {@link ProducerLoop} in a single-threaded
 * environment.
 * Makes sure that state-transitions are correct.
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
public class TestProducerLoop
    extends TestCase
{
    
    private ProducerLoop            target;  //Object under test.
    private MockAsyncByteBuffer     buffer;  //Mock linked to target.
    private MockByteBufferFiller    producer;  //Mock linked to target.
    private Runnable                execCmd;  //To simulate service execution.
    private ExecHandle              execHandle;  //To simulate cancellation.
    
    
    //REPLACES setUp().  Call it b/f each test.
    //payload: value producer.getTotalLength() has to return.
    //nWrites: how many times buffer.writeToBuffer() will be invoked.  Every
    //          write will be 1-byte long, except for the nWrite call if
    //          setLastWrite is true.  In this case, this last call will
    //          will return -1 instead.
    //e: if not null, it will be thrown by the last write.
    private void setUpTestEnvironment(int payload, int nWrites, 
            boolean setLastWrite, Exception e)
    {
        //Create mocks.
        buffer = new MockAsyncByteBuffer(2, 1); //Sz irrelevant for these tests.
        producer = new MockByteBufferFiller();
        
        //ProducerLoop should always call this in its constructor and only
        //once in its life-span.
        producer.getTotalLength(payload);
        
        //Do nWrites-1 calls that write 1 byte each, nWrites call has to 
        //return -1 if setLastWrite.  Do nothing if nWrites is 0.
        for (int i = 0; i < nWrites-1; ++i)
            buffer.writeToBuffer(producer, i, 1, null);
        if (0 < nWrites) {
            if (setLastWrite) 
                buffer.writeToBuffer(producer, nWrites-1, -1, e);
            else 
                buffer.writeToBuffer(producer, nWrites-1, 1, e);
        }
        
        //Transition mocks to verification mode.
        producer.activate();  //This has to be done b/f creating ProducerLoop.
        buffer.activate();
        
        //Create target and link it to mocks.
        target = new ProducerLoop(buffer, producer);  //Calls getTotalLength().
        
        //Get the commmand that a concrete CmdProcessor would use to execute
        //the producer loop.
        FakeCmdProcessor cmdPrc = new FakeCmdProcessor();
        execHandle = cmdPrc.exec(target, target);  //(srv, observer)
        execCmd = cmdPrc.getCommand();
    }
    
    private void ensureAllExpectedCallsWerePerformed()
    {
        producer.verify();
        buffer.verify();
    }
    
    public void setUp()
    {
        //Clean fixture so to fail if test method didn't call 
        //setUpTestEnvironment().
        target = null;
        buffer = null;
        producer = null;
        execCmd = null;
        execHandle = null;
    }
    
    public void testInitBadArgs()
    {
        try {
            setUpTestEnvironment(0, 0, false, null);  //Bad payload=0. 
            fail("ProducerLoop constructor should error if the producer "+
                    "provides a non-positive payload.");
        } catch (IllegalArgumentException iae) {
            //OK, expected.
        }
        ensureAllExpectedCallsWerePerformed();
        
        try {
            new ProducerLoop(null, null);
            fail("ProducerLoop constructor shouldn't accept null args.");
        } catch (NullPointerException npe) {
            //OK, expected.
        }
        
        try {
            new ProducerLoop(new MockAsyncByteBuffer(2, 1), null);
            fail("ProducerLoop constructor shouldn't accept null args.");
        } catch (NullPointerException npe) {
            //OK, expected.
        }
        
        try {
            new ProducerLoop(null, new MockByteBufferFiller());
            fail("ProducerLoop constructor shouldn't accept null args.");
        } catch (NullPointerException npe) {
            //OK, expected.
        }
    }
    
    public void testInit()
    {
        setUpTestEnvironment(1, 0, false, null);  //payload=1 and do no write.
        
        //Test.
        assertEquals("State should be FILLING after creation.", 
                ProducerLoop.FILLING, target.getState());
        int bw = target.getBytesWritten(), pl = target.getPayload();
        assertTrue("Count of bytes written ("+bw+") should be less than "+
                "payload ("+pl+")in the FILLING state.", bw < pl);
        assertNull("No exception should be set in the FILLING state.", 
                target.getDiscardCause());
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void testFillingToDataDiscardedBecauseOfPayloadOverflow()
    {
        //Simulate writing more data than payload.  Set payload to 1 and do
        //two calls that write 1 byte each.
        setUpTestEnvironment(1, 2, false, null);
        
        //Test.
        execCmd.run();  //Execute service.
        assertEquals("State should be DATA_DISCARDED after a write overflow.", 
                ProducerLoop.DATA_DISCARDED, target.getState());
        int bw = target.getBytesWritten(), pl = target.getPayload();
        assertTrue("Count of bytes written ("+bw+") should be greater than "+
                "payload ("+pl+") after a write overflow.", pl < bw);
        assertNotNull("An exception should be set in the DATA_DISCARDED state.", 
                target.getDiscardCause());
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void testFillingToDataDiscardedBecauseOfPayloadUnderflow()
    {
        //Simulate writing less data than payload.  Set payload to 2 and do
        //two calls: first one writes 1 byte, second one returns -1.
        setUpTestEnvironment(2, 2, true, null);
        
        //Test.
        execCmd.run();  //Execute service.
        assertEquals("State should be DATA_DISCARDED after a write underflow.", 
                ProducerLoop.DATA_DISCARDED, target.getState());
        int bw = target.getBytesWritten(), pl = target.getPayload();
        assertTrue("Count of bytes written ("+bw+") should be less than "+
                "payload ("+pl+") after a write overflow.", bw < pl);
        assertNotNull("An exception should be set in the DATA_DISCARDED state.", 
                target.getDiscardCause());
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void testFillingToDataDiscardedBecauseOfBufferWriteException()
    {
        //Simulate writing less data than payload.  Set payload to 2 and do
        //one call to write 1 byte.  Then set up an additional call to throw
        //the exception.
        BufferWriteException exc = new BufferWriteException("");
        setUpTestEnvironment(2, 1, false, exc);
        
        //Test.
        execCmd.run();  //Execute service.
        assertEquals("State should be DATA_DISCARDED after a write exception.", 
                ProducerLoop.DATA_DISCARDED, target.getState());
        int bw = target.getBytesWritten(), pl = target.getPayload();
        assertTrue("Count of bytes written ("+bw+") should be less than "+
                "payload ("+pl+") after a write exception.", bw < pl);
        assertNotNull("An exception should be set in the DATA_DISCARDED state.", 
                target.getDiscardCause());
        assertSame("The original BufferWriteException thrown by the producer "+
                "should always be propagated.", 
                exc, target.getDiscardCause());
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void testFillingToDataDiscardedBecauseOfRuntimeException()
    {
        //Simulate writing less data than payload.  Set payload to 2 and do
        //one call to write 1 byte.  Then set up an additional call to throw
        //the exception.
        RuntimeException exc =  //Simulate runtime overflow of internal buffer. 
            new ArrayIndexOutOfBoundsException("");
        setUpTestEnvironment(2, 1, false, exc);
        
        //Test.
        execCmd.run();  //Execute service.
        assertEquals("State should be DATA_DISCARDED after a write exception.", 
                ProducerLoop.DATA_DISCARDED, target.getState());
        int bw = target.getBytesWritten(), pl = target.getPayload();
        assertTrue("Count of bytes written ("+bw+") should be less than "+
                "payload ("+pl+") after a write exception.", bw < pl);
        assertNotNull("An exception should be set in the DATA_DISCARDED state.", 
                target.getDiscardCause());
        assertSame("Any runtime exception thrown by the producer should "+
                "always be propagated by wrapping into a BufferWriteException.", 
                exc, target.getDiscardCause().getCause());
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void testFillingToDataDiscardedBecauseOfCancellation()
    {
        setUpTestEnvironment(1, 0, false, null);  //payload=1 and do no write.
        
        //Test.
        execHandle.cancelExecution();
        assertEquals("State should be DATA_DISCARDED after cancellation.", 
                ProducerLoop.DATA_DISCARDED, target.getState());
        int bw = target.getBytesWritten(), pl = target.getPayload();
        assertTrue("Count of bytes written ("+bw+") shouldn't be greater than "+
                "payload ("+pl+") after cancellation.", bw <= pl);
        assertNotNull("An exception should be set in the DATA_DISCARDED state.", 
                target.getDiscardCause());
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void testFillingToDone()
    {
        //Simulate writing as much data as payload.  Set payload to 2 and do
        //three calls: first two write 1 byte, last one returns -1.
        setUpTestEnvironment(2, 3, true, null);
        
        //Test.
        execCmd.run();  //Execute service.
        assertEquals("State should be DONE after normal termination.", 
                ProducerLoop.DONE, target.getState());
        int bw = target.getBytesWritten(), pl = target.getPayload();
        assertTrue("Count of bytes written ("+bw+") should be equal to "+
                "payload ("+pl+") after normal termination.", bw == pl);
        assertNull("No exception should be set in the DONE state.", 
                target.getDiscardCause());
        
        ensureAllExpectedCallsWerePerformed();
    }
    
}
