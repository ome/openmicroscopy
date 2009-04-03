/*
 * org.openmicroscopy.shoola.util.concur.TestProducerLoopStateDep
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

/** 
 * Tests the operation of the <code>waitForData</code> method of 
 * {@link ProducerLoop} in a single-threaded environment.
 * Makes sure that, depending on state, the correct behavior is attained.
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
public class TestProducerLoopStateDep
    extends TestCase
{

    private static final int    BUF_SZ = 10;  //both buffer size and payload.
    
    private ProducerLoop    target;  //Object under test.
    
    
    private void transitionToDone()
        throws Exception
    {
        while (!target.isDone()) target.doStep();  //Write payload.
        target.onEnd(null);
        assertEquals("Should have transitioned to DONE.", 
                ProducerLoop.DONE, target.getState());
    }
    
    private void transitionToDataDiscarded()
    {
        target.onAbort(new BufferWriteException(""));
        assertEquals("Should have transitioned to DATA_DISCARDED.", 
                ProducerLoop.DATA_DISCARDED, target.getState());
    }
    
    public void setUp()
    {
        AsyncByteBuffer buffer = new AsyncByteBuffer(BUF_SZ, BUF_SZ/3);
        FakeByteBufferFiller producer = 
            new FakeByteBufferFiller(BUF_SZ, (byte) 1);
        target = new ProducerLoop(buffer, producer);
    }
    
    private void doTestWaitForDataBadArgs(boolean timedWait) 
        throws BufferWriteException, InterruptedException
    {
        try {
            if (timedWait) target.waitForData(0, -1, 100);
            else target.waitForData(0, -1);
            fail("[0, -1] is not a valid interval.");
        } catch (IllegalArgumentException iae) {
            //OK, expected.
        }
        try {
            if (timedWait) target.waitForData(BUF_SZ-1, BUF_SZ+1, 100);
            else target.waitForData(BUF_SZ-1, BUF_SZ+1);
            fail("["+(BUF_SZ-1)+", "+(BUF_SZ+1)+"] doesn't fall into "+
                    "[0, "+BUF_SZ+"].");
        } catch (IllegalArgumentException iae) {
            //OK, expected.
        }
        try {
            if (timedWait) target.waitForData(0, BUF_SZ+1, 100);
            else target.waitForData(0, BUF_SZ+1);
            fail("[0, "+(BUF_SZ+1)+"] doesn't fall into "+
                    "[0, "+BUF_SZ+"].");
        } catch (IllegalArgumentException iae) {
            //OK, expected.
        }
        try {
            if (timedWait) target.waitForData(BUF_SZ-1, BUF_SZ, 100);
            else target.waitForData(BUF_SZ-1, BUF_SZ);
            fail("["+(BUF_SZ-1)+", "+BUF_SZ+"] doesn't fall into "+
                    "[0, "+BUF_SZ+"].");
        } catch (IllegalArgumentException iae) {
            //OK, expected.
        }
    }
    
    public void testWaitForDataBadArgs() 
        throws Exception
    {
        //State is FILLING after setUp().
        doTestWaitForDataBadArgs(false);
        
        transitionToDataDiscarded();
        doTestWaitForDataBadArgs(false);
        
        transitionToDone();
        doTestWaitForDataBadArgs(false);
    }
    
    public void testTimedWaitForDataBadArgs() 
        throws Exception
    {
        //State is FILLING after setUp().
        doTestWaitForDataBadArgs(true);
        
        transitionToDataDiscarded();
        doTestWaitForDataBadArgs(true);
        
        transitionToDone();
        doTestWaitForDataBadArgs(true);
    }
    
    public void testTimedWaitForDataWhenFilling()
        throws Exception
    {
        //State is FILLING after setUp().
        assertFalse("A call to waitForData with a non-positive timeout should "+
                "return false immediately if the data is not available and the"+
                " state is FILLING.", 
                target.waitForData(0, BUF_SZ, -1));
        assertFalse("A call to waitForData with a non-positive timeout should "+
                "return false immediately if the data is not available and the"+
                " state is FILLING.", 
                target.waitForData(0, BUF_SZ, 0));
        assertFalse("A call to waitForData with a positive timeout should "+
                "return false after the timeout elapses if the data is not "+
                " available and the state is FILLING.", 
                target.waitForData(0, BUF_SZ, 100));
    }
    //NOTE: unbounded wait will be tested in multi-threaded test cases.
    
    public void testTimedWaitForDataWhenDone()
        throws Exception
    {
        transitionToDone();
        assertTrue("A call to waitForData with a non-positive timeout should "+
                "return true immediately if the state is DONE.", 
                target.waitForData(0, BUF_SZ, -1));
        assertTrue("A call to waitForData with a non-positive timeout should "+
                "return true immediately if the state is DONE.", 
                target.waitForData(0, BUF_SZ, 0));
        assertTrue("A call to waitForData with a positive timeout should "+
                "return true immediately if the state is DONE.", 
                target.waitForData(0, BUF_SZ, 100));
    } 
    
    public void testWaitForDataWhenDone()
        throws Exception
    {
        transitionToDone();
        assertTrue("A call to waitForData should return true immediately if "+
                "the state is DONE.", 
                target.waitForData(0, BUF_SZ));
    }
    
    public void testTimedWaitForDataWhenDataDiscarded()
        throws Exception
    {
        transitionToDataDiscarded();
        try {
            target.waitForData(0, BUF_SZ, -1);
            fail("A call to waitForData with a non-positive timeout should "+
                 "return an BufferWriteException immediately if the state is "+
                 "DATA_DISCARDED.");
        } catch (BufferWriteException bwe) {
            //OK, expected.
        }
        try {
            target.waitForData(0, BUF_SZ, 0);
            fail("A call to waitForData with a non-positive timeout should "+
                 "return an BufferWriteException immediately if the state is "+
                 "DATA_DISCARDED.");
        } catch (BufferWriteException bwe) {
            //OK, expected.
        }
        try {
            target.waitForData(0, BUF_SZ, 100);
            fail("A call to waitForData with a positive timeout should "+
                 "return an BufferWriteException immediately if the state is "+
                 "DATA_DISCARDED.");
        } catch (BufferWriteException bwe) {
            //OK, expected.
        }
    } 
    
    public void testWaitForDataWhenDataDiscarded()
        throws Exception
    {
        transitionToDataDiscarded();
        try {
            target.waitForData(0, BUF_SZ);
            fail("A call to waitForData should return an BufferWriteException "+
                    "immediately if the state is DATA_DISCARDED.");
        } catch (BufferWriteException bwe) {
            //OK, expected.
        }
    }
    
}
