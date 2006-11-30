/*
 * org.openmicroscopy.shoola.util.concur.TestObjectTransferInt
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
 * Verifies that {@link ObjectTransfer} correctly aborts in the case of 
 * interruption.
 * We call <code>handOff</code> and <code>collect</code> in interrupted threads
 * and verify they return right on by throwing an {@link InterruptedException}.
 * We then verify that if an hand-off is in progress in the producer thread,
 * then this thread will rendezvous with the consumer doing collection even
 * in the face of interruption.
 * <p><b>NOTE</b>:
 * Each test spawns an ad-hoc thread which is interrupted to verify how 
 * {@link ObjectTransfer} reacts to interruption.  The result of the test is 
 * passed back into the main flow (JUnit) so to perform the needed assertions.  
 * Note that we never interrupt JUnit.  Spawning another thread might seem 
 * unecessary when we could just interrupt JUnit and then clear the interrupted
 * status after the test.  However implementation characteristics of 
 * interruption-based methods are quite uncertain, so we prefer using a 
 * separate thread (which is simply discarded at the end of the test) rather
 * than interrupting JUnit and making thus room for possible side effects.</p>
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
public class TestObjectTransferInt
        extends TestCase
{

    private ObjectTransfer  target;  //Object under test.
    
    //Task performed in a separate thread, which is interrupted.
    private class DoHandOff implements Runnable
    {
        InterruptedException intExc;  //To transfer exc to the main thread.
        long timeout;  //handOff(timeout) or handOff() if -1.
        Object handedOffObj;  //Object to put in the channel.
        
        DoHandOff(Object handedOffObj, long timeout) { 
            this.handedOffObj = handedOffObj;
            this.timeout = timeout; 
        }
        public void run() {
            try {
                if (0 <= timeout) target.handOff(handedOffObj, timeout);
                else target.handOff(handedOffObj);
            } catch (InterruptedException ie) {
                intExc = ie;
            }
        }
    }
    
    //Task performed in the alternate flow.
    private class DoCollect implements Runnable
    {
        InterruptedException intExc;  //To transfer exc to the main thread.
        long timeout;  //collect(timeout) or collect() if -1.
        Object collectedObj;  //Object collected from the channel.
        
        DoCollect(long timeout) { this.timeout = timeout; }
        public void run() {
            try {
                if (0 <= timeout) collectedObj = target.collect(timeout);
                else collectedObj = target.collect();
            } catch (InterruptedException ie) {
                //Should never happen in the rendezvous tests; don't bother b/c 
                //this would show up as a deadlock.  OTOH, this is relevant in
                //the tests that run in a pre-interrupted thread.
                intExc = ie;
            }
        }
    }

    private void doTestHandOffInIntThread(long timeout)
    {
        //Disable observer (see setUp).
        target.register(null);
        
        //Create a task to call handOff(timeout).
        DoHandOff doHandOff = new DoHandOff(new Object(), timeout);
        
        //Run the task in a new interrupted thread and wait for it to finish
        //-- so its working memory will be flushed. 
        ThreadSupport.runInNewInterruptedThread(doHandOff);
        
        //Now check that an InterruptedException was thrown.
        assertNotNull("An hand-off shouldn't proceed if the thread is "+
                "interrupted.", doHandOff.intExc);
    }
    
    private void doTestCollectInIntThread(long timeout)
    {
        //Disable observer (see setUp).
        target.register(null);
        
        //Create a task to call collect(timeout).
        DoCollect doCollect = new DoCollect(timeout);
        
        //Run the task in a new interrupted thread and wait for it to finish
        //-- so its working memory will be flushed. 
        ThreadSupport.runInNewInterruptedThread(doCollect);
        
        //Now check that an InterruptedException was thrown.
        assertNotNull("A collect shouldn't proceed if the thread is "+
                "interrupted.", doCollect.intExc);
    }
    
    private void doTestRendezvous(long timeout)
    {
        //Set up and start a thread to do a collect() asynchronously. 
        DoCollect doCollect = new DoCollect(-1);  //-1 for unbounded collect.
        ThreadSupport ts = new ThreadSupport(doCollect);
        ts.startAltFlow();
        
        //Set up and start a thread to do an handOff() synchronously.
        //Update (see setUp) will be called from handOff() right b/f 
        //completing the transition to Full.  Update in turn will interrupt
        //the thread.
        DoHandOff doHandOff = new DoHandOff(new Object(), timeout);
        ThreadSupport.runInNewThread(doHandOff);
        
        //The hand-off thread joined back, let's wait for the collect thread.
        ts.awaitAltFlow();
        
        //Now working memories have been flushed, so doCollect and doHandOff
        //objects are in sync with this thread.
        
        //Check that an InterruptedException was thrown and the handed-off
        //object was collected.
        assertNotNull("An hand-off has to wait for a collect.", 
                doHandOff.intExc);  //If not thrown => didn't do down.
        assertSame("Collected object is not the one originally handed off.", 
                doHandOff.handedOffObj, doCollect.collectedObj); 
    }
    
    public void setUp()
    {
        target = new ObjectTransfer();
        target.register(new ControlFlowObserver() {
            public void update(int checkPointID) {
                if (checkPointID == ObjectTransfer.TRANSITION_TO_FULL)
                    //Empty->Full, we're in handOff().  Interrupt:
                    Thread.currentThread().interrupt();
            }
        });
    }
    
    public void testRendezvous_1()
    {
        doTestRendezvous(-1);  //Unbounded hand-off.
    }
    
    public void testRendezvous_2()
    {
        doTestRendezvous(1000);  //Bounded hand-off.
    }
    
    public void testRendezvous_3()
    {
        doTestRendezvous(0);  //No wait hand-off.
    }
    
    public void testHandOffInIntThread_1()
    {
        doTestHandOffInIntThread(-1);  //Unbounded hand-off.
    }
    
    public void testHandOffInIntThread_2()
    {
        doTestHandOffInIntThread(100);  //Bounded hand-off.
    }
    
    public void testHandOffInIntThread_3()
    {
        doTestHandOffInIntThread(0);  //No wait hand-off.
    }
    
    public void testCollectInIntThread_1()
    {
        doTestCollectInIntThread(-1);  //Unbounded collect.
    }
    
    public void testCollectInIntThread_2()
    {
        doTestCollectInIntThread(100);  //Bounded collect.
    }
    
    public void testCollectInIntThread_3()
    {
        doTestCollectInIntThread(0);  //No wait collect.
    }
    
}
