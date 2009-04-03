/*
 * org.openmicroscopy.shoola.util.concur.TestProducerLoopSync2
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
 * We test that <code>doStep</code> calls are serialized in the 
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
public class TestProducerLoopSync2
    extends TestCase
{

    private static final int    BUF_SZ = 10;  //Both buffer size and payload.
    
    private ProducerLoop    target;  //Object under test.
    private int             bytesWritten; //Transfers value from alternate flow.
    private ThreadSupport   threads;  //To manage main/alt flows.
   
    
    public void setUp()
    {
        threads = new ThreadSupport(new Runnable() {  //Alternate flow.
            public void run() { bytesWritten = target.getBytesWritten(); }
        });
        //NOTE: getBytesWritten() is sync.  So if it is called while doStep is
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
                if (checkPointID == ProducerLoop.LOCK_ACQUIRED) {
                    //doStep in progress.  Spawn alt thread to retrieve 
                    //target's bytesWritten field.
                    threads.startAltFlow();  //getBytesWritten() will be called.
                    
                    threads.pauseMainFlow();
                    //NOTE: Even though pausing the main flow doesn't guarantee
                    //that getBytesWritten() will be invoked in the mean time, 
                    //the 2-second delay used should in practice make this 
                    //extremely likely to happen.
                }
            }
        });
        
        //NOTE: If getBytesWritten() gets executed b/f doStep() modifies the 
        //target's bytesWritten field, then we would also get the wrong value
        //for our bytesWritten field.  In this case, we can deduce that locks
        //were screwed up and thus fail the test.
        //The test would be optimal if we could make sure that getBytesWritten()
        //is actually invoked while doStep() is being executed (in practice this
        //is quite sure though).  In any case, pausing the main thread is the 
        //best we can do as there's no way to tell whether a thread is waiting 
        //to acquire a lock.
    }
    
    public void testDoStep() 
        throws Exception
    {
        //Target's state is FILLING.
        
        //This will call observer, which will spawn alt flow and pause 
        //main flow.  As the main flow is paused, the alt flow will
        //retrieve the target's bytesWritten field.
        target.doStep();
        
        //Wait for alt flow to join back so that its memory will be flushed 
        //and the state value it read will be available to the main flow.
        threads.awaitAltFlow();
        assertEquals("Concurrent access to target's state.", 
                BUF_SZ, bytesWritten);
    }
    
}