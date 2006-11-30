/*
 * org.openmicroscopy.shoola.util.concur.TestProducerLoopSync3
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
 * Verifies the safety of {@link ProducerLoop}.
 * We test that <code>waitForData</code> calls are serialized in the 
 * presence of multiple threads acting on the object.
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
public class TestProducerLoopSync3
    extends TestCase
{

    private static final int    BUF_SZ = 10;  //Both buffer size and payload.
    
    private ProducerLoop            target;  //Object under test.
    private BufferWriteException    exc; //To transfer exc from alternate flow.
    
    //Tells which waitForData() the alt flow will call.  True for the timed
    //version, false otherwise.
    private boolean                 timedCall;
    
    private ThreadSupport           threads;  //To manage main/alt flows.
   
    
    public void setUp()
    {
        threads = new ThreadSupport(new Runnable() {  //Alternate flow.
            public void run() { 
                try {
                    if (timedCall) target.waitForData(0, BUF_SZ, 2000);
                    else target.waitForData(0, BUF_SZ);
                } catch (BufferWriteException bwe) {
                    exc = bwe;
                } catch (InterruptedException ie) {
                    //Don't bother, exc will be null and test will fail.
                } 
            }
        });
        //NOTE: waitForData() is sync.  So if it is called while onEnd() is
        //being executed in the main thread, then the alternate thread
        //will have to wait until the lock is released.  This is enough to test
        //safety, there's no need to use more than two threads and verify all
        //possible combinations of the sync methods of ProducerLoop.
        
        AsyncByteBuffer buffer = new AsyncByteBuffer(BUF_SZ, BUF_SZ);
        FakeByteBufferFiller producer = 
            new FakeByteBufferFiller(BUF_SZ, (byte) 1);
        target = new ProducerLoop(buffer, producer);
       
        target.register(new ControlFlowObserver() {
            public void update(int checkPointID)
            {  //Called w/in main thread.
                if (checkPointID == ProducerLoop.LOCK_ACQUIRED &&
                        threads.isMainFlow()) {
                    //onEnd() in progress.  Spawn alt thread to call 
                    //waitForData().
                    threads.startAltFlow();
                    
                    threads.pauseMainFlow();
                    //NOTE: Even though pausing the main flow doesn't guarantee
                    //that waitForData() will be invoked in the mean time, 
                    //the 2-second delay used should in practice make this 
                    //extremely likely to happen.
                }
            }
        });
        
        //NOTE: If waitForData() gets executed b/f onEnd() modifies the 
        //target's state field, then we would also get no exception value
        //for our exc field.  In this case, we can deduce that locks
        //were screwed up and thus fail the test.
        //The test would be optimal if we could make sure that waitForData()
        //is actually invoked while onEnd() is being executed (in practice this
        //is quite sure though).  In any case, pausing the main thread is the 
        //best we can do as there's no way to tell whether a thread is waiting 
        //to acquire a lock.
    }
    
    public void testWaitForData() 
        throws Exception
    {
        //Target's state is FILLING.
        
        //First tell alt flow to call waitForData() w/out timeout.  This 
        //boolean value will be correctly read by alt flow b/c it'll be 
        //created within the call to onEnd() below.
        timedCall = false; 
        
        //This will call observer, which will spawn alt flow and pause 
        //main flow.  As the main flow is paused, the alt flow will
        //call waitForData().
        target.onEnd(null);
        
        //Wait for alt flow to join back so that its memory will be flushed 
        //and the state value it read will be available to the main flow.
        threads.awaitAltFlow();
        assertNotNull("Concurrent access to target's state.", exc);
    }
    
    public void testTimedWaitForData() 
        throws Exception
    {
        //Target's state is FILLING.
        
        //First tell alt flow to call waitForData() w/ timeout.  This 
        //boolean value will be correctly read by alt flow b/c it'll be 
        //created within the call to onEnd() below.
        timedCall = true; 
        
        //This will call observer, which will spawn alt flow and pause 
        //main flow.  As the main flow is paused, the alt flow will
        //call waitForData().
        target.onEnd(null);
        
        //Wait for alt flow to join back so that its memory will be flushed 
        //and the state value it read will be available to the main flow.
        threads.awaitAltFlow();
        assertNotNull("Concurrent access to target's state.", exc);
    }
    
}
