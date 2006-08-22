/*
 * org.openmicroscopy.shoola.util.concur.TestProducerLoopStateDep2
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
 * {@link ProducerLoop} in a multi-threaded environment.
 * Makes sure that state transitions awake waiting threads and 
 * liveness is attained.
 *
 * @see org.openmicroscopy.shoola.util.concur.ThreadSupport
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
public class TestProducerLoopStateDep2
    extends TestCase
{

    private static final int    BUF_SZ = 10;  //Both buffer size and payload.
    
    private ProducerLoop    target;  //Object under test.
    private ThreadSupport   threads;  //To manage main/alt flows.
    
    //Tells which transition the alt flow will fire.  True for a transition to
    //DONE, false for a transition to DATA_DISCARDED.
    private boolean         transToDone;
    private int             state;  //To transfer value from alt thread.
    
    
    private void transitionToDone()  //Run in alt flow.
    {
        try {
            while (!target.isDone()) target.doStep();  //Write payload.
            target.onEnd(null);
            state = target.getState();
        } catch (Exception e) {
            //Don't bother, state won't be DONE and test will fail.
        }   
    }
    
    private void transitionToDataDiscarded()  //Run in alt flow.
    {
        target.onAbort(new BufferWriteException(""));
        state = target.getState();
    }
    
    public void setUp()
    {
        AsyncByteBuffer buffer = new AsyncByteBuffer(BUF_SZ, BUF_SZ/3);
        FakeByteBufferFiller producer = 
            new FakeByteBufferFiller(BUF_SZ, (byte) 1);
        target = new ProducerLoop(buffer, producer);
        
        threads = new ThreadSupport(new Runnable() {  //Alternate flow.
            public void run() {
                if (transToDone) transitionToDone();
                else transitionToDataDiscarded();
            }
        });
        
        target.register(new ControlFlowObserver() {
            public void update(int checkPointID)
            {
                //If main thread acquired the lock during waitForData below, 
                //then spawn a thread to fire a state transition and get the
                //target's state after the transition.
                if (checkPointID == ProducerLoop.LOCK_ACQUIRED &&
                        threads.isMainFlow())
                    threads.startAltFlow();  //Fires transition.
            }
        });
    }
    
    public void testWaitForDataAcceptance() 
        throws Exception
    {
        //First tell alt flow to fire a transition to DONE.  This boolean
        //value will be correctly read by alt flow b/c it'll be created 
        //within the call to waitForData below.
        transToDone = true;  
        
        //Wait for the whole buffer to be written.  B/c the current state is
        //FILLING, waitForData will have to wait until the transition to DONE
        //has completed.
        assertTrue("A call to waitForData should return true when the state "+
                "becomes DONE.", 
                target.waitForData(0, BUF_SZ));
        
        //Wait for alt flow to join back so that its memory will be flushed and
        //the state value it read will be available to the main flow.
        threads.awaitAltFlow();
        assertEquals("waitForData shouldn't have proceeded on a FILLING state.", 
                ProducerLoop.DONE, state);
    }
    
    public void testWaitForDataAcceptance2() 
        throws Exception
    {
        //First tell alt flow to fire a transition to DATA_DISCARDED.  This 
        //boolean value will be correctly read by alt flow b/c it'll be 
        //created within the call to waitForData below.
        transToDone = false;  
        
        //Wait for the data to be discarded.  B/c the current state is
        //FILLING, waitForData will have to wait until the transition to
        //DATA_DISCARDED has completed.
        try {
            target.waitForData(0, BUF_SZ);
            fail("A call to waitForData should throw a BufferWriteException "+
                    "when the state becomes DATA_DISCARDED.");
        } catch (BufferWriteException bwe) {
            //OK, expected.
        }
        
        //Wait for alt flow to join back so that its memory will be flushed and
        //the state value it read will be available to the main flow.
        threads.awaitAltFlow();
        assertEquals("waitForData shouldn't have proceeded on a FILLING state.", 
                ProducerLoop.DATA_DISCARDED, state);
    }
    
    public void testTimedWaitForDataAcceptance() 
        throws Exception
    {
        //First tell alt flow to fire a transition to DONE.  This boolean
        //value will be correctly read by alt flow b/c it'll be created 
        //within the call to waitForData below.
        transToDone = true;  
        
        //Wait for the whole buffer to be written.  B/c the current state is
        //FILLING, waitForData will have to wait until the transition to DONE
        //has completed.  It's very unlikely it will timeout on 10000ms, so
        //it should return true.  Occasional failure should be ignored.
        assertTrue("A call to waitForData should return true when the state "+
                "becomes DONE.", 
                target.waitForData(0, BUF_SZ, 10000));
        
        //Wait for alt flow to join back so that its memory will be flushed and
        //the state value it read will be available to the main flow.
        threads.awaitAltFlow();
        assertEquals("waitForData shouldn't have proceeded on a FILLING state.", 
                ProducerLoop.DONE, state);
    }
    
    public void testTimedWaitForDataAcceptance2() 
        throws Exception
    {
        //First tell alt flow to fire a transition to DATA_DISCARDED.  This 
        //boolean value will be correctly read by alt flow b/c it'll be 
        //created within the call to waitForData below.
        transToDone = false;  
        
        //Wait for the data to be discarded.  B/c the current state is FILLING,
        //waitForData will have to wait until the transition to DATA_DISCARDED
        //has completed.  It's very unlikely it will timeout on 10000ms, so it 
        //should return true.  Occasional failure should be ignored.
        try {
            target.waitForData(0, BUF_SZ, 10000);
            fail("A call to waitForData should throw a BufferWriteException "+
                    "when the state becomes DATA_DISCARDED.");
        } catch (BufferWriteException bwe) {
            //OK, expected.
        }
        
        //Wait for alt flow to join back so that its memory will be flushed and
        //the state value it read will be available to the main flow.
        threads.awaitAltFlow();
        assertEquals("waitForData shouldn't have proceeded on a FILLING state.", 
                ProducerLoop.DATA_DISCARDED, state);
    }
    
}
